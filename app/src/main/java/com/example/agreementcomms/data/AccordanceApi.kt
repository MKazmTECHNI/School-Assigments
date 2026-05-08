package com.example.agreementcomms.data

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Part
import okhttp3.MultipartBody

data class ApiServer(
    val id: String,
    val name: String,
    val icon: String
)

data class ApiChannel(
    val id: String,
    val name: String
)

data class ApiRole(
    val id: String,
    val name: String,
    val color: String? = null,
    val position: Int = 0
)

data class ApiMessage(
    val id: String,
    val author: String,
    val text: String,
    val time: String,
    val attachment: ApiAttachmentResponse? = null
)

data class ApiAttachmentResponse(
    val type: String,
    val name: String,
    val url: String? = null
)

data class ApiAttachmentRequest(
    val type: String,
    val name: String,
    val path: String
)

data class UploadResponse(
    val type: String,
    val name: String,
    val path: String,
    val url: String,
    val contentType: String? = null
)

data class CreateMessageRequest(
    val author: String,
    val text: String,
    val attachment: ApiAttachmentRequest? = null
)

data class CreateServerRequest(
    val name: String,
    val icon: String? = null,
    val id: String? = null
)

data class UpdateServerRequest(
    val name: String? = null,
    val icon: String? = null
)

data class CreateChannelRequest(
    val name: String,
    val id: String? = null
)

data class UpdateChannelRequest(
    val name: String
)

data class CreateRoleRequest(
    val name: String,
    val color: String? = null,
    val position: Int = 0,
    val id: String? = null
)

data class UpdateRoleRequest(
    val name: String? = null,
    val color: String? = null,
    val position: Int? = null
)

interface AccordanceApi {
    @GET("servers")
    suspend fun getServers(): List<ApiServer>

    @POST("servers")
    suspend fun createServer(@Body request: CreateServerRequest): ApiServer

    @PATCH("servers/{serverId}")
    suspend fun updateServer(
        @Path("serverId") serverId: String,
        @Body request: UpdateServerRequest
    ): ApiServer

    @DELETE("servers/{serverId}")
    suspend fun deleteServer(@Path("serverId") serverId: String)

    @GET("servers/{serverId}/channels")
    suspend fun getChannels(@Path("serverId") serverId: String): List<ApiChannel>

    @POST("servers/{serverId}/channels")
    suspend fun createChannel(
        @Path("serverId") serverId: String,
        @Body request: CreateChannelRequest
    ): ApiChannel

    @PATCH("servers/{serverId}/channels/{channelId}")
    suspend fun updateChannel(
        @Path("serverId") serverId: String,
        @Path("channelId") channelId: String,
        @Body request: UpdateChannelRequest
    ): ApiChannel

    @DELETE("servers/{serverId}/channels/{channelId}")
    suspend fun deleteChannel(
        @Path("serverId") serverId: String,
        @Path("channelId") channelId: String
    )

    @GET("servers/{serverId}/roles")
    suspend fun getRoles(@Path("serverId") serverId: String): List<ApiRole>

    @POST("servers/{serverId}/roles")
    suspend fun createRole(
        @Path("serverId") serverId: String,
        @Body request: CreateRoleRequest
    ): ApiRole

    @PATCH("servers/{serverId}/roles/{roleId}")
    suspend fun updateRole(
        @Path("serverId") serverId: String,
        @Path("roleId") roleId: String,
        @Body request: UpdateRoleRequest
    ): ApiRole

    @DELETE("servers/{serverId}/roles/{roleId}")
    suspend fun deleteRole(
        @Path("serverId") serverId: String,
        @Path("roleId") roleId: String
    )

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

    @Multipart
    @POST("uploads")
    suspend fun uploadFile(
        @Part file: MultipartBody.Part
    ): UploadResponse
}

object AccordanceApiClient {
    // Android emulator -> host machine localhost
    const val BASE_URL = "http://10.0.2.2:8000/"

    val api: AccordanceApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AccordanceApi::class.java)
    }
}
