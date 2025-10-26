package rk.powermilk.request.enums

/**
 * Represents HTTP request methods supported by the DSL.
 *
 * This enum defines the standard HTTP methods that can be used when building
 * HTTP requests using the DSL.
 *
 * Example usage:
 * ```kotlin
 * httpRequest {
 *     url("https://api.example.com/users")
 *     method(HttpMethod.POST)
 * }
 * ```
 *
 * @since 1.0.0
 */
enum class HttpMethod {
    /**
     * HTTP GET method for retrieving resources.
     */
    GET,

    /**
     * HTTP POST method for creating resources.
     */
    POST,

    /**
     * HTTP PUT method for updating/replacing resources.
     */
    PUT,

    /**
     * HTTP DELETE method for deleting resources.
     */
    DELETE,

    /**
     * HTTP PATCH method for partially updating resources.
     */
    PATCH
}
