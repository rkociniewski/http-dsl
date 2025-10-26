package rk.powermilk.request.model

/**
 * Represents timeout configuration for HTTP requests.
 *
 * All timeout values are specified in milliseconds. A `null` value indicates
 * that no specific timeout is set for that operation.
 *
 * Example:
 * ```kotlin
 * httpRequest {
 *     url("https://api.example.com/users")
 *     timeout {
 *         connect = 5000  // 5 seconds
 *         read = 10000    // 10 seconds
 *         write = 7000    // 7 seconds
 *     }
 * }
 * ```
 *
 * @property connect The connection timeout in milliseconds.
 *                   Time allowed to establish a connection to the server.
 *                   Must be positive if set.
 * @property read The read timeout in milliseconds.
 *                Time allowed to read data from the server after connection is established.
 *                Must be positive if set.
 * @property write The write timeout in milliseconds.
 *                 Time allowed to write data to the server.
 *                 Must be positive if set.
 * @since 1.0.0
 */
data class Timeout(
    val connect: Long? = null,
    val read: Long? = null,
    val write: Long? = null,
)
