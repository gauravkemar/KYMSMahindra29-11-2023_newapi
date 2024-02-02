package com.kemarport.kymsmahindra.model.changepassword

data class ChangePasswordRequest(
    val ConfirmPassword: String,
    val OldPassword: String,
    val Password: String,
    val UserName: String
)