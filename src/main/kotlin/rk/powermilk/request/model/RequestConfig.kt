package rk.powermilk.request.model

/**
 * Configuration class for default request settings.
 *
 * This class allows defining default values that will be applied to all requests
 * built with a [ConfigurableRequestBuilder].
 *
 * Example:
 * ```kotlin
 * val config = RequestConfig(
 *     defaultTimeout = 10000,
 *     defaultHeaders = mapOf(
 *         "User-Agent" to "MyApp/1.0",
 *         "Accept" to "application/json"
 *     ),
 *     baseUrl = "https://api.example.com"
 * )
 * ```
 *
 * @property defaultTimeout Default timeout value in milliseconds applied to all timeout types
 *                          (connect, read, write) unless overridden.
 * @property defaultHeaders Map of default headers added to every request unless overridden.
 * @property baseUrl Optional base URL that can be used for building relative URLs.
 * @since 1.0.0
 */
data class RequestConfig(
    val defaultTimeout: Long = 5000,
    val defaultHeaders: Map<String, String> = emptyMap(),
    val baseUrl: String = ""
)
