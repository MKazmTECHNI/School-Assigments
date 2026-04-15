# Accordance API (FastAPI)

Minimal backend API for servers, channels and messages.

## Run

1. Create and activate virtual environment
2. Install deps:

```bash
pip install -r requirements.txt
```

3. Start API:

```bash
uvicorn main:app --reload --host 127.0.0.1 --port 8000
```

Windows alternative:

```bash
py -m uvicorn main:app --reload --host 127.0.0.1 --port 8000
```

Note: Android emulator uses `http://10.0.2.2:8000/` to reach host localhost.

## Endpoints

- `GET /health`
- `GET /servers`
- `GET /servers/{server_id}/channels`
- `GET /servers/{server_id}/channels/{channel_id}/messages`
- `POST /servers/{server_id}/channels/{channel_id}/messages`

Docs available on:

- `http://127.0.0.1:8000/docs`
