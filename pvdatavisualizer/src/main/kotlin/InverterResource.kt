import nl.kataru.pvdata.domain.Account
import nl.kataru.pvdata.domain.Inverter
import javax.inject.Inject
import javax.ws.rs.*
import javax.ws.rs.container.ContainerRequestContext
import javax.ws.rs.core.Context
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

/**
 *
 */
@Path("/inverters")
open class InverterResource {

    @Inject
    lateinit internal var inverterService: InverterService
// This does not work:
//    @Context
//    lateinit internal var requestContext: ContainerRequestContext

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    fun getInverters(): Array<Inverter> {
        return arrayOf(
                Inverter("id1", "0123", "Brand1", "2000K", 2000, "account1"),
                Inverter("id2", "4567", "Brand2", "2000K", 2000, "account1"),
                Inverter("id3", "7890", "Brand3", "2000K", 2000, "account1"))
    }

    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    fun getInverter(@Context requestContext: ContainerRequestContext, @PathParam("id") id: String): Response {
        val account = requestContext.getProperty("account") as Account

        val inverter = inverterService.findByIdUsingAccount(id, account)

        if (inverter.isPresent) {
            return Response.ok(inverter.get()).build()
        } else {
            return Response.status(Response.Status.NOT_FOUND).build()
        }
    }

    @DELETE
    @Path("{id}")
    fun deleteInverter(@Context requestContext: ContainerRequestContext, @PathParam("id") id: String): Response {
        val account = requestContext.getProperty("account") as Account
        if (inverterService.deleteWithIdUsingAccount(id, account)) {
            return Response.ok().build()
        } else {
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
    fun createInverter(@Context requestContext: ContainerRequestContext, inverter: Inverter): Response {
        val account = requestContext.getProperty("account") as Account

        try {
            val createdInverter = inverterService.createUsingAccount(inverter, account)
            return Response.ok("created inverter with id: ${createdInverter.id}").build()
        } catch (exception: IllegalArgumentException) {
            // An inverter with the given serialnumber already exists.
            val error = Error(exception.message)
            return Response.status(Response.Status.CONFLICT).entity(error).build()
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
