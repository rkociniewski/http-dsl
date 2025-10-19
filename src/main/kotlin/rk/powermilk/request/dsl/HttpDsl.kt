package rk.powermilk.request.dsl

import rk.powermilk.request.enums.HttpMethod
import rk.powermilk.request.model.HttpRequest
import rk.powermilk.request.model.RequestBody
import rk.powermilk.request.model.Timeout

@DslMarker
annotation class HttpDsl

@HttpDsl
class HttpRequestBuilder {
    private var url: String = ""
    private var method: HttpMethod = HttpMethod.GET
    private val headers = mutableMapOf<String, String>()
    private var body: RequestBody? = null
    private var timeout: Timeout = Timeout(5000, 5000)

    fun url(url: String) {
        this.url = url
    }

    fun method(method: HttpMethod) {
        this.method = method
    }

    fun headers(block: HeadersBuilder.() -> Unit) {
        headers.putAll(HeadersBuilder().apply(block).build())
    }

    fun body(block: BodyBuilder.() -> Unit) {
        body = BodyBuilder().apply(block).build()
    }

    fun timeout(block: TimeoutBuilder.() -> Unit) {
        timeout = TimeoutBuilder().apply(block).build()
    }

    internal fun build(): HttpRequest {
        require(url.isNotEmpty()) { "URL is required" }
        return HttpRequest(url, method, headers, body, timeout)
    }
}

@HttpDsl
class HeadersBuilder {
    private val headers = mutableMapOf<String, String>()

    infix fun String.to(value: String) {
        headers[this] = value
    }

    internal fun build() = headers.toMap()
}

@HttpDsl
class BodyBuilder {
    private var body: RequestBody? = null

    fun json(block: JsonBuilder.() -> Unit) {
        body = RequestBody.JsonBody(JsonBuilder().apply(block).build())
    }

    fun text(content: String) {
        body = RequestBody.TextBody(content)
    }

    internal fun build() = body
}

@HttpDsl
class JsonBuilder {
    private val data = mutableMapOf<String, Any?>()

    infix fun String.to(value: Any?) {
        data[this] = value
    }

    internal fun build() = data.toMap()
}

@HttpDsl
class TimeoutBuilder {
    var connect: Long = 5000
    var read: Long = 5000

    internal fun build() = Timeout(connect, read)
}

fun httpRequest(block: HttpRequestBuilder.() -> Unit): HttpRequest {
    return HttpRequestBuilder().apply(block).build()
}
