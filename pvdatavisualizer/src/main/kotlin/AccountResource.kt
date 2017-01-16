import nl.kataru.pvdata.domain.Account
import javax.annotation.security.PermitAll
import javax.inject.Inject
import javax.ws.rs.*
import javax.ws.rs.core.Context
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response
import javax.ws.rs.core.SecurityContext

/**
 *
 */
@Path("/accounts")
open class AccountResource() {

    @Inject
    lateinit internal var accountService: AccountService

    @Context
    lateinit internal var securityContext: SecurityContext
//    @Context
//    lateinit internal var requestContext: ContainerRequestContext

//    @GET
//    @Produces(MediaType.APPLICATION_JSON)
//    fun getAll(): Array<Inverter> {
//        return arrayOf()
//    }

//    @GET
//    @Path("{userName}")
//    @Produces(MediaType.APPLICATION_JSON)
//    fun getSingle(@PathParam("userName") userName: String): Response {
//        val account = accountService!!.findWithUserName(userName)
//
//        val response = account.map { Response.ok(it) }.orElse(Response.status(Response.Status.NOT_FOUND))
//        return response.build()
//    }

    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    fun getSingle(@PathParam("id") id: String): Response {
        val accountPrincipal = securityContext.userPrincipal as AccountPrincipal

        val account = accountService.findById(id)
        val response = account.map { Response.ok(it) }.orElse(Response.status(Response.Status.NOT_FOUND))
        return response.build()
    }

    @DELETE
    @Path("{id}")
    fun delete(@PathParam("id") id: String): Response {
        val accountPrincipal = securityContext.userPrincipal as AccountPrincipal

        if (accountService.deleteWithIdUsingAccount(id, accountPrincipal.account)) {
            return Response.ok().build()
        } else {
            return Response.status(Response.Status.NOT_FOUND).build()
        }
    }

    @PUT
    @Path("{userName}")
    @Consumes(MediaType.APPLICATION_JSON)
    fun update(@PathParam("userName") userName: String, account: Account): String {
        val accountPrincipal = securityContext.userPrincipal as AccountPrincipal
        return "updated account with userName#: ${account.username}"
    }

    /**
     * Create a new account. This method is accessible for everyone (otherwise you could not create an account without logging in!)
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @PermitAll
    fun create(account: Account): Response {
        try {
            val id = accountService.create(account)
            return Response.ok("created account with id: ${id}").build()
        } catch (exception: IllegalArgumentException) {
            // An inverter with the given serialnumber already exists.
            val error = Error("Could not create account. An account with the given user name already exists.")
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
