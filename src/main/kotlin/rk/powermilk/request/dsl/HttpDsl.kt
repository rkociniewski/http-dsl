package rk.powermilk.request.dsl

import rk.powermilk.request.constant.ErrorMessage
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
    private var timeout: Timeout = Timeout()

    fun url(url: String) {
        require(url.isNotEmpty()) { ErrorMessage.EMPTY_URL }
        require(url.isNotBlank()) { ErrorMessage.BLANK_URL }
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
        val finalUrl = checkNotNull(url) { ErrorMessage.REQUIRED_URL }
        return HttpRequest(finalUrl, method, headers.toMap(), body, timeout)
    }
}

@HttpDsl
class HeadersBuilder {
    private val headers = mutableMapOf<String, String>()

    infix fun String.to(value: String) {
        require(this.isNotEmpty()) { ErrorMessage.EMPTY_HEADER_NAME }
        require(this.isNotBlank()) { ErrorMessage.BLANK_HEADER_NAME }
        require(value.isNotEmpty()) { ErrorMessage.EMPTY_HEADER_VALUE }
        require(value.isNotBlank()) { ErrorMessage.BLANK_HEADER_VALUE }
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
        check(body == null) { ErrorMessage.BODY_SET_ONCE }
        body = RequestBody.JsonBody(JsonBuilder().apply(block).build())
    }

    fun text(content: String) {
        check(body == null) { ErrorMessage.BODY_SET_ONCE }
        require(content.isNotEmpty()) { ErrorMessage.EMPTY_TEXT_CONTENT }
        require(content.isNotBlank()) { ErrorMessage.BLANK_TEXT_CONTENT }
        body = RequestBody.TextBody(content)
    }

    fun raw(content: ByteArray) {
        check(body == null) { ErrorMessage.BODY_SET_ONCE }
        require(content.isNotEmpty()) { ErrorMessage.EMPTY_RAW_CONTENT }
        body = RequestBody.RawBody(content)
    }

    internal fun build() = body
}

@HttpDsl
class JsonBuilder {
    private val data = mutableMapOf<String, Any?>()

    infix fun String.to(value: Any?) {
        require(this.isNotEmpty()) { ErrorMessage.EMPTY_JSON_KEY }
        require(this.isNotBlank()) { ErrorMessage.BLANK_JSON_KEY }
        data[this] = value
    }

    operator fun String.invoke(value: Any?) {
        this to value
    }

    fun nested(key: String, block: JsonBuilder.() -> Unit) {
        require(key.isNotEmpty()) { ErrorMessage.EMPTY_JSON_KEY }
        require(key.isNotBlank()) { ErrorMessage.BLANK_JSON_KEY }
        data[key] = JsonBuilder().apply(block).build()
    }

    internal fun build() = data.toMap()
}

@HttpDsl
class TimeoutBuilder {
    var connect: Long? = null
        set(value) {
            value?.let { require(it > 0) { ErrorMessage.TIMEOUT_NEGATIVE_CONNECT } }
            field = value
        }

    var read: Long? = null
        set(value) {
            value?.let { require(it > 0) { ErrorMessage.TIMEOUT_NEGATIVE_READ } }
            field = value
        }

    var write: Long? = null
        set(value) {
            value?.let { require(it > 0) { ErrorMessage.TIMEOUT_NEGATIVE_WRITE } }
            field = value
        }

    internal fun build() = Timeout(connect, read, write)
}

fun httpRequest(block: HttpRequestBuilder.() -> Unit): HttpRequest {
    return HttpRequestBuilder().apply(block).build()
}
