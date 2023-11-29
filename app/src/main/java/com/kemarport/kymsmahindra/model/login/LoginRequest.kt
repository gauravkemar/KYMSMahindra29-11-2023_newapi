package com.kemarport.kymsmahindra.model.login

data class LoginRequest(
    val Password: String?,
    val UserName: String?,
    val DeviceId: String?
)