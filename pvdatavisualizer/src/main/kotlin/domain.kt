/**
 * Created by morten on 2-1-17.
 */
package nl.kataru.pvdata.domain

data class Inverter(var serialNumber: String, var brand: String, var type: String, var ratedPower: Int) {
    constructor() : this("", "", "", -1)
}

