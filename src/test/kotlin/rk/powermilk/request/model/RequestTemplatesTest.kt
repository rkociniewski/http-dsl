package rk.powermilk.request.model

import org.junit.jupiter.api.Test
import rk.powermilk.request.enums.HttpMethod
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RequestTemplatesTest {
    @Test
    fun `should build authenticated request with token`() {
        val request = RequestTemplates.authenticated("https://api.example.com", "secret-token") {
            path("/users/123")
            method(HttpMethod.GET)
        }

        assertEquals("https://api.example.com/users/123", request.url)
        assertEquals(HttpMethod.GET, request.method)
        assertEquals("Bearer secret-token", request.headers["Authorization"])
        assertEquals("application/json", request.headers["Accept"])
    }

    @Test
    fun `should build authenticated POST request with body`() {
        val request = RequestTemplates.authenticated(
            baseUrl = "https://api.example.com",
            token = "my-token"
        ) {
            path("/users")
            method(HttpMethod.POST)

            body {
                json {
                    "name" to "John"
                    "email" to "john@example.com"
                }
            }
        }

        assertEquals(HttpMethod.POST, request.method)
        assertTrue(request.body is RequestBody.JsonBody)
    }

    @Test
    fun `should build authenticated request with additional headers`() {
        val request = RequestTemplates.authenticated(
            baseUrl = "https://api.example.com",
            token = "token123"
        ) {
            path("/data")

            headers {
                "X-Custom-Header" to "custom-value"
                "X-Request-ID" to "12345"
            }
        }

        assertEquals("Bearer token123", request.headers["Authorization"])
        assertEquals("custom-value", request.headers["X-Custom-Header"])
        assertEquals("12345", request.headers["X-Request-ID"])
    }
}
