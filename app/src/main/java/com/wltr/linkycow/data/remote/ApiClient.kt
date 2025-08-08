package com.wltr.linkycow.data.remote

import com.wltr.linkycow.data.remote.dto.ApiError
import com.wltr.linkycow.data.remote.dto.ArchiveLinkResponse
import com.wltr.linkycow.data.remote.dto.AuthResponse
import com.wltr.linkycow.data.remote.dto.CreateLinkRequest
import com.wltr.linkycow.data.remote.dto.DashboardResponse
import com.wltr.linkycow.data.remote.dto.DashboardData
import com.wltr.linkycow.data.remote.dto.DeleteLinkResponse
import com.wltr.linkycow.data.remote.dto.LinkDetailResponse
import com.wltr.linkycow.data.remote.dto.LinkDetailData
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
     * Temporarily using search endpoint to avoid archive-related server errors
     */
    suspend fun getDashboard(): Result<DashboardResponse> {
        return try {
            // Use the search endpoint as fallback, which doesn't load archives
            val searchResult = getLinksPaged()
            searchResult.map { searchResponse ->
                DashboardResponse(
                    data = DashboardData(
                        links = searchResponse.data.links
                    )
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
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
     * With graceful handling of archive file errors
     */
    suspend fun archiveLink(id: Int): Result<ArchiveLinkResponse> {
        return try {
            val result = executeAuthenticatedRequest<ArchiveLinkResponse> {
                val url = URLBuilder(instanceUrl).apply {
                    path("api", "v1", "links", id.toString(), "archive")
                }.buildString()

                client.put(url) {
                    addAuthHeader()
                }
            }
            
            // If the request fails due to archive file errors, treat as success
            result.recoverCatching { error ->
                val errorMsg = error.message ?: ""
                if ((errorMsg.contains("ENOENT") && errorMsg.contains("unlink") && errorMsg.contains("archives/")) ||
                    (errorMsg.contains("no such file or directory") && errorMsg.contains("archives/")) ||
                    (errorMsg.contains("syscall: 'unlink'") && errorMsg.contains("/data/data/archives/"))) {
                    // Specific archive file deletion error - treat as successful archive
                    ArchiveLinkResponse(response = "Link archived successfully")
                } else {
                    throw error
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
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
     * With graceful handling of archive file errors
     */
    suspend fun deleteLink(id: Int): Result<DeleteLinkResponse> {
        return try {
            val result = executeAuthenticatedRequest<DeleteLinkResponse> {
                val url = URLBuilder(instanceUrl).apply {
                    path("api", "v1", "links", id.toString())
                }.buildString()

                client.delete(url) {
                    addAuthHeader()
                }
            }
            
            // If the request fails due to archive file errors, treat as success
            result.recoverCatching { error ->
                val errorMsg = error.message ?: ""
                if ((errorMsg.contains("ENOENT") && errorMsg.contains("unlink") && errorMsg.contains("archives/")) ||
                    (errorMsg.contains("no such file or directory") && errorMsg.contains("archives/")) ||
                    (errorMsg.contains("syscall: 'unlink'") && errorMsg.contains("/data/data/archives/"))) {
                    // Specific archive file deletion error - link was likely deleted from DB
                    DeleteLinkResponse(response = LinkDetailData(
                        id = id,
                        name = "Deleted",
                        type = "link", 
                        description = null,
                        createdById = 0,
                        collectionId = null,
                        icon = null,
                        iconWeight = null,
                        color = null,
                        url = "",
                        textContent = null,
                        preview = null,
                        image = null,
                        pdf = null,
                        readable = null,
                        monolith = null,
                        lastPreserved = null,
                        importDate = null,
                        createdAt = "",
                        updatedAt = "",
                        tags = emptyList(),
                        collection = null
                    ))
                } else {
                    throw error
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
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
                try {
                    Result.success(response.body<T>())
                } catch (e: Exception) {
                    // If deserialization fails, provide detailed error
                    val responseText = try {
                        response.bodyAsText()
                    } catch (e2: Exception) {
                        "Could not read response body"
                    }
                    Result.failure(Exception("Failed to parse successful response: ${e.message}. Response: $responseText"))
                }
            } else {
                // Try to parse as ApiError JSON, fallback to plain text
                val errorMessage = try {
                    val apiError: ApiError = response.body()
                    apiError.error ?: "Unknown server error"
                } catch (e: Exception) {
                    // If JSON parsing fails, get the raw response text
                    try {
                        response.bodyAsText()
                    } catch (e2: Exception) {
                        "Server error ${response.status.value}: ${response.status.description}"
                    }
                }
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Network error: ${e.message}"))
        }
    }
} 