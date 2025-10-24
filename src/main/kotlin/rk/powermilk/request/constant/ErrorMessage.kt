package rk.powermilk.request.constant

object ErrorMessage {
    const val BODY_SET_ONCE = "Body can only be set once"
    const val EMPTY_RAW_CONTENT = "Raw body cannot be empty"
    const val BLANK_TEXT_CONTENT = "Text body cannot be blank"
    const val EMPTY_TEXT_CONTENT = "Text body cannot be empty"
    const val BLANK_JSON_KEY = "JSON key cannot be blank"
    const val EMPTY_JSON_KEY = "JSON key cannot be empty"
    const val EMPTY_HEADER_NAME = "Header name cannot be empty"
    const val BLANK_HEADER_NAME = "Header name cannot be blank"
    const val EMPTY_HEADER_VALUE = "Header value cannot be empty"
    const val BLANK_HEADER_VALUE = "Header value cannot be blank"
    const val BLANK_URL = "URL must be non-blank"
    const val EMPTY_URL = "URL must be non-empty"
    const val REQUIRED_URL = "URL is required"
    const val TIMEOUT_NEGATIVE_WRITE = "Write timeout must be positive"
    const val TIMEOUT_NEGATIVE_READ = "Read timeout must be positive"
    const val TIMEOUT_NEGATIVE_CONNECT = "Connect timeout must be positive"
}
