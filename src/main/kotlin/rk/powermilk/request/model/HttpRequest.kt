package rk.powermilk.request.model

import rk.powermilk.request.enums.HttpMethod

/**
 * Represents a complete HTTP request with all its components.
 *
 * This immutable data class encapsulates all the information needed to make an HTTP request,
 * including URL, method, headers, optional body, and timeout configuration.
 *
 * Instances of this class are typically created using the [rk.powermilk.request.dsl.httpRequest] DSL function.
 *
 * Example:
 * ```kotlin
 * val request = httpRequest {
 *     url("https://api.example.com/users")
 *     method(HttpMethod.POST)
 *     headers {
 *         "Content-Type" to "application/json"
 *     }
 *     body {
 *         json { "name" to "John" }
 *     }
 * }
 * ```
 *
 * @property url The target URL for the HTTP request. Must be non-empty and non-blank.
 * @property method The HTTP method to use for the request.
 * @property headers A map of HTTP headers where keys are header names and values are header values.
 * @property body Optional request body. Can be JSON, text, or raw bytes.
 * @property timeout Timeout configuration for the request.
 * @since 1.0.0
 */
data class HttpRequest(
    val url: String,
    val method: HttpMethod,
    val headers: Map<String, String>,
    val body: RequestBody?,
    val timeout: Timeout,
)
