package rk.powermilk.request.model

import org.junit.jupiter.api.Test
import rk.powermilk.request.dsl.httpRequest
import rk.powermilk.request.enums.HttpMethod
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class RawBodyTest {

    @Test
    fun `should build request with raw body`() {
        val content = "test content".toByteArray()
        val request = httpRequest {
            url("https://api.example.com/upload")
            method(HttpMethod.POST)

            body {
                raw(content)
            }
        }

        assertTrue(request.body is RequestBody.RawBody)
        val rawBody = request.body as RequestBody.RawBody
        assertEquals("test content", String(rawBody.bytes))
    }

    @Test
    fun `should compare RawBody instances correctly`() {
        val content1 = "test".toByteArray()
        val content2 = "test".toByteArray()
        val content3 = "different".toByteArray()

        val body1 = RequestBody.RawBody(content1)
        val body2 = RequestBody.RawBody(content2)
        val body3 = RequestBody.RawBody(content3)

        assertEquals(body1, body2)
        assertEquals(body1.hashCode(), body2.hashCode())
        assertNotEquals(body1, body3)
    }
}
