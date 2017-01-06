import com.mongodb.client.MongoDatabase
import nl.kataru.pvdata.domain.Inverter
import org.bson.Document
import java.util.*
import javax.inject.Inject
import javax.ws.rs.*
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

class InverterService {

    @Inject
    internal var mongoDatabase: MongoDatabase? = null

    fun saveInverter(inverter: Inverter) {
        val document = transformToDocument(inverter)

        val dbCollection = mongoDatabase?.getCollection("inverters")
        dbCollection?.insertOne(document)
    }

    fun findInverterWithSerialNumber(serialNumber: String): Optional<Inverter> {
        val collection = mongoDatabase!!.getCollection("inverters")

        val query = Document("serialnumber", serialNumber)
        val document = collection!!.find(query)?.first() ?: return Optional.empty()

        val inverter = transformToInverter(document)
        return Optional.of(inverter)
    }

    fun deleteInverterWithSerialNumber(serialNumber: String) : Boolean {
        if(findInverterWithSerialNumber(serialNumber).isPresent) {
            val collection = mongoDatabase!!.getCollection("inverters")

            val query = Document("serialnumber", serialNumber)

            collection!!.deleteOne(query)
            return true
        }

        return false;
    }

    fun transformToDocument(inverter: Inverter): Document {
        val document: Document = Document();
        with(inverter)
        {
            document.append("serialnumber", serialNumber)
            document.append("brand", brand)
            document.append("type", type)
            document.append("ratedPower", ratedPower)
        }
        return document
    }

    fun transformToInverter(document: Document): Inverter {
        with(document) {
            val serialNumber = getString("serialnumber")
            val brand = getString("brand") ?: "Unknown"
            val type = getString("type") ?: "Unknown"
            val ratedPower = getInteger("ratedPower") ?: -1

            val inverter = Inverter(serialNumber, brand, type, ratedPower)
            return inverter
        }
    }
}


/**
 *
 */
@Path("/inverters")
class InverterResource {

    @Inject
    internal var inverterService: InverterService? = null

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    fun getInverters(): Array<Inverter> {
        return arrayOf(
                Inverter("0123", "Brand1", "2000K", 2000),
                Inverter("4567", "Brand2", "2000K", 2000),
                Inverter("7890", "Brand3", "2000K", 2000))
    }

    @GET
    @Path("{serialNumber}")
    @Produces(MediaType.APPLICATION_JSON)
    fun getInverter(@PathParam("serialNumber") serialNumber: String): Response {
        if( inverterService!!.findInverterWithSerialNumber(serialNumber).isPresent ) {
            var response: Response = Response.ok(inverterService!!.findInverterWithSerialNumber(serialNumber).get()).build()
            return response
        }
        else {
            return Response.status(Response.Status.NOT_FOUND).build()
        }
    }

    @DELETE
    @Path("{serialNumber}")
    fun deleteInverter(@PathParam("serialNumber") serialNumber: String): Response {
        if(inverterService!!.deleteInverterWithSerialNumber(serialNumber))
        {
            return Response.ok().build()
        }
        else
        {
            return Response.status(Response.Status.NOT_FOUND).build()
        }
    }

    @PUT
    @Path("{serialNumber}")
    @Consumes(MediaType.APPLICATION_JSON)
    fun updateInverter(@PathParam("serialNumber") serialNumber: String, inverter: Inverter): String {
        return "updated inverter with serial#: ${inverter.serialNumber}"
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    fun createInverter(inverter: Inverter): Response {

        if (!inverterService!!.findInverterWithSerialNumber(inverter.serialNumber).isPresent) {
            inverterService!!.saveInverter(inverter)

            return Response.ok( "created inverter with serial#: ${inverter.serialNumber}" ).build()
        } else {
            // An inverter with the given serialnumber already exists.
            return Response.status(Response.Status.CONFLICT).entity("Could not create inverter. Inverter with the given serial number already exists.").build()
        }
    }
}

//@ApplicationPath("api/v2/")
//class MyApplication : Application() {
//    override fun getClasses(): MutableSet<Class<*>>? {
//        val classes = HashSet<Class<*>>()
//        classes.add(InverterResource::class.java)
//        return classes
//    }
//}