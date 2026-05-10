import os
import uuid
import jwt
import re
from datetime import datetime, timezone, timedelta
from typing import Optional

from flask import Flask, jsonify, request, send_from_directory
from flask_cors import CORS
from flask_socketio import SocketIO, emit, join_room, leave_room
from sqlalchemy import Boolean, DateTime, ForeignKey, Integer, String, Text, Table, Column, create_engine, select, func, delete
from sqlalchemy.orm import DeclarativeBase, Mapped, Session, mapped_column, relationship
from werkzeug.security import generate_password_hash, check_password_hash
from werkzeug.utils import secure_filename

# --- CONFIGURATION ---
SECRET_KEY = os.getenv("SECRET_KEY", "accordance-discord-master-key-2025")
DATABASE_URL = os.getenv("DATABASE_URL") or "sqlite:///accordance.db"
UPLOAD_DIR = os.getenv("UPLOAD_DIR", "uploads")
os.makedirs(UPLOAD_DIR, exist_ok=True)

class Base(DeclarativeBase):
    pass

# Many-to-Many: Membership <-> Roles
membership_roles = Table(
    "membership_roles",
    Base.metadata,
    Column("membership_id", ForeignKey("memberships.id", ondelete="CASCADE"), primary_key=True),
    Column("role_id", ForeignKey("roles.id", ondelete="CASCADE"), primary_key=True),
)

class UserModel(Base):
    __tablename__ = "users"
    id: Mapped[str] = mapped_column(String(50), primary_key=True)
    username: Mapped[str] = mapped_column(String(50), unique=True, nullable=False)
    password_hash: Mapped[str] = mapped_column(String(255), nullable=False)
    display_name: Mapped[Optional[str]] = mapped_column(String(100))
    avatar_url: Mapped[Optional[str]] = mapped_column(String(255))
    bio: Mapped[Optional[str]] = mapped_column(String(255))
    status_text: Mapped[str] = mapped_column(String(50), default="Online")
    is_online: Mapped[bool] = mapped_column(Boolean, default=False)

    memberships: Mapped[list["MembershipModel"]] = relationship(back_populates="user", cascade="all, delete-orphan")

class ServerModel(Base):
    __tablename__ = "servers"
    id: Mapped[str] = mapped_column(String(50), primary_key=True)
    name: Mapped[str] = mapped_column(String(100), nullable=False)
    icon: Mapped[str] = mapped_column(String(10), nullable=False)
    owner_id: Mapped[str] = mapped_column(ForeignKey("users.id"))
    invite_code: Mapped[str] = mapped_column(String(10), unique=True, nullable=False)

    channels: Mapped[list["ChannelModel"]] = relationship(back_populates="server", cascade="all, delete-orphan")
    roles: Mapped[list["RoleModel"]] = relationship(back_populates="server", cascade="all, delete-orphan")
    members: Mapped[list["MembershipModel"]] = relationship(back_populates="server", cascade="all, delete-orphan")

class MembershipModel(Base):
    __tablename__ = "memberships"
    id: Mapped[int] = mapped_column(Integer, primary_key=True, autoincrement=True)
    user_id: Mapped[str] = mapped_column(ForeignKey("users.id"), index=True)
    server_id: Mapped[str] = mapped_column(ForeignKey("servers.id"), index=True)
    nickname: Mapped[Optional[str]] = mapped_column(String(100))

    user: Mapped[UserModel] = relationship(back_populates="memberships")
    server: Mapped[ServerModel] = relationship(back_populates="members")
    roles: Mapped[list["RoleModel"]] = relationship(secondary=membership_roles)

class ChannelModel(Base):
    __tablename__ = "channels"
    id: Mapped[str] = mapped_column(String(50), primary_key=True)
    name: Mapped[str] = mapped_column(String(100), nullable=False)
    server_id: Mapped[str] = mapped_column(ForeignKey("servers.id"), index=True)
    topic: Mapped[Optional[str]] = mapped_column(String(200), nullable=True)
    category: Mapped[str] = mapped_column(String(100), default="KANAŁY TEKSTOWE")
    slowmode_seconds: Mapped[int] = mapped_column(Integer, default=0)
    is_nsfw: Mapped[bool] = mapped_column(Boolean, default=False)

    server: Mapped[ServerModel] = relationship(back_populates="channels")

class RoleModel(Base):
    __tablename__ = "roles"
    id: Mapped[str] = mapped_column(String(50), primary_key=True)
    server_id: Mapped[str] = mapped_column(ForeignKey("servers.id"), index=True)
    name: Mapped[str] = mapped_column(String(60), nullable=False)
    color: Mapped[Optional[str]] = mapped_column(String(20), nullable=True)
    position: Mapped[int] = mapped_column(Integer, default=0)
    manage_server: Mapped[bool] = mapped_column(Boolean, default=False)
    manage_channels: Mapped[bool] = mapped_column(Boolean, default=False)
    manage_roles: Mapped[bool] = mapped_column(Boolean, default=False)
    manage_messages: Mapped[bool] = mapped_column(Boolean, default=True)

    server: Mapped[ServerModel] = relationship(back_populates="roles")

class MessageModel(Base):
    __tablename__ = "messages"
    id: Mapped[int] = mapped_column(Integer, primary_key=True, autoincrement=True)
    server_id: Mapped[str] = mapped_column(String(50), index=True)
    channel_id: Mapped[str] = mapped_column(String(50), index=True)
    author_id: Mapped[str] = mapped_column(ForeignKey("users.id"))
    author_name: Mapped[str] = mapped_column(String(80))
    text: Mapped[str] = mapped_column(Text, nullable=False)
    time: Mapped[str] = mapped_column(String(10), nullable=False)
    created_at: Mapped[datetime] = mapped_column(DateTime, nullable=False, server_default=func.now())

# Initialize DB
engine = create_engine(DATABASE_URL)
Base.metadata.create_all(engine)

app = Flask(__name__)
CORS(app)
socketio = SocketIO(app, cors_allowed_origins="*", async_mode='threading')

# --- JWT HELPERS ---
def create_token(user_id):
    exp = datetime.now(timezone.utc) + timedelta(days=7)
    payload = {'exp': exp, 'iat': datetime.now(timezone.utc), 'sub': user_id}
    return jwt.encode(payload, SECRET_KEY, algorithm='HS256')

def decode_token(token):
    try:
        payload = jwt.decode(token, SECRET_KEY, algorithms=['HS256'])
        return payload['sub']
    except Exception as e:
        print(f"Token decode error: {e}")
        return None

def token_required(f):
    def decorated(*args, **kwargs):
        auth_header = request.headers.get("Authorization", "")
        if not auth_header:
            print("Missing Authorization header")
            return jsonify({"detail": "Missing token"}), 401

        # Robust token extraction
        token = auth_header
        if auth_header.lower().startswith("bearer "):
            token = auth_header[7:].strip()

        user_id = decode_token(token)
        if not user_id:
            print(f"Invalid token: '{token[:10]}...'")
            return jsonify({"detail": "Unauthorized"}), 401

        kwargs['current_user_id'] = user_id
        return f(*args, **kwargs)
    decorated.__name__ = f.__name__
    return decorated

# --- AUTH ROUTES ---
@app.route("/auth/register", methods=['POST'])
def api_register():
    payload = request.json
    username = str(payload.get("username", "")).strip()
    password = str(payload.get("password", ""))
    if len(username) < 3: return jsonify({"detail": "Username too short"}), 400

    with Session(engine) as db:
        if db.scalar(select(UserModel).where(UserModel.username == username)):
            return jsonify({"detail": "Username taken"}), 400
        uid = str(uuid.uuid4())
        user = UserModel(id=uid, username=username, password_hash=generate_password_hash(password), display_name=username)
        db.add(user)
        db.commit()
        return jsonify({"token": create_token(uid), "user": {"id": uid, "username": username}})

@app.route("/auth/login", methods=['POST'])
def api_login():
    payload = request.json
    username = str(payload.get("username", "")).strip()
    password = str(payload.get("password", ""))
    with Session(engine) as db:
        user = db.scalar(select(UserModel).where(UserModel.username == username))
        if not user or not check_password_hash(user.password_hash, password):
            return jsonify({"detail": "Invalid credentials"}), 401
        return jsonify({
            "token": create_token(user.id),
            "user": {
                "id": user.id, "username": user.username, "displayName": user.display_name,
                "avatarUrl": user.avatar_url, "statusText": user.status_text, "bio": user.bio
            }
        })

# --- SERVER ROUTES ---
@app.route("/servers", methods=['GET'])
@token_required
def api_get_servers(current_user_id):
    with Session(engine) as db:
        mems = db.scalars(select(MembershipModel).where(MembershipModel.user_id == current_user_id)).all()
        sids = [m.server_id for m in mems]
        servers = db.scalars(select(ServerModel).where(ServerModel.id.in_(sids))).all()
        return jsonify([{"id": s.id, "name": s.name, "icon": s.icon, "ownerId": s.owner_id, "inviteCode": s.invite_code} for s in servers])

