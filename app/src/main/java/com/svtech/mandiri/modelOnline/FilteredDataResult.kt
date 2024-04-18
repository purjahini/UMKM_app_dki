package com.svtech.mandiri.modelOnline

data class FilteredDataResult(
    val filteredData: List<ResponseMenuNeraca.Data>,
    val fromDate: String,
    val toDate: String
)
