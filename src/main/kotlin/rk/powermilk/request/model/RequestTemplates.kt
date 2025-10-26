package rk.powermilk.request.model

import rk.powermilk.request.dsl.BodyBuilder
import rk.powermilk.request.dsl.HeadersBuilder
import rk.powermilk.request.dsl.HttpRequestBuilder
import rk.powermilk.request.dsl.httpRequest
import rk.powermilk.request.enums.HttpMethod

/**
 * Collection of request template builders for common HTTP patterns.
 *
 * This object provides convenient factory methods for creating commonly-used
 * request types, such as authenticated requests.
 *
 * @since 1.0.0
 */
object RequestTemplates {
    /**
     * Creates an authenticated HTTP request with Bearer token.
     *
     * This template automatically adds the Authorization header and Accept header,
     * simplifying the creation of authenticated API requests.
     *
     * Example:
     * ```kotlin
     * val request = RequestTemplates.authenticated(
     *     baseUrl = "https://api.example.com",
     *     token = "secret-token"
     * ) {
     *     path("/users/123")
     *     method(HttpMethod.GET)
     *     headers {
     *         "X-Custom-Header" to "value"
     *     }
     * }
     * ```
     *
     * @param baseUrl The base URL for the API.
     * @param token The authentication token (will be prefixed with "Bearer ").
     * @param block Lambda with receiver for [AuthenticatedRequestBuilder] to configure the request.
     * @return A configured [HttpRequest] with authentication headers.
     */
    fun authenticated(
        baseUrl: String,
        token: String,
        block: AuthenticatedRequestBuilder.() -> Unit
    ): HttpRequest {
        return AuthenticatedRequestBuilder(baseUrl, token).apply(block).build()
    }

    /**
     * Builder for authenticated HTTP requests.
     *
     * Provides a simplified DSL for building requests that require Bearer token authentication.
     * Automatically adds Authorization and Accept headers.
     *
     * @property baseUrl The base URL for all requests.
     * @property token The authentication token.
     * @since 1.0.0
     */
    class AuthenticatedRequestBuilder(
        private val baseUrl: String,
        private val token: String
    ) {
        private var path: String = ""
        private var method: HttpMethod = HttpMethod.GET
        private val additionalHeaders = mutableMapOf<String, String>()
        private var bodyBlock: (BodyBuilder.() -> Unit)? = null

        /**
         * Sets the path to append to the base URL.
         *
         * @param path The path component (e.g., "/users/123").
         */
        fun path(path: String) {
            this.path = path
        }

        /**
         * Sets the HTTP method for the request.
         *
         * @param method The HTTP method to use.
         */
        fun method(method: HttpMethod) {
            this.method = method
        }

        /**
         * Adds additional headers to the request.
         *
         * These headers will be added alongside the default Authorization and Accept headers.
         *
         * @param block Lambda with receiver for [HeadersBuilder] to configure additional headers.
         */
        fun headers(block: HeadersBuilder.() -> Unit) {
            additionalHeaders.putAll(
                HeadersBuilder().apply(block).build()
            )
        }

        /**
         * Configures the request body.
         *
         * @param block Lambda with receiver for [BodyBuilder] to configure the body.
         */
        fun body(block: BodyBuilder.() -> Unit) {
            this.bodyBlock = block
        }

        /**
         * Builds the authenticated HTTP request.
         *
         * @return A configured [HttpRequest] with authentication headers.
         */
        fun build(): HttpRequest = httpRequest {
            url("$baseUrl$path")
            method(this@AuthenticatedRequestBuilder.method)

            headers {
                "Authorization" to "Bearer ${this@AuthenticatedRequestBuilder.token}"
                "Accept" to "application/json"
                this@AuthenticatedRequestBuilder.additionalHeaders.forEach { (k, v) ->
                    k to v
                }
            }

            bodyBlock?.let { body(it) }
        }
    }
}

