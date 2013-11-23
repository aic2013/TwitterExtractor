package aic2013.extractor;

import java.util.List;
import java.util.Set;

import twitter4j.Status;
import aic2013.extractor.entities.Topic;

/**
 * @author Moritz Becker (moritz.becker@gmx.at)
 *
 */
public interface TopicExtractor {
	Set<Topic> extract(String input) throws ExtractionException;
	Set<Topic> extract(Status status) throws ExtractionException;
}
