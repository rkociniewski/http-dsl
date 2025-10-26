package rk.powermilk.request.dsl

import rk.powermilk.request.constant.ErrorMessage
import rk.powermilk.request.enums.HttpMethod
import rk.powermilk.request.model.HttpRequest
import rk.powermilk.request.model.RequestBody
import rk.powermilk.request.model.Timeout

/**
 * DSL marker annotation for HTTP request builders.
 *
 * This annotation prevents implicit receivers from outer scopes being used
 * in inner scopes, ensuring type-safe DSL construction.
 *
 * @since 1.0.0
 */
@DslMarker
annotation class HttpDsl

/**
 * Builder class for constructing HTTP requests using a type-safe DSL.
 *
 * This is the main entry point for building HTTP requests. It provides methods
 * for configuring all aspects of an HTTP request including URL, method, headers,
 * body, and timeouts.
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
 *         json {
 *             "name" to "John"
 *         }
 *     }
 * }
 * ```
 *
 * @since 1.0.0
 */
@HttpDsl
class HttpRequestBuilder {
    private var url: String? = null
    private var method: HttpMethod = HttpMethod.GET
    private val headers = mutableMapOf<String, String>()
    private var body: RequestBody? = null
    private var timeout: Timeout = Timeout()

    /**
     * Sets the URL for the HTTP request.
     *
     * @param url The target URL. Must be non-empty and non-blank.
     * @throws IllegalArgumentException if the URL is empty or blank.
     */
    fun url(url: String) {
        require(url.isNotEmpty()) { ErrorMessage.EMPTY_URL }
        require(url.isNotBlank()) { ErrorMessage.BLANK_URL }
        this.url = url
    }

    /**
     * Sets the HTTP method for the request.
     *
     * @param method The HTTP method to use (GET, POST, PUT, DELETE, PATCH).
     */
    fun method(method: HttpMethod) {
        this.method = method
    }

    /**
     * Configures HTTP headers using a DSL block.
     *
     * Headers can be set multiple times; subsequent calls add to existing headers.
     * If a header with the same name is set multiple times, the last value wins.
     *
     * Example:
     * ```kotlin
     * headers {
     *     "Content-Type" to "application/json"
     *     "Authorization" to "Bearer token"
     * }
     * ```
     *
     * @param block Lambda with receiver for [HeadersBuilder] to configure headers.
     */
    fun headers(block: HeadersBuilder.() -> Unit) {
        headers.putAll(HeadersBuilder().apply(block).build())
    }

    /**
     * Configures the request body using a DSL block.
     *
     * Only one body type can be set per request. The body can be JSON, text, or raw bytes.
     *
     * Example:
     * ```kotlin
     * body {
     *     json {
     *         "name" to "John"
     *         "age" to 30
     *     }
     * }
     * ```
     *
     * @param block Lambda with receiver for [BodyBuilder] to configure the body.
     */
    fun body(block: BodyBuilder.() -> Unit) {
        body = BodyBuilder().apply(block).build()
    }

    /**
     * Configures timeout settings using a DSL block.
     *
     * Example:
     * ```kotlin
     * timeout {
     *     connect = 5000
     *     read = 10000
     *     write = 7000
     * }
     * ```
     *
     * @param block Lambda with receiver for [TimeoutBuilder] to configure timeouts.
     */
    fun timeout(block: TimeoutBuilder.() -> Unit) {
        timeout = TimeoutBuilder().apply(block).build()
    }

    /**
     * Builds and returns the configured [HttpRequest].
     *
     * @return The constructed [HttpRequest] instance.
     * @throws IllegalStateException if the URL has not been set.
     */
    internal fun build(): HttpRequest {
        val finalUrl = checkNotNull(url) { ErrorMessage.REQUIRED_URL }
        return HttpRequest(finalUrl, method, headers.toMap(), body, timeout)
    }
}

/**
 * Builder class for constructing HTTP headers.
 *
 * Provides a DSL for defining HTTP headers using infix notation or invoke syntax.
 *
 * Example:
 * ```kotlin
 * headers {
 *     "Content-Type" to "application/json"
 *     "Authorization"("Bearer token")
 * }
 * ```
 *
 * @since 1.0.0
 */
@HttpDsl
class HeadersBuilder {
    private val headers = mutableMapOf<String, String>()

