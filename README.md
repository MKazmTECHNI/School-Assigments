# Accordance

Accordance to szkolny projekt na przedmiot **Aplikacje Mobilne**.
Pomysł: lekka kopia komunikatora w stylu Discord. (Discord = Niezgoda, Accordance = Zgoda)

## Technologie

- **Android (Kotlin)**
- **Jetpack Compose (Material 3)**
- Gradle Kotlin DSL

## Skład zespołu

- Autor: MKazm
- Tryb pracy: indywidualnie

## Główne funkcjonalności (MVP)

- Ekran wejścia/logowania (mock z nickiem)
- Lista serwerów
- Lista kanałów dla wybranego serwera
- Prosty ekran czatu (wiadomości lokalne, bez backendu)
- Nawigacja dolna: Czat / Ustawienia

## Status względem harmonogramu

### Etap 1 (31 marca) — zrealizowane

W aplikacji zaimplementowano:

1. ekran logowania,
2. wybór serwera i kanału,
3. widok czatu z wysyłaniem wiadomości lokalnych,
4. ekran ustawień (podstawowy).

## Plan dalszych prac (do 10 maja)

- Architektura z warstwą danych i ViewModel
- Trwałość danych (np. Room)
- Integracja z backendem / API czasu rzeczywistego
- Uwierzytelnianie i profile użytkowników
- Lepszy UI i walidacje
