package rk.powermilk.request.model

import org.junit.jupiter.api.Test
import rk.powermilk.request.dsl.httpRequest
import kotlin.test.assertEquals
import kotlin.test.assertNull

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
    fun `should allow null connect timeout`() {
        val request = httpRequest {
            url("https://api.example.com/users")

            timeout {
                connect = null
                read = 5000
                write = 3000
            }
        }

        assertNull(request.timeout.connect)
    }

    @Test
    fun `should allow null read timeout`() {
        val request = httpRequest {
            url("https://api.example.com/users")

            timeout {
                connect = 5000
                read = null
                write = 3000
            }
        }

        assertNull(request.timeout.read)
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

        assertNull(request.timeout.write)
    }
}
