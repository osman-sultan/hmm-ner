package util;

import java.io.PrintStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import data.DataLoader;
import model.State;

public final class Util {
	
	public final static DecimalFormat DF_4F = new DecimalFormat("#0.0000");	
	public final static DecimalFormat DF_4INT = new DecimalFormat("#0000");
	
	
	/**
	 * Computes the test performance of the given decode.
	 * Since the label is not binary, we can first compute the accuracy for the overall performance.  
	 *  - accuracy = (# correctly labeled tokens) / (# all predictions)
	 * 
	 *  Since there are many "O" tokens, high accuracy can be attributed to many tokens identified
	 * as being "O". Instead, what we want more is to retrieve named entities. Hence, to better account 
	 * for the model performance across different entities, we compute the accuracy, precision, recall, and F1 score 
	 * per each entity.
	 * 
	 *  - accuracy(entity_i) = (# correct label entity_i) / (# all predictions) 
	 *  - precision(entity_i) = (# correctly retrieved entity_i) / (# tokens labeled as entity_i)
	 *  - recall(entity_i) = (# correctly retrieved entity_i) / (# total occurrences of entity_i)
	 *  - F1(entity_i) = (2 * precision_i * recall_i) / (precision_i + recall_i)
	 * 
	 *  Note that we do not differentiate B-Entity and I-Entity because both of them correctly label
	 * the given entity. 
	 *  
	 * @param model			The HMM model object
	 * @param hmDecodedPath	The mapping from the document ID to the decoded state transition path
	 * @param testLoader	The DataLoader object storing the test data
	 * @param ps			(Optional) the print stream
	 * @return				Returns the overall F1 score
	 */
	public static double computePerformance(
			HashMap<Integer, Integer[]> hmDecodedPath, DataLoader testLoader, PrintStream ps) {
		
		int numTestDocs = testLoader._numDocs;
		int numEntities = DataLoader.NUM_ENTITIES;
		int numTotalLabels = 0;
		
		int correctDecode = 0;											// Total number of correctly decoded tokens
		int[] numEntityDecoded = new int[numEntities];					// Number of tokens decoded as entity 
		int[] entityCounts = new int[numEntities];						// Number of entity tokens in the test set (B-, I-) 
		int[] truePositiveEntity = new int[numEntities];				// Number of tokens "correctly" decoded as entity
		int[] correctDecodeEntity = new int[numEntities];				// Number of correct decode (either correct entity or O)
		
		double avgAccuracy = 0, avgPrecision = 0, avgRecall = 0, avgF1 = 0;
		double[] avgAccuracyEntity = new double[numEntities];
		double[] avgPrecisionEntity = new double[numEntities];
		double[] avgRecallEntity = new double[numEntities];
		double[] avgF1Entity = new double[numEntities];
		
		// Count the total number of labels and entities tokens in the test set
		for (int d = 0; d < numTestDocs; d++) {
			numTotalLabels += testLoader.getStates(d).size();
			for (int e = 0; e < numEntities; e++) {
				entityCounts[e] += testLoader._aEntityCounts[d][e];
			}
		}
		
		
		for (int d = 0; d < numTestDocs; d++) {
			// Get the true state labels
			ArrayList<State> alStates = testLoader.getStates(d);
			
			// Get the decoded path of state IDs (decodedPath[0] is always START_STATE_ID)
			Integer[] decodedPath = Arrays.copyOfRange(hmDecodedPath.get(d), 1, hmDecodedPath.get(d).length);
			
			int lenDoc = alStates.size();
			for (int t = 0; t < lenDoc; t++) {
				State trueState = alStates.get(t);
				State decodedState = State.getStateFromID(decodedPath[t]);	
				String decodedEntity = ""; 
				String entity = "";
				int entityId = -1, decodedEntityId = -1;
			
				if (trueState != State.O) {
					String[] aLabel = trueState.toString().split("_");
					entity = String.join("_", Arrays.copyOfRange(aLabel, 1, aLabel.length));
					entityId = DataLoader.hmEntityName2Id.get(entity);
				}
				
				if (decodedState != State.O) {
					String[] aDecoded = decodedState.toString().split("_");
					decodedEntity = String.join("_", Arrays.copyOfRange(aDecoded, 1, aDecoded.length));
					decodedEntityId = DataLoader.hmEntityName2Id.get(decodedEntity);
					numEntityDecoded[decodedEntityId]++;
				}			
				
				// Both labeled as "O"
				if (entityId == -1 && decodedEntityId == -1) {
					correctDecode++;
					for (int e = 0; e < numEntities; e++) {
						correctDecodeEntity[e]++;
					}
					continue;
				}
				
				// When the correct label is "O" while decoded label is not "O" 
				if (entityId == -1 && decodedEntityId != -1) {
					// For other entities than the decoded entity, this is a correct decode as "O"
					for (int e = 0; e < numEntities; e++) {
						if (e != decodedEntityId) {
							correctDecodeEntity[e]++;
						}
					}
				}
				
				// If the label is not "O" and decode is "O"
				// For other entities than the true entity, it's a correct decode
				else if (entityId != -1 && decodedEntityId == -1) {
				
					for (int e = 0; e < numEntities; e++) {
						if (e != entityId) {
							correctDecodeEntity[e]++;
						}
					}
				}
				
				// Correct retrieval of an entity (correct for all the other entities as well)
				else if (entityId == decodedEntityId) {
					correctDecode++;
					for (int e = 0; e < numEntities; e++) {
						correctDecodeEntity[e]++;
					}
					truePositiveEntity[entityId]++;
				}
				
				// Entity of the label != entity of the decode (correct prediction for other entities)
				else if (entityId != decodedEntityId) {
					for (int e = 0; e < numEntities; e++) {
						if (e != entityId && e != decodedEntityId) {
							correctDecodeEntity[e]++;
						}
					}
				}
			}
		}
		
		// Compute the recall and precision per each entity given the counts 
		// Note: these metrics are only computed when there are positive number of a given entity
		for (int e = 0; e < numEntities; e++) {
			avgAccuracyEntity[e] = (double)correctDecodeEntity[e] / numTotalLabels;
			if (numEntityDecoded[e] > 0)
				avgPrecisionEntity[e] = (double)truePositiveEntity[e] / numEntityDecoded[e];
			if (entityCounts[e] > 0)
				avgRecallEntity[e] = (double)truePositiveEntity[e] / entityCounts[e];
			if (avgPrecisionEntity[e] > 0 && avgRecallEntity[e] > 0)
				avgF1Entity[e] = (2 * avgPrecisionEntity[e] * avgRecallEntity[e]) / 
				 				  (avgPrecisionEntity[e] + avgRecallEntity[e]);
		}
		
		// Compute the average metrics for all entities
		avgAccuracy = (double)correctDecode / numTotalLabels;
		int numPositives = 0;
		int numTruePositives = 0;
		int numTotalEntities = 0;
		for (int e = 0; e < numEntities; e++) {
			numPositives += numEntityDecoded[e];
			numTruePositives += truePositiveEntity[e];
			numTotalEntities += entityCounts[e];
		}
		avgPrecision = (double)numTruePositives / numPositives;
		avgRecall = (double)numTruePositives / numTotalEntities;
		avgF1 = (2 * avgPrecision * avgRecall) / (avgPrecision + avgRecall);
		
		// Print out the results
		if (ps == null)
			ps = System.out;
		
		DecimalFormat df = DF_4F;
		ps.println();
		ps.println("Performance report:\n");
		ps.println(" Overall accuracy: " + df.format(avgAccuracy) + 
				   "\t Total word counts: " + testLoader._numWordCount);
		ps.println(" F1 performance: " + df.format(avgF1));
		ps.println();
		ps.println(String.format("%-30s", "Per entity performance:") + 
				   String.format("%-10s", "Accuracy") +	String.format("%-10s", "Precision") +
				   String.format("%-10s", "Recall") + String.format("%-10s", "F1") + 
				   String.format("%-10s", "# Entity") + String.format("%-7s", "TP") +
				   String.format("%-12s", "# Positive"));

		String resultEntity;
		for (int e = 0; e < numEntities; e++) {
			resultEntity  = "";
			resultEntity  = String.format("%-30s", "[" + DataLoader.hmId2EntityName.get(e) + "]");
			resultEntity += String.format("%-10s", df.format(avgAccuracyEntity[e]));
			resultEntity += String.format("%-10s", df.format(avgPrecisionEntity[e]));
			resultEntity += String.format("%-10s", df.format(avgRecallEntity[e]));
			resultEntity += String.format("%-10s", df.format(avgF1Entity[e]));
			resultEntity += String.format("%-10d", entityCounts[e]);
			resultEntity += String.format("%-7d", truePositiveEntity[e]);
			resultEntity += String.format("%-12d", numEntityDecoded[e]);
			ps.println(resultEntity);
		}
		ps.println(String.format("%-30s", "Average over entities") + 
				   String.format("%-10s", "") + 
				   String.format("%-10s", df.format(avgPrecision)) +
				   String.format("%-10s", df.format(avgRecall)) + 
				   String.format("%-10s", df.format(avgF1)));
		
		return avgF1;
	}
}
