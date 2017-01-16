import com.mongodb.client.MongoDatabase
import nl.kataru.pvdata.domain.Account
import nl.kataru.pvdata.domain.Inverter
import org.bson.Document
import java.util.*
import javax.inject.Inject

open class InverterService {
    internal val COLLECTION_INVERTERS = "inverters"

    @Inject
    lateinit internal var mongoDatabase: MongoDatabase

    /**
     * Create a new inverter entry in persistent storage.
     * @return a inverter instance containing the id of the newly created entry.
     * @throws IllegalArgumentException if an inverter with the given serial number already exists.
     * @throws RuntimeException if something went wrong while inserting the document and a new id could not be obtained.
     */
    fun createUsingAccount(inverter: Inverter, account: Account): Inverter {
        if (findBySerialNumberUsingAccount(inverter.serialNumber, account).isPresent) {
            throw IllegalArgumentException("Inverter with serialnumber already exists")
        }

        val inverterWithOwner = inverter.copy(owner = account.id)
        val document = transformToDocument(inverterWithOwner)

        val dbCollection = mongoDatabase.getCollection(COLLECTION_INVERTERS)
        dbCollection?.insertOne(document)
        val objectId = document.getObjectId("_id")
        if (objectId != null) {
            return transformToInverter(document)
        } else {
            throw RuntimeException("Unknown error occured, no id generated for new instance")
        }
    }

    fun save(account: Account) {
        throw UnsupportedOperationException("Not yet implemented")
    }

    fun findByIdUsingAccount(id: String, account: Account): Optional<Inverter> {
        val collection = mongoDatabase.getCollection(COLLECTION_INVERTERS)

        val query = Document()
        query.append("_id", id)
        query.append("owner", account.id)
        val document = collection!!.find(query)?.first() ?: return Optional.empty()

        val inverter = transformToInverter(document)
        return Optional.of(inverter)
    }

    fun findBySerialNumberUsingAccount(serialNumber: String, account: Account): Optional<Inverter> {
        val collection = mongoDatabase.getCollection(COLLECTION_INVERTERS)

        val query = Document()
        query.append("serialnumber", serialNumber)
        query.append("owner", account.id)
        val document = collection!!.find(query)?.first() ?: return Optional.empty()

        val inverter = transformToInverter(document)
        return Optional.of(inverter)
    }

    fun deleteWithIdUsingAccount(id: String, account: Account): Boolean {
        val collection = mongoDatabase.getCollection(COLLECTION_INVERTERS)

        val query = Document()
        query.append("_id", id)
        query.append("owner", account.id)

        val result = collection!!.deleteOne(query)
        if (result.deletedCount == 1L) {
            return true
        } else {
            return false
        }
    }

    fun transformToDocument(inverter: Inverter): Document {
        val document: Document = Document()
        with(inverter)
        {
            if (!id.isNullOrBlank()) {
                document.append("_id", id)
            }
            document.append("serialnumber", serialNumber)
            document.append("brand", brand)
            document.append("type", type)
            document.append("ratedPower", ratedPower)
            document.append("owner", inverter.owner)
        }
        return document
    }

    fun transformToInverter(document: Document): Inverter {
        with(document) {
            val id = getString("_id")
            val serialNumber = getString("serialnumber")
            val brand = getString("brand") ?: "Unknown"
            val type = getString("type") ?: "Unknown"
            val ratedPower = getInteger("ratedPower") ?: -1
            val owner = getString("owner")

            val inverter = Inverter(id, serialNumber, brand, type, ratedPower, owner)
            return inverter
        }
    }
}
