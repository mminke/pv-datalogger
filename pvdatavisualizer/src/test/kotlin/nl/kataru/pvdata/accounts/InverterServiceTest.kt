package nl.kataru.pvdata.accounts

//import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
//import com.fasterxml.jackson.datatype.jdk8.Jdk8Module
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.mongodb.client.FindIterable
import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import nl.kataru.pvdata.core.InverterMeasurement
import nl.kataru.pvdata.core.InverterService
import nl.kataru.pvdata.core.bd
import org.bson.Document
import org.bson.types.Decimal128
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import java.time.OffsetDateTime
import java.util.*
import kotlin.test.assertEquals


/**
 *
 */
class InverterServiceTest {
    private val COLLECTION_INVERTERS = "inverters"
    private val COLLECTION_MEASUREMENTS = "measurements"
    private val MEASUREMENT_ID = "111111111111111111111111"
    private val INVERTER_ID = "222222222222222222222222"

    @Rule
    @JvmField
    val expectedException = ExpectedException.none()

    @Mock
    lateinit private var mongoDatabaseMock: MongoDatabase
    @Mock
    lateinit private var collectionMock: MongoCollection<Document>
    @Mock
    lateinit private var queryResult: FindIterable<Document>

    private val account = Account(username = "JohnDoe", password = "secret")
    lateinit private var inverterService: InverterService

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        inverterService = InverterService(mongoDatabaseMock)
    }


    @After
    fun tearDown() {

    }

    // The test below was used to analyse problems with the use of jackson icw the new java time types.
// It is kept here for now for archiving purposes. Currently the numeric long representation is used.
// The drawback is that this format is completely not readable by humans.
//    @Test
    fun testJackson() {
        val time = OffsetDateTime.now()
        println("The format I want: " + time)

        val mapper = ObjectMapper()
        //mapper.registerModule(ParameterNamesModule())
        //mapper.registerModule(Jdk8Module())
//        mapper.registerModule(JavaTimeModule())
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        mapper.disable(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE)

        var map = ""
        try {
            map = mapper.writeValueAsString(time)
        } catch (e: JsonProcessingException) {
            e.printStackTrace()
        }

        println("ObjectMapper returns:")
        println(map)

        val offsetDateTimeValue = mapper.readValue(map, OffsetDateTime::class.java)

        println("Back again: " + offsetDateTimeValue)
    }

    @Test
    fun testTransformMeasurementToDocument() {
        val timestamp = Date().time

        val measurement = InverterMeasurement(MEASUREMENT_ID, INVERTER_ID, timestamp, "24.5".bd, 2400.bd, "5.8".bd, "6000".bd, "RAW")

        val document = inverterService.transformToDocument(measurement)

        with(document) {
            assertEquals(MEASUREMENT_ID, getObjectId("_id").toString())
            assertEquals(INVERTER_ID, getString("inverterId"))

            assertEquals(timestamp, getLong("timestamp"))
            assertEquals("24.5".bd, get("temperature", Decimal128::class.java).bigDecimalValue())
            assertEquals(2400.bd, get("yieldTotal", Decimal128::class.java).bigDecimalValue())
            assertEquals("5.8".bd, get("yieldToday", Decimal128::class.java).bigDecimalValue())
            assertEquals("6000".bd, get("totalOperatingHours", Decimal128::class.java).bigDecimalValue())
            assertEquals("RAW", getString("rawData"))
        }
    }


    @Test
    fun save() {

    }

    @Test
    fun findWith() {

    }

    @Test
    fun findByIdUsingAccount() {

    }

    @Test
    fun deleteWithIdUsingAccount() {

    }

}