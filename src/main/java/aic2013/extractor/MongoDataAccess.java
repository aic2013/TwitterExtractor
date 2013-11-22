/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package aic2013.extractor;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import java.util.logging.Level;
import java.util.logging.Logger;
import twitter4j.Status;
import twitter4j.TwitterException;
import twitter4j.json.DataObjectFactory;

/**
 *
 * @author Christian
 */
public class MongoDataAccess {
    
    private final MongoClient mongoClient;

    public MongoDataAccess(MongoClient mongoClient) {
        this.mongoClient = mongoClient;
    }
    
    public void forAll(Processor<Status> processor) {
		DB db = mongoClient.getDB( "twitterdb" );
		DBCollection statusCollection = db.getCollection("statuses");
        DBCursor cursor = statusCollection.find();
        
        while(cursor.hasNext()) {
            try {
                processor.process(DataObjectFactory.createStatus(cursor.next().toString()));
            } catch (TwitterException ex) {
                Logger.getLogger(MongoDataAccess.class.getName())
                    .log(Level.SEVERE, null, ex);
            }
        }
    }
}
