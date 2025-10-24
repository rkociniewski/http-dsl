package rk.powermilk.request.dsl

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import rk.powermilk.request.constant.ErrorMessage
import rk.powermilk.request.enums.HttpMethod
import rk.powermilk.request.model.RequestBody
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull

const val PLAIN_TEXT = "Plain text content"
const val CONTENT_TYPE_JSON = "application/json"
const val USER_URL = "https://api.example.com/users"
const val HEADER_CONTENT_TYPE = "Content-Type"
const val HEADER_AUTH = "Authorization"
const val HEADER_X_CUSTOM = "X-Custom-Header"
const val BEARER_TOKEN = "Bearer token123"
const val CUSTOM_VALUE = "custom-value"
const val CONTENT_TYPE_TEXT  = "text/plain"
const val URL_EXAMPLE  = "https://api.example.com"

class HttpDslTest {

    @Test
    fun `should build minimal GET request`() {
        val request = httpRequest {
            url(USER_URL)
        }

        assertEquals(USER_URL, request.url)
        assertEquals(HttpMethod.GET, request.method)
        assertEquals(emptyMap(), request.headers)
        assertNull(request.body)
    }

    @Test
    fun `should build POST request with JSON body`() {
        val request = httpRequest {
            url(USER_URL)
            method(HttpMethod.POST)

            body {
                json {
                    "name" to "John"
                    "age" to 30
                    "active" to true
                }
            }
        }

        assertEquals(HttpMethod.POST, request.method)
        assertNotNull(request.body)
        assertIs<RequestBody.JsonBody>(request.body)

        val jsonBody = request.body
        assertEquals("John", jsonBody.data["name"])
        assertEquals(30, jsonBody.data["age"])
        assertEquals(true, jsonBody.data["active"])
    }

    @Test
    fun `should build request with headers`() {
        val request = httpRequest {
            url(USER_URL)

            headers {
                HEADER_CONTENT_TYPE to CONTENT_TYPE_JSON
                HEADER_AUTH to BEARER_TOKEN
                HEADER_X_CUSTOM to CUSTOM_VALUE
            }
        }

        assertEquals(3, request.headers.size)
        assertEquals(CONTENT_TYPE_JSON, request.headers[HEADER_CONTENT_TYPE])
        assertEquals(BEARER_TOKEN, request.headers[HEADER_AUTH])
        assertEquals(CUSTOM_VALUE, request.headers[HEADER_X_CUSTOM])
    }

    @Test
    fun `should build request with custom timeout`() {
        val request = httpRequest {
            url(USER_URL)

            timeout {
                connect = 3000
                read = 15000
            }
        }

        assertEquals(3000, request.timeout.connect)
        assertEquals(15000, request.timeout.read)
    }

    @Test
    fun `should build request with text body`() {
        val request = httpRequest {
            url("${URL_EXAMPLE}/data")
            method(HttpMethod.POST)

            body {
                text(PLAIN_TEXT)
            }
        }

        assertIs<RequestBody.TextBody>(request.body)
        assertEquals(PLAIN_TEXT, (request.body).text)
    }

    @Test
    fun `should build request with nested JSON`() {
        val request = httpRequest {
            url(USER_URL)
            method(HttpMethod.POST)

            body {
                json {
                    "name" to "John"
                    nested("address") {
                        "street" to "Main St"
                        "city" to "New York"
                        "zip" to "10001"
                    }
                    "emails" to listOf("john@example.com", "j.doe@example.com")
                }
            }
        }

        val jsonBody = request.body as RequestBody.JsonBody
        assertEquals("John", jsonBody.data["name"])

        val address = jsonBody.data["address"] as Map<*, *>
        assertEquals("Main St", address["street"])
        assertEquals("New York", address["city"])
        assertEquals("10001", address["zip"])
    }

    @Test
    fun `should throw exception with empty JSON key`() {
        val exception = assertFailsWith<IllegalArgumentException> {
            httpRequest {
                url(USER_URL)
                method(HttpMethod.POST)

                body {
                    json {
                        "" to "John"
                        nested("address") {
                            "street" to "Main St"
                            "city" to "New York"
                            "zip" to "10001"
                        }
                        "emails" to listOf("john@example.com", "j.doe@example.com")
                    }
                }
            }
        }

        assertEquals(ErrorMessage.EMPTY_JSON_KEY, exception.message)
    }

    @Test
    fun `should throw exception with blank JSON key`() {
        val exception = assertFailsWith<IllegalArgumentException> {
            httpRequest {
                url(USER_URL)
                method(HttpMethod.POST)

                body {
                    json {
                        "   " to "John"
                        nested("address") {
                            "street" to "Main St"
                            "city" to "New York"
                            "zip" to "10001"
                        }
                        "emails" to listOf("john@example.com", "j.doe@example.com")
                    }
                }
            }
        }

        assertEquals(ErrorMessage.BLANK_JSON_KEY, exception.message)
    }

