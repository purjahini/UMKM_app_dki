package com.svtech.dhuwit.Models

import com.orm.SugarRecord

data class Kategori(
    var nama : String? = null,
    var gambar : String? = null
): SugarRecord<Kategori>(){
    override fun toString(): String {
        return this.nama.toString()
    }
}