    /**
     * Adds a header using infix notation.
     *
     * @receiver The header name. Must be non-empty and non-blank.
     * @param value The header value. Must be non-empty and non-blank.
     * @throws IllegalArgumentException if name or value is empty or blank.
     */
    infix fun String.to(value: String) {
        require(this.isNotEmpty()) { ErrorMessage.EMPTY_HEADER_NAME }
        require(this.isNotBlank()) { ErrorMessage.BLANK_HEADER_NAME }
        require(value.isNotEmpty()) { ErrorMessage.EMPTY_HEADER_VALUE }
        require(value.isNotBlank()) { ErrorMessage.BLANK_HEADER_VALUE }
        headers[this] = value
    }

    /**
     * Adds a header using invoke operator syntax.
     *
     * Alternative syntax to the infix `to` operator.
     *
     * @receiver The header name.
     * @param value The header value.
     */
    operator fun String.invoke(value: String) {
        this to value
    }

    /**
     * Builds and returns the headers map.
     *
     * @return An immutable map of headers.
     */
    internal fun build(): Map<String, String> = headers.toMap()
}

/**
 * Builder class for constructing request bodies.
 *
 * Supports three types of bodies: JSON, text, and raw bytes.
 * Only one body type can be set per request.
 *
 * @since 1.0.0
 */
@HttpDsl
class BodyBuilder {
    private var body: RequestBody? = null

    /**
     * Creates a JSON body using a DSL block.
     *
     * Example:
     * ```kotlin
     * body {
     *     json {
     *         "name" to "John"
     *         "age" to 30
     *         nested("address") {
     *             "city" to "New York"
     *         }
     *     }
     * }
     * ```
     *
     * @param block Lambda with receiver for [JsonBuilder] to construct JSON structure.
     * @throws IllegalStateException if a body has already been set.
     */
    fun json(block: JsonBuilder.() -> Unit) {
        check(body == null) { ErrorMessage.BODY_SET_ONCE }
        body = RequestBody.JsonBody(JsonBuilder().apply(block).build())
    }

    /**
     * Creates a plain text body.
     *
     * Example:
     * ```kotlin
     * body {
     *     text("Plain text content")
     * }
     * ```
     *
     * @param content The text content. Must be non-empty and non-blank.
     * @throws IllegalArgumentException if content is empty or blank.
     * @throws IllegalStateException if a body has already been set.
     */
    fun text(content: String) {
        check(body == null) { ErrorMessage.BODY_SET_ONCE }
        require(content.isNotEmpty()) { ErrorMessage.EMPTY_TEXT_CONTENT }
        require(content.isNotBlank()) { ErrorMessage.BLANK_TEXT_CONTENT }
        body = RequestBody.TextBody(content)
    }

    /**
     * Creates a raw binary body.
     *
     * Example:
     * ```kotlin
     * body {
     *     raw(fileBytes)
     * }
     * ```
     *
     * @param content The binary content as a byte array. Must not be empty.
     * @throws IllegalArgumentException if content is empty.
     * @throws IllegalStateException if a body has already been set.
     */
    fun raw(content: ByteArray) {
        check(body == null) { ErrorMessage.BODY_SET_ONCE }
        require(content.isNotEmpty()) { ErrorMessage.EMPTY_RAW_CONTENT }
        body = RequestBody.RawBody(content)
    }

    /**
     * Builds and returns the configured request body.
     *
     * @return The [RequestBody] instance, or null if no body was configured.
     */
    internal fun build() = body
}

/**
 * Builder class for constructing JSON structures.
 *
 * Provides a DSL for building JSON objects with support for nested structures.
 *
 * Example:
 * ```kotlin
 * json {
 *     "name" to "John"
 *     "age" to 30
 *     "emails" to listOf("john@example.com")
 *     nested("address") {
 *         "street" to "Main St"
 *         "city" to "New York"
 *     }
 * }
 * ```
 *
 * @since 1.0.0
 */
@HttpDsl
class JsonBuilder {
    private val data = mutableMapOf<String, Any?>()

    /**
     * Adds a key-value pair to the JSON object using infix notation.
     *
     * @receiver The JSON key. Must be non-empty and non-blank.
     * @param value The JSON value. Can be any type including null, primitives,
     *              collections, or nested maps.
     * @throws IllegalArgumentException if the key is empty or blank.
     */
    infix fun String.to(value: Any?) {
        require(this.isNotEmpty()) { ErrorMessage.EMPTY_JSON_KEY }
        require(this.isNotBlank()) { ErrorMessage.BLANK_JSON_KEY }
        data[this] = value
    }

