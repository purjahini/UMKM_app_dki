package com.svtech.dhuwit.Models

import com.orm.SugarRecord

data class Profile(
    var id :Int? = 0,
    var kode: String? = null,
    var namaToko: String? = null,
    var alamatToko: String? = null,
    var logoToko: String? = null,
    var USERNAME: String? = null
) : SugarRecord<Profile>()
