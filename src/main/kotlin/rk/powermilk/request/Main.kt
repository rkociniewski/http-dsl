package rk.powermilk.request

import rk.powermilk.request.dsl.httpRequest
import rk.powermilk.request.enums.HttpMethod

@Suppress("MagicNumber")
fun main() {
    val request = httpRequest {
        url("https://api.example.com/users")
        method(HttpMethod.POST)

        headers {
            "Content-Type" to "application/json"
            "Authorization" to "Bearer token123"
        }

        body {
            json {
                "name" to "John"
                "age" to 30
                "emails" to listOf("john@example.com")
            }
        }

        timeout {
            connect = 5000
            read = 10000
        }
    }

    println("httpRequest: $request")
}