    /**
     * Adds a key-value pair using invoke operator syntax.
     *
     * Alternative syntax to the infix `to` operator.
     *
     * @receiver The JSON key.
     * @param value The JSON value.
     */
    operator fun String.invoke(value: Any?) {
        this to value
    }

    /**
     * Creates a nested JSON object.
     *
     * Example:
     * ```kotlin
     * nested("address") {
     *     "street" to "Main St"
     *     "city" to "New York"
     * }
     * ```
     *
     * @param key The key for the nested object. Must be non-empty and non-blank.
     * @param block Lambda with receiver for [JsonBuilder] to construct the nested structure.
     * @throws IllegalArgumentException if the key is empty or blank.
     */
    fun nested(key: String, block: JsonBuilder.() -> Unit) {
        require(key.isNotEmpty()) { ErrorMessage.EMPTY_JSON_KEY }
        require(key.isNotBlank()) { ErrorMessage.BLANK_JSON_KEY }
        data[key] = JsonBuilder().apply(block).build()
    }

    /**
     * Builds and returns the JSON structure as a map.
     *
     * @return An immutable map representing the JSON structure.
     */
    internal fun build() = data.toMap()
}

/**
 * Builder class for configuring request timeouts.
 *
 * All timeout values are in milliseconds and must be positive if set.
 *
 * Example:
 * ```kotlin
 * timeout {
 *     connect = 5000
 *     read = 10000
 *     write = 7000
 * }
 * ```
 *
 * @since 1.0.0
 */
@HttpDsl
class TimeoutBuilder {
    /**
     * Connection timeout in milliseconds.
     *
     * Time allowed to establish a connection to the server.
     * Must be positive if set.
     *
     * @throws IllegalArgumentException if set to a non-positive value.
     */
    var connect: Long? = null
        set(value) {
            value?.let { require(it > 0) { ErrorMessage.TIMEOUT_NEGATIVE_CONNECT } }
            field = value
        }

    /**
     * Read timeout in milliseconds.
     *
     * Time allowed to read data from the server after connection is established.
     * Must be positive if set.
     *
     * @throws IllegalArgumentException if set to a non-positive value.
     */
    var read: Long? = null
        set(value) {
            value?.let { require(it > 0) { ErrorMessage.TIMEOUT_NEGATIVE_READ } }
            field = value
        }

    /**
     * Write timeout in milliseconds.
     *
     * Time allowed to write data to the server.
     * Must be positive if set.
     *
     * @throws IllegalArgumentException if set to a non-positive value.
     */
    var write: Long? = null
        set(value) {
            value?.let { require(it > 0) { ErrorMessage.TIMEOUT_NEGATIVE_WRITE } }
            field = value
        }

    /**
     * Builds and returns the configured timeout.
     *
     * @return A [Timeout] instance with the configured values.
     */
    internal fun build() = Timeout(connect, read, write)
}

/**
 * Entry point function for building HTTP requests using the DSL.
 *
 * This is the main function users should call to create HTTP requests.
 * It provides a type-safe DSL for configuring all aspects of the request.
 *
 * Example:
 * ```kotlin
 * val request = httpRequest {
 *     url("https://api.example.com/users")
 *     method(HttpMethod.POST)
 *
 *     headers {
 *         "Content-Type" to "application/json"
 *         "Authorization" to "Bearer token123"
 *     }
 *
 *     body {
 *         json {
 *             "name" to "John"
 *             "age" to 30
 *         }
 *     }
 *
 *     timeout {
 *         connect = 5000
 *         read = 10000
 *     }
 * }
 * ```
 *
 * @param block Lambda with receiver for [HttpRequestBuilder] to configure the request.
 * @return A configured [HttpRequest] instance ready to be executed.
 * @throws IllegalStateException if required fields (like URL) are not set.
 * @throws IllegalArgumentException if any validation constraints are violated.
 * @since 1.0.0
 */
fun httpRequest(block: HttpRequestBuilder.() -> Unit): HttpRequest {
    return HttpRequestBuilder().apply(block).build()
}
