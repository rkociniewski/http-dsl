package rk.powermilk.request.model

import org.junit.jupiter.api.Test
import rk.powermilk.request.enums.HttpMethod
import kotlin.test.assertEquals

class UrlBuilderTest {

    @Test
    fun `should build URL with single segment`() {
        val url = UrlBuilder("https://api.example.com")
            .segment("users")
            .build()

        assertEquals("https://api.example.com/users", url)
    }

    @Test
    fun `should build URL with multiple segments`() {
        val url = UrlBuilder("https://api.example.com")
            .segment("users")
            .segment("123")
            .segment("posts")
            .build()

        assertEquals("https://api.example.com/users/123/posts", url)
    }

    @Test
    fun `should build URL with query parameters`() {
        val url = UrlBuilder("https://api.example.com")
            .segment("search")
            .query("q", "kotlin")
            .query("limit", "10")
            .build()

        assertEquals("https://api.example.com/search?q=kotlin&limit=10", url)
    }

    @Test
    fun `should build URL with segments and query parameters`() {
        val url = UrlBuilder("https://api.example.com")
            .segment("users")
            .segment("123")
            .query("include", "posts")
            .query("format", "json")
            .build()

        assertEquals("https://api.example.com/users/123?include=posts&format=json", url)
    }

    @Test
    fun `should trim slashes from segments`() {
        val url = UrlBuilder("https://api.example.com")
            .segment("/users/")
            .segment("/123/")
            .build()

        assertEquals("https://api.example.com/users/123", url)
    }

    @Test
    fun `should build request with httpRequestWithUrl helper`() {
        val request = httpRequestWithUrl(
            baseUrl = "https://api.example.com",
            urlBlock = {
                segment("users")
                segment("123")
                query("include", "posts")
            }
        ) {
            method(HttpMethod.GET)
            headers {
                "Accept" to "application/json"
            }
        }

        assertEquals("https://api.example.com/users/123?include=posts", request.url)
        assertEquals(HttpMethod.GET, request.method)
        assertEquals("application/json", request.headers["Accept"])
    }
}
