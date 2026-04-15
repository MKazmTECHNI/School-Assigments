from datetime import datetime

from flask import Flask, jsonify, request
from flask_cors import CORS

app = Flask(__name__)
CORS(app)

SERVERS = [
    {"id": "general", "name": "General", "icon": "G"},
    {"id": "mobile-dev", "name": "Mobile Dev", "icon": "M"},
    {"id": "szkola", "name": "Szkoła", "icon": "S"},
]

CHANNELS = {
    "general": [
        {"id": "ogolny", "name": "#ogólny"},
        {"id": "nauka", "name": "#nauka"},
        {"id": "offtopic", "name": "#offtopic"},
    ],
    "mobile-dev": [
        {"id": "android", "name": "#android"},
        {"id": "ios", "name": "#ios"},
        {"id": "react-native", "name": "#react-native"},
    ],
    "szkola": [
        {"id": "projekt", "name": "#projekt"},
        {"id": "terminy", "name": "#terminy"},
        {"id": "pomoc", "name": "#pomoc"},
    ],
}

MESSAGES = {
    "general|ogolny": [
        {"id": "m1", "author": "Ola", "text": "Ej, kto ma plan na wieczór?", "time": "17:05"},
        {"id": "m2", "author": "Bartek", "text": "Ja klasycznie: serial + kebs 😎", "time": "17:06"},
    ],
    "general|offtopic": [
        {"id": "m3", "author": "Kuba", "text": "[meme: kot przy 4 monitorach]", "time": "19:25"},
    ],
    "mobile-dev|android": [
        {"id": "m4", "author": "Natalia", "text": "Najskuteczniejszy fix ever", "time": "16:48"},
        {"id": "m5", "author": "Kuba", "text": "u mnie działa dopiero po restartcie laptopa", "time": "16:49"},
    ],
    "szkola|terminy": [
        {"id": "m6", "author": "Natalia", "text": "Update: jutro jednak normalnie", "time": "19:48"},
    ],
}


def _server_exists(server_id: str) -> bool:
    return any(s["id"] == server_id for s in SERVERS)


def _channel_exists(server_id: str, channel_id: str) -> bool:
    return any(c["id"] == channel_id for c in CHANNELS.get(server_id, []))


def _conversation_key(server_id: str, channel_id: str) -> str:
    return f"{server_id}|{channel_id}"


@app.get("/health")
def health():
    return jsonify({"status": "ok"})


@app.get("/servers")
def get_servers():
    return jsonify(SERVERS)


@app.get("/servers/<server_id>/channels")
def get_channels(server_id: str):
    if not _server_exists(server_id):
        return jsonify({"detail": "Server not found"}), 404
    return jsonify(CHANNELS.get(server_id, []))


@app.get("/servers/<server_id>/channels/<channel_id>/messages")
def get_messages(server_id: str, channel_id: str):
    if not _server_exists(server_id):
        return jsonify({"detail": "Server not found"}), 404
    if not _channel_exists(server_id, channel_id):
        return jsonify({"detail": "Channel not found"}), 404

    key = _conversation_key(server_id, channel_id)
    return jsonify(MESSAGES.get(key, []))


@app.post("/servers/<server_id>/channels/<channel_id>/messages")
def create_message(server_id: str, channel_id: str):
    if not _server_exists(server_id):
        return jsonify({"detail": "Server not found"}), 404
    if not _channel_exists(server_id, channel_id):
        return jsonify({"detail": "Channel not found"}), 404

    payload = request.get_json(silent=True) or {}
    author = str(payload.get("author", "")).strip()
    text = str(payload.get("text", "")).strip()

    if not author or not text:
        return jsonify({"detail": "author and text are required"}), 400

    key = _conversation_key(server_id, channel_id)
    conversation = MESSAGES.setdefault(key, [])
    new_message = {
        "id": f"m{len(conversation) + 1}_{int(datetime.utcnow().timestamp())}",
        "author": author,
        "text": text,
        "time": datetime.now().strftime("%H:%M"),
    }
    conversation.append(new_message)
    return jsonify(new_message), 201


if __name__ == "__main__":
    app.run(host="127.0.0.1", port=8000, debug=False, use_reloader=False)