/**
 * Validates an HTTP request according to common best practices.
 *
 * Performs validation checks such as:
 * - URL must use HTTPS
 * - POST and PUT requests should have a body
 *
 * Example:
 * ```kotlin
 * val request = httpRequest { /* ... */ }
 * request.validate()
 *     .onSuccess { validRequest -> /* use validated request */ }
 *     .onFailure { error -> /* handle validation error */ }
 * ```
 *
 * @receiver The HTTP request to validate.
 * @return A [Result] containing the request if valid, or an exception if invalid.
 * @since 1.0.0
 */
fun HttpRequest.validate(): Result<HttpRequest> {
    return runCatching {
        require(url.startsWith("https://")) {
            "URL must use HTTPS"
        }

        if (method == HttpMethod.POST || method == HttpMethod.PUT) {
            requireNotNull(body) {
                "$method requests should have a body"
            }
        }

        this
    }
}

/**
 * Interface for request interceptors.
 *
 * Interceptors can modify requests before they are executed, enabling
 * cross-cutting concerns like logging, retry logic, or header injection.
 *
 * @since 1.0.0
 */
interface RequestInterceptor {
    /**
     * Intercepts and potentially modifies an HTTP request.
     *
     * @param request The original request to intercept.
     * @return The potentially modified request.
     */
    fun intercept(request: HttpRequest): HttpRequest
}

/**
 * Logging interceptor that prints request details to console.
 *
 * Useful for debugging and monitoring HTTP requests.
 *
 * Example:
 * ```kotlin
 * val interceptor = LoggingInterceptor()
 * val modifiedRequest = interceptor.intercept(originalRequest)
 * ```
 *
 * @since 1.0.0
 */
class LoggingInterceptor : RequestInterceptor {
    /**
     * Logs the request method, URL, and headers to the console.
     *
     * @param request The request to log.
     * @return The unmodified request.
     */
    override fun intercept(request: HttpRequest): HttpRequest {
        println(">>> Request: ${request.method} ${request.url}")
        request.headers.forEach { (k, v) ->
            println("    $k: $v")
        }
        return request
    }
}

/**
 * Retry interceptor that adds retry information to request headers.
 *
 * Adds an "X-Max-Retries" header to indicate the maximum number of retry attempts.
 *
 * Example:
 * ```kotlin
 * val interceptor = RetryInterceptor(maxRetries = 3)
 * val requestWithRetry = interceptor.intercept(originalRequest)
 * ```
 *
 * @property maxRetries The maximum number of retry attempts.
 * @since 1.0.0
 */
class RetryInterceptor(private val maxRetries: Int = 3) : RequestInterceptor {
    /**
     * Adds retry information to the request headers.
     *
     * @param request The request to modify.
     * @return A new request with the X-Max-Retries header added.
     */
    override fun intercept(request: HttpRequest): HttpRequest {
        return request.copy(headers = request.headers + ("X-Max-Retries" to maxRetries.toString()))
    }
}

/**
 * Pipeline for executing multiple interceptors in sequence.
 *
 * Interceptors are executed in the order they are provided, with each
 * interceptor receiving the output of the previous one.
 *
 * Example:
 * ```kotlin
 * val pipeline = RequestPipeline(
 *     listOf(
 *         RetryInterceptor(maxRetries = 3),
 *         LoggingInterceptor()
 *     )
 * )
 * val processedRequest = pipeline.execute(originalRequest)
 * ```
 *
 * @property interceptors List of interceptors to execute in order.
 * @since 1.0.0
 */
class RequestPipeline(
    private val interceptors: List<RequestInterceptor>
) {
    /**
     * Executes all interceptors in sequence.
     *
     * @param request The initial request to process.
     * @return The request after all interceptors have been applied.
     */
    fun execute(request: HttpRequest): HttpRequest {
        return interceptors.fold(request) { req, interceptor ->
            interceptor.intercept(req)
        }
    }
}

/**
 * Builder for constructing URLs with segments and query parameters.
 *
 * Provides a fluent API for building URLs programmatically.
 *
 * Example:
 * ```kotlin
 * val url = UrlBuilder("https://api.example.com")
 *     .segment("users")
 *     .segment("123")
 *     .query("include", "posts")
 *     .query("format", "json")
 *     .build()
 * // Result: "https://api.example.com/users/123?include=posts&format=json"
 * ```
 *
 * @property base The base URL (including protocol and domain).
 * @since 1.0.0
 */
class UrlBuilder(private val base: String) {
    private val segments = mutableListOf<String>()
    private val queryParams = mutableMapOf<String, String>()

    /**
     * Adds a path segment to the URL.
     *
     * Leading and trailing slashes are automatically trimmed.
     *
     * @param segment The path segment to add.
     * @return This builder for chaining.
     */
    fun segment(segment: String): UrlBuilder {
        segments.add(segment.trim('/'))
        return this
    }

    /**
     * Adds a query parameter to the URL.
     *
     * @param key The query parameter key.
     * @param value The query parameter value.
     * @return This builder for chaining.
     */
    fun query(key: String, value: String): UrlBuilder {
        queryParams[key] = value
        return this
    }

    /**
     * Builds and returns the complete URL.
     *
     * @return The constructed URL string with all segments and query parameters.
     */
    fun build(): String {
        val path = segments.joinToString("/")
        val query = if (queryParams.isNotEmpty()) {
            "?" + queryParams.entries.joinToString("&") { (k, v) ->
                "$k=$v"
            }
        } else ""

        return "$base/$path$query"
    }
}

/**
 * Creates an HTTP request with a programmatically built URL.
 *
 * Combines [UrlBuilder] with [httpRequest] for convenient URL construction.
 *
 * Example:
 * ```kotlin
 * val request = httpRequestWithUrl(
 *     baseUrl = "https://api.example.com",
 *     urlBlock = {
 *         segment("users")
 *         segment("123")
 *         query("include", "posts")
 *     }
 * ) {
 *     method(HttpMethod.GET)
 * }
 * ```
 *
 * @param baseUrl The base URL.
 * @param urlBlock Lambda with receiver for [UrlBuilder] to construct the URL.
 * @param requestBlock Lambda with receiver for [HttpRequestBuilder] to configure the request.
 * @return A configured [HttpRequest] with the built URL.
 * @since 1.0.0
 */
fun httpRequestWithUrl(
    baseUrl: String,
    urlBlock: UrlBuilder.() -> Unit,
    requestBlock: HttpRequestBuilder.() -> Unit
): HttpRequest {
    val url = UrlBuilder(baseUrl).apply(urlBlock).build()
    return httpRequest {
        url(url)
        requestBlock()
    }
}

/**
 * Builder for creating multiple HTTP requests at once.
 *
 * Useful for scenarios where multiple related requests need to be created together.
 *
 * Example:
 * ```kotlin
 * val requests = batchRequests {
 *     request {
 *         url("https://api.example.com/users/1")
 *         method(HttpMethod.GET)
 *     }
 *     request {
 *         url("https://api.example.com/users/2")
 *         method(HttpMethod.GET)
 *     }
 * }
 * ```
 *
 * @since 1.0.0
 */
class BatchRequestBuilder {
    private val requests = mutableListOf<HttpRequest>()

    /**
     * Adds a request to the batch.
     *
     * @param block Lambda with receiver for [HttpRequestBuilder] to configure the request.
     */
    fun request(block: HttpRequestBuilder.() -> Unit) {
        requests.add(httpRequest(block))
    }

    /**
     * Builds and returns the list of requests.
     *
     * @return An immutable list of all configured requests.
     */
    fun build(): List<HttpRequest> = requests.toList()
}

/**
 * Creates a batch of HTTP requests.
 *
 * Example:
 * ```kotlin
 * val requests = batchRequests {
 *     request { /* first request config */ }
 *     request { /* second request config */ }
 *     request { /* third request config */ }
 * }
 * ```
 *
 * @param block Lambda with receiver for [BatchRequestBuilder] to configure all requests.
 * @return A list of configured [HttpRequest] instances.
 * @since 1.0.0
 */
fun batchRequests(block: BatchRequestBuilder.() -> Unit): List<HttpRequest> {
    return BatchRequestBuilder().apply(block).build()
}

/**
 * Conditionally adds headers to an HTTP request.
 *
 * Headers are only added if the condition is true.
 *
 * Example:
 * ```kotlin
 * httpRequest {
 *     url("https://api.example.com/users")
 *
 *     conditionalHeaders(userIsAuthenticated) {
 *         "Authorization" to "Bearer $token"
 *     }
 * }
 * ```
 *
 * @receiver The HTTP request builder.
 * @param condition If true, headers are added; if false, headers are skipped.
 * @param block Lambda with receiver for [HeadersBuilder] to configure headers.
 * @since 1.0.0
 */
fun HttpRequestBuilder.conditionalHeaders(
    condition: Boolean,
    block: HeadersBuilder.() -> Unit
) {
    if (condition) {
        headers(block)
    }
}

/**
 * Conditionally adds a body to an HTTP request.
 *
 * Body is only added if the condition is true.
 *
 * Example:
 * ```kotlin
 * httpRequest {
 *     url("https://api.example.com/users")
 *     method(HttpMethod.POST)
 *
 *     conditionalBody(hasData) {
 *         json { "data" to someValue }
 *     }
 * }
 * ```
 *
 * @receiver The HTTP request builder.
 * @param condition If true, body is added; if false, body is skipped.
 * @param block Lambda with receiver for [BodyBuilder] to configure the body.
 * @since 1.0.0
 */
fun HttpRequestBuilder.conditionalBody(
    condition: Boolean,
    block: BodyBuilder.() -> Unit
) {
    if (condition) {
        body(block)
    }
}

/**
 * Creates a modified copy of an HTTP request.
 *
 * Allows modifying an existing request while preserving its original values.
 * The modification block can override any part of the request.
 *
 * Example:
 * ```kotlin
 * val original = httpRequest {
 *     url("https://api.example.com/users")
 *     method(HttpMethod.GET)
 * }
 *
 * val modified = original.modify {
 *     method(HttpMethod.POST)
 *     headers {
 *         "Authorization" to "Bearer token"
 *     }
 *     body {
 *         json { "name" to "Jane" }
 *     }
 * }
 * ```
 *
 * @receiver The original HTTP request.
 * @param block Lambda with receiver for [HttpRequestBuilder] to specify modifications.
 * @return A new [HttpRequest] with the modifications applied.
 * @since 1.0.0
 */
fun HttpRequest.modify(block: HttpRequestBuilder.() -> Unit): HttpRequest {
    return httpRequest {
        url(this@modify.url)
        method(this@modify.method)

        headers {
            this@modify.headers.forEach { (k, v) ->
                k to v
            }
        }

        this@modify.body?.let { originalBody ->
            body {
                when (originalBody) {
                    is RequestBody.JsonBody -> {
                        json {
                            originalBody.data.forEach { (k, v) ->
                                k to v
                            }
                        }
                    }

                    is RequestBody.TextBody -> {
                        text(originalBody.text)
                    }

                    is RequestBody.RawBody -> {
                        raw(originalBody.bytes)
                    }
                }
            }
        }

        timeout {
            connect = this@modify.timeout.connect
            read = this@modify.timeout.read
            write = this@modify.timeout.write
        }

        // Apply modifications
        block()
    }
}

/**
 * Builder for creating requests with default configuration.
 *
 * Applies default values from [RequestConfig] to all requests built with this builder.
 *
 * Example:
 * ```kotlin
 * val config = RequestConfig(
 *     defaultTimeout = 15000,
 *     defaultHeaders = mapOf("User-Agent" to "MyApp/1.0")
 * )
 *
 * val builder = ConfigurableRequestBuilder(config)
 * val request = builder.build {
 *     url("https://api.example.com/users")
 *     method(HttpMethod.GET)
 * }
 * ```
 *
 * @property config The configuration providing default values.
 * @since 1.0.0
 */
class ConfigurableRequestBuilder(private val config: RequestConfig) {
    /**
     * Builds an HTTP request with default configuration applied.
     *
     * Default values from the config are applied first, then can be overridden
     * by values specified in the block.
     *
     * @param block Lambda with receiver for [HttpRequestBuilder] to configure the request.
     * @return A configured [HttpRequest] with defaults applied.
     */
    fun build(block: HttpRequestBuilder.() -> Unit): HttpRequest {
        return httpRequest {
            // Apply defaults
            timeout {
                connect = config.defaultTimeout
                read = config.defaultTimeout
            }

            if (config.defaultHeaders.isNotEmpty()) {
                headers {
                    config.defaultHeaders.forEach { (k, v) ->
                        k to v
                    }
                }
            }

            // Apply user configuration
            block()
        }
    }
}
