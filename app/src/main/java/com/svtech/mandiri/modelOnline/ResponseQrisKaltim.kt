package com.svtech.mandiri.modelOnline

data class ResponseQrisKaltim(
    val code: String,
    val `data`: Data,
    val message: String,
    val status: Boolean
) {
    data class Data(
        val acquirer_code: String,
        val acquirer_name: String,
        val amount: String,
        val bill_number: String,
        val created_at: String,
        val merchant_id: String,
        val merchant_ref: String,
        val nmid: String,
        val payment_description: String,
        val payment_status: String,
        val qr_expired: String,
        val qr_string: String,
        val request_id: String,
        val url_notif: String
    )
}