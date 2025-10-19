package rk.powermilk.request.model

sealed class RequestBody {
    data class JsonBody(val data: Map<String, Any?>) : RequestBody()
    data class TextBody(val text: String) : RequestBody()
}
