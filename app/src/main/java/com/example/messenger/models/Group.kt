package com.example.messenger.models

import com.example.messenger.R

class Group {
    var name: String? = null
    var description: String? = null
    var image: String? = null
    var accounts: ArrayList<Account>? = null
    var admin: String? = null
    var key: String? = null
    var account_color: Int = R.color.def_siyoh

    constructor()
    constructor(
        name: String?,
        description: String?,
        image: String?,
        accounts: ArrayList<Account>,
        admin: String?,
        key: String?,
    ) {
        this.name = name
        this.description = description
        this.image = image
        this.accounts = accounts
        this.admin = admin
        this.key = key
    }

    constructor(
        name: String?,
        description: String?,
        image: String?,
        accounts: ArrayList<Account>,
        admin: String?,
        key: String?,
        account_color: Int,
    ) {
        this.name = name
        this.description = description
        this.image = image
        this.accounts = accounts
        this.admin = admin
        this.key = key
        this.account_color = account_color
    }
}