    @Test
    fun `should throw exception with empty nested JSON key`() {
        val exception = assertFailsWith<IllegalArgumentException> {
            httpRequest {
                url(USER_URL)
                method(HttpMethod.POST)

                body {
                    json {
                        "name" to "John"
                        nested("address") {
                            "" to "Main St"
                            "city" to "New York"
                            "zip" to "10001"
                        }
                        "emails" to listOf("john@example.com", "j.doe@example.com")
                    }
                }
            }
        }

        assertEquals(ErrorMessage.EMPTY_JSON_KEY, exception.message)
    }

    @Test
    fun `should throw exception with blank nested JSON key`() {
        val exception = assertFailsWith<IllegalArgumentException> {
            httpRequest {
                url(USER_URL)
                method(HttpMethod.POST)

                body {
                    json {
                        "name" to "John"
                        nested("address") {
                            "   " to "Main St"
                            "city" to "New York"
                            "zip" to "10001"
                        }
                        "emails" to listOf("john@example.com", "j.doe@example.com")
                    }
                }
            }
        }

        assertEquals(ErrorMessage.BLANK_JSON_KEY, exception.message)
    }

    @Test
    fun `should build complex request with all features`() {
        val request = httpRequest {
            url(USER_URL)
            method(HttpMethod.POST)

            headers {
                HEADER_CONTENT_TYPE to CONTENT_TYPE_JSON
                HEADER_AUTH to BEARER_TOKEN
            }

            body {
                json {
                    "name" to "John"
                    "age" to 30
                    "emails" to listOf("john@example.com")
                }
            }

            timeout {
                connect = 5000
                read = 10000
            }
        }

        assertEquals(USER_URL, request.url)
        assertEquals(HttpMethod.POST, request.method)
        assertEquals(2, request.headers.size)
        assertNotNull(request.body)
        assertEquals(5000, request.timeout.connect)
        assertEquals(10000, request.timeout.read)
    }

    @Test
    fun `should throw exception when URL is missing`() {
        val exception = assertFailsWith<IllegalStateException> {
            httpRequest {
                method(HttpMethod.GET)
            }
        }
        assertEquals(ErrorMessage.REQUIRED_URL, exception.message)
    }

    @Test
    fun `should throw exception when URL is empty`() {
        val exception = assertFailsWith<IllegalArgumentException> {
            httpRequest {
                method(HttpMethod.GET)
                url("")
            }
        }
        assertEquals(ErrorMessage.EMPTY_URL, exception.message)
    }

    @Test
    fun `should throw exception when URL is blank`() {
        val exception = assertFailsWith<IllegalArgumentException> {
            httpRequest {
                url("   ")
            }
        }
        assertEquals(ErrorMessage.BLANK_URL, exception.message)
    }

    @Test
    fun `should throw exception when header name is blank`() {
        val exception = assertFailsWith<IllegalArgumentException> {
            httpRequest {
                url(URL_EXAMPLE)
                headers {
                    " " to "value"
                }
            }
        }
        assertEquals(ErrorMessage.BLANK_HEADER_NAME, exception.message)
    }

    @Test
    fun `should throw exception when header name is empty`() {
        val exception = assertFailsWith<IllegalArgumentException> {
            httpRequest {
                url(URL_EXAMPLE)
                headers {
                    "" to "value"
                }
            }
        }
        assertEquals(ErrorMessage.EMPTY_HEADER_NAME, exception.message)
    }

    @Test
    fun `should throw exception when header value is blank`() {
        val exception = assertFailsWith<IllegalArgumentException> {
            httpRequest {
                url(URL_EXAMPLE)
                headers {
                    HEADER_CONTENT_TYPE to "   "
                }
            }
        }
        assertEquals(ErrorMessage.BLANK_HEADER_VALUE, exception.message)
    }

    @Test
    fun `should throw exception when header value is empty`() {
        val exception = assertFailsWith<IllegalArgumentException> {
            httpRequest {
                url(URL_EXAMPLE)
                headers {
                    HEADER_CONTENT_TYPE to ""
                }
            }
        }
        assertEquals(ErrorMessage.EMPTY_HEADER_VALUE, exception.message)
    }

    @Test
    fun `should throw exception when body is set twice`() {
        val exception = assertFailsWith<IllegalStateException> {
            httpRequest {
                url(URL_EXAMPLE)
                body {
                    text("First body")
                    text("Second body")
                }
            }
        }
        assertEquals(ErrorMessage.BODY_SET_ONCE, exception.message)
    }

    @Test
    fun `should throw exception when text body is blank`() {
        val exception = assertFailsWith<IllegalArgumentException> {
            httpRequest {
                url(URL_EXAMPLE)
                body {
                    text("    ")
                }
            }
        }
        assertEquals(ErrorMessage.BLANK_TEXT_CONTENT, exception.message)
    }

