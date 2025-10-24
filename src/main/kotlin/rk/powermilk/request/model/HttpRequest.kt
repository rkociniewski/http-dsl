package rk.powermilk.request.model

import rk.powermilk.request.enums.HttpMethod

data class HttpRequest(
    val url: String,
    val method: HttpMethod,
    val headers: Map<String, String>,
    val body: RequestBody?,
    val timeout: Timeout,
)
