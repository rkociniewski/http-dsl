package rk.powermilk.request.model

data class RequestConfig(
    val defaultTimeout: Long = 5000,
    val defaultHeaders: Map<String, String> = emptyMap(),
    val baseUrl: String = ""
)
