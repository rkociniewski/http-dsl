package rk.powermilk.request.model

import org.junit.jupiter.api.Test
import rk.powermilk.request.dsl.httpRequest
import rk.powermilk.request.enums.HttpMethod
import kotlin.test.assertEquals


class InterceptorTest {

    @Test
    fun `should apply logging interceptor`() {
        val interceptor = LoggingInterceptor()
        val request = httpRequest {
            url("https://api.example.com/users")
            method(HttpMethod.GET)
        }

        val result = interceptor.intercept(request)
        assertEquals(request, result)
    }

    @Test
    fun `should apply retry interceptor`() {
        val interceptor = RetryInterceptor(maxRetries = 5)
        val request = httpRequest {
            url("https://api.example.com/users")
            method(HttpMethod.GET)
        }

        val result = interceptor.intercept(request)
        assertEquals("5", result.headers["X-Max-Retries"])
    }

    @Test
    fun `should execute pipeline with multiple interceptors`() {
        val pipeline = RequestPipeline(
            listOf(
                RetryInterceptor(maxRetries = 3),
                LoggingInterceptor()
            )
        )

        val request = httpRequest {
            url("https://api.example.com/users")
            method(HttpMethod.GET)
        }

        val result = pipeline.execute(request)
        assertEquals("3", result.headers["X-Max-Retries"])
    }
}
