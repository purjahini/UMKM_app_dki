package com.svtech.mandiri.modelOnline

data class UserOnline(
    val api_message: String?,
    val api_status: Int?,
    val `data`: Data?
) {
    data class Data(
        val foto: Any?,
        val kontak: String?,
        val nama: String?,
        val password: String?,
        val role: String?,
        val roleid: Int?,
        val username: String?,
        val id: Int?
    )
}