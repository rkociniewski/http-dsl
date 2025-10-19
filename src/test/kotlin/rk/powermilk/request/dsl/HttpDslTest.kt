package rk.powermilk.request.dsl

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import rk.powermilk.request.enums.HttpMethod
import rk.powermilk.request.model.RequestBody
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class HttpDslTest {

    @Test
    fun `should build minimal GET request`() {
        val request = httpRequest {
            url("https://api.example.com/users")
        }

        assertEquals("https://api.example.com/users", request.url)
        assertEquals(HttpMethod.GET, request.method)
        assertEquals(emptyMap(), request.headers)
        assertNull(request.body)
    }

    @Test
    fun `should build POST request with JSON body`() {
        val request = httpRequest {
            url("https://api.example.com/users")
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

        val jsonBody = request.body as RequestBody.JsonBody
        assertEquals("John", jsonBody.data["name"])
        assertEquals(30, jsonBody.data["age"])
        assertEquals(true, jsonBody.data["active"])
    }

    @Test
    fun `should build request with headers`() {
        val request = httpRequest {
            url("https://api.example.com/users")

            headers {
                "Content-Type" to "application/json"
                "Authorization" to "Bearer token123"
                "X-Custom-Header" to "custom-value"
            }
        }

        assertEquals(3, request.headers.size)
        assertEquals("application/json", request.headers["Content-Type"])
        assertEquals("Bearer token123", request.headers["Authorization"])
        assertEquals("custom-value", request.headers["X-Custom-Header"])
    }

    @Test
    fun `should build request with custom timeout`() {
        val request = httpRequest {
            url("https://api.example.com/users")

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
            url("https://api.example.com/data")
            method(HttpMethod.POST)

            body {
                text("Plain text content")
            }
        }

        assertIs<RequestBody.TextBody>(request.body)
        assertEquals("Plain text content", (request.body as RequestBody.TextBody).text)
    }

    @Test
    fun `should build request with nested JSON`() {
        val request = httpRequest {
            url("https://api.example.com/users")
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

        @Suppress("UNCHECKED_CAST")
        val address = jsonBody.data["address"] as Map<String, Any?>
        assertEquals("Main St", address["street"])
        assertEquals("New York", address["city"])
        assertEquals("10001", address["zip"])
    }

    @Test
    fun `should build complex request with all features`() {
        val request = httpRequest {
            url("https://api.example.com/users")
            method(HttpMethod.POST)

            headers {
                "Content-Type" to "application/json"
                "Authorization" to "Bearer token123"
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

        assertEquals("https://api.example.com/users", request.url)
        assertEquals(HttpMethod.POST, request.method)
        assertEquals(2, request.headers.size)
        assertNotNull(request.body)
        assertEquals(5000, request.timeout.connect)
        assertEquals(10000, request.timeout.read)
    }

    @Test
    fun `should throw exception when URL is missing`() {
        val exception = assertThrows<IllegalStateException> {
            httpRequest {
                method(HttpMethod.GET)
            }
        }
        assertEquals("URL is required", exception.message)
    }

    @Test
    fun `should throw exception when URL is blank`() {
        val exception = assertThrows<IllegalArgumentException> {
            httpRequest {
                url("   ")
            }
        }
        assertEquals("URL cannot be blank", exception.message)
    }

    @Test
    fun `should throw exception when header name is blank`() {
        val exception = assertThrows<IllegalArgumentException> {
            httpRequest {
                url("https://api.example.com")
                headers {
                    "" to "value"
                }
            }
        }
        assertEquals("Header name cannot be blank", exception.message)
    }

    @Test
    fun `should throw exception when header value is blank`() {
        val exception = assertThrows<IllegalArgumentException> {
            httpRequest {
                url("https://api.example.com")
                headers {
                    "Content-Type" to "   "
                }
            }
        }
        assertEquals("Header value cannot be blank", exception.message)
    }

    @Test
    fun `should throw exception when body is set twice`() {
        val exception = assertThrows<IllegalArgumentException> {
            httpRequest {
                url("https://api.example.com")
                body {
                    text("First body")
                    text("Second body")
                }
            }
        }
        assertEquals("Body can only be set once", exception.message)
    }

    @Test
    fun `should throw exception when JSON and text body are both set`() {
        val exception = assertThrows<IllegalArgumentException> {
            httpRequest {
                url("https://api.example.com")
                body {
                    json {
                        "key" to "value"
                    }
                    text("Text body")
                }
            }
        }
        assertEquals("Body can only be set once", exception.message)
    }

    @Test
    fun `should throw exception for negative connect timeout`() {
        val exception = assertThrows<IllegalArgumentException> {
            httpRequest {
                url("https://api.example.com")
                timeout {
                    connect = -1000
                }
            }
        }
        assertEquals("Connect timeout must be positive", exception.message)
    }

    @Test
    fun `should throw exception for negative read timeout`() {
        val exception = assertThrows<IllegalArgumentException> {
            httpRequest {
                url("https://api.example.com")
                timeout {
                    read = -5000
                }
            }
        }
        assertEquals("Read timeout must be positive", exception.message)
    }

    @Test
    fun `should allow multiple headers blocks`() {
        val request = httpRequest {
            url("https://api.example.com")

            headers {
                "Content-Type" to "application/json"
            }

            headers {
                "Authorization" to "Bearer token"
            }
        }

        assertEquals(2, request.headers.size)
        assertEquals("application/json", request.headers["Content-Type"])
        assertEquals("Bearer token", request.headers["Authorization"])
    }

    @Test
    fun `should override header when set twice`() {
        val request = httpRequest {
            url("https://api.example.com")

            headers {
                "Content-Type" to "text/plain"
                "Content-Type" to "application/json"
            }
        }

        assertEquals(1, request.headers.size)
        assertEquals("application/json", request.headers["Content-Type"])
    }

    @Test
    fun `should handle null values in JSON`() {
        val request = httpRequest {
            url("https://api.example.com")
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
            url("https://api.example.com")
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
            url("https://api.example.com")
            headers {
                "Content-Type" to "application/json"
            }
        }

        val originalSize = request.headers.size
        assertThrows<UnsupportedOperationException> {
            (request.headers as MutableMap)["New-Header"] = "value"
        }
        assertEquals(originalSize, request.headers.size)
    }
}
