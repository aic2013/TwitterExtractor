/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aic2013.extractor;

import aic2013.extractor.entities.Topic;
import aic2013.extractor.entities.TwitterUser;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.neo4j.jdbc.Neo4jConnection;

/**
 *
 * @author Christian
 */
public class Neo4jService {

    private final Neo4jConnection connection;

    public Neo4jService(Neo4jConnection connection) {
        this.connection = connection;
    }

    public void transactional(Neo4jUnitOfWork r) {
        boolean closed = false;
        boolean rollbackOnly = false;

        try {
            r.process();
        } catch (SQLException | RuntimeException ex) {
            rollbackOnly = true;

            try {
                closed = connection.isClosed();
            } catch (SQLException ex1) {
                Logger.getLogger(Neo4jService.class.getName())
                    .log(Level.SEVERE, null, ex1);
            }

            if (closed) {
                throw new RuntimeException(ex);
            }

            Logger.getLogger(TwitterExtractor.class.getName())
                .log(Level.SEVERE, null, ex);
        } finally {
//            try {
//                if (rollbackOnly) {
//                    connection.rollback();
//                } else {
//                    connection.commit();
//                }
//            } catch (SQLException ex) {
//                Logger.getLogger(TwitterExtractor.class.getName())
//                    .log(Level.SEVERE, null, ex);
//            }
        }
    }

    public void createPersonIfAbsent(TwitterUser user) throws SQLException {
        StringBuilder sb = new StringBuilder().append("MERGE (pers:")
            .append(user.toNeo4j())
            .append(")");
        query(sb.toString());
    }

    public void createTopicIfAbsent(Topic topic) throws SQLException {
        StringBuilder sb = new StringBuilder().append("MERGE (topic: ")
            .append(topic.toNeo4j())
            .append(")");
        query(sb.toString());
    }

    public void createRelationIfAbsent(String relation, TwitterUser user, Topic topic) throws SQLException {
        StringBuilder sb = new StringBuilder().append("MATCH (pers:")
            .append(user.toNeo4j())
            .append("),")
            .append("(topic:")
            .append(topic.toNeo4j())
            .append(")\n")
            .append("MERGE (pers)-[r:")
            .append(relation)
            .append("]->(topic)\n")
            .append("ON CREATE SET r.count = 1\n")
            .append("ON MATCH SET r.count = r.count + 1");
        query(sb.toString());
    }

    public void createUniqueRelation(String relation, TwitterUser user, TwitterUser follower) throws SQLException {
        StringBuilder sb = new StringBuilder().append("MATCH (pers:")
            .append(user.toNeo4j())
            .append("),")
            .append("(follower:")
            .append(follower.toNeo4j())
            .append(")\n")
            .append("CREATE UNIQUE (follower)-[:")
            .append(relation)
            .append("]->(pers)");
        query(sb.toString());
    }

    interface Neo4jProcessor<T> {

        public T process(ResultSet resultSet) throws SQLException;
    }

    private void query(String query) throws SQLException {
        Statement statement = null;

        try {
            statement = connection.createStatement();
            statement.executeUpdate(query);
        } finally {
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException ex) {
                    // Ignore
                }
            }
        }
    }
}
