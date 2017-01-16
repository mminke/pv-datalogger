package nl.kataru.pvdata.accounts

import com.mongodb.client.MongoDatabase
import org.bson.Document
import org.bson.types.ObjectId
import java.util.*
import javax.inject.Inject


open class AccountService {
    private val COLLECTION_ACCOUNTS = "accounts"
    private val FIELD_ID = "_id"
    private val FIELD_USERNAME = "userName"
    private val FIELD_PASSWORD = "password"

    private var mongoDatabase: MongoDatabase

    @Inject
    constructor(mongoDatabase: MongoDatabase) {
        this.mongoDatabase = mongoDatabase
    }

    /**
     * Store the given account in persistent storage. Return the a new instance of the account, updated with the
     * generated id.
     *
     * @param account The account to store.
     * @return the account as it is stored in the persistent storage.
     * @throws IllegalArgumentException if an account with the given username already exists in persistent storage
     * @throws RuntimeException if the store action did not result in a new id.
     */
    fun create(account: Account): Account {
        if (accountWithUsernameExists(account.username)) {
            throw IllegalArgumentException("Account with username already exists")
        }

        val document = transformToDocument(account)

        val mongoCollection = mongoDatabase.getCollection(COLLECTION_ACCOUNTS)
        mongoCollection.insertOne(document)
        val objectId = document.getObjectId("_id")
        if (objectId != null) {
            return transformToAccount(document)
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
        val document = collection.find(query).first() ?: return Optional.empty()

        val account = transformToAccount(document)
        return Optional.of(account)
    }

    fun findByIdUsingAccount(id: String, currentAccount: Account): Optional<Account> {
        // A User is only allowed to access its own account
        if (currentAccount.id != id) {
            return Optional.empty()
        }

        val query = Document(FIELD_ID, ObjectId(id))

        val collection = mongoDatabase.getCollection(COLLECTION_ACCOUNTS)
        val document = collection.find(query).first() ?: return Optional.empty()

        val account = transformToAccount(document)
        return Optional.of(account)
    }


    fun deleteWithIdUsingAccount(id: String, currentAccount: Account): Boolean {
        // A User is only allowed to delete its own account
        if (currentAccount.id != id) {
            return false
        }

        val query = Document()
        query.append(FIELD_ID, ObjectId(id))

        val collection = mongoDatabase.getCollection(COLLECTION_ACCOUNTS)
        val result = collection.deleteOne(query)
        if (result.deletedCount == 1L) {
            return true
        } else {
            return false
        }
    }

    private fun accountWithUsernameExists(userName: String): Boolean {
        val collection = mongoDatabase.getCollection(COLLECTION_ACCOUNTS)

        val query = Document(FIELD_USERNAME, userName)
        collection.find(query).first() ?: return false

        return true
    }

    private fun transformToDocument(account: Account): Document {
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

    private fun transformToAccount(document: Document): Account {
        with(document) {
            val id = getObjectId(FIELD_ID).toString()
            val userName = getString(FIELD_USERNAME)
            val password = getString(FIELD_PASSWORD)

            val account = Account(id, userName, password)
            return account
        }
    }
}
