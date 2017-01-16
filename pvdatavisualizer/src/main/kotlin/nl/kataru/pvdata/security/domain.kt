package nl.kataru.pvdata.security

import nl.kataru.pvdata.accounts.Account
import java.security.Principal

data class Credentials(val username: String, val password: String)

class AccountPrincipal(val account: Account) : Principal {
    override fun getName(): String {
        return account.username
    }
}