package rk.powermilk.request.model

import org.junit.jupiter.api.Test
import rk.powermilk.request.enums.HttpMethod
import kotlin.test.assertEquals

class BatchRequestBuilderTest {

    @Test
    fun `should build single request in batch`() {
        val requests = batchRequests {
            request {
                url("https://api.example.com/users/1")
                method(HttpMethod.GET)
            }
        }

        assertEquals(1, requests.size)
        assertEquals("https://api.example.com/users/1", requests[0].url)
    }

    @Test
    fun `should build multiple requests in batch`() {
        val requests = batchRequests {
            request {
                url("https://api.example.com/users/1")
                method(HttpMethod.GET)
            }

            request {
                url("https://api.example.com/users/2")
                method(HttpMethod.GET)
            }

            request {
                url("https://api.example.com/posts")
                method(HttpMethod.POST)
                body {
                    json {
                        "title" to "New Post"
                    }
                }
            }
        }

        assertEquals(3, requests.size)
        assertEquals(HttpMethod.GET, requests[0].method)
        assertEquals(HttpMethod.GET, requests[1].method)
        assertEquals(HttpMethod.POST, requests[2].method)
    }
}
