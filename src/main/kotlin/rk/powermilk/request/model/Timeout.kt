package rk.powermilk.request.model

data class Timeout(
    val connect: Long? = null,
    val read: Long? = null,
    val write: Long? = null,
)
