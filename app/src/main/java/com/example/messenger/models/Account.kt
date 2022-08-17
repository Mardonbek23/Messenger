package com.example.messenger.models

import com.example.messenger.R


class Account{
    var phone:String?=null
    var name:String?="No name"
    var surname:String?=""
    var description:String?=null
    var email:String?=null
    var image:String?=null
    var password:String?=null
    var isOnline:Long?=1L
    var account_color:Int= R.color.def_siyoh
    var notification_key:String?=null
    var background_color:Int?=null

    constructor()
    constructor(phone:String?,name:String?,surname:String?,description:String?,email:String?,image:String?,password:String?,isOnline:Long) {
        this.phone=phone
        this.name=name
        this.surname=surname
        this.description=description
        this.email=email
        this.image=image
        this.password=password
        this.isOnline=isOnline
    }
    constructor(phone:String?,name:String?,surname:String?,description:String?,email:String?,image:String?,password:String?,isOnline:Long,account_color:Int) {
        this.phone=phone
        this.name=name
        this.surname=surname
        this.description=description
        this.email=email
        this.image=image
        this.password=password
        this.isOnline=isOnline
        this.account_color=account_color
    }
    constructor(phone:String?,name:String?,surname:String?,description:String?,email:String?,image:String?,password:String?,isOnline:Long,account_color:Int,notification_key:String?,background_color:Int?) {
        this.phone=phone
        this.name=name
        this.surname=surname
        this.description=description
        this.email=email
        this.image=image
        this.password=password
        this.isOnline=isOnline
        this.account_color=account_color
        this.notification_key=notification_key
        this.background_color=background_color
    }
}



