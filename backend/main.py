from datetime import datetime
from typing import Dict, List

from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel, Field


class Server(BaseModel):
    id: str
    name: str
    icon: str


class Channel(BaseModel):
    id: str
    name: str


class Message(BaseModel):
    id: str
    author: str
    text: str
    time: str


class CreateMessageRequest(BaseModel):
    author: str = Field(min_length=1, max_length=40)
    text: str = Field(min_length=1, max_length=1200)


app = FastAPI(title="Accordance API", version="0.1.0")

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)


SERVERS: List[Server] = [
    Server(id="general", name="General", icon="G"),
    Server(id="mobile-dev", name="Mobile Dev", icon="M"),
    Server(id="szkola", name="Szkoła", icon="S"),
]

CHANNELS: Dict[str, List[Channel]] = {
    "general": [
        Channel(id="ogolny", name="#ogólny"),
        Channel(id="nauka", name="#nauka"),
        Channel(id="offtopic", name="#offtopic"),
    ],
    "mobile-dev": [
        Channel(id="android", name="#android"),
        Channel(id="ios", name="#ios"),
        Channel(id="react-native", name="#react-native"),
    ],
    "szkola": [
        Channel(id="projekt", name="#projekt"),
        Channel(id="terminy", name="#terminy"),
        Channel(id="pomoc", name="#pomoc"),
    ],
}


MESSAGES: Dict[str, List[Message]] = {
    "general|ogolny": [
        Message(id="m1", author="Ola", text="Ej, kto ma plan na wieczór?", time="17:05"),
        Message(id="m2", author="Bartek", text="Ja klasycznie: serial + kebs 😎", time="17:06"),
    ],
    "general|offtopic": [
        Message(id="m3", author="Kuba", text="[meme: kot przy 4 monitorach]", time="19:25"),
    ],
    "mobile-dev|android": [
        Message(id="m4", author="Natalia", text="Najskuteczniejszy fix ever", time="16:48"),
        Message(id="m5", author="Kuba", text="u mnie działa dopiero po restartcie laptopa", time="16:49"),
    ],
    "szkola|terminy": [
        Message(id="m6", author="Natalia", text="Update: jutro jednak normalnie", time="19:48"),
    ],
}


def _server_exists(server_id: str) -> bool:
    return any(server.id == server_id for server in SERVERS)


def _channel_exists(server_id: str, channel_id: str) -> bool:
    return any(channel.id == channel_id for channel in CHANNELS.get(server_id, []))


def _conversation_key(server_id: str, channel_id: str) -> str:
    return f"{server_id}|{channel_id}"


@app.get("/health")
def health() -> dict:
    return {"status": "ok"}


@app.get("/servers", response_model=List[Server])
def get_servers() -> List[Server]:
    return SERVERS


@app.get("/servers/{server_id}/channels", response_model=List[Channel])
def get_channels(server_id: str) -> List[Channel]:
    if not _server_exists(server_id):
        raise HTTPException(status_code=404, detail="Server not found")
    return CHANNELS.get(server_id, [])


@app.get("/servers/{server_id}/channels/{channel_id}/messages", response_model=List[Message])
def get_messages(server_id: str, channel_id: str) -> List[Message]:
    if not _server_exists(server_id):
        raise HTTPException(status_code=404, detail="Server not found")
    if not _channel_exists(server_id, channel_id):
        raise HTTPException(status_code=404, detail="Channel not found")

    key = _conversation_key(server_id, channel_id)
    return MESSAGES.get(key, [])


@app.post("/servers/{server_id}/channels/{channel_id}/messages", response_model=Message)
def create_message(server_id: str, channel_id: str, body: CreateMessageRequest) -> Message:
    if not _server_exists(server_id):
        raise HTTPException(status_code=404, detail="Server not found")
    if not _channel_exists(server_id, channel_id):
        raise HTTPException(status_code=404, detail="Channel not found")

    key = _conversation_key(server_id, channel_id)
    conversation = MESSAGES.setdefault(key, [])

    new_message = Message(
        id=f"m{len(conversation) + 1}_{int(datetime.utcnow().timestamp())}",
        author=body.author.strip(),
        text=body.text.strip(),
        time=datetime.now().strftime("%H:%M"),
    )

    conversation.append(new_message)
    return new_message
