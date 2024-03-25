package com.svtech.mandiri.Models

import com.orm.SugarRecord

data class Kategori(
    var id : Int = 0,
    var nama : String? = null,
    var gambar : String? = null
): SugarRecord<Kategori>(){
    override fun toString(): String {
        return this.nama.toString()
    }
}