    @Test
    fun `should throw exception when text body is empty`() {
        val exception = assertFailsWith<IllegalArgumentException> {
            httpRequest {
                url(URL_EXAMPLE)
                body {
                    text("")
                }
            }
        }
        assertEquals(ErrorMessage.EMPTY_TEXT_CONTENT, exception.message)
    }

    @Test
    fun `should throw exception when raw body is empty`() {
        val exception = assertFailsWith<IllegalArgumentException> {
            httpRequest {
                url(URL_EXAMPLE)
                body {
                    raw(byteArrayOf())
                }
            }
        }
        assertEquals(ErrorMessage.EMPTY_RAW_CONTENT, exception.message)
    }

    @Test
    fun `should throw exception when JSON and text body are both set`() {
        val exception = assertFailsWith<IllegalStateException> {
            httpRequest {
                url(URL_EXAMPLE)
                body {
                    json {
                        "key" to "value"
                    }
                    text("Text body")
                }
            }
        }
        assertEquals(ErrorMessage.BODY_SET_ONCE, exception.message)
    }

    @Test
    fun `should throw exception for negative connect timeout`() {
        val exception = assertFailsWith<IllegalArgumentException> {
            httpRequest {
                url(URL_EXAMPLE)
                timeout {
                    connect = -1000
                }
            }
        }
        assertEquals(ErrorMessage.TIMEOUT_NEGATIVE_CONNECT, exception.message)
    }

    @Test
    fun `should throw exception for negative read timeout`() {
        val exception = assertFailsWith<IllegalArgumentException> {
            httpRequest {
                url(URL_EXAMPLE)
                timeout {
                    read = -5000
                }
            }
        }
        assertEquals(ErrorMessage.TIMEOUT_NEGATIVE_READ, exception.message)
    }

    @Test
    fun `should throw exception for negative write timeout`() {
        val exception = assertFailsWith<IllegalArgumentException> {
            httpRequest {
                url(URL_EXAMPLE)
                timeout {
                    write = -5000
                }
            }
        }
        assertEquals(ErrorMessage.TIMEOUT_NEGATIVE_WRITE, exception.message)
    }

    @Test
    fun `should allow multiple headers blocks`() {
        val request = httpRequest {
            url(URL_EXAMPLE)

            headers {
                HEADER_CONTENT_TYPE to CONTENT_TYPE_JSON
            }

            headers {
                HEADER_AUTH to BEARER_TOKEN
            }
        }

        assertEquals(2, request.headers.size)
        assertEquals(CONTENT_TYPE_JSON, request.headers[HEADER_CONTENT_TYPE])
        assertEquals(BEARER_TOKEN, request.headers[HEADER_AUTH])
    }

    @Test
    fun `should override header when set twice`() {
        val request = httpRequest {
            url(URL_EXAMPLE)

            headers {
                HEADER_CONTENT_TYPE to CONTENT_TYPE_TEXT
                HEADER_CONTENT_TYPE to CONTENT_TYPE_JSON
            }
        }

        assertEquals(1, request.headers.size)
        assertEquals(CONTENT_TYPE_JSON, request.headers[HEADER_CONTENT_TYPE])
    }

    @Test
    fun `should handle null values in JSON`() {
        val request = httpRequest {
            url(URL_EXAMPLE)
            method(HttpMethod.POST)

            body {
                json {
                    "name" to "John"
                    "middleName" to null
                    "age" to 30
                }
            }
        }

        val jsonBody = request.body as RequestBody.JsonBody
        assertEquals(3, jsonBody.data.size)
        assertEquals("John", jsonBody.data["name"])
        assertNull(jsonBody.data["middleName"])
        assertEquals(30, jsonBody.data["age"])
    }

    @Test
    fun `should handle empty collections in JSON`() {
        val request = httpRequest {
            url(URL_EXAMPLE)
            method(HttpMethod.POST)

            body {
                json {
                    "emails" to emptyList<String>()
                    "tags" to emptySet<String>()
                }
            }
        }

        val jsonBody = request.body as RequestBody.JsonBody
        assertEquals(emptyList<String>(), jsonBody.data["emails"])
        assertEquals(emptySet<String>(), jsonBody.data["tags"])
    }

    @Test
    fun `should test immutability of headers`() {
        val request = httpRequest {
            url(URL_EXAMPLE)
            headers {
                HEADER_CONTENT_TYPE to CONTENT_TYPE_JSON
            }
        }

        val originalSize = request.headers.size
        assertThrows<UnsupportedOperationException> {
            (request.headers as MutableMap)["New-Header"] = "value"
        }
        assertEquals(originalSize, request.headers.size)
    }
}
