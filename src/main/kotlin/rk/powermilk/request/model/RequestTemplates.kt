package rk.powermilk.request.model

import rk.powermilk.request.dsl.BodyBuilder
import rk.powermilk.request.dsl.HeadersBuilder
import rk.powermilk.request.dsl.HttpRequestBuilder
import rk.powermilk.request.dsl.httpRequest
import rk.powermilk.request.enums.HttpMethod

object RequestTemplates {
    fun authenticated(
        baseUrl: String,
        token: String,
        block: AuthenticatedRequestBuilder.() -> Unit
    ): HttpRequest {
        return AuthenticatedRequestBuilder(baseUrl, token).apply(block).build()
    }

    class AuthenticatedRequestBuilder(
        private val baseUrl: String,
        private val token: String
    ) {
        private var path: String = ""
        private var method: HttpMethod = HttpMethod.GET
        private val additionalHeaders = mutableMapOf<String, String>()
        private var bodyBlock: (BodyBuilder.() -> Unit)? = null

        fun path(path: String) {
            this.path = path
        }

        fun method(method: HttpMethod) {
            this.method = method
        }

        fun headers(block: HeadersBuilder.() -> Unit) {
            additionalHeaders.putAll(
                HeadersBuilder().apply(block).build()
            )
        }

        fun body(block: BodyBuilder.() -> Unit) {
            this.bodyBlock = block
        }

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

fun exampleAuthenticated() {
    RequestTemplates.authenticated(
        baseUrl = "https://api.example.com",
        token = "secret-token"
    ) {
        path("/users/123")
        method(HttpMethod.PATCH)

        body {
            json {
                "email" to "newemail@example.com"
            }
        }
    }
}

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

fun exampleValidation() {
    val result = httpRequest {
        url("https://api.example.com/users")
        method(HttpMethod.POST)

        body {
            json {
                "name" to "John"
            }
        }
    }.validate()

    result.fold(
        onSuccess = { request -> println("Valid request: $request") },
        onFailure = { error -> println("Invalid request: ${error.message}") }
    )
}

interface RequestInterceptor {
    fun intercept(request: HttpRequest): HttpRequest
}

class LoggingInterceptor : RequestInterceptor {
    override fun intercept(request: HttpRequest): HttpRequest {
        println(">>> Request: ${request.method} ${request.url}")
        request.headers.forEach { (k, v) ->
            println("    $k: $v")
        }
        return request
    }
}

class RetryInterceptor(private val maxRetries: Int = 3) : RequestInterceptor {
    override fun intercept(request: HttpRequest): HttpRequest {
        // W prawdziwej implementacji dodalibyśmy metadata o retry
        return request.copy(
            headers = request.headers + ("X-Max-Retries" to maxRetries.toString())
        )
    }
}

class RequestPipeline(
    private val interceptors: List<RequestInterceptor>
) {
    fun execute(request: HttpRequest): HttpRequest {
        return interceptors.fold(request) { req, interceptor ->
            interceptor.intercept(req)
        }
    }
}


fun exampleInterceptors() {
    val pipeline = RequestPipeline(
        listOf(
            LoggingInterceptor(),
            RetryInterceptor(maxRetries = 3)
        )
    )

    val request = httpRequest {
        url("https://api.example.com/users")
        method(HttpMethod.GET)
    }

    pipeline.execute(request)
}

class UrlBuilder(private val base: String) {
    private val segments = mutableListOf<String>()
    private val queryParams = mutableMapOf<String, String>()

    fun segment(segment: String): UrlBuilder {
        segments.add(segment.trim('/'))
        return this
    }

    fun query(key: String, value: String): UrlBuilder {
        queryParams[key] = value
        return this
    }

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

fun exampleTypeSafeUrl() {
    httpRequestWithUrl(
        baseUrl = "https://api.example.com",
        urlBlock = {
            segment("users")
            segment("123")
            segment("posts")
            query("limit", "10")
            query("offset", "0")
        }
    ) {
        method(HttpMethod.GET)
        headers {
            "Accept" to "application/json"
        }
    }
}

class BatchRequestBuilder {
    private val requests = mutableListOf<HttpRequest>()

    fun request(block: HttpRequestBuilder.() -> Unit) {
        requests.add(httpRequest(block))
    }

    fun build(): List<HttpRequest> = requests.toList()
}

fun batchRequests(block: BatchRequestBuilder.() -> Unit): List<HttpRequest> {
    return BatchRequestBuilder().apply(block).build()
}

fun exampleBatch() {
    val requests = batchRequests {
        request {
            url("https://api.example.com/users/1")
            method(HttpMethod.GET)
        }

        request {
            url("https://api.example.com/users/2")
            method(HttpMethod.GET)
        }

        request {
            url("https://api.example.com/posts")
            method(HttpMethod.GET)
        }
    }

    println("Created ${requests.size} requests")
}

fun HttpRequestBuilder.conditionalHeaders(
    condition: Boolean,
    block: HeadersBuilder.() -> Unit
) {
    if (condition) {
        headers(block)
    }
}

fun HttpRequestBuilder.conditionalBody(
    condition: Boolean,
    block: BodyBuilder.() -> Unit
) {
    if (condition) {
        body(block)
    }
}

// Użycie:
fun exampleConditional(includeAuth: Boolean, includeBody: Boolean) {
    httpRequest {
        url("https://api.example.com/users")
        method(HttpMethod.POST)

        conditionalHeaders(includeAuth) {
            "Authorization" to "Bearer token"
        }

        conditionalBody(includeBody) {
            json {
                "name" to "John"
            }
        }
    }
}

// ============================================
// 7. Request Cloning & Modification
// ============================================

/**
 * Extension do modyfikacji istniejących requestów
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

// Użycie:
fun exampleModification() {
    val originalRequest = httpRequest {
        url("https://api.example.com/users")
        method(HttpMethod.GET)
    }

    originalRequest.modify {
        method(HttpMethod.POST)
        body {
            json {
                "name" to "Jane"
            }
        }
    }
}

// ============================================
// 8. Smart Defaults with Config
// ============================================

/**
 * Configuration object dla default values
 */
data class RequestConfig(
    val defaultTimeout: Long = 5000,
    val defaultHeaders: Map<String, String> = emptyMap(),
    val baseUrl: String = ""
)

class ConfigurableRequestBuilder(private val config: RequestConfig) {
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

// Użycie:
fun exampleConfiguration() {
    val config = RequestConfig(
        defaultTimeout = 10000,
        defaultHeaders = mapOf(
            "User-Agent" to "MyApp/1.0",
            "Accept" to "application/json"
        ),
        baseUrl = "https://api.example.com"
    )

    val builder = ConfigurableRequestBuilder(config)

    builder.build {
        url("${builder}") // Would need access to config.baseUrl
        method(HttpMethod.GET)
    }
}
