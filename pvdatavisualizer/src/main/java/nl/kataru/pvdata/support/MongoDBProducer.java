package nl.kataru.pvdata.support;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoDatabase;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import java.net.UnknownHostException;

/**
 * @author morten
 *
 */
@ApplicationScoped
public class MongoDBProducer {

	@Resource(name = "mongoClientURI")
	private MongoClientURI mongoClientURI;

	private MongoClient mongoClient;
	private MongoDatabase database;

	@PostConstruct
	public void init() throws UnknownHostException {
		System.out.println("=====================Creates database");
		final MongoClientURI uri = mongoClientURI;
		mongoClient = new MongoClient(uri);
		database = mongoClient.getDatabase(uri.getDatabase());
	}

	@PreDestroy
	public void close() {
		System.out.println("=====================Close client");
		mongoClient.close();
	}

	@Produces
	public MongoDatabase getDB() {
		return database;
	}

}