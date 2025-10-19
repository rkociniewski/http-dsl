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
        return request.copy(headers = request.headers + ("X-Max-Retries" to maxRetries.toString()))
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
