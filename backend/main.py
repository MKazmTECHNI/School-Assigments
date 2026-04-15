import os
import uuid
from datetime import datetime
from typing import Optional

from dotenv import load_dotenv
from flask import Flask, jsonify, request, send_from_directory
from flask_cors import CORS
from sqlalchemy import DateTime, ForeignKey, Integer, String, Text, create_engine, func, select
from sqlalchemy.exc import OperationalError
from sqlalchemy.orm import DeclarativeBase, Mapped, Session, mapped_column, relationship
from werkzeug.utils import secure_filename

load_dotenv()

_DATABASE_URL_FROM_ENV = os.getenv("DATABASE_URL")
DATABASE_URL = _DATABASE_URL_FROM_ENV or "mysql+pymysql://root:root@127.0.0.1:3306/accordance"
SQLITE_FALLBACK_URL = f"sqlite:///{os.path.join(os.path.dirname(__file__), 'accordance.db')}"
UPLOAD_DIR = os.getenv("UPLOAD_DIR", os.path.join(os.path.dirname(__file__), "uploads"))
os.makedirs(UPLOAD_DIR, exist_ok=True)


class Base(DeclarativeBase):
    pass


class ServerModel(Base):
    __tablename__ = "servers"

    id: Mapped[str] = mapped_column(String(50), primary_key=True)
    name: Mapped[str] = mapped_column(String(100), nullable=False)
    icon: Mapped[str] = mapped_column(String(10), nullable=False)

    channels: Mapped[list["ChannelModel"]] = relationship(back_populates="server")


class ChannelModel(Base):
    __tablename__ = "channels"

    id: Mapped[str] = mapped_column(String(50), primary_key=True)
    name: Mapped[str] = mapped_column(String(100), nullable=False)
    server_id: Mapped[str] = mapped_column(ForeignKey("servers.id"), index=True)

    server: Mapped[ServerModel] = relationship(back_populates="channels")


class MessageModel(Base):
    __tablename__ = "messages"

    id: Mapped[int] = mapped_column(Integer, primary_key=True, autoincrement=True)
    server_id: Mapped[str] = mapped_column(String(50), index=True)
    channel_id: Mapped[str] = mapped_column(String(50), index=True)
    author: Mapped[str] = mapped_column(String(80), nullable=False)
    text: Mapped[str] = mapped_column(Text, nullable=False)
    time: Mapped[str] = mapped_column(String(10), nullable=False)
    attachment_type: Mapped[Optional[str]] = mapped_column(String(20), nullable=True)
    attachment_name: Mapped[Optional[str]] = mapped_column(String(255), nullable=True)
    attachment_path: Mapped[Optional[str]] = mapped_column(String(255), nullable=True)
    created_at: Mapped[datetime] = mapped_column(DateTime, nullable=False, server_default=func.now())


def _create_engine_with_fallback():
    engine = create_engine(DATABASE_URL, pool_pre_ping=True)
    try:
        with engine.connect() as conn:
            conn.execute(select(1))
        return engine
    except OperationalError:
        if _DATABASE_URL_FROM_ENV:
            raise
        print("[backend] MySQL is unavailable. Falling back to local SQLite database.")
        fallback_engine = create_engine(SQLITE_FALLBACK_URL, pool_pre_ping=True)
        with fallback_engine.connect() as conn:
            conn.execute(select(1))
        return fallback_engine


engine = _create_engine_with_fallback()
Base.metadata.create_all(engine)

app = Flask(__name__)
CORS(app)

