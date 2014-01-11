/**
 * 
 */
package aic2013.extractor;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import twitter4j.Status;
import aic2013.extractor.entities.Topic;
import cc.mallet.classify.tui.Text2Vectors;
import cc.mallet.examples.TopicModel;
import cc.mallet.topics.ParallelTopicModel;
import cc.mallet.topics.tui.Vectors2Topics;

import com.mongodb.MongoClient;

/**
 * @author Moritz Becker (moritz.becker@gmx.at)
 * 
 */
public class TopicExtractorTest {
	private static Logger logger = Logger.getLogger(TopicExtractorTest.class
			.getSimpleName());
	private static final CharsetEncoder asciiEncoder = Charset.forName(
			"US-ASCII").newEncoder();

	/**
	 * This example program extracts tweets from the complete set of statuses for a specified user
	 * (specified by providing the userId).
	 * It was created to compare topic modeling performance when regarding only separate statuses
	 * vs all statuses of a user.
	 * @param args
	 */
	public static void main(String[] args) {
		BufferedReader topicInputStream = null;
		try {
			long userId = 1322920717;
			MongoClient mongoClient = new MongoClient("localhost");
			final MongoDataAccess mongoDataAccess = new MongoDataAccess(
					mongoClient);

			final Path vectorsFile = Files.createTempFile(
					"uid" + Long.toString(userId), ".mallet");
			final Path rawTweetsDir = Files.createTempDirectory("uid"
					+ Long.toString(userId) + "_raw");
			final Path topicKeysFile = Files.createTempFile("uid"
					+ Long.toString(userId) + "_topicKeys", ".txt");
			final Path topicsFile = Files.createTempFile("uid"
					+ Long.toString(userId) + "_topics", ".txt");
			
			//for debug
//			final Path vectorsFile = Paths.get("C:\\Users\\Mo\\AppData\\Local\\Temp", "test.mallet");
//			final Path rawTweetsDir = Paths.get("C:\\Users\\Mo\\AppData\\Local\\Temp", "test_raw");
//			final Path topicKeysFile = Paths.get("C:\\Users\\Mo\\AppData\\Local\\Temp","test_topicKeys.txt");
//			final Path topicsFile = Paths.get("C:\\Users\\Mo\\AppData\\Local\\Temp","test_topics.txt");
			
			// final vectorsFile = Files.createTempFile("tweetVectors",
			// ".mallet").toFile();
			// final Path dirPath = Paths.get(tmpdirPrefix,
			// Long.toString(userId));
			deleteDirectoryIfExists(rawTweetsDir);

			Files.createDirectory(rawTweetsDir);
			
			// write tweets to files
			mongoDataAccess.forAllByUserId(userId, new Processor<Status>() {
				private final TextFilter filter = new PrefixFilter("#", new PrefixFilter("@", new PrefixFilter("http", new GlobalPrefixFilter("RT", false, new BaseFilter()))));
				@Override
				public void process(Status status) {
					FileOutputStream fos = null;
					try {
						if (!asciiEncoder.canEncode(status.getText())) {
							logger.log(Level.WARNING,
									"Non-ASCII tweet encountered (id = "
											+ status.getId() + ")");
							return;
						}
						fos = new FileOutputStream(Paths.get(
								rawTweetsDir.toString(),
								Long.toString(status.getId())).toString());
						
						/* remove @ and hashtags from status */
						String filteredTweetText = filter.filter(status.getText());
						System.out.println(filteredTweetText);
						
						
						fos.write(filteredTweetText.getBytes(
								Charset.forName("US-ASCII")));
					} catch (FileNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} finally {
						if (fos != null) {
							try {
								fos.close();
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					}

				}
			});
			

			Text2Vectors t2v = new Text2Vectors();

			// form string array args and invoke main of Text2Vectors.
			String[] argsT2v = { "--remove-stopwords", "true",
					"--preserve-case", "false", "--input",
					rawTweetsDir.toString(), "--output",
					vectorsFile.toString(), "--keep-sequence" };
			Text2Vectors.main(argsT2v);

			Vectors2Topics v2t = new Vectors2Topics();

			// form string array args and invoke main of Vectors2Topics.
			String[] argsV2t = {
					"--num-iterations", "200",
					"--num-top-words", "3",
					"--optimize-interval", "10",
					"--doc-topics-threshold", "0.26",
					"--input", vectorsFile.toString(), "--num-topics", "2",
					// "--output-state", <output directory
					// path>+"/output_state.gz",
					"--output-topic-keys", topicKeysFile.toString(),
			// path>+"/output_topic_keys",
					"--output-doc-topics", topicsFile.toString()
			};

			try {
				Vectors2Topics.main(argsV2t);
			} catch (IllegalArgumentException e) {
				System.err.println("rawTweetDir = " + rawTweetsDir.toString());
				System.err.println("vectorsFile = " + vectorsFile.toString());
				System.err.println("topicsFile = " + topicKeysFile.toString());
				e.printStackTrace();
			}

			System.out.println("rawTweetDir = " + rawTweetsDir.toString());
			System.out.println("vectorsFile = " + vectorsFile.toString());
			System.out.println("topicKeys = " + topicKeysFile.toString());
			System.out.println("topics = " + topicsFile.toString());
			
			// topicInputStream = new BufferedReader(new InputStreamReader(
			// new FileInputStream(topicsFile.toFile())));
			// String line;
			// Set<Topic> extractedTopics = new HashSet<>();
			// while ((line = topicInputStream.readLine()) != null) {
			// System.out.println(line);
			// String[] parts = line.split("\t");
			// if(parts.length != 3)
			// break;
			// for(String topicString : parts[2].split(" ")){
			// extractedTopics.add(new Topic(topicString));
			// }
			// }
			//
			//
			// return extractedTopics;

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (topicInputStream != null) {
				try {
					topicInputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private static void deleteDirectoryIfExists(Path dir) throws IOException {
		if (!Files.exists(dir))
			return;
		Files.walkFileTree(dir, new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult visitFile(Path file,
					BasicFileAttributes attrs) throws IOException {
				Files.delete(file);
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult visitFileFailed(Path file, IOException exc)
					throws IOException {
				// try to delete the file anyway, even if its attributes
				// could not be read, since delete-only access is
				// theoretically possible
				Files.delete(file);
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult postVisitDirectory(Path dir, IOException exc)
					throws IOException {
				if (exc == null) {
					Files.delete(dir);
					return FileVisitResult.CONTINUE;
				} else {
					// directory iteration failed; propagate exception
					throw exc;
				}
			}
		});
	}
}
