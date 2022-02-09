package com.svtech.dhuwit.modelOnline

data class UserOnline(
    val api_message: String?,
    val api_status: Int?,
    val `data`: Data?
) {
    data class Data(
        val FOTO: Any?,
        val KONTAK: String?,
        val NAMA: String?,
        val PASSWORD: String?,
        val ROLE: String?,
        val ROLEID: Int?,
        val USERNAME: String?,
        val id: Int?
    )
}