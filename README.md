# Accordance — Komunikator Nowej Generacji

Accordance to nowoczesna aplikacja mobilna zainspirowana platformą Discord (Discord = Niezgoda, Accordance = Zgoda). Jest to kompleksowy projekt realizowany w ramach przedmiotu **Aplikacje Mobilne**, łączący zaawansowane technologie Androida z wydajnym backendem.

## 🌟 Kluczowe Funkcjonalności

### 💬 Komunikacja i Czat
- **Real-time Messaging**: Natychmiastowa wymiana wiadomości dzięki integracji z **Socket.io**.
- **Szyfrowanie AES**: Bezpieczeństwo danych dzięki symetrycznemu szyfrowaniu wiadomości (AES/ECB/PKCS5Padding).
- **Wskaźniki Pisania**: Powiadomienia w czasie rzeczywistym, gdy inny użytkownik tworzy wiadomość.
- **Statusy Obecności**: System śledzenia statusu online/offline członków serwera.
- **Załączniki**: Możliwość przesyłania zdjęć oraz plików bezpośrednio na czacie.
- **Wyszukiwanie**: Intuicyjna wyszukiwarka wiadomości wewnątrz kanałów.

### 🛡️ Zarządzanie Społecznością
- **System Serwerów i Kanałów**: Tworzenie własnych serwerów oraz kategoryzowanie kanałów (tekstowe, NSFW, itp.).
- **Zaawansowane Role**: Tworzenie ról z niestandardowymi kolorami, pozycjami i uprawnieniami.
- **Precyzyjne Uprawnienia**: System permisji obejmujący zarządzanie serwerem, kanałami, rolami oraz uprawnienia do wysyłania wiadomości.
- **Kody Zaproszeń**: Łatwe dołączanie do nowych społeczności za pomocą unikalnych kodów.

### 👤 Personalizacja i UX
- **Profile Użytkowników**: Edycja pseudonimu (Display Name), statusu tekstowego, bio oraz awatara.
- **Tryb Wyglądu**: Opcjonalny **tryb kompaktowy** dla bardziej gęstego układu wiadomości.
- **Powiadomienia**: Zarządzanie powiadomieniami push oraz wibracjami z poziomu ustawień.
- **Discord-like UI**: Nowoczesny interfejs oparty na Jetpack Compose z drawerem nawigacyjnym i panelem członków.

## 🛠️ Stos Technologiczny

- **Frontend**: 
  - Kotlin + Jetpack Compose (Material 3)
  - **Retrofit** (komunikacja REST API)
  - **Socket.io Client** (WebSockety)
  - **Coil** (ładowanie obrazów)
  - **DataStore** (lokalne preferencje użytkownika)
- **Backend**:
  - Python Flask (serwer API)
  - MySQL (baza danych)
  - Socket.io (komunikacja dwukierunkowa)
- **Bezpieczeństwo**:
  - JCE (Java Cryptography Extension) dla szyfrowania AES.
  - Interceptor autoryzacji (Bearer Token).

## 🏗️ Architektura Systemu

Aplikacja została zaprojektowana zgodnie z wzorcem **MVVM (Model-View-ViewModel)**:
- **UI (Compose)**: Reaktywne widoki odświeżane na podstawie stanu `ChatUiState`.
- **ViewModel**: Centralny punkt logiki biznesowej, zarządzający połączeniami socketowymi i synchronizacją danych.
- **Repository**: Warstwa abstrakcji nad API i socketami, odpowiedzialna za pobieranie i wysyłanie danych.
- **SocketHandler**: Dedykowany moduł do obsługi komunikacji niskopoziomowej przez WebSockety.

## 🚀 Jak zacząć?

1.  **Backend**: Upewnij się, że serwer Python Flask jest uruchomiony na adresie `http://10.0.2.2:8000` (domyślny adres dla emulatora Androida).
2.  **Baza danych**: Skonfiguruj MySQL zgodnie ze schematem dostępnym w folderze `backend/`.
3.  **Android**: Skompiluj aplikację w Android Studio. Przy pierwszym uruchomieniu skorzystaj z opcji **Zarejestruj się**.

---
*Autor: MKazm*
*Projekt szkolny — Accordance 2024*
