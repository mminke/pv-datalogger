package nl.kataru.pvdata.accounts

import com.mongodb.client.FindIterable
import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import org.bson.Document
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException
import org.mockito.Matchers
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations

/**
 * Created by morten on 16-1-17.
 */
class AccountServiceTest {
    private val COLLECTION_ACCOUNTS = "accounts"

    @Rule
    @JvmField
    val expectedException = ExpectedException.none()

    @Mock
    lateinit private var mongoDatabaseMock: MongoDatabase
    @Mock
    lateinit private var collectionMock: MongoCollection<Document>
    @Mock
    lateinit private var queryResult: FindIterable<Document>

    private val account = Account(username = "JohnDoe", password = "secret")
    lateinit private var accountService: AccountService

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this);
        accountService = AccountService(mongoDatabaseMock)
    }

    @After
    fun tearDown() {

    }

    @Test
    fun givenUsernameAlreadyExistsWhenCreatingAccountExpectException() {
        // Prepare for accountWithUserNameExists
        `when`(mongoDatabaseMock.getCollection(COLLECTION_ACCOUNTS)).thenReturn(collectionMock)
        `when`(collectionMock.find(Matchers.any())).thenReturn(queryResult)
        `when`(queryResult.first()).thenReturn(Document())

        expectedException.expect(IllegalArgumentException::class.java)
        expectedException.expectMessage("Account with username already exists")
        accountService.create(account)
    }

    @Test
    fun givenNonExistingAccountWhenCreatingAccountExpectAccountWithId() {
        // Prepare for accountWithUserNameExists
        `when`(mongoDatabaseMock.getCollection(COLLECTION_ACCOUNTS)).thenReturn(collectionMock)
        `when`(collectionMock.find(Matchers.any())).thenReturn(queryResult)
        `when`(queryResult.first()).thenReturn(null)

        expectedException.expect(RuntimeException::class.java)
        expectedException.expectMessage("Unknown error occured, no id generated for new instance")
        accountService.create(account)
    }

    @Test
    fun save() {

    }

    @Test
    fun findWith() {

    }

    @Test
    fun findByIdUsingAccount() {

    }

    @Test
    fun deleteWithIdUsingAccount() {

    }

}