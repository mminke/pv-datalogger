import nl.kataru.pvdata.domain.Account
import java.security.Principal
import java.util.*
import javax.annotation.Priority
import javax.annotation.security.PermitAll
import javax.inject.Inject
import javax.ws.rs.Priorities
import javax.ws.rs.container.ContainerRequestContext
import javax.ws.rs.container.ContainerRequestFilter
import javax.ws.rs.container.ResourceInfo
import javax.ws.rs.core.Context
import javax.ws.rs.core.Response
import javax.ws.rs.core.SecurityContext
import javax.ws.rs.ext.Provider


data class Credentials(val username: String, val password: String)

class AccountPrincipal(val account: Account) : Principal {
    override fun getName(): String {
        return account.username
    }
}

class MyApplicationSecurityContext(private val principal: Principal, private val scheme: String) : SecurityContext {

    override fun getUserPrincipal(): Principal {
        return this.principal
    }

    override fun isUserInRole(s: String): Boolean {
//        if (user.getRole() != null) {
//            return user.getRole().contains(s)
//        }
        return true
    }

    override fun isSecure(): Boolean {
        return "https" == this.scheme
    }

    override fun getAuthenticationScheme(): String {
        return SecurityContext.BASIC_AUTH
    }
}

@Provider
@Priority(Priorities.AUTHENTICATION)
class AuthenticationFilter : ContainerRequestFilter {
    internal val BASIC_AUTHENTICATION_SCHEME = "Basic"
    internal val AUTHORIZATION_HEADER = "Authorization"
    internal val UNAUTHORIZED_RESPONSE = Response.status(Response.Status.UNAUTHORIZED).header("WWW-Authenticate", "Basic realm=\"PVData\"").build()
    internal val FORBIDDEN_RESPONSE = Response.status(Response.Status.FORBIDDEN).build()

    @Context
    lateinit var resourceInfo: ResourceInfo

    @Inject
    lateinit internal var accountService: AccountService

    override fun filter(requestContext: ContainerRequestContext) {
        val method = resourceInfo.getResourceMethod()
        if (method.isAnnotationPresent(PermitAll::class.java)) {
            //Access allowed for all
            return
        }

        val credentials = obtainCredentialsFrom(requestContext)
        if (!credentials.isPresent) {
            requestContext.abortWith(UNAUTHORIZED_RESPONSE)
            return
        }

        val account = accountService.findWith(credentials.get().username, credentials.get().password)
        if (!account.isPresent) {
            requestContext.abortWith(UNAUTHORIZED_RESPONSE)
            return
        }

        val scheme = requestContext.getUriInfo().getRequestUri().getScheme()
        requestContext.setSecurityContext(MyApplicationSecurityContext(AccountPrincipal(account.get()), scheme))

        //requestContext.setProperty("account", account)

    }

    fun obtainCredentialsFrom(requestContext: ContainerRequestContext): Optional<Credentials> {
        val headers = requestContext.headers

        val authenticationHeaders = headers[AUTHORIZATION_HEADER]
        if (authenticationHeaders != null) {
            for (authorizationHeader in authenticationHeaders) {
                if (authorizationHeader.startsWith(BASIC_AUTHENTICATION_SCHEME)) {
                    val encodedCredentials = authorizationHeader.replaceFirst(BASIC_AUTHENTICATION_SCHEME + " ", "")
                    val decodedCredentials = String(Base64.getDecoder().decode(encodedCredentials))
                    val tokenizer = StringTokenizer(decodedCredentials, ":")

                    return Optional.of(Credentials(tokenizer.nextToken(), tokenizer.nextToken()))
                }
            }
        }

        return Optional.empty()
    }
}
