package nl.kataru.pvdata.security

import nl.kataru.pvdata.accounts.AccountService
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
import javax.ws.rs.ext.Provider


@Provider
@Priority(Priorities.AUTHENTICATION)
class AuthenticationFilter : ContainerRequestFilter {
    private val BASIC_AUTHENTICATION_SCHEME = "Basic"
    private val AUTHORIZATION_HEADER = "Authorization"
    private val UNAUTHORIZED_RESPONSE = Response.status(Response.Status.UNAUTHORIZED).header("WWW-Authenticate", "Basic realm=\"PVData\"").build()
    private val FORBIDDEN_RESPONSE = Response.status(Response.Status.FORBIDDEN).build()
    @Context
    lateinit private var resourceInfo: ResourceInfo
    @Inject
    lateinit private var accountService: AccountService

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
        requestContext.setSecurityContext(SecurityContextImpl(AccountPrincipal(account.get()), scheme))
    }

    fun obtainCredentialsFrom(requestContext: ContainerRequestContext): Optional<Credentials> {
        val headers = requestContext.headers

        val authenticationHeaders = headers[AUTHORIZATION_HEADER]
        if (authenticationHeaders != null) {
            for (authorizationHeader in authenticationHeaders) {
                if (authorizationHeader.startsWith(BASIC_AUTHENTICATION_SCHEME)) {
                    return obtainBasicAuthCredentials(authorizationHeader)
                }
            }
        }

        return Optional.empty()
    }

    fun obtainBasicAuthCredentials(authorizationHeader: String): Optional<Credentials> {
        val encodedCredentials = authorizationHeader.replaceFirst(BASIC_AUTHENTICATION_SCHEME + " ", "")
        val decodedCredentials = String(Base64.getDecoder().decode(encodedCredentials))
        val tokenizer = StringTokenizer(decodedCredentials, ":")
        if (tokenizer.countTokens() == 2) {
            return Optional.of(Credentials(tokenizer.nextToken(), tokenizer.nextToken()))
        }

        return Optional.empty()
    }

}
