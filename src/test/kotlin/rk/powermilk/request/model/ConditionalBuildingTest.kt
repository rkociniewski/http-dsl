package rk.powermilk.request.model

import org.junit.jupiter.api.Test
import rk.powermilk.request.dsl.httpRequest
import rk.powermilk.request.enums.HttpMethod
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ConditionalBuildingTest {

    @Test
    fun `should include headers when condition is true`() {
        val request = httpRequest {
            url("https://api.example.com/users")

            conditionalHeaders(true) {
                "Authorization" to "Bearer token"
            }
        }

        assertEquals("Bearer token", request.headers["Authorization"])
    }

    @Test
    fun `should not include headers when condition is false`() {
        val request = httpRequest {
            url("https://api.example.com/users")

            conditionalHeaders(false) {
                "Authorization" to "Bearer token"
            }
        }

        assertFalse(request.headers.containsKey("Authorization"))
    }

    @Test
    fun `should include body when condition is true`() {
        val request = httpRequest {
            url("https://api.example.com/users")
            method(HttpMethod.POST)

            conditionalBody(true) {
                json {
                    "name" to "John"
                }
            }
        }

        assertTrue(request.body is RequestBody.JsonBody)
    }

    @Test
    fun `should not include body when condition is false`() {
        val request = httpRequest {
            url("https://api.example.com/users")
            method(HttpMethod.POST)

            conditionalBody(false) {
                json {
                    "name" to "John"
                }
            }
        }

        assertEquals(null, request.body)
    }
}
