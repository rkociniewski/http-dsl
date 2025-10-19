package rk.powermilk.request.model

sealed class RequestBody {
    data class JsonBody(val data: Map<String, Any?>) : RequestBody()
    data class TextBody(val text: String) : RequestBody()
    data class RawBody(val bytes: ByteArray) : RequestBody() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false
            other as RawBody
            return bytes.contentEquals(other.bytes)
        }

        override fun hashCode(): Int {
            return bytes.contentHashCode()
        }
    }
}
