# Accordance API (Flask + MySQL)

Minimal backend API for servers, channels and messages.

Data persistence:

- MySQL (messages/servers/channels)
- Local file system (`uploads/`) for images/files

## Run

1. Create and activate virtual environment
2. Install deps:

```bash
pip install -r requirements.txt
```

3. Configure environment:

```bash
copy .env.example .env
```

4. Start MySQL (Docker option):

```bash
docker compose up -d
```

5. Start API:

```bash
py main.py
```

Note: Android emulator uses `http://10.0.2.2:8000/` to reach host localhost.

Note: Tables are auto-created on startup.

## Endpoints

- `GET /health`
- `GET /servers`
- `GET /servers/{server_id}/channels`
- `GET /servers/{server_id}/channels/{channel_id}/messages`
- `POST /servers/{server_id}/channels/{channel_id}/messages`
- `POST /uploads` (multipart form-data: `file`)
- `GET /uploads/{filename}`
