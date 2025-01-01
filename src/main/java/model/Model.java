package model;

import java.util.ArrayList;
import java.util.HashMap;

import data.DataLoader;
import probability.ProbabilityTable;

/**
 *  An abstract model class which should be inherited by the HMM class
 * or any other model class that you use for training and decoding.
 * 
 *  Any child class of this class should define the following methods:
 *   	- fit(DataLoader trainLoader)
 * 	 	- decode(DataLoader testLoader)
 * 
 * @author jhjeong
 *
 */
public abstract class Model {
	
	public static final double NEGINF = Double.NEGATIVE_INFINITY;
	public static final double EPSILON = 1e-9;
	public static final int UNKNOWN = DataLoader.UNKNOWN;				// The ID of an unseen word  
	
	public String _probPath;
	
	public Integer START_STATE_ID;				// The ID of the "START" state
	public Integer END_STATE_ID;				// The ID of the "END" state
	
	public int _sizeObsSpace;					// The size of the observation space (the number of words)
	public int _sizeStateSpace;					// The size of the state space (BIO tags + START + END)
	
	public int[] _stateVisitCounts;				// Stores the visit count of each state
	public int[][] _state2StateVisitCounts;		// Stores the count of state transitions
	public int[][] _obsVisitCounts;				// Stores the count of word observations at each state
	
	public ProbabilityTable _transitionProbability;	// The CPT of state transitions
	public ProbabilityTable _emissionProbability;	// The CPT of emission 
		
	public int _numWords;

	public Model() {
	}
	
	/**
	 * 
	 */
	public void initialize() {
		// Get the sizes of the state and observation spaces
		_numWords 		= DataLoader.numWords;
		_sizeObsSpace   = _numWords;
		_sizeStateSpace = State.SIZE;
		
		START_STATE_ID = State.START.getID();
		END_STATE_ID   = State.END.getID();
		
		// Instantiate the arrays of visit counts
		_stateVisitCounts 		= new int[_sizeStateSpace];
		_state2StateVisitCounts = new int[_sizeStateSpace][_sizeStateSpace];
		_obsVisitCounts 		= new int[_sizeStateSpace][_sizeObsSpace];
		
		// Instantiate the ProbabilityTables of transition and emission probabilities
		_emissionProbability 	= new ProbabilityTable(_sizeStateSpace, _sizeObsSpace);
		_transitionProbability  = new ProbabilityTable(_sizeStateSpace, _sizeStateSpace);
	}
	
	/**
	 * Fits an HMM using the train set. 
	 * Transition and emission probabilities should be learned.
	 *  
	 * @param trainLoader	The DataLoader object containing the train dataset
	 */
	abstract void fit(DataLoader trainLoader);
	
	/**
	 * 	Decodes the latent states (i.e. finds out the most likely path of the states) 
	 * given a single document (i.e. a sequence of observations) via the Viterbi algorithm.
	 * 
	 * @param doc	The list of word tokens of a single document
	 * @return		An array of state IDs representing the most likely path of state progression
	 */
	abstract Integer[] decode(ArrayList<String> doc);

	/**
	 * Decodes the BIO labels of all of the documents in the test set.
	 * 
	 * @param testLoader 	A DataLoader object of the test dataset
	 * @return 				A hash map mapping document ID to an array of integers corresponding to 
	 * 				 		the decoded state progression
	 */
	public HashMap<Integer, Integer[]> decodeAllDocs(DataLoader testLoader) {
		int numTestDocs = testLoader._numDocs;
		System.out.println("Decode using Viterbi algorithm");
		System.out.println("Number of documents to decode: " + numTestDocs + "\n");
		
		HashMap<Integer, Integer[]> hmMostLikelyPath = new HashMap<Integer, Integer[]>();
		
		// Decode each document in the test set sequentially
		for (int d = 0; d < numTestDocs; d++) {
			ArrayList<String> doc = testLoader.getDoc(d);
			Integer[] mostLikelyPath = decode(doc);
			hmMostLikelyPath.put(d, mostLikelyPath);
		}
		
		return hmMostLikelyPath;
	}
}
