/**
 *
 */
package nl.kataru.pvdata.support;

import java.net.UnknownHostException;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoDatabase;

/**
 * @author morten
 *
 */
@ApplicationScoped
public class MongoDBProducer {

	@Resource(name = "mongoClientURI")
	private MongoClientURI mongoClientURI;

	private MongoClient mongoClient;
	private MongoDatabase databbase;

	@PostConstruct
	public void init() throws UnknownHostException {
		final MongoClientURI uri = mongoClientURI;
		mongoClient = new MongoClient(uri);
		databbase = mongoClient.getDatabase(uri.getDatabase());
	}

	@PreDestroy
	public void close() {
		System.out.println("=====================Close client");
		mongoClient.close();
	}

	@Produces
	public MongoDatabase createDB() {
		System.out.println("=====================Creates database");
		return databbase;
	}

}