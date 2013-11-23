package aic2013.extractor;

import java.util.concurrent.TimeUnit;

import twitter4j.Status;

/**
 * @author Moritz Becker (moritz.becker@gmx.at)
 *
 */
public interface TopicExtractionCoordinator {
	void doExtraction(String input, TopicExtractionCallback callback);
	void doExtraction(Status status, TopicExtractionCallback callback);
	void awaitTermination(long timeout, TimeUnit unit) throws InterruptedException;
}
