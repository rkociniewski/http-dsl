package rk.powermilk.request.dsl

import rk.powermilk.request.enums.HttpMethod
import rk.powermilk.request.model.HttpRequest
import rk.powermilk.request.model.RequestBody
import rk.powermilk.request.model.Timeout

@DslMarker
annotation class HttpDsl

@HttpDsl
class HttpRequestBuilder {
    private var url: String? = null
    private var method: HttpMethod = HttpMethod.GET
    private val headers = mutableMapOf<String, String>()
    private var body: RequestBody? = null
    private var timeout: Timeout = Timeout(5000, 5000)

    fun url(url: String) {
        require(url.isNotBlank()) { "URL cannot be blank" }
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
        val finalUrl = url ?: throw IllegalStateException("URL is required")
        return HttpRequest(finalUrl, method, headers.toMap(), body, timeout)
    }
}

@HttpDsl
class HeadersBuilder {
    private val headers = mutableMapOf<String, String>()

    infix fun String.to(value: String) {
        require(this.isNotBlank()) { "Header name cannot be blank" }
        require(value.isNotBlank()) { "Header value cannot be blank" }
        headers[this] = value
    }

    operator fun String.invoke(value: String) {
        this to value
    }

    internal fun build(): Map<String, String> = headers.toMap()
}

@HttpDsl
class BodyBuilder {
    private var body: RequestBody? = null

    fun json(block: JsonBuilder.() -> Unit) {
        require(body == null) { "Body can only be set once" }
        body = RequestBody.JsonBody(JsonBuilder().apply(block).build())
    }

    fun text(content: String) {
        require(body == null) { "Body can only be set once" }
        require(content.isNotBlank()) { "Text body cannot be blank" }
        body = RequestBody.TextBody(content)
    }

    fun raw(content: ByteArray) {
        require(body == null) { "Body can only be set once" }
        require(content.isNotEmpty()) { "Raw body cannot be empty" }
        body = RequestBody.RawBody(content)
    }

    internal fun build() = body
}

@HttpDsl
class JsonBuilder {
    private val data = mutableMapOf<String, Any?>()

    infix fun String.to(value: Any?) {
        require(this.isNotBlank()) { "JSON key cannot be blank" }
        data[this] = value
    }

    operator fun String.invoke(value: Any?) {
        this to value
    }

    fun nested(key: String, block: JsonBuilder.() -> Unit) {
        require(key.isNotBlank()) { "Nested JSON key cannot be blank" }
        data[key] = JsonBuilder().apply(block).build()
    }

    internal fun build() = data.toMap()
}

@HttpDsl
class TimeoutBuilder {
    var connect: Long = 5000
        set(value) {
            require(value > 0) { "Connect timeout must be positive" }
            field = value
        }

    var read: Long = 5000
        set(value) {
            require(value > 0) { "Read timeout must be positive" }
            field = value
        }

    var write: Long? = null
        set(value) {
            value?.let { require(it > 0) { "Write timeout must be positive" } }
            field = value
        }

    internal fun build() = Timeout(connect, read, write)
}

fun httpRequest(block: HttpRequestBuilder.() -> Unit): HttpRequest {
    return HttpRequestBuilder().apply(block).build()
}
