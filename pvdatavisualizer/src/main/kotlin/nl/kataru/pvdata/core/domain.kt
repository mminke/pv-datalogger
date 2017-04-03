package nl.kataru.pvdata.core

import java.math.BigDecimal


//@Provider
//class ObjectMapperContextResolver : ContextResolver<ObjectMapper> {
//    private val mapper: ObjectMapper
//
//    init {
//        mapper = ObjectMapper()
//        mapper.registerModule(JavaTimeModule())
//        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
//        mapper.disable(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE)
//    }
//
//    override fun getContext(type: Class<*>): ObjectMapper {
//        return mapper
//    }
//}

val DEFAULT_TIME = 0L

data class Inverter(var id: String, var serialNumber: String, var brand: String, var type: String, var ratedPower: Int, var owner: String) {
    constructor() : this("", "", "", "", -1, "")
}


data class InverterMeasurement(var id: String, var inverterId: String, var timestamp: Long, var temperature: BigDecimal, var yieldTotal: BigDecimal, var yieldToday: BigDecimal, var totalOperatingHours: BigDecimal, var rawData: String) {
    constructor() : this("", "", DEFAULT_TIME, -1.bd, -1.bd, -1.bd, -1.bd, "")
}

data class Error(var reason: String) {
    constructor() : this("")
}

/**
 * BigDecimal extensions
 * Allow usage of 1.bd which results in a BigDecimal of the integer value 1.
 */
val Int.bd: BigDecimal
    get() = BigDecimal(this)

val Double.bd: BigDecimal
    get() = BigDecimal(this)

val String.bd: BigDecimal
    get() = BigDecimal(this)

/*
Modeling:

System
    Inverter
        Group
            Panel


Available data:

Inverter
    Temperature
    Yield Today
    Yield Total (cummulative)
    Hours Total (Total operating hours)
    Message
    Raw Data

Group
    Panel (DC) Current (generated, input)
    Panel (DC) Voltage
    AC Current (exported, output)
    AC Voltage
    AC Power
    AC Frequency


Interface per sensor/metric:

inverters/123/sensors/temperatures
inverters/123/groups/1/sensors/power
inverters/123/groups/1/sensors/current
inverters/123/groups/1/sensors/

Or per group of sensor/metrics:

inverters/123/data
inverters/123/groups/1/data

Dynamic sensors per device:

inverters/123/measurements/
    Message contains a map with the sensor id and its value


 */
