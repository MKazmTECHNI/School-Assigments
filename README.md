# Accordance

Accordance to szkolny projekt na przedmiot **Aplikacje Mobilne**.
Pomysł: lekka kopia komunikatora w stylu Discord. (Discord = Niezgoda, Accordance = Zgoda)

## Technologie

- **Android (Kotlin)**
- **Jetpack Compose (Material 3)**
- Gradle Kotlin DSL
- **Backend: Python FastAPI** (folder `backend/`)

## Skład zespołu

- Autor: MKazm
- Tryb pracy: indywidualnie

## Główne funkcjonalności (MVP)

- Ekran wejścia/logowania (mock z nickiem)
- Lista serwerów (drawer)
- Lista kanałów dla wybranego serwera
- Osobny czat dla każdego kanału każdego serwera
- Prosty ekran czatu (wiadomości lokalne, bez backendu)
- Drawer typu Discord: pełny ekran czatu + wysuwany panel
- Ustawienia (mock)

## Status względem harmonogramu

### Etap 1 (31 marca) — zrealizowane

W aplikacji zaimplementowano:

1. ekran logowania,
2. wybór serwera i kanału,
3. widok czatu z wysyłaniem wiadomości lokalnych,
4. ekran ustawień (podstawowy),
5. dark mode + Discord-like UI,
6. mockowe konwersacje o różnej długości (test scrolla).

## Architektura (stan aktualny)

- UI: Jetpack Compose (single-activity)
- Stan: local state w Compose (`remember`, `rememberSaveable`)
- Dane: mock in-memory (hardcoded serwery/kanały/wiadomości)
- Routing widoków: sekcje `Chat` / `Settings`

To oznacza: obecnie dane nie są pobierane z API i nie są trwałe po restarcie aplikacji.

## Architektura docelowa (kolejny etap)

- `ui/` – composable + screen state
- `domain/` – modele i use-case'y
- `data/` – repository + remote source (API) + local cache (Room)
- `ViewModel` + `UiState` + eventy UI
- Docelowo serwery/kanały/wiadomości będą pobierane z backendu

## Backend (wstępnie dodany)

Dodano prosty serwer API w folderze `backend/`:

- `GET /servers`
- `GET /servers/{server_id}/channels`
- `GET /servers/{server_id}/channels/{channel_id}/messages`
- `POST /servers/{server_id}/channels/{channel_id}/messages`

To pozwala stopniowo przechodzić z mocków lokalnych na realny fetch.

## Plan dalszych prac (do 10 maja)

- [ ] Refactor do `ViewModel + Repository`
- [ ] Integracja backendu (serwery, kanały, wiadomości)
- [ ] Trwałość lokalna (Room) + cache
- [ ] Reakcje, piny, edycja/usuwanie wiadomości
- [ ] Uwierzytelnianie i profile użytkowników
- [ ] Finalne poprawki UI/UX pod prezentację
