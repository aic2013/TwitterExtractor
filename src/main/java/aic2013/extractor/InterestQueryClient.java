/**
 * 
 */
package aic2013.extractor;

import java.io.FileInputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.persistence.EntityManager;
import javax.persistence.Persistence;

import org.neo4j.jdbc.Driver;
import org.neo4j.jdbc.Neo4jConnection;

import twitter4j.TwitterFactory;
import aic2013.extractor.entities.Topic;

import com.mongodb.MongoClient;

/**
 * @author Moritz Becker (moritz.becker@gmx.at)
 * 
 */
public class InterestQueryClient {
	private static Neo4jConnection neo4j = null;
	public static void main(String[] args) throws Exception {
		try {

			Properties prop = new Properties();
		    prop.load(new FileInputStream("neo4j.properties"));
			// final String neo4jJdbc =
			// "jdbc:neo4j:ec2-54-217-131-208.eu-west-1.compute.amazonaws.com:7474";
			final String neo4jJdbc = "jdbc:neo4j://localhost:7474";
			neo4j = new Driver().connect(neo4jJdbc, prop);
			neo4j.setAutoCommit(true);// false);

			
			getTopicsOfInterestForUser(1);
			

		} finally {
			if (neo4j != null) {
				neo4j.close();
			}
		}
	}
	
	public static List<Topic> getTopicsOfInterestForUser(long userId) throws Exception{
		final long tweetsWeight = 1;
		final long retweetsWeight = 1;
		
		long internalPersonId = -1;
		ResultSet rs = neo4j.createStatement().executeQuery("MATCH (a:Person) WHERE a.id=" + userId + " RETURN ID(a)");
//		
		if(rs.next()){
			internalPersonId = rs.getLong(1);
		}else{
			throw new Exception("Person not found");
		}
		
		rs = neo4j.createStatement().executeQuery("START a=node(1433) MATCH (a)-[retRel:RETWEETS]->(b) " + 
"WITH retRel, b, retRel.count * 1 as weight " +
"ORDER BY weight desc " + 
"RETURN b " +
"UNION " +
"START a=node(1433) MATCH (a)-[rel:TWEETS]->(b) " +
"WITH rel, b, rel.count * 3 as weight " +
"ORDER BY weight desc " +
"RETURN b");
		List<Topic> result = new ArrayList<Topic>();
		while(rs.next()){
			
			for(int i = 1; i <= rs.getMetaData().getColumnCount(); i++){
				System.out.println(rs.getString(i));
			}
		}
		return result;
	}
}
