package com.example.agreementcomms.data

import android.util.Log
import io.socket.client.IO
import io.socket.client.Socket
import org.json.JSONObject
import java.net.URISyntaxException

class SocketHandler {
    private var mSocket: Socket? = null

    @Synchronized
    fun establishConnection() {
        try {
            mSocket = IO.socket(AccordanceApiClient.BASE_URL)
        } catch (e: URISyntaxException) {
            Log.e("SocketHandler", "Connection error: ${e.message}")
        }
    }

    @Synchronized
    fun getSocket(): Socket? {
        return mSocket
    }

    @Synchronized
    fun closeConnection() {
        mSocket?.disconnect()
        mSocket = null
    }

    fun joinChannel(channelId: String) {
        val data = JSONObject()
        data.put("channelId", channelId)
        mSocket?.emit("join", data)
    }

    fun sendMessage(token: String, serverId: String, channelId: String, text: String) {
        val data = JSONObject()
        data.put("token", token)
        data.put("serverId", serverId)
        data.put("channelId", channelId)
        data.put("text", text)
        mSocket?.emit("send_message", data)
    }

    fun onNewMessage(callback: (JSONObject) -> Unit) {
        mSocket?.on("new_message") { args ->
            if (args.isNotEmpty()) {
                val data = args[0] as JSONObject
                callback(data)
            }
        }
    }
}
