package com.example.agreementcomms.data

import okhttp3.Interceptor
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

data class ApiUser(
    val id: String,
    val username: String,
    val displayName: String? = null,
    val avatarUrl: String? = null,
    val statusText: String? = null,
    val bio: String? = null
)

data class AuthRequest(
    val username: String,
    val password: String
)

data class AuthResponse(
    val token: String,
    val user: ApiUser
)

data class ApiServer(
    val id: String,
    val name: String,
    val icon: String,
    val ownerId: String? = null,
    val inviteCode: String? = null
)

data class ApiChannel(
    val id: String,
    val name: String,
    val topic: String? = null,
    val category: String? = null,
    val slowmodeSeconds: Int = 0,
    val isNsfw: Boolean = false
)

data class ApiRolePermissions(
    val manageServer: Boolean = false,
    val manageChannels: Boolean = false,
    val manageRoles: Boolean = false,
    val manageMessages: Boolean = false
)

data class ApiRole(
    val id: String,
    val name: String,
    val color: String? = null,
    val position: Int = 0,
    val permissions: ApiRolePermissions = ApiRolePermissions()
)

data class ApiMember(
    val userId: String,
    val username: String,
    val nickname: String? = null,
    val isOnline: Boolean = false,
    val roles: List<ApiRole> = emptyList()
)

data class ApiMessage(
    val id: String,
    val author: String,
    val authorId: String? = null,
    val text: String,
    val time: String,
    val attachment: ApiAttachmentResponse? = null
)

data class ApiAttachmentResponse(
    val type: String,
    val name: String,
    val url: String? = null
)

data class UploadResponse(
    val type: String,
    val name: String,
    val path: String,
    val url: String,
    val contentType: String? = null
)

data class CreateServerRequest(val name: String, val icon: String? = null)
data class UpdateServerRequest(val name: String? = null, val icon: String? = null)
data class CreateChannelRequest(val name: String, val topic: String? = null, val category: String? = null, val slowmodeSeconds: Int = 0, val isNsfw: Boolean = false)
data class UpdateChannelRequest(val name: String? = null, val topic: String? = null, val category: String? = null, val slowmodeSeconds: Int? = null, val isNsfw: Boolean? = null)
data class CreateRoleRequest(val name: String, val color: String? = null, val position: Int = 0, val permissions: ApiRolePermissions? = null)
data class UpdateRoleRequest(val name: String? = null, val color: String? = null, val position: Int? = null, val permissions: ApiRolePermissions? = null)

data class ApiChannelRoleOverride(
    val roleId: String,
    val permissions: ApiRolePermissionsOverride = ApiRolePermissionsOverride()
)

data class ApiRolePermissionsOverride(
    val manageServer: Boolean? = null,
    val manageChannels: Boolean? = null,
    val manageRoles: Boolean? = null,
    val manageMessages: Boolean? = null
)

data class UpdateChannelRoleOverrideRequest(val permissions: ApiRolePermissionsOverride)

interface AccordanceApi {
    @POST("auth/register")
    suspend fun register(@Body request: AuthRequest): AuthResponse

    @POST("auth/login")
    suspend fun login(@Body request: AuthRequest): AuthResponse

    @PATCH("profile")
    suspend fun updateProfile(@Body user: ApiUser): ApiUser

    @GET("servers")
    suspend fun getServers(): List<ApiServer>

    @POST("servers")
    suspend fun createServer(@Body request: CreateServerRequest): ApiServer

    @POST("servers/join/{inviteCode}")
    suspend fun joinServer(@Path("inviteCode") inviteCode: String): ApiServer

    @PATCH("servers/{serverId}")
    suspend fun updateServer(@Path("serverId") serverId: String, @Body request: UpdateServerRequest): ApiServer

    @DELETE("servers/{serverId}")
    suspend fun deleteServer(@Path("serverId") serverId: String)

    @GET("servers/{serverId}/channels")
    suspend fun getChannels(@Path("serverId") serverId: String): List<ApiChannel>

    @POST("servers/{serverId}/channels")
    suspend fun createChannel(@Path("serverId") serverId: String, @Body request: CreateChannelRequest): ApiChannel

    @PATCH("servers/{serverId}/channels/{channelId}")
    suspend fun updateChannel(@Path("serverId") serverId: String, @Path("channelId") channelId: String, @Body request: UpdateChannelRequest): ApiChannel

    @DELETE("servers/{serverId}/channels/{channelId}")
    suspend fun deleteChannel(@Path("serverId") serverId: String, @Path("channelId") channelId: String)

    @GET("servers/{serverId}/roles")
    suspend fun getRoles(@Path("serverId") serverId: String): List<ApiRole>

    @POST("servers/{serverId}/roles")
    suspend fun createRole(@Path("serverId") serverId: String, @Body request: CreateRoleRequest): ApiRole

    @PATCH("servers/{serverId}/roles/{roleId}")
    suspend fun updateRole(@Path("serverId") serverId: String, @Path("roleId") roleId: String, @Body request: UpdateRoleRequest): ApiRole

    @DELETE("servers/{serverId}/roles/{roleId}")
    suspend fun deleteRole(@Path("serverId") serverId: String, @Path("roleId") roleId: String)

    @GET("servers/{serverId}/channels/{channelId}/messages")
    suspend fun getMessages(@Path("serverId") serverId: String, @Path("channelId") channelId: String): List<ApiMessage>

    @GET("servers/{serverId}/members")
    suspend fun getMembers(@Path("serverId") serverId: String): List<ApiMember>

    @PUT("servers/{serverId}/members/{userId}/roles/{roleId}")
    suspend fun addRoleToMember(@Path("serverId") serverId: String, @Path("userId") userId: String, @Path("roleId") roleId: String)

    @DELETE("servers/{serverId}/members/{userId}/roles/{roleId}")
    suspend fun removeRoleFromMember(@Path("serverId") serverId: String, @Path("userId") userId: String, @Path("roleId") roleId: String)

    @GET("servers/{serverId}/channels/{channelId}/role-overrides")
    suspend fun getChannelRoleOverrides(@Path("serverId") serverId: String, @Path("channelId") channelId: String): List<ApiChannelRoleOverride>

    @PUT("servers/{serverId}/channels/{channelId}/role-overrides/{roleId}")
    suspend fun upsertChannelRoleOverride(@Path("serverId") serverId: String, @Path("channelId") channelId: String, @Path("roleId") roleId: String, @Body request: UpdateChannelRoleOverrideRequest): ApiChannelRoleOverride

    @DELETE("servers/{serverId}/channels/{channelId}/role-overrides/{roleId}")
    suspend fun deleteChannelRoleOverride(@Path("serverId") serverId: String, @Path("channelId") channelId: String, @Path("roleId") roleId: String)

    @Multipart
    @POST("uploads")
    suspend fun uploadFile(@Part file: MultipartBody.Part): UploadResponse
}

class AuthInterceptor : Interceptor {
    var token: String? = null
    override fun intercept(chain: Interceptor.Chain): okhttp3.Response {
        val originalRequest = chain.request()
        val requestBuilder = originalRequest.newBuilder()
        token?.let { 
            requestBuilder.header("Authorization", "Bearer $it") 
        }
        return chain.proceed(requestBuilder.build())
    }
}

object AccordanceApiClient {
    const val BASE_URL = "http://10.0.2.2:8000/"
    val authInterceptor = AuthInterceptor()
    
    private val client = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .build()

    val api: AccordanceApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AccordanceApi::class.java)
    }
}
