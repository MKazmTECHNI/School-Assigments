# Accordance API (Flask)

Minimal backend API for servers, channels and messages.

## Run

1. Create and activate virtual environment
2. Install deps:

```bash
pip install -r requirements.txt
```

3. Start API:

```bash
py main.py
```

Note: Android emulator uses `http://10.0.2.2:8000/` to reach host localhost.

## Endpoints

- `GET /health`
- `GET /servers`
- `GET /servers/{server_id}/channels`
- `GET /servers/{server_id}/channels/{channel_id}/messages`
- `POST /servers/{server_id}/channels/{channel_id}/messages`
