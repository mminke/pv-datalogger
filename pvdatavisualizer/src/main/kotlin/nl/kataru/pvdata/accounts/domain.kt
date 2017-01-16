package nl.kataru.pvdata.accounts

data class Account(var id: String = "", var username: String = "", var password: String = "") {
    constructor() : this("", "", "")
}