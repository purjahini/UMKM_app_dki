package com.svtech.mandiri.modelOnline

data class ResponseQris(
    val code: String,
    val `data`: Data,
    val message: String,
    val status: Boolean
) {
    data class Data(
        val additionalInfo: AdditionalInfo,
        val expired_at: String,
        val merchantName: String,
        val partnerReferenceNo: String,
        val qrContent: String,
        val qr_expired: String,
        val responseCode: String,
        val responseMessage: String,
        val storeId: String
    ) {
        data class AdditionalInfo(
            val bill_number: String,
            val channel: String,
            val deviceId: String
        )
    }
}