/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aic2013.extractor;

import aic2013.extractor.entities.Topic;
import aic2013.extractor.entities.TwitterUser;

import com.mongodb.MongoClient;

import java.io.FileInputStream;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.neo4j.jdbc.Driver;
import org.neo4j.jdbc.Neo4jConnection;

import twitter4j.HashtagEntity;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.User;

/**
 *
 * @author Christian
 */
public class TwitterExtractor {

	public static void main(String[] args) throws Exception {
		EntityManagerFactory emf = null;
		MongoClient mongoClient = null;
		Twitter twitter = null;
		Neo4jConnection neo4j = null;

		try {
			// Database configuration
			Properties prop = new Properties();
      prop.load(new FileInputStream("hibernate.properties"));

			emf = Persistence.createEntityManagerFactory("twitterdb", prop);
			final String mongoServer = "localhost";
			mongoClient = new MongoClient(mongoServer);
			twitter = TwitterFactory.getSingleton();

//			final String neo4jJdbc = "jdbc:neo4j:ec2-54-217-131-208.eu-west-1.compute.amazonaws.com:7474";
			final String neo4jJdbc = "jdbc:neo4j://localhost:7474";
			neo4j = new Driver().connect(neo4jJdbc, new Properties());
			neo4j.setAutoCommit(false);

			final TwitterDataAccess twitterDataAccess = new TwitterDataAccess(
					twitter);
			final MongoDataAccess mongoDataAccess = new MongoDataAccess(
					mongoClient);
			final Neo4jService neo4jService = new Neo4jService(neo4j);
			final EntityManager em = emf.createEntityManager();
			final UserDataAccess userDataAcces = new UserDataAccess(em);

			final TopicExtractionCoordinator extractionCoordinator = new TopicExtractionCoordinatorImpl();

//			final ConcurrentLinkedQueue<E>
			final UserService userService = new UserService(em);
			final Processor<Status> mongoProcessor = new Processor<Status>() {

				@Override
				public void process(final Status status) {
					final TwitterUser user = new TwitterUser(status.getUser());
					userService.persist(user);
					neo4jService.transactional(new Neo4jUnitOfWork() {

						@Override
						public void process() throws SQLException {
							neo4jService.createPersonIfAbsent(user);
							// HashtagEntity[] topics =
							// status.getHashtagEntities();

//							for (HashtagEntity tag : status
//									.getHashtagEntities()) {
//								Topic t = new Topic(tag.getText());
//								if (status.isRetweet()) {
//									neo4jService
//											.createRelationIfAbsent(
//													"RETWEETS",
//													user,
//													t);
//								} else {
//									neo4jService
//											.createRelationIfAbsent(
//													"TWEETS",
//													user,
//													t);
//								}
//							}

							extractionCoordinator.doExtraction(status,
								new TopicExtractionCallback() {

									@Override
									public void handleExtractionResult(
											Set<Topic> extractedTopics) {
										/* TODO: comment out outputs */
//										System.out.println(status.getText());
//										for (Topic t : extractedTopics) {
//											System.out.println(t.getName());
//										}
										try {
											/* add hash tags to topics */
											for (HashtagEntity tag : status
													.getHashtagEntities()) {
												extractedTopics.add(new Topic(tag
														.getText()));
											}
											for (Topic topic : extractedTopics) {
												neo4jService
														.createTopicIfAbsent(topic);
												if (status.isRetweet()) {
													neo4jService
															.createRelationIfAbsent(
																	"RETWEETS",
																	user,
																	topic);
												} else {
													neo4jService
															.createRelationIfAbsent(
																	"TWEETS",
																	user,
																	topic);
												}
											}
										} catch (SQLException e) {
											throw new RuntimeException(e);
										}

									}

									@Override
									public void handleExtractionError(
											ExtractionException e) {
										throw new RuntimeException(e);
									}
								});
						}
					});
				}
			};
			final Processor<TwitterUser> userProcessor = new Processor<TwitterUser>() {

				@Override
				public void process(final TwitterUser user) {
					try {
						twitterDataAccess.forAllFollowers(user,
								new Processor<User>() {

									@Override
									public void process(User u) {
										final TwitterUser follower = em.find(
												TwitterUser.class, u.getId());

										if (follower != null) {
											neo4jService
													.transactional(new Neo4jUnitOfWork() {

														@Override
														public void process()
																throws SQLException {
															neo4jService
																	.createUniqueRelation(
																			"FOLLOWS",
																			user,
																			follower);
														}
													});
										}
									}
								});
					} catch (Exception ex) {
						throw new RuntimeException(ex);
					}
				}
			};

			// invoke the processor for every entry in mongo
			mongoDataAccess.forAll(mongoProcessor);
			extractionCoordinator.awaitTermination(-1, TimeUnit.SECONDS);
			// invoke the processor for every entry in the rdbms
			userDataAcces.forAll(userProcessor);
		} finally {
			if (emf != null && emf.isOpen()) {
				emf.close();
			}
			if (mongoClient != null) {
				mongoClient.close();
			}
			if (twitter != null) {
				twitter.shutdown();
			}
			if (neo4j != null) {
				neo4j.close();
			}
		}
	}
}
