package rk.powermilk.request.model

import org.junit.jupiter.api.Test
import rk.powermilk.request.dsl.httpRequest
import rk.powermilk.request.enums.HttpMethod
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ValidationTest {

    @Test
    fun `should pass validation for valid HTTPS request`() {
        val request = httpRequest {
            url("https://api.example.com/users")
            method(HttpMethod.POST)

            body {
                json {
                    "name" to "John"
                }
            }
        }

        val result = request.validate()
        assertTrue(result.isSuccess)
    }

    @Test
    fun `should fail validation for HTTP url`() {
        val request = httpRequest {
            url("http://api.example.com/users")
            method(HttpMethod.GET)
        }

        val result = request.validate()
        assertTrue(result.isFailure)
        assertEquals("URL must use HTTPS", result.exceptionOrNull()?.message)
    }

    @Test
    fun `should fail validation for POST without body`() {
        val request = httpRequest {
            url("https://api.example.com/users")
            method(HttpMethod.POST)
        }

        val result = request.validate()
        assertTrue(result.isFailure)
        assertEquals(result.exceptionOrNull()?.message?.contains("should have a body"), true)
    }

    @Test
    fun `should pass validation for GET without body`() {
        val request = httpRequest {
            url("https://api.example.com/users")
            method(HttpMethod.GET)
        }

        val result = request.validate()
        assertTrue(result.isSuccess)
    }
}
