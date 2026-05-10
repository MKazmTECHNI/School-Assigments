package com.example.agreementcomms

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList

fun buildSampleConversations(): MutableMap<String, SnapshotStateList<Message>> {
    return mutableMapOf(
        "ogolny" to mutableStateListOf(
            Message("m1", "Ola", "Ej, kto ma plan na wieczór?", "17:05", false),
            Message("m2", "Bartek", "Ja klasycznie: serial + kebs 😎", "17:06", false),
            Message("m3", "Natalia", "Brzmi uczciwie", "17:07", false),
            Message("m4", "Kuba", "Ja może wyskoczę na kosza", "17:09", false),
            Message("m5", "Ola", "Jak coś to jestem chętna po 19", "17:10", false)
        ),
        "android" to mutableStateListOf(
            Message("m10", "Bartek", "Czy tylko mnie emulator czasem nienawidzi?", "16:45", false),
            Message("m11", "Ola", "Cold boot i modlitwa", "16:46", false)
        )
    )
}

fun defaultServers(): List<Server> {
    return listOf(
        Server(
            id = "general",
            name = "General",
            icon = "G",
            channels = listOf(
                Channel("ogolny", "#ogólny", "general"),
                Channel("nauka", "#nauka", "general"),
                Channel("offtopic", "#offtopic", "general")
            )
        ),
        Server(
            id = "mobile-dev",
            name = "Mobile Dev",
            icon = "M",
            channels = listOf(
                Channel("android", "#android", "mobile-dev"),
                Channel("ios", "#ios", "mobile-dev"),
                Channel("react-native", "#react-native", "mobile-dev")
            )
        )
    )
}
