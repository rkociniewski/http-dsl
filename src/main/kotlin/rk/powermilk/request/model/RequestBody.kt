package rk.powermilk.request.model

/**
 * Sealed class representing different types of HTTP request bodies.
 *
 * This sealed hierarchy allows type-safe representation of various body formats
 * that can be sent in an HTTP request.
 *
 * @since 1.0.0
 */
sealed class RequestBody {
    /**
     * Represents a JSON request body.
     *
     * The data is stored as a map that can be serialized to JSON format.
     * Supports nested structures through nested maps.
     *
     * Example:
     * ```kotlin
     * body {
     *     json {
     *         "name" to "John"
     *         "age" to 30
     *         nested("address") {
     *             "city" to "New York"
     *         }
     *     }
     * }
     * ```
     *
     * @property data A map representing the JSON structure with keys as field names
     *                and values as field values (supports nested maps and collections).
     */
    data class JsonBody(val data: Map<String, Any?>) : RequestBody()

    /**
     * Represents a plain text request body.
     *
     * Used for sending plain text content such as XML, CSV, or any text-based format.
     *
     * Example:
     * ```kotlin
     * body {
     *     text("Plain text content")
     * }
     * ```
     *
     * @property text The text content to be sent. Must be non-empty and non-blank.
     */
    data class TextBody(val text: String) : RequestBody()

    /**
     * Represents a raw binary request body.
     *
     * Used for sending binary data such as files, images, or any byte array content.
     *
     * Example:
     * ```kotlin
     * body {
     *     raw(fileBytes)
     * }
     * ```
     *
     * @property bytes The raw binary data to be sent. Must not be empty.
     */
    data class RawBody(val bytes: ByteArray) : RequestBody() {
        /**
         * Compares this RawBody with another object for equality.
         *
         * Two RawBody instances are equal if their byte arrays have the same content.
         *
         * @param other The object to compare with.
         * @return `true` if the objects are equal, `false` otherwise.
         */
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false
            other as RawBody
            return bytes.contentEquals(other.bytes)
        }

        /**
         * Returns the hash code for this RawBody.
         *
         * The hash code is computed from the content of the byte array.
         *
         * @return The hash code value.
         */
        override fun hashCode(): Int {
            return bytes.contentHashCode()
        }
    }
}
