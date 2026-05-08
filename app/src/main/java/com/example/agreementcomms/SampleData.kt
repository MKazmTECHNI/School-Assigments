package com.example.agreementcomms

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList

fun buildSampleConversations(): MutableMap<String, SnapshotStateList<Message>> {
    return mutableMapOf(
        conversationKey("general", "#ogólny") to mutableStateListOf(
            Message("Ola", "Ej, kto ma plan na wieczór?", "17:05", false),
            Message("Bartek", "Ja klasycznie: serial + kebs 😎", "17:06", false),
            Message("Natalia", "Brzmi uczciwie", "17:07", false),
            Message("Kuba", "Ja może wyskoczę na kosza", "17:09", false),
            Message("Ola", "Jak coś to jestem chętna po 19", "17:10", false),
            Message("Bartek", "To ja tylko buty ogarnę i lecimy", "17:11", false),
            Message("Natalia", "W końcu aktywnie, wow", "17:12", false),
            Message("Kuba", "screen tego momentu 📸", "17:13", false),
            Message("Ola", "to po 19:15 przy boisku?", "17:14", false),
            Message("Bartek", "pasuje", "17:14", false),
            Message("Natalia", "ja 10 min później, korki", "17:15", false)
        ),
        conversationKey("general", "#nauka") to mutableStateListOf(
            Message("Ola", "Czy tylko ja się uczę lepiej w nocy?", "18:11", false),
            Message("Bartek", "+1, po 22 nagle mózg: let's go", "18:12", false),
            Message("Natalia", "A rano ten sam mózg: nope", "18:13", false),
            Message("Kuba", "Rel", "18:13", false),
            Message("Ola", "Polecacie jakieś lofi playlisty?", "18:14", false),
            Message("Bartek", "lofi girl i zero powiadomień", "18:15", false),
            Message("Natalia", "i technika pomodoro 25/5 serio działa", "18:16", false),
            Message("Kuba", "ja robię 40/10 bo 25 to rozgrzewka", "18:17", false),
            Message("Ola", "notuję, thanks", "18:18", false)
        ),
        conversationKey("general", "#offtopic") to mutableStateListOf(
            Message("Kuba", "wrzucam mema dnia", "19:21", false),
            Message(
                "Kuba",
                "idealne podsumowanie dnia",
                "19:21",
                false,
                attachments = listOf(
                    MessageAttachment(
                        type = AttachmentType.Image,
                        name = "meme_first_try.jpg",
                        url = "https://images.unsplash.com/photo-1517336714739-489689fd1ca8?auto=format&fit=crop&w=1000&q=80"
                    )
                )
            ),
            Message("Ola", "to fake, takie rzeczy nie istnieją", "19:22", false),
            Message("Bartek", "dokładnie, to AI-generated", "19:23", false),
            Message("Natalia", "xDDD", "19:23", false),
            Message("Kuba", "mam jeszcze jednego z kotem programistą", "19:24", false),
            Message("Ola", "dawaj", "19:24", false),
            Message(
                "Kuba",
                "kot dev edition",
                "19:25",
                false,
                attachments = listOf(
                    MessageAttachment(
                        type = AttachmentType.Image,
                        name = "cat_dev.png",
                        url = "https://images.unsplash.com/photo-1511044568932-338cba0ad803?auto=format&fit=crop&w=1000&q=80"
                    )
                )
            ),
            Message("Bartek", "to ja po 3 kawie", "19:25", false),
            Message("Natalia", "i z deadline'em za 15 minut", "19:26", false),
            Message("Ola", "literally", "19:26", false),
            Message("Kuba", "ok koniec spamu, pa 😅", "19:27", false),
            Message("Bartek", "nie no jeszcze jeden i serio kończymy", "19:28", false),
            Message("Natalia", "klasyk", "19:28", false),
            Message("Ola", "ten kanał nigdy nie śpi", "19:29", false)
        ),
        conversationKey("mobile-dev", "#android") to mutableStateListOf(
            Message("Bartek", "Czy tylko mnie emulator czasem nienawidzi?", "16:45", false),
            Message("Ola", "Cold boot i modlitwa", "16:46", false),
            Message("Natalia", "Najskuteczniejszy fix ever", "16:48", false),
            Message("Kuba", "u mnie działa dopiero po restartcie laptopa", "16:49", false),
            Message("Bartek", "to już rytuał", "16:50", false),
            Message("Ola", "przynajmniej adb jeszcze żyje", "16:51", false),
            Message("Natalia", "czasem…", "16:51", false),
            Message("Kuba", "jak gradle cache pęknie to już tylko płacz", "16:52", false),
            Message("Bartek", "i invalidate caches + restart studio", "16:53", false),
            Message("Ola", "to powinno być oficjalne zaklęcie", "16:54", false)
        ),
        conversationKey("mobile-dev", "#ios") to mutableStateListOf(
            Message("Kuba", "Ktoś faktycznie lubi Xcode? pytam dla kolegi", "15:30", false),
            Message("Ola", "lubię… jak się nie crashuje", "15:31", false),
            Message("Bartek", "czyli 2 razy w miesiącu?", "15:33", false),
            Message("Natalia", "💀", "15:33", false),
            Message("Kuba", "simulator też dziś wolniejszy niż ja rano", "15:34", false),
            Message("Ola", "to akurat normalne", "15:35", false),
            Message("Bartek", "kawa dla ciebie i dla Maca", "15:35", false)
        ),
        conversationKey("mobile-dev", "#react-native") to mutableStateListOf(
            Message("Natalia", "RN hot reload to nadal magia", "14:12", false),
            Message("Kuba", "true, to jest najlepsza część", "14:13", false),
            Message("Ola", "plus jeden codebase i mniej bólu", "14:14", false),
            Message("Bartek", "dopóki native module nie powie stop", "14:15", false),
            Message("Natalia", "facts", "14:15", false)
        ),
        conversationKey("szkola", "#projekt") to mutableStateListOf(
            Message("Natalia", "Kto widział moją czarną bluzę z kapturem?", "13:40", false),
            Message("Bartek", "ta z małym logo?", "13:41", false),
            Message("Natalia", "tak", "13:41", false),
            Message("Ola", "chyba została w sali obok okna", "13:42", false),
            Message(
                "Natalia",
                "ratujecie życie, dzięki",
                "13:43",
                false,
                attachments = listOf(
                    MessageAttachment(
                        type = AttachmentType.File,
                        name = "lista_zakupow_weekend.pdf",
                        meta = "PDF • 1.2 MB"
                    )
                )
            ),
            Message("Kuba", "znalazłem jeszcze powerbank, czyj?", "13:44", false),
            Message("Bartek", "mój! oddam ci jutro batonika", "13:45", false),
            Message("Natalia", "deal accepted", "13:45", false)
        ),
        conversationKey("szkola", "#terminy") to mutableStateListOf(
            Message("Kuba", "Jutro pierwsza lekcja odwołana czy plotka?", "11:02", false),
            Message("Natalia", "Podobno odwołana, ale czekam na potwierdzenie", "11:03", false),
            Message("Bartek", "u mnie na librusie jeszcze cisza", "11:05", false),
            Message("Ola", "jak nic nie wrzucą do 20:00 to i tak przyjdę na później", "11:06", false),
            Message("Kuba", "fair", "11:06", false),
            Message("Natalia", "dam znać jak coś się pojawi", "11:07", false),
            Message("Bartek", "🙏", "11:07", false),
            Message("Ola", "dzięki", "11:08", false),
            Message("Natalia", "update: jednak normalnie jest", "19:48", false),
            Message("Kuba", "czyli budzik na 6:30, super…", "19:49", false),
            Message("Bartek", "trzymajcie się tam", "19:49", false),
            Message("Ola", "weźcie termos, będzie zimno", "19:50", false)
        ),
        conversationKey("szkola", "#pomoc") to mutableStateListOf(
            Message("Ola", "Jak usunąć plamę po kawie z notatek?", "10:10", false),
            Message("Kuba", "ryż 😂", "10:11", false),
            Message("Natalia", "Kuba pls", "10:11", false),
            Message("Bartek", "chusteczki + delikatnie wodą, tylko nie trzeć mocno", "10:12", false),
            Message("Ola", "ok, testuję", "10:13", false),
            Message("Ola", "działa, dzięki!", "10:16", false),
            Message("Kuba", "ej no ryż też działa na wszystko", "10:17", false),
            Message("Natalia", "tylko nie na ten argument", "10:17", false),
            Message("Bartek", "przynajmniej morale podniósł", "10:18", false)
        )
    )
}

fun defaultServers(): List<Server> {
    return listOf(
        Server(
            id = "general",
            name = "General",
            icon = "G",
            channels = listOf("#ogólny", "#nauka", "#offtopic")
        ),
        Server(
            id = "mobile-dev",
            name = "Mobile Dev",
            icon = "M",
            channels = listOf("#android", "#ios", "#react-native")
        ),
        Server(
            id = "szkola",
            name = "Szkoła",
            icon = "S",
            channels = listOf("#projekt", "#terminy", "#pomoc")
        )
    )
}
