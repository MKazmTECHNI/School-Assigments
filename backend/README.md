# Accordance Backend (Python Flask + Socket.io)

Wydajny i skalowalny serwer API oraz WebSocket dla komunikatora Accordance. Obsługuje komunikację w czasie rzeczywistym, zaawansowany system uprawnień oraz bezpieczne przechowywanie danych.

## 🚀 Funkcje

-   **Real-time Communication**: Pełna integracja z **Flask-SocketIO** dla natychmiastowej wymiany wiadomości i wskaźników pisania.
-   **Autentykacja JWT**: Bezpieczne logowanie i rejestracja z wykorzystaniem tokenów JSON Web Token.
-   **System Uprawnień**: 
    -   Dynamiczne role z definiowalnymi kolorami i priorytetami.
    -   Nadpisywanie uprawnień na poziomie kanałów (Channel Overrides).
    -   Zarządzanie serwerem, kanałami, rolami i wiadomościami.
-   **Trwałość Danych**: Obsługa baz danych **MySQL** (produkcyjnie) oraz **SQLite** (lokalnie/dewelopersko) przez SQLAlchemy.
-   **File Storage**: System przesyłania załączników (zdjęcia, pliki) z obsługą systemów plików.
-   **Presence Tracking**: Śledzenie statusu online/offline użytkowników w czasie rzeczywistym.

## 🛠️ Instalacja i Uruchomienie

1.  **Środowisko wirtualne**:
    ```bash
    python -m venv venv
    source venv/bin/activate  # Linux/macOS
    venv\Scripts\activate     # Windows
    ```

2.  **Instalacja zależności**:
    ```bash
    pip install -r requirements.txt
    ```

3.  **Konfiguracja**:
    Skopiuj plik `.env.example` do `.env` i uzupełnij klucze:
    -   `DATABASE_URL`: Adres Twojej bazy MySQL (lub pozostaw puste dla SQLite).
    -   `SECRET_KEY`: Unikalny klucz dla podpisów JWT.

4.  **Baza danych (Opcjonalnie - Docker)**:
    ```bash
    docker compose up -d
    ```

5.  **Start Serwera**:
    ```bash
    python main.py
    ```

Serwer domyślnie uruchamia się na `http://0.0.0.0:8000`.

## 📡 Główne Endpointy API

-   `POST /auth/register` - Rejestracja nowego użytkownika.
-   `POST /auth/login` - Logowanie i pobranie tokena.
-   `GET /servers` - Lista serwerów użytkownika.
-   `POST /servers` - Tworzenie nowego serwera.
-   `GET /servers/{sid}/channels` - Pobieranie kanałów danego serwera.
-   `POST /uploads` - Przesyłanie załączników.

## 🔌 Zdarzenia WebSocket (Socket.io)

-   `authenticate` - Autoryzacja połączenia tokenem JWT.
-   `send_message` - Wysyłanie zaszyfrowanej wiadomości na kanał.
-   `join` - Dołączanie do pokoju (room) kanału.
-   `typing_start` - Rozpoczęcie pisania wiadomości.

---
*Projekt zrealizowany na potrzeby przedmiotu Aplikacje Mobilne.*
