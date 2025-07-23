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
import com.wltr.linkycow.data.remote.dto.CollectionDto
import com.wltr.linkycow.data.remote.dto.TagDto
import com.wltr.linkycow.data.remote.dto.CollectionsResponse
import com.wltr.linkycow.data.remote.dto.TagsResponse
import com.wltr.linkycow.data.remote.dto.FilteredLinksResponse
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

/**
 * Singleton API client for Linkwarden server communication.
 * Handles all HTTP requests with proper authentication, error handling, and JSON serialization.
 */
object ApiClient {

    // Authentication state - initialized after successful login
    private var instanceUrl: String = ""
    private var authToken: String? = null

    // HTTP client configuration with OkHttp engine
    private val client = HttpClient(OkHttp) {
        expectSuccess = false // Manual HTTP error handling for better user feedback

        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true // Allow flexible JSON parsing
                ignoreUnknownKeys = true // Ignore API fields we don't need
            })
        }

        install(Logging) {
            logger = Logger.DEFAULT
            level = LogLevel.INFO // TODO: Reduce to LogLevel.INFO in production
        }
    }

    /**
     * Set authentication credentials for API requests
     */
    fun setAuth(url: String, token: String) {
        this.instanceUrl = url
        this.authToken = token
    }

    /**
     * Clear authentication state on logout
     */
    fun clearAuth() {
        this.instanceUrl = ""
        this.authToken = null
    }

    /**
     * Authenticate with Linkwarden server
     * @param instanceUrl The server URL (e.g., "https://links.example.com")
     * @param loginRequest Login credentials
     * @return Result containing auth response or error
     */
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
            Result.failure(e)
        }
    }

    /**
     * Convenience method for login with username/password
     */
    suspend fun login(instanceUrl: String, username: String, password: String): Result<AuthResponse> {
        val loginRequest = LoginRequest(username = username, password = password)
        return login(instanceUrl, loginRequest)
    }

    /**
     * Get dashboard data including recent links
     */
    suspend fun getDashboard(): Result<DashboardResponse> {
        return executeAuthenticatedRequest {
            val url = URLBuilder(instanceUrl).apply {
                path("api", "v2", "dashboard")
            }.buildString()

            client.get(url) {
                addAuthHeader()
                contentType(ContentType.Application.Json)
            }
        }
    }

    /**
     * Get detailed information for a specific link
     */
    suspend fun getLinkById(id: Int): Result<LinkDetailResponse> {
        return executeAuthenticatedRequest {
            val url = URLBuilder(instanceUrl).apply {
                path("api", "v1", "links", id.toString())
            }.buildString()

            client.get(url) {
                addAuthHeader()
                contentType(ContentType.Application.Json)
            }
        }
    }

    /**
     * Archive a link (preserve content snapshot)
     */
    suspend fun archiveLink(id: Int): Result<ArchiveLinkResponse> {
        return executeAuthenticatedRequest {
            val url = URLBuilder(instanceUrl).apply {
                path("api", "v1", "links", id.toString(), "archive")
            }.buildString()

            client.put(url) {
                addAuthHeader()
                contentType(ContentType.Application.Json)
            }
        }
    }

    /**
     * Search links by query string
     */
    suspend fun searchLinks(query: String): Result<SearchResponse> {
        return executeAuthenticatedRequest {
            val url = URLBuilder(instanceUrl).apply {
                path("api", "v1", "search")
                parameters.append("searchQueryString", query)
            }.buildString()

            client.get(url) {
                addAuthHeader()
                contentType(ContentType.Application.Json)
            }
        }
    }

    /**
     * Get preview image for a link
     */
    suspend fun getLinkPreviewImage(linkId: Int): Result<ByteArray> {
        if (!isAuthenticated()) {
            return Result.failure(Exception("User is not authenticated."))
        }

        return try {
            val url = URLBuilder(instanceUrl).apply {
                path("api", "v1", "archives", linkId.toString())
                parameters.append("format", "1") // 1 for preview image
                parameters.append("preview", "true")
            }.buildString()

            val response = client.get(url) {
                addAuthHeader()
            }

            if (response.status.isSuccess()) {
                Result.success(response.readBytes())
            } else {
                val errorBody = response.bodyAsText()
                Result.failure(Exception("Failed to get image: ${response.status} - $errorBody"))
            }

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Delete a link permanently
     */
    suspend fun deleteLink(id: Int): Result<DeleteLinkResponse> {
        return executeAuthenticatedRequest {
            val url = URLBuilder(instanceUrl).apply {
                path("api", "v1", "links", id.toString())
            }.buildString()

            client.delete(url) {
                addAuthHeader()
            }
        }
    }

    /**
     * Create a new link
     */
    suspend fun createLink(createLinkRequest: CreateLinkRequest): Result<LinkResponse> {
        return executeAuthenticatedRequest {
            val url = URLBuilder(instanceUrl).apply {
                path("api", "v1", "links")
            }.buildString()

            client.post(url) {
                addAuthHeader()
                contentType(ContentType.Application.Json)
                setBody(createLinkRequest)
            }
        }
    }

    /**
     * Update an existing link
     */
    suspend fun updateLink(linkId: Int, updateRequest: CreateLinkRequest): Result<LinkResponse> {
        return executeAuthenticatedRequest {
            val url = URLBuilder(instanceUrl).apply {
                path("api", "v1", "links", linkId.toString())
            }.buildString()
            
            client.put(url) {
                addAuthHeader()
                contentType(ContentType.Application.Json)
                setBody(updateRequest)
            }
        }
    }

    /**
     * Get all available collections/categories
     */
    suspend fun getCollections(): Result<List<CollectionDto>> {
        return executeAuthenticatedRequest<CollectionsResponse> {
            val url = URLBuilder(instanceUrl).apply {
                path("api", "v1", "collections")
            }.buildString()
            
            client.get(url) {
                addAuthHeader()
                contentType(ContentType.Application.Json)
            }
        }.map { it.response }
    }

    /**
     * Get all available tags
     */
    suspend fun getTags(): Result<List<TagDto>> {
        return executeAuthenticatedRequest<TagsResponse> {
            val url = URLBuilder(instanceUrl).apply {
                path("api", "v1", "tags")
            }.buildString()
            
            client.get(url) {
                addAuthHeader()
                contentType(ContentType.Application.Json)
            }
        }.map { it.response }
    }

    /**
     * Get links filtered by collection
     */
    suspend fun getLinksByCollection(collectionId: Int): Result<FilteredLinksResponse> {
        return executeAuthenticatedRequest {
            val url = URLBuilder(instanceUrl).apply {
                path("api", "v1", "links")
                parameters.append("collectionId", collectionId.toString())
            }.buildString()
            
            client.get(url) {
                addAuthHeader()
                contentType(ContentType.Application.Json)
            }
        }
    }

    /**
     * Get links filtered by tag
     */
    suspend fun getLinksByTag(tagId: Int): Result<FilteredLinksResponse> {
        return executeAuthenticatedRequest {
            val url = URLBuilder(instanceUrl).apply {
                path("api", "v1", "links")
                parameters.append("tagId", tagId.toString())
            }.buildString()
            
            client.get(url) {
                addAuthHeader()
                contentType(ContentType.Application.Json)
            }
        }
    }

    /**
     * Get paginated links for infinite scrolling
     * @param cursor Optional cursor for pagination (null for first page)
     */
    suspend fun getLinksPaged(cursor: Int? = null): Result<SearchResponse> {
        return executeAuthenticatedRequest {
            val url = URLBuilder(instanceUrl).apply {
                path("api", "v1", "search")
                if (cursor != null) parameters.append("cursor", cursor.toString())
            }.buildString()
            
            client.get(url) {
                addAuthHeader()
                contentType(ContentType.Application.Json)
            }
        }
    }

    // --- Private Helper Methods ---

    /**
     * Check if user is authenticated
     */
    private fun isAuthenticated(): Boolean = instanceUrl.isNotEmpty() && authToken != null

    /**
     * Add authentication header to request
     */
    private fun HttpRequestBuilder.addAuthHeader() {
        header("Authorization", "Bearer $authToken")
    }

    /**
     * Execute an authenticated API request with consistent error handling
     */
    private suspend inline fun <reified T> executeAuthenticatedRequest(
        crossinline requestBuilder: suspend () -> HttpResponse
    ): Result<T> {
        if (!isAuthenticated()) {
            return Result.failure(Exception("User is not authenticated."))
        }

        return try {
            val response = requestBuilder()
            
            if (response.status.isSuccess()) {
                Result.success(response.body<T>())
            } else {
                val apiError: ApiError = response.body()
                Result.failure(Exception(apiError.error ?: "Unknown server error"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
} 