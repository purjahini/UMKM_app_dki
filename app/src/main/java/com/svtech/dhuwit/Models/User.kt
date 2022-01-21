package com.svtech.dhuwit.Models

import com.orm.SugarRecord

data class User (
    var id :Int? = 0,
    var nama:String? = null,
    var kontak:String? = null,
    var username:String? = null,
    var password:String? = null,
    var role:String? = null,
    var foto:String? = null
):SugarRecord<User>(){
    companion object{
        val userAdmin = "userAdmin"
        val userSysAdmin = "userSysAdmin"
        val userBuyer = "userBuyer"
    }
}
