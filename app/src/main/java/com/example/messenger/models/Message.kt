package com.example.messenger.models

class Message {
    var from: String? = null
    var to: String? = null
    var date: Long? = null
    var check: Int? = 0
    var key: String? = null

    var text: String? = ""
    var audio:String?=null
    var image:String?=null
    var video:String?=null
    var file:String?=null
    constructor()
    constructor(text: String?, from: String, to: String, date: Long, check: Int, key: String,audio:String?,image:String?,video:String?,file:String?) {
        this.text = text
        this.from = from
        this.to = to
        this.date = date
        this.check = check
        this.key = key
        this.audio=audio
        this.image=image
        this.video=video
        this.file=file
    }
}