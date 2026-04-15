package com.example.agreementcomms.data

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

data class ApiServer(
    val id: String,
    val name: String,
    val icon: String
)

data class ApiChannel(
    val id: String,
    val name: String
)

data class ApiMessage(
    val id: String,
    val author: String,
    val text: String,
    val time: String
)

data class CreateMessageRequest(
    val author: String,
    val text: String
)

interface AccordanceApi {
    @GET("servers")
    suspend fun getServers(): List<ApiServer>

    @GET("servers/{serverId}/channels")
    suspend fun getChannels(@Path("serverId") serverId: String): List<ApiChannel>

    @GET("servers/{serverId}/channels/{channelId}/messages")
    suspend fun getMessages(
        @Path("serverId") serverId: String,
        @Path("channelId") channelId: String
    ): List<ApiMessage>

    @POST("servers/{serverId}/channels/{channelId}/messages")
    suspend fun createMessage(
        @Path("serverId") serverId: String,
        @Path("channelId") channelId: String,
        @Body request: CreateMessageRequest
    ): ApiMessage
}

object AccordanceApiClient {
    // Android emulator -> host machine localhost
    private const val BASE_URL = "http://10.0.2.2:8000/"

    val api: AccordanceApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AccordanceApi::class.java)
    }
}
