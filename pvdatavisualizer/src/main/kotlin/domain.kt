package nl.kataru.pvdata.domain

data class Account(var id: String = "", var username: String = "", var password: String = "") {
    constructor() : this("", "", "")
}

data class Inverter(var id: String, var serialNumber: String, var brand: String, var type: String, var ratedPower: Int, var owner: String) {
    constructor() : this("", "", "", "", -1, "")
}

data class Error(var reason: String) {
    constructor() : this("")
}
