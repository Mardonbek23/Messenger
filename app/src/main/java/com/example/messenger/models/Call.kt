package com.example.messenger.models

class Call {
    var from: String? = null
    var to: String? = null
    var date: Long? = null
    var key: String? = null
    constructor()
    constructor(from:String?,to:String?,date:Long?,key:String?){
        this.from=from
        this.to=to
        this.date=date
        this.key=key
    }
}