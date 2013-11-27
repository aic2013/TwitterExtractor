package aic2013.extractor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import twitter4j.Status;
import aic2013.extractor.entities.Topic;

/**
 * @author Moritz Becker (moritz.becker@gmx.at)
 *
 */
public class TopicExtractionCoordinatorImpl implements TopicExtractionCoordinator {
	private ConcurrentHashMap<Long, TopicExtractor> extractors = new ConcurrentHashMap<>();
	private ExecutorService executorService;
	
	public TopicExtractionCoordinatorImpl() {
		/* For the time being, only use 1 thread here since multiple thread cause neo4j deadlocks
		 * and mallat api issues (don't understand why).
		 * However, using only 1 thread might not be a problem since the bottleneck might be on the
		 * neo4j query side.
		 */
		executorService = Executors.newSingleThreadExecutor();//newFixedThreadPool(1);
	}
	@Override
	public void doExtraction(final String input, final TopicExtractionCallback callback) {
				Set<Topic> extractedTopics = null;
				try {
					extractedTopics = getExtractor().extract(input);
				} catch (ExtractionException e) {
					callback.handleExtractionError(e);
				}
				callback.handleExtractionResult(extractedTopics);
//		executorService.execute(new Runnable(){
//			@Override
//			public void run() {
//				Set<Topic> extractedTopics = null;
//				try {
//					extractedTopics = getExtractor().extract(input);
//				} catch (ExtractionException e) {
//					callback.handleExtractionError(e);
//				}
//				callback.handleExtractionResult(extractedTopics);
//			}
//		});
	}
	
	private TopicExtractor getExtractor() throws ExtractionException{
		if(extractors.contains(Thread.currentThread().getId())){
			return extractors.get(Thread.currentThread().getId());
		}
		TopicExtractor extractor = new TopicExtractorImpl();
		extractors.put(Thread.currentThread().getId(), extractor);

		return extractor;
	}
	
	@Override
	public void awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
		executorService.shutdown();
		executorService.awaitTermination(timeout, unit);
	}
	@Override
	public void doExtraction(Status status, TopicExtractionCallback callback) {
		doExtraction(status.getText(), callback);
	}
}
