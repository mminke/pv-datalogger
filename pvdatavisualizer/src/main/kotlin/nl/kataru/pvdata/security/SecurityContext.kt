package nl.kataru.pvdata.security

import java.security.Principal
import javax.ws.rs.core.SecurityContext

class SecurityContextImpl(private val principal: Principal, private val scheme: String) : SecurityContext {

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