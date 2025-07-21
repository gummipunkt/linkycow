package com.wltr.linkycow.data.remote

import com.wltr.linkycow.data.remote.dto.ApiError
import com.wltr.linkycow.data.remote.dto.ArchiveLinkResponse
import com.wltr.linkycow.data.remote.dto.AuthResponse
import com.wltr.linkycow.data.remote.dto.CreateLinkRequest
import com.wltr.linkycow.data.remote.dto.DashboardResponse
import com.wltr.linkycow.data.remote.dto.DeleteLinkResponse
import com.wltr.linkycow.data.remote.dto.LinkDetailResponse
import com.wltr.linkycow.data.remote.dto.LoginRequest
import com.wltr.linkycow.data.remote.dto.LinkResponse
import com.wltr.linkycow.data.remote.dto.SearchResponse
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

object ApiClient {

    // These should be empty in a real app and set after login
    private var instanceUrl: String = ""
    private var authToken: String? = null

    private val client = HttpClient(OkHttp) {
        expectSuccess = false // We want to handle HTTP errors manually

        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
            })
        }

        install(Logging) {
            logger = Logger.DEFAULT
            level = LogLevel.ALL
        }
    }

    fun setAuth(url: String, token: String) {
        this.instanceUrl = url
        this.authToken = token
    }

    fun clearAuth() {
        this.instanceUrl = ""
        this.authToken = null
    }

    suspend fun login(instanceUrl: String, loginRequest: LoginRequest): Result<AuthResponse> {
        return try {
            val url = URLBuilder(instanceUrl).apply {
                path("api", "v1", "session")
            }.buildString()

            val response = client.post(url) {
                contentType(ContentType.Application.Json)
                setBody(loginRequest)
            }

            if (response.status.isSuccess()) {
                val authResponse: AuthResponse = response.body()
                setAuth(instanceUrl, authResponse.response.token)
                Result.success(authResponse)
            } else {
                val apiError: ApiError = response.body()
                Result.failure(Exception(apiError.error))
            }

        } catch (e: Exception) {
            e.printStackTrace()
            // Catch other exceptions like network errors
            Result.failure(e)
        }
    }

    suspend fun login(instanceUrl: String, username: String, password: String): Result<AuthResponse> {
        val loginRequest = LoginRequest(username = username, password = password)
        return login(instanceUrl, loginRequest)
    }

    suspend fun getDashboard(): Result<DashboardResponse> {
        if (instanceUrl.isEmpty() || authToken == null) {
            return Result.failure(Exception("User is not authenticated."))
        }

        return try {
            val url = URLBuilder(instanceUrl).apply {
                path("api", "v2", "dashboard")
            }.buildString()

            val response = client.get(url) {
                header("Authorization", "Bearer $authToken")
                contentType(ContentType.Application.Json)
            }

            if (response.status.isSuccess()) {
                Result.success(response.body())
            } else {
                val apiError: ApiError = response.body()
                Result.failure(Exception(apiError.error))
            }

        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    suspend fun getLinkById(id: Int): Result<LinkDetailResponse> {
        if (instanceUrl.isEmpty() || authToken == null) {
            return Result.failure(Exception("User is not authenticated."))
        }

        return try {
            val url = URLBuilder(instanceUrl).apply {
                path("api", "v1", "links", id.toString())
            }.buildString()

            val response = client.get(url) {
                header("Authorization", "Bearer $authToken")
                contentType(ContentType.Application.Json)
            }

            if (response.status.isSuccess()) {
                Result.success(response.body())
            } else {
                val apiError: ApiError = response.body()
                Result.failure(Exception(apiError.error))
            }

        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    suspend fun archiveLink(id: Int): Result<ArchiveLinkResponse> {
        if (instanceUrl.isEmpty() || authToken == null) {
            return Result.failure(Exception("User is not authenticated."))
        }

        return try {
            val url = URLBuilder(instanceUrl).apply {
                path("api", "v1", "links", id.toString(), "archive")
            }.buildString()

            val response = client.put(url) {
                header("Authorization", "Bearer $authToken")
                contentType(ContentType.Application.Json)
            }

            if (response.status.isSuccess()) {
                Result.success(response.body())
            } else {
                val apiError: ApiError = response.body()
                Result.failure(Exception(apiError.error))
            }

        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    suspend fun searchLinks(query: String): Result<SearchResponse> {
        if (instanceUrl.isEmpty() || authToken == null) {
            return Result.failure(Exception("User is not authenticated."))
        }

        return try {
            val url = URLBuilder(instanceUrl).apply {
                path("api", "v1", "search")
                parameters.append("searchQueryString", query)
            }.buildString()

            val response = client.get(url) {
                header("Authorization", "Bearer $authToken")
                contentType(ContentType.Application.Json)
            }

            if (response.status.isSuccess()) {
                Result.success(response.body())
            } else {
                val apiError: ApiError = response.body()
                Result.failure(Exception(apiError.error))
            }

        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    suspend fun getLinkPreviewImage(linkId: Int): Result<ByteArray> {
        if (instanceUrl.isEmpty() || authToken == null) {
            return Result.failure(Exception("User is not authenticated."))
        }

        return try {
            val url = URLBuilder(instanceUrl).apply {
                path("api", "v1", "archives", linkId.toString())
                parameters.append("format", "1") // 1 for preview image
                parameters.append("preview", "true")
            }.buildString()

            val response = client.get(url) {
                header("Authorization", "Bearer $authToken")
            }

            if (response.status.isSuccess()) {
                Result.success(response.readBytes())
            } else {
                val errorBody = response.bodyAsText()
                Result.failure(Exception("Failed to get image: ${response.status} - $errorBody"))
            }

        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    suspend fun deleteLink(id: Int): Result<DeleteLinkResponse> {
        if (instanceUrl.isEmpty() || authToken == null) {
            return Result.failure(Exception("User is not authenticated."))
        }

        return try {
            val url = URLBuilder(instanceUrl).apply {
                path("api", "v1", "links", id.toString())
            }.buildString()

            val response = client.delete(url) {
                header("Authorization", "Bearer $authToken")
            }

            if (response.status.isSuccess()) {
                Result.success(response.body())
            } else {
                val apiError: ApiError = response.body()
                Result.failure(Exception(apiError.error))
            }

        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    suspend fun createLink(createLinkRequest: CreateLinkRequest): Result<LinkResponse> {
        if (instanceUrl.isEmpty() || authToken == null) {
            return Result.failure(Exception("User is not authenticated."))
        }

        return try {
            val url = URLBuilder(instanceUrl).apply {
                path("api", "v1", "links")
            }.buildString()

            val response = client.post(url) {
                header("Authorization", "Bearer $authToken")
                contentType(ContentType.Application.Json)
                setBody(createLinkRequest)
            }

            if (response.status.isSuccess()) {
                Result.success(response.body())
            } else {
                val apiError: ApiError = response.body()
                Result.failure(Exception(apiError.error))
            }

        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }
} 