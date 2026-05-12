# Light Switch Controller

Symulacja komunikacji aplikacji webowej z włącznikami światła przez MQTT.

## Architektura

- **app/** — aplikacja FastAPI z REST API
- **simulator/** — symulator urządzeń MQTT (sterownik oświetlenia)
- **Mosquitto** — broker MQTT (domyślnie publiczny `broker.emqx.io:1883`, można zmienić przez zmienną `MQTT_BROKER_HOST`)

### Tematy MQTT

| Topic | Opis |
|---|---|
| `switch/register` | Rejestracja włącznika (web app → symulator) |
| `switch/register/confirm/{id}` | Potwierdzenie rejestracji (symulator → web app) |
| `switch/{id}/command` | Komenda ON/OFF (web app → symulator) |
| `switch/{id}/status` | Status włącznika (symulator → web app) |

### Endpointy REST

| Metoda | Ścieżka | Opis |
|---|---|---|
| POST | `/switches` | Dodaje włącznik (czeka na potwierdzenie MQTT) |
| GET | `/switches` | Lista włączników |
| GET | `/switches/{id}` | Szczegóły włącznika |
| PUT | `/switches/{id}/turn` | Włącza/wyłącza światło (`{"state": "on"/"off"}`) |
| GET | `/switches/{id}/stats` | Statystyki czasu działania (sekundy) |

## Wymagania

- Python 3.10+
- Broker MQTT — domyślnie używany jest publiczny `broker.emqx.io:1883` (nie wymaga instalacji).  
  Aby użyć lokalnego brokera, ustaw zmienne środowiskowe:
  ```
  MQTT_BROKER_HOST=localhost
  MQTT_BROKER_PORT=1883
  ```

## Uruchomienie

### 1. Instalacja zależności

```bash
pip install -r requirements.txt
```

### 2. Symulator urządzeń (terminal 1)

```bash
python -m simulator.simulator
```

### 3. Aplikacja webowa (terminal 2)

```bash
uvicorn app.main:app --reload
```

API dostępne pod `http://localhost:8000`, dokumentacja Swagger pod `http://localhost:8000/docs`.

## Przykład użycia

```bash
# Dodanie włącznika
curl -X POST http://localhost:8000/switches -H "Content-Type: application/json" -d '{"name":"Lampa biurkowa"}'

# Włączenie światła
curl -X PUT http://localhost:8000/switches/<UUID>/turn -H "Content-Type: application/json" -d '{"state":"on"}'

# Wyłączenie światła
curl -X PUT http://localhost:8000/switches/<UUID>/turn -H "Content-Type: application/json" -d '{"state":"off"}'

# Statystyki
curl http://localhost:8000/switches/<UUID>/stats
```
