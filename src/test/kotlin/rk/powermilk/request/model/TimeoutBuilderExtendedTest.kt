package rk.powermilk.request.model

import org.junit.jupiter.api.Test
import rk.powermilk.request.dsl.httpRequest
import kotlin.test.assertEquals

class TimeoutBuilderExtendedTest {

    @Test
    fun `should set write timeout`() {
        val request = httpRequest {
            url("https://api.example.com/users")

            timeout {
                connect = 3000
                read = 5000
                write = 7000
            }
        }

        assertEquals(3000, request.timeout.connect)
        assertEquals(5000, request.timeout.read)
        assertEquals(7000, request.timeout.write)
    }

    @Test
    fun `should allow null write timeout`() {
        val request = httpRequest {
            url("https://api.example.com/users")

            timeout {
                connect = 3000
                read = 5000
                write = null
            }
        }

        assertEquals(null, request.timeout.write)
    }
}
