package data;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class DataSplitter {
  private static final String TRAIN_DATA_PATH = "data/trivia10k13train.bio";
  private static final String TEST_DATA_PATH = "data/trivia10k13test.bio";
  private static final String NEW_TRAIN_DATA_PATH = "data/new_train.bio";
  private static final String NEW_TEST_DATA_PATH = "data/new_test.bio";
  private static final long RANDOM_SEED = 369; // fixed random seed

  public static void main(String[] args) throws IOException {
    // read the provided train and test data
    List<String> trainData = Files.readAllLines(Paths.get(TRAIN_DATA_PATH));
    List<String> testData = Files.readAllLines(Paths.get(TEST_DATA_PATH));

    // combine the provided train and test data
    List<String> combinedData = new ArrayList<>(trainData);
    combinedData.addAll(testData);

    // shuffle the combined data
    Collections.shuffle(combinedData, new Random(RANDOM_SEED));

    // split data into new train and test splits
    int trainSize = trainData.size();
    int testSize = testData.size();
    List<String> newTrainData = combinedData.subList(0, trainSize);
    List<String> newTestData = combinedData.subList(trainSize, trainSize + testSize);

    // write data to files
    Files.write(Paths.get(NEW_TRAIN_DATA_PATH), newTrainData);
    Files.write(Paths.get(NEW_TEST_DATA_PATH), newTestData);

    System.out.println("New train and test data generated successfully.");
  }
}