def _seed_initial_data() -> None:
    with Session(engine) as db:
        has_servers = db.scalar(select(func.count()).select_from(ServerModel))
        if has_servers and has_servers > 0:
            return

        db.add_all(
            [
                ServerModel(id="general", name="General", icon="G"),
                ServerModel(id="mobile-dev", name="Mobile Dev", icon="M"),
                ServerModel(id="szkola", name="Szkoła", icon="S"),
            ]
        )

        db.add_all(
            [
                ChannelModel(id="ogolny", name="#ogólny", server_id="general"),
                ChannelModel(id="nauka", name="#nauka", server_id="general"),
                ChannelModel(id="offtopic", name="#offtopic", server_id="general"),
                ChannelModel(id="android", name="#android", server_id="mobile-dev"),
                ChannelModel(id="ios", name="#ios", server_id="mobile-dev"),
                ChannelModel(id="react-native", name="#react-native", server_id="mobile-dev"),
                ChannelModel(id="projekt", name="#projekt", server_id="szkola"),
                ChannelModel(id="terminy", name="#terminy", server_id="szkola"),
                ChannelModel(id="pomoc", name="#pomoc", server_id="szkola"),
            ]
        )

        db.add_all(
            [
                MessageModel(server_id="general", channel_id="ogolny", author="Ola", text="Ej, kto ma plan na wieczór?", time="17:05"),
                MessageModel(server_id="general", channel_id="ogolny", author="Bartek", text="Ja klasycznie: serial + kebs 😎", time="17:06"),
                MessageModel(server_id="general", channel_id="ogolny", author="Natalia", text="Jak coś to po 19 jestem wolna", time="17:10"),
                MessageModel(server_id="general", channel_id="ogolny", author="Kuba", text="to lecimy na boisko", time="17:11"),

                MessageModel(server_id="general", channel_id="nauka", author="Ola", text="Czy tylko ja się uczę lepiej w nocy?", time="18:11"),
                MessageModel(server_id="general", channel_id="nauka", author="Bartek", text="+1, po 22 mózg dopiero startuje", time="18:12"),
                MessageModel(server_id="general", channel_id="nauka", author="Natalia", text="Pomodoro 25/5 u mnie działa", time="18:15"),

                MessageModel(server_id="general", channel_id="offtopic", author="Kuba", text="Wrzucam mema dnia", time="19:21"),
                MessageModel(server_id="general", channel_id="offtopic", author="Ola", text="to fake, takie rzeczy nie istnieją", time="19:22"),
                MessageModel(server_id="general", channel_id="offtopic", author="Bartek", text="ten kanał to złoto", time="19:24"),

                MessageModel(server_id="mobile-dev", channel_id="android", author="Natalia", text="Najskuteczniejszy fix ever", time="16:48"),
                MessageModel(server_id="mobile-dev", channel_id="android", author="Bartek", text="emulator po cold boocie żyje", time="16:50"),
                MessageModel(server_id="mobile-dev", channel_id="android", author="Ola", text="adb też dziś współpracuje", time="16:51"),

                MessageModel(server_id="mobile-dev", channel_id="ios", author="Kuba", text="Ktoś faktycznie lubi Xcode?", time="15:30"),
                MessageModel(server_id="mobile-dev", channel_id="ios", author="Ola", text="jak się nie crashuje to lubię", time="15:31"),

                MessageModel(server_id="mobile-dev", channel_id="react-native", author="Natalia", text="RN hot reload to magia", time="14:12"),
                MessageModel(server_id="mobile-dev", channel_id="react-native", author="Kuba", text="najlepsza część tego stacku", time="14:13"),

                MessageModel(server_id="szkola", channel_id="projekt", author="Natalia", text="Kto widział moją bluzę?", time="13:40"),
                MessageModel(server_id="szkola", channel_id="projekt", author="Ola", text="została chyba w sali obok", time="13:42"),

                MessageModel(server_id="szkola", channel_id="terminy", author="Kuba", text="Jutro pierwsza lekcja odwołana?", time="11:02"),
                MessageModel(server_id="szkola", channel_id="terminy", author="Natalia", text="update dam wieczorem", time="11:07"),

                MessageModel(server_id="szkola", channel_id="pomoc", author="Ola", text="Jak usunąć plamę po kawie?", time="10:10"),
                MessageModel(server_id="szkola", channel_id="pomoc", author="Bartek", text="chusteczki i delikatnie wodą", time="10:12"),
            ]
        )
        db.commit()


def _server_exists(db: Session, server_id: str) -> bool:
    return db.scalar(select(ServerModel.id).where(ServerModel.id == server_id)) is not None


def _channel_exists(db: Session, server_id: str, channel_id: str) -> bool:
    return (
        db.scalar(
            select(ChannelModel.id).where(
                ChannelModel.server_id == server_id,
                ChannelModel.id == channel_id,
            )
        )
        is not None
    )


_seed_initial_data()


@app.get("/health")
def health():
    try:
        with Session(engine) as db:
            db.execute(select(1))
        return jsonify({"status": "ok", "db": "connected"})
    except Exception as ex:
        return jsonify({"status": "error", "db": str(ex)}), 500


@app.get("/servers")
def get_servers():
    with Session(engine) as db:
        servers = db.scalars(select(ServerModel).order_by(ServerModel.name.asc())).all()
        return jsonify([
            {"id": s.id, "name": s.name, "icon": s.icon}
            for s in servers
        ])


@app.get("/servers/<server_id>/channels")
def get_channels(server_id: str):
    with Session(engine) as db:
        if not _server_exists(db, server_id):
            return jsonify({"detail": "Server not found"}), 404
        channels = db.scalars(
            select(ChannelModel).where(ChannelModel.server_id == server_id).order_by(ChannelModel.name.asc())
        ).all()
        return jsonify([
            {"id": c.id, "name": c.name}
            for c in channels
        ])


@app.get("/servers/<server_id>/channels/<channel_id>/messages")
def get_messages(server_id: str, channel_id: str):
    with Session(engine) as db:
        if not _server_exists(db, server_id):
            return jsonify({"detail": "Server not found"}), 404
        if not _channel_exists(db, server_id, channel_id):
            return jsonify({"detail": "Channel not found"}), 404

        rows = db.scalars(
            select(MessageModel)
            .where(
                MessageModel.server_id == server_id,
                MessageModel.channel_id == channel_id,
            )
            .order_by(MessageModel.id.asc())
        ).all()
        return jsonify(
            [
                {
                    "id": f"m{m.id}",
                    "author": m.author,
                    "text": m.text,
                    "time": m.time,
                    "attachment": (
                        {
                            "type": m.attachment_type,
                            "name": m.attachment_name,
                            "url": f"/uploads/{m.attachment_path}" if m.attachment_path else None,
                        }
                        if m.attachment_type
                        else None
                    ),
                }
                for m in rows
            ]
        )


@app.post("/servers/<server_id>/channels/<channel_id>/messages")
def create_message(server_id: str, channel_id: str):
    with Session(engine) as db:
        if not _server_exists(db, server_id):
            return jsonify({"detail": "Server not found"}), 404
        if not _channel_exists(db, server_id, channel_id):
            return jsonify({"detail": "Channel not found"}), 404

        payload = request.get_json(silent=True) or {}
        author = str(payload.get("author", "")).strip()
        text = str(payload.get("text", "")).strip()
        attachment = payload.get("attachment") or {}

        if not author or not text:
            return jsonify({"detail": "author and text are required"}), 400

        row = MessageModel(
            server_id=server_id,
            channel_id=channel_id,
            author=author,
            text=text,
            time=datetime.now().strftime("%H:%M"),
            attachment_type=str(attachment.get("type", "")).strip() or None,
            attachment_name=str(attachment.get("name", "")).strip() or None,
            attachment_path=str(attachment.get("path", "")).strip() or None,
        )
        db.add(row)
        db.commit()
        db.refresh(row)

        return (
            jsonify(
                {
                    "id": f"m{row.id}",
                    "author": row.author,
                    "text": row.text,
                    "time": row.time,
                }
            ),
            201,
        )


@app.post("/uploads")
def upload_file():
    uploaded = request.files.get("file")
    if not uploaded or not uploaded.filename:
        return jsonify({"detail": "file is required"}), 400

    original = secure_filename(uploaded.filename)
    unique_name = f"{uuid.uuid4().hex}_{original}"
    path = os.path.join(UPLOAD_DIR, unique_name)
    uploaded.save(path)

    file_type = "image" if (uploaded.mimetype or "").startswith("image/") else "file"
    return jsonify(
        {
            "type": file_type,
            "name": original,
            "path": unique_name,
            "url": f"/uploads/{unique_name}",
            "contentType": uploaded.mimetype,
        }
    )


@app.get("/uploads/<path:filename>")
def serve_uploaded_file(filename: str):
    return send_from_directory(UPLOAD_DIR, filename)


if __name__ == "__main__":
    app.run(host="127.0.0.1", port=8000, debug=False, use_reloader=False)
