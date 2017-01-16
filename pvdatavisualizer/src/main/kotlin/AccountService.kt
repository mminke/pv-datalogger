import com.mongodb.client.MongoDatabase
import nl.kataru.pvdata.domain.Account
import org.bson.Document
import org.bson.types.ObjectId
import java.util.*
import javax.inject.Inject


open class AccountService {
    internal val COLLECTION_ACCOUNTS = "accounts"
    internal val FIELD_ID = "_id"
    internal val FIELD_USERNAME = "userName"
    internal val FIELD_PASSWORD = "password"
    @Inject
    lateinit internal var mongoDatabase: MongoDatabase

    fun create(account: Account): String {
        if (findWithUserName(account.username).isPresent) {
            throw IllegalArgumentException("Account with username already exists")
        }

        val document = transformToDocument(account)

        val dbCollection = mongoDatabase.getCollection(COLLECTION_ACCOUNTS)
        dbCollection?.insertOne(document)
        val objectId = document.getObjectId("_id")
        if (objectId != null) {
            return objectId.toHexString()
        } else {
            throw RuntimeException("Unknown error occured, no id generated for new instance")
        }
    }

    fun save(account: Account) {
        throw UnsupportedOperationException("Not yet implemented")
    }

    fun findWith(userName: String, password: String): Optional<Account> {
        val collection = mongoDatabase.getCollection(COLLECTION_ACCOUNTS)

        val query = Document()
        query.append(FIELD_USERNAME, userName)
        query.append(FIELD_PASSWORD, password)
        val document = collection!!.find(query)?.first() ?: return Optional.empty()

        val account = transformToAccount(document)
        return Optional.of(account)
    }

    fun findWithUserName(userName: String): Optional<Account> {
        val collection = mongoDatabase.getCollection(COLLECTION_ACCOUNTS)

        val query = Document(FIELD_USERNAME, userName)
        val document = collection!!.find(query)?.first() ?: return Optional.empty()

        val account = transformToAccount(document)
        return Optional.of(account)
    }

    fun findById(id: String): Optional<Account> {
        val collection = mongoDatabase.getCollection(COLLECTION_ACCOUNTS)

        val query = Document(FIELD_ID, ObjectId(id))
        val document = collection!!.find(query)?.first() ?: return Optional.empty()

        val account = transformToAccount(document)
        return Optional.of(account)
    }

    fun deleteById(id: String): Boolean {
        if (findById(id).isPresent) {
            val collection = mongoDatabase.getCollection(COLLECTION_ACCOUNTS)

            val query = Document(FIELD_ID, id)

            collection!!.deleteOne(query)
            return true
        }

        return false
    }

    fun deleteWithIdUsingAccount(id: String, account: Account): Boolean {
        val collection = mongoDatabase.getCollection(COLLECTION_ACCOUNTS)

        // A User is only allowed to delete its own account
        if (account.id != id) {
            return false
        }

        val query = Document()
        query.append("_id", ObjectId(id))

        val result = collection!!.deleteOne(query)
        if (result.deletedCount == 1L) {
            return true
        } else {
            return false
        }
    }

    fun transformToDocument(account: Account): Document {
        val document: Document = Document()
        with(account)
        {
            if (!id.isNullOrBlank()) {
                document.append(FIELD_ID, id)
            }
            document.append(FIELD_USERNAME, username)
            document.append(FIELD_PASSWORD, password)
        }
        return document
    }

    fun transformToAccount(document: Document): Account {
        with(document) {
            val id = getObjectId(FIELD_ID).toString()
            val userName = getString(FIELD_USERNAME)
            val password = getString(FIELD_PASSWORD)

            val account = Account(id, userName, password)
            return account
        }
    }
}
