import model.EnhancedHMM;
import org.junit.BeforeClass;
import org.junit.Test;

import data.DataLoader;
import model.HMM;
import model.State;
import probability.ProbabilityTable;

import java.util.HashMap;

import static org.junit.Assert.assertEquals;

public class Tests {
	
	static HMM model;
	static DataLoader trainLoader;
	static DataLoader testLoader;
	static String trainDataPath = "data/trivia10k13train.bio";
	static String testDataPath = "data/trivia10k13test.bio";
	static HashMap<String, Integer> hmName2ID = new HashMap<String, Integer>();
	static HashMap<Integer, String> hmID2Name = new HashMap<Integer, String>();
	
	@BeforeClass
	public static void init() {
		trainLoader = new DataLoader(trainDataPath);
		testLoader = new DataLoader(testDataPath, false);
		
		model = new HMM();
	}
		
	@Test
	public void testFitting() {
		model.fit(trainLoader);
		
		ProbabilityTable transitionProbability = model._transitionProbability;
		ProbabilityTable emissionProbability = model._emissionProbability;
		
		Integer START_STATE_ID = model.START_STATE_ID;
		Integer END_STATE_ID = model.END_STATE_ID;
		double[] startTransProb = new double[model._sizeStateSpace];
		double[] endTransProb = new double[model._sizeStateSpace];
		double[][] emissionProb = new double[model._sizeStateSpace][30];
		
		for (int i = 0; i < model._sizeStateSpace; i++) {
			startTransProb[i] = transitionProbability.getProbability(START_STATE_ID, i);
			endTransProb[i] = transitionProbability.getProbability(i, END_STATE_ID);
			for (int j = 0; j < 30; j++) {
				emissionProb[i][j] = emissionProbability.getProbability(i, j);
			}
		}
		
		double[] expectedStartTransProb = new double[] {
				0.0, 0.06230808516747698, 9.999999870000002E-10, 0.030578300523669402, 9.999999870000002E-10, 
				0.003326509680399182, 9.999999870000002E-10, 3.838280400460594E-4, 9.999999870000002E-10, 
				0.006141248640736951, 0.005885363280706245, 0.0029426816403531225, 9.999999870000002E-10, 
				0.013306038721596728, 9.999999870000002E-10, 9.999999870000002E-10, 9.999999870000002E-10, 
				9.999999870000002E-10, 9.999999870000002E-10, 0.001663254840199591, 9.999999870000002E-10, 
				0.003966223080475948, 9.999999870000002E-10, 0.001791197520214944, 9.999999870000002E-10, 
				0.867707255864125, 0.0
		};
		double[] expectedEndTransProb = new double[] {
				0.0, 0.001397205560878245, 0.10129063736644336, 0.004329004259740261, 0.07908931265486985, 
				0.013580246723456799, 0.07606678913543603, 0.012944983533980594, 0.1126564657385258, 
				0.013693560133974839, 0.028073285630910175, 0.0025673940436457014, 0.07724550790059884, 
				0.00335758248684947, 0.08832425759830612, 0.040297853137100305, 0.056410255338461536, 
				9.999999760000008E-10, 0.19620252732911406, 0.01896551693793104, 0.05721392949004978,
				0.09658536382536591, 0.22499999595000003, 0.01587301552380953, 0.09914320477233786, 
				0.01790857836994364, 0.0
		};
		double[][] expectedEmissionProb = new double[][] {
			{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
			 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0},
			{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
			 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0},
			{0.0145707078117367, 9.999896731068612E-10, 9.999896731068612E-10, 5.987962114412343E-4, 
			 9.999896731068612E-10, 9.999896731068612E-10, 9.999896731068612E-10, 9.999896731068612E-10,
			 1.9959873714707807E-4, 9.999896731068612E-10, 9.999896731068612E-10, 9.999896731068612E-10,
			 9.999896731068612E-10, 9.999896731068612E-10, 9.999896731068612E-10, 9.999896731068612E-10, 
			 9.999896731068612E-10, 3.9919747429415615E-4, 9.999896731068612E-10, 9.999896731068612E-10, 
			 0.0013971911600295466, 9.999896731068612E-10, 9.999896731068612E-10, 9.999896731068612E-10, 
			 9.999896731068612E-10, 9.999896731068612E-10, 9.999896731068612E-10, 9.999896731068612E-10, 
			 9.999896731068612E-10, 9.999896731068612E-10},
			{3.267408351883009E-4, 0.0016337041759415046, 9.99990326093795E-10, 3.267408351883009E-4, 
			 9.99990326093795E-10, 9.99990326093795E-10, 0.0022871858463181065, 9.99990326093795E-10, 
			 9.99990326093795E-10, 9.99990326093795E-10, 1.6337041759415045E-4, 9.99990326093795E-10,
			 9.99990326093795E-10, 9.99990326093795E-10, 9.99990326093795E-10, 9.99990326093795E-10,
			 9.99990326093795E-10, 1.6337041759415045E-4, 6.534816703766018E-4, 0.0019604450111298053,
			 6.534816703766018E-4, 3.267408351883009E-4, 9.99990326093795E-10, 9.99990326093795E-10,
			 9.99990326093795E-10, 9.99990326093795E-10, 9.99990326093795E-10, 9.99990326093795E-10, 
			 1.6337041759415045E-4, 9.99990326093795E-10}
		};
		
		for (int i = 0; i < model._sizeStateSpace; i++) {
			assertEquals(String.format("Incorrect probability from START to %s: ", hmID2Name.get(i)), 
					expectedStartTransProb[i], startTransProb[i], 1e-5);
			assertEquals(String.format("Incorrect probability from %s to END: ", hmID2Name.get(i)),
					expectedEndTransProb[i], endTransProb[i], 1e-5);
		}
		
		for (int j = 0; j < expectedEmissionProb[0].length; j++) {
			assertEquals(String.format("Incorrect emission probability of %s from START: ", DataLoader.getWordFromIndex(j)),
					expectedEmissionProb[0][j], emissionProb[START_STATE_ID][j], 1e-5);
			assertEquals(String.format("Incorrect emission probability of %s from END: ", DataLoader.getWordFromIndex(j)),
					expectedEmissionProb[1][j], emissionProb[END_STATE_ID][j], 1e-5);
			assertEquals(String.format("Incorrect emission probability of %s from %s: ", DataLoader.getWordFromIndex(j), "B-Actor"),
					expectedEmissionProb[2][j], emissionProb[State.B_Actor.getID()][j], 1e-5);
			assertEquals(String.format("Incorrect emission probability of %s from %s: ", DataLoader.getWordFromIndex(j), "I-Actor"),
					expectedEmissionProb[3][j], emissionProb[State.I_Actor.getID()][j], 1e-5);
		}
	}
	
