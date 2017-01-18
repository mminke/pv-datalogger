package nl.kataru.pvdata.core

import nl.kataru.pvdata.security.AccountPrincipal
import javax.inject.Inject
import javax.ws.rs.*
import javax.ws.rs.core.Context
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response
import javax.ws.rs.core.SecurityContext

/**
 *
 */
@Path("/inverters")
open class InverterResource {

    @Inject
    lateinit private var inverterService: InverterService

    @Context
    lateinit private var securityContext: SecurityContext

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    fun getInverters(): Set<Inverter> {
        val accountPrincipal = securityContext.userPrincipal as AccountPrincipal

        return inverterService.findAllUsingAccount(accountPrincipal.account)
    }

    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    fun getInverter(@PathParam("id") id: String): Response {
        val accountPrincipal = securityContext.userPrincipal as AccountPrincipal

        val inverter = inverterService.findByIdUsingAccount(id, accountPrincipal.account)

        if (inverter.isPresent) {
            return Response.ok(inverter.get()).build()
        } else {
            return Response.status(Response.Status.NOT_FOUND).build()
        }
    }

    @DELETE
    @Path("{id}")
    fun deleteInverter(@PathParam("id") id: String): Response {
        val accountPrincipal = securityContext.userPrincipal as AccountPrincipal

        if (inverterService.deleteWithIdUsingAccount(id, accountPrincipal.account)) {
            return Response.ok().build()
        } else {
            return Response.status(Response.Status.NOT_FOUND).build()
        }
    }

    @PUT
    @Path("{serialNumber}")
    @Consumes(MediaType.APPLICATION_JSON)
    fun updateInverter(@PathParam("serialNumber") serialNumber: String, inverter: Inverter): String {
        val accountPrincipal = securityContext.userPrincipal as AccountPrincipal

        return "updated inverter with serial#: ${inverter.serialNumber}"
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    fun createInverter(inverter: Inverter): Response {
        val accountPrincipal = securityContext.userPrincipal as AccountPrincipal

        try {
            val createdInverter = inverterService.createUsingAccount(inverter, accountPrincipal.account)
            return Response.ok("created inverter with id: ${createdInverter.id}").build()
        } catch (exception: IllegalArgumentException) {
            // An inverter with the given serialnumber already exists.
            val error = nl.kataru.pvdata.core.Error(exception.message ?: "Unknown reason.")
            return Response.status(Response.Status.CONFLICT).entity(error).build()
        }
    }
}


//@ApplicationPath("api/v2/")
//class MyApplication : Application() {
//    override fun getClasses(): MutableSet<Class<*>>? {
//        val classes = HashSet<Class<*>>()
//        classes.add(nl.kataru.pvdata.core.InverterResource::class.java)
//        return classes
//    }
//}
