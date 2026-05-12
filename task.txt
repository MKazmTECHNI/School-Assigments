Ideą projektu jest zasymulowanie komunikacji aplikacji webowej z prostymi urządzeniami (takimi jak włączniki światła) za pomocą MQTT.

Stwórz aplikację w FastAPI, która będzie posiadała następujące właściwości:
1. możliwość dodania nowego włącznika światła wraz z nazwą - włącznik powinien zostać dodany jedynie wówczas gdy otrzyma potwierdzenie komunikacji przez MQTT - włączniki powinny być identyfikowane przez UUID.
2. możliwość włączenia i wyłączenia światła - i zapis stanu włącznika po stronie aplikacji webowej.
3. możliwość zbierania statystyk czasu działania oświetlenia.

Stwórz aplikację odbierającą dane po MQTT z następującymi właściwościami:
1. odbiór informacji o rejestracji włącznika i wysłanie potwierdzenia tej operacji.
2. odbiór informacji o włączeniu i wyłączeniu oświetlenia: zasymulowanie przez wylogowanie zmiany stanu.

Oceniane będzie:
do 2 ptk. - styl, użycie gita, oddanie pracy na GitHubie.
do 5 ptk. - poprawność implementacji i architektury aplikacji webowej w FastAPI
do 5 ptk. - poprawność implementacji i architektury aplikacji symulującej sterownik oświetlenia
do 3 ptk. - poprawnośc komunikacji po MQTT