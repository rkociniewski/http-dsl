package rk.powermilk.request.constant

/**
 * Contains error message constants used throughout the HTTP Request DSL library.
 *
 * This object centralizes all error messages to ensure consistency and maintainability.
 * All messages are used in validation checks within the DSL builders.
 *
 * @since 1.0.0
 */
object ErrorMessage {
    /**
     * Error message when attempting to set the request body more than once.
     */
    const val BODY_SET_ONCE = "Body can only be set once"

    /**
     * Error message when raw body content is empty.
     */
    const val EMPTY_RAW_CONTENT = "Raw body cannot be empty"

    /**
     * Error message when text body content is blank (contains only whitespace).
     */
    const val BLANK_TEXT_CONTENT = "Text body cannot be blank"

    /**
     * Error message when text body content is empty.
     */
    const val EMPTY_TEXT_CONTENT = "Text body cannot be empty"

    /**
     * Error message when a JSON key is blank (contains only whitespace).
     */
    const val BLANK_JSON_KEY = "JSON key cannot be blank"

    /**
     * Error message when a JSON key is empty.
     */
    const val EMPTY_JSON_KEY = "JSON key cannot be empty"

    /**
     * Error message when a header name is empty.
     */
    const val EMPTY_HEADER_NAME = "Header name cannot be empty"

    /**
     * Error message when a header name is blank (contains only whitespace).
     */
    const val BLANK_HEADER_NAME = "Header name cannot be blank"

    /**
     * Error message when a header value is empty.
     */
    const val EMPTY_HEADER_VALUE = "Header value cannot be empty"

    /**
     * Error message when a header value is blank (contains only whitespace).
     */
    const val BLANK_HEADER_VALUE = "Header value cannot be blank"

    /**
     * Error message when URL is blank (contains only whitespace).
     */
    const val BLANK_URL = "URL must be non-blank"

    /**
     * Error message when URL is empty.
     */
    const val EMPTY_URL = "URL must be non-empty"

    /**
     * Error message when URL is not provided (null).
     */
    const val REQUIRED_URL = "URL is required"

    /**
     * Error message when write timeout is negative.
     */
    const val TIMEOUT_NEGATIVE_WRITE = "Write timeout must be positive"

    /**
     * Error message when read timeout is negative.
     */
    const val TIMEOUT_NEGATIVE_READ = "Read timeout must be positive"

    /**
     * Error message when connect timeout is negative.
     */
    const val TIMEOUT_NEGATIVE_CONNECT = "Connect timeout must be positive"
}