	@Test
	public void testViterbi() {
		HashMap<Integer, Integer[]> decodedPath = model.decodeAllDocs(testLoader);
		for (int i = 0; i < 10; i++) {
			String res = "";
			for (int j = 0; j < decodedPath.get(i).length; j++) {
				res += String.format("%d, ", decodedPath.get(i)[j]);
			}
			System.out.println(res.substring(0, res.length() - 2));
		}
		
		Integer[][] expectedPath = new Integer[][] {
			{0, 25, 25, 25, 25, 25, 3, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 26},
			{0, 25, 25, 10, 15, 15, 25, 3, 4, 4, 25, 25, 25, 25, 13, 14, 25, 13, 14, 26},
			{0, 25, 10, 5, 11, 12, 12, 12, 12, 12, 25, 25, 25, 9, 25, 25, 25, 25, 25, 25, 25, 25, 25, 26},
			{0, 25, 25, 25, 25, 25, 1, 2, 25, 9, 25, 3, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 26},
			{0, 25, 25, 25, 1, 2, 25, 3, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 26},
			{0, 25, 5, 10, 15, 15, 25, 25, 25, 1, 2, 25, 1, 2, 25, 3, 4, 4, 4, 4, 26},
			{0, 11, 12, 12, 12, 25, 1, 2, 25, 25, 25, 1, 2, 25, 7, 8, 8, 8, 8, 26},
			{0, 25, 25, 3, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 26},
			{0, 25, 9, 25, 25, 25, 13, 14, 25, 25, 25, 25, 13, 14, 25, 3, 4, 4, 4, 4, 26},
			{0, 25, 25, 25, 3, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 26}
		};
		
		for (int d = 0; d < 10; d++) {
			assertEquals(String.format("Incorrect decoded sequence length... Should be %s not %s", expectedPath[d].length, decodedPath.get(d).length), expectedPath[d].length, decodedPath.get(d).length, 1e-1);
			for (int i = 0; i < expectedPath[d].length; i++) {
				assertEquals("Incorrect decode", expectedPath[d][i], decodedPath.get(d)[i], 1e-1);
			}
		}
		System.out.println();
	}
}
