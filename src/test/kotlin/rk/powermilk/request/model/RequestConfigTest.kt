package rk.powermilk.request.model

import org.junit.jupiter.api.Test
import rk.powermilk.request.enums.HttpMethod
import kotlin.test.assertEquals

class RequestConfigTest {

    @Test
    fun `should create config with default values`() {
        val config = RequestConfig()

        assertEquals(5000, config.defaultTimeout)
        assertEquals(emptyMap(), config.defaultHeaders)
        assertEquals("", config.baseUrl)
    }

    @Test
    fun `should create config with custom values`() {
        val config = RequestConfig(
            defaultTimeout = 10000,
            defaultHeaders = mapOf("Accept" to "application/json"),
            baseUrl = "https://api.example.com"
        )

        assertEquals(10000, config.defaultTimeout)
        assertEquals(1, config.defaultHeaders.size)
        assertEquals("https://api.example.com", config.baseUrl)
    }

    @Test
    fun `should build request with config defaults`() {
        val config = RequestConfig(
            defaultTimeout = 15000,
            defaultHeaders = mapOf(
                "User-Agent" to "MyApp/1.0",
                "Accept" to "application/json"
            )
        )

        val builder = ConfigurableRequestBuilder(config)
        val request = builder.build {
            url("https://api.example.com/users")
            method(HttpMethod.GET)
        }

        assertEquals(15000, request.timeout.connect)
        assertEquals(15000, request.timeout.read)
        assertEquals("MyApp/1.0", request.headers["User-Agent"])
        assertEquals("application/json", request.headers["Accept"])
    }
}
