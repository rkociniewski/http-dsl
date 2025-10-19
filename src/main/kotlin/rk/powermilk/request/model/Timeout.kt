package rk.powermilk.request.model

data class Timeout(
    val connect: Long,
    val read: Long,
    val write: Long? = null
)
