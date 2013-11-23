package aic2013.extractor;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import cc.mallet.classify.tui.Text2Vectors;
import cc.mallet.topics.tui.Vectors2Topics;
import twitter4j.Status;
import aic2013.extractor.entities.Topic;

/**
 * @author Moritz Becker (moritz.becker@gmx.at)
 *
 */
public class TopicExtractorImpl implements TopicExtractor {

	private final File rawTweetFile;
	private final Path rawTweetDir;
	private final File vectorsFile;
	private final File topicsFile;

	public TopicExtractorImpl() throws ExtractionException {
		try {
			rawTweetDir = Files.createTempDirectory("rawTweetDir");
			rawTweetFile = Files.createTempFile(rawTweetDir, "rawTweet", ".txt").toFile();
			rawTweetFile.deleteOnExit();
			vectorsFile = Files.createTempFile("tweetVectors", ".mallet").toFile();
			vectorsFile.deleteOnExit();
			topicsFile = Files.createTempFile("tweetTopics", ".txt").toFile();
			topicsFile.deleteOnExit();
		} catch (IOException e) {
			throw new ExtractionException(e);
		}
	}

	@Override
	public Set<Topic> extract(String input) throws ExtractionException {
		PrintStream rawTweetOutStream = null;
//		FileOutputStream rawTweetOutStream = null;
		BufferedReader topicInputStream = null;
		try {
			/* write input to temporary file */
			rawTweetOutStream = new PrintStream(new FileOutputStream(rawTweetFile));
			rawTweetOutStream.print(input);
			rawTweetOutStream.close();
			rawTweetOutStream = null;
//			rawTweetOutStream = new FileOutputStream(rawTweetFile);
//			rawTweetOutStream.write(input.getBytes());
//			rawTweetOutStream.close();
//			rawTweetOutStream = null;

			Text2Vectors t2v = new Text2Vectors();

			// form string array args and invoke main of Text2Vectors.
			String[] argsT2v = { "--remove-stopwords", "true",
					"--preserve-case", "false", "--input",
					rawTweetDir.toString(), "--output", vectorsFile.getPath(),
					"--keep-sequence" };
			Text2Vectors.main(argsT2v);

			Vectors2Topics v2t = new Vectors2Topics();

			// form string array args and invoke main of Vectors2Topics.
			String[] argsV2t = { "--num-iterations", "200", "--num-top-words",
					"3", "--doc-topics-threshold", "0.26", "--input",
					vectorsFile.getPath(), "--num-topics", "1",
					// "--output-state", <output directory
					// path>+"/output_state.gz",
					 "--output-topic-keys", topicsFile.getPath()
					// path>+"/output_topic_keys",
//					"--output-doc-topics", topicsFile.getPath()
					};
			
			try{
				Vectors2Topics.main(argsV2t);
			}catch(IllegalArgumentException e){
				System.err.println("Tweet: " + input);
				System.err.println("rawTweetFile = " + rawTweetFile.getPath());
				System.err.println("vectorsFile = " + vectorsFile.getPath());
				System.err.println("topicsFile = " + topicsFile.getPath());
				e.printStackTrace();
			}

			topicInputStream = new BufferedReader(new InputStreamReader(
					new FileInputStream(topicsFile)));
			String line;
			Set<Topic> extractedTopics = new HashSet<>();
			while ((line = topicInputStream.readLine()) != null) {
				System.out.println(line);
				String[] parts  = line.split("\t");
				if(parts.length != 3)
					break;
				for(String topicString : parts[2].split(" ")){
					extractedTopics.add(new Topic(topicString));
				}
			}
			return extractedTopics;

		} catch (IOException e) {
			throw new ExtractionException(e);
		} finally {
			try {
				if (rawTweetOutStream != null) {
					rawTweetOutStream.close();
				}
				if (topicInputStream != null) {
					topicInputStream.close();
				}
			} catch (IOException e) {
				throw new ExtractionException(e);
			}
		}
	}

	@Override
	public Set<Topic> extract(Status status) throws ExtractionException {
		return extract(status.getText());
	}

}
