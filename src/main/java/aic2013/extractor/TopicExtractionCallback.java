package aic2013.extractor;

import java.util.List;
import java.util.Set;

import aic2013.extractor.entities.Topic;

/**
 * @author Moritz Becker (moritz.becker@gmx.at)
 *
 */
public interface TopicExtractionCallback {
	void handleExtractionResult(Set<Topic> extractedTopics);
	void handleExtractionError(ExtractionException e);
}