@app.route("/servers", methods=['POST'])
@token_required
def api_create_server(current_user_id):
    p = request.json
    if not p: return jsonify({"detail": "Missing body"}), 400
    name = p.get('name', 'New Server')
    with Session(engine) as db:
        sid, inv = str(uuid.uuid4())[:8], str(uuid.uuid4())[:6].upper()
        s = ServerModel(id=sid, name=name, icon=p.get('icon', name[:1].upper()), owner_id=current_user_id, invite_code=inv)
        db.add(s)
        owner_role = RoleModel(id=f"{sid}-owner", server_id=sid, name="Owner", position=100, manage_server=True, manage_channels=True, manage_roles=True, manage_messages=True)
        db.add(owner_role)
        m = MembershipModel(user_id=current_user_id, server_id=sid)
        m.roles.append(owner_role)
        db.add(m)
        db.add(ChannelModel(id=str(uuid.uuid4())[:8], server_id=sid, name="ogólny"))
        db.commit()
        return jsonify({"id": s.id, "name": s.name, "inviteCode": s.invite_code, "ownerId": s.owner_id, "icon": s.icon}), 201

@app.route("/servers/join/<invite_code>", methods=['POST'])
@token_required
def api_join_server(invite_code, current_user_id):
    code = invite_code.upper()
    with Session(engine) as db:
        s = db.scalar(select(ServerModel).where(ServerModel.invite_code == code))
        if not s: return jsonify({"detail": "Invalid code"}), 404
        if db.scalar(select(MembershipModel).where(MembershipModel.server_id == s.id, MembershipModel.user_id == current_user_id)):
            return jsonify({"detail": "Already member"}), 400
        db.add(MembershipModel(user_id=current_user_id, server_id=s.id))
        db.commit()
        return jsonify({"id": s.id, "name": s.name, "icon": s.icon, "ownerId": s.owner_id})

# --- CHANNEL ROUTES ---
@app.route("/servers/<sid>/channels", methods=['GET'])
@token_required
def api_get_channels(sid, current_user_id):
    with Session(engine) as db:
        chans = db.scalars(select(ChannelModel).where(ChannelModel.server_id == sid).order_by(ChannelModel.category.asc(), ChannelModel.name.asc())).all()
        return jsonify([{"id": c.id, "name": c.name, "category": c.category, "topic": c.topic} for c in chans])

@app.route("/servers/<sid>/members", methods=['GET'])
@token_required
def api_get_members(sid, current_user_id):
    with Session(engine) as db:
        mems = db.scalars(select(MembershipModel).where(MembershipModel.server_id == sid)).all()
        return jsonify([{
            "userId": m.user_id, "username": m.user.username, "nickname": m.nickname or m.user.display_name,
            "isOnline": m.user.is_online, "roles": [{"id": r.id, "name": r.name} for r in m.roles]
        } for m in mems])

@app.route("/servers/<sid>/channels/<cid>/messages", methods=['GET'])
@token_required
def api_get_messages(sid, cid, current_user_id):
    with Session(engine) as db:
        msgs = db.scalars(select(MessageModel).where(MessageModel.channel_id == cid).order_by(MessageModel.id.asc())).all()
        return jsonify([{"id": m.id, "author": m.author_name, "authorId": m.author_id, "text": m.text, "time": m.time} for m in msgs])

# --- SOCKETIO EVENTS ---
sid_to_uid = {}

@socketio.on('authenticate')
def on_authenticate(data):
    uid = decode_token(data.get('token'))
    if uid:
        sid_to_uid[request.sid] = uid
        with Session(engine) as db:
            u = db.get(UserModel, uid)
            if u:
                u.is_online = True
                db.commit()
                emit('presence_update', {"userId": uid, "isOnline": True}, broadcast=True)

@socketio.on('disconnect')
def on_disconnect():
    uid = sid_to_uid.pop(request.sid, None)
    if uid:
        with Session(engine) as db:
            u = db.get(UserModel, uid)
            if u:
                u.is_online = False
                db.commit()
                emit('presence_update', {"userId": uid, "isOnline": False}, broadcast=True)

@socketio.on('join')
def on_join(data):
    join_room(data['channelId'])

@socketio.on('send_message')
def on_send_message(data):
    uid = sid_to_uid.get(request.sid)
    if not uid: return
    with Session(engine) as db:
        u = db.get(UserModel, uid)
        msg = MessageModel(server_id=data['serverId'], channel_id=data['channelId'], author_id=uid, author_name=u.display_name or u.username, text=data['text'], time=datetime.now().strftime("%H:%M"))
        db.add(msg)
        db.commit()
        db.refresh(msg)
        emit('new_message', {
            "id": msg.id, "author": msg.author_name, "authorId": uid, "text": msg.text, "time": msg.time, "channelId": data['channelId']
        }, room=data['channelId'])

if __name__ == "__main__":
    socketio.run(app, host="0.0.0.0", port=8000, debug=True)
