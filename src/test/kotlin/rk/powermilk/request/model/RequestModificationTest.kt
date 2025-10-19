package rk.powermilk.request.model

import org.junit.jupiter.api.Test
import rk.powermilk.request.dsl.httpRequest
import rk.powermilk.request.enums.HttpMethod
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RequestModificationTest {

    @Test
    fun `should modify request method`() {
        val original = httpRequest {
            url("https://api.example.com/users")
            method(HttpMethod.GET)
        }

        val modified = original.modify {
            method(HttpMethod.POST)
        }

        assertEquals(HttpMethod.GET, original.method)
        assertEquals(HttpMethod.POST, modified.method)
    }

    @Test
    fun `should modify request by adding headers`() {
        val original = httpRequest {
            url("https://api.example.com/users")
            headers {
                "Accept" to "application/json"
            }
        }

        val modified = original.modify {
            headers {
                "Authorization" to "Bearer token"
            }
        }

        assertEquals(1, original.headers.size)
        assertEquals(2, modified.headers.size)
        assertEquals("Bearer token", modified.headers["Authorization"])
    }

    @Test
    fun `should modify request by adding body`() {
        val original = httpRequest {
            url("https://api.example.com/users")
            method(HttpMethod.GET)
        }

        val modified = original.modify {
            method(HttpMethod.POST)
            body {
                json {
                    "name" to "Jane"
                }
            }
        }

        assertEquals(null, original.body)
        assertTrue(modified.body is RequestBody.JsonBody)
    }

    // Dodaj te testy do RequestModificationTest

    @Test
    fun `should modify request with text body preserved`() {
        val original = httpRequest {
            url("https://api.example.com/data")
            method(HttpMethod.POST)
            body {
                text("Original text")
            }
        }

        val modified = original.modify {
            headers {
                "Content-Type" to "text/plain"
            }
        }

        assertTrue(modified.body is RequestBody.TextBody)
        assertEquals("Original text", modified.body.text)
        assertEquals("text/plain", modified.headers["Content-Type"])
    }

    @Test
    fun `should modify request with raw body preserved`() {
        val content = "binary data".toByteArray()
        val original = httpRequest {
            url("https://api.example.com/upload")
            method(HttpMethod.POST)
            body {
                raw(content)
            }
        }

        val modified = original.modify {
            headers {
                "Content-Type" to "application/octet-stream"
            }
        }

        assertTrue(modified.body is RequestBody.RawBody)
        val rawBody = modified.body
        assertEquals("binary data", String(rawBody.bytes))
        assertEquals("application/octet-stream", modified.headers["Content-Type"])
    }

    @Test
    fun `should modify request with json body preserved`() {
        val original = httpRequest {
            url("https://api.example.com/users")
            method(HttpMethod.POST)
            body {
                json {
                    "name" to "John"
                    "age" to 30
                }
            }
        }

        val modified = original.modify {
            headers {
                "X-Custom" to "value"
            }
        }

        assertTrue(modified.body is RequestBody.JsonBody)
        val jsonBody = modified.body
        assertEquals("John", jsonBody.data["name"])
        assertEquals(30, jsonBody.data["age"])
    }
}
