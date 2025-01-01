package model;

import java.util.ArrayList;

import data.DataLoader;

public class HMM extends Model {

	/**
	 * TODO: Define any necessary variables here
	 */

	public HMM() {
		super();
	}

	/**
	 * TODO: Complete the fit method
	 * Tip: probabilities are computed based on visit counts
	 * 		Update and use _stateVisitCounts, _state2StateVisitCounts, _obsVisitCounts arrays
	 */
	@Override
	public void fit(DataLoader trainLoader) {

		// Initialize the sizes of the visit count arrays and probability tables
		initialize();

		//////////////////////////////////////////////////////////////
		/**
		 * TODO: Update the visit count arrays
		 */
		//////////////////////////////////////////////////////////////

		// update the visit count arrays
		for (int docIndex = 0; docIndex < trainLoader._numDocs; docIndex++) {
			ArrayList<State> states = trainLoader.getStates(docIndex);
			ArrayList<String> words = trainLoader.getDoc(docIndex);
			State previousState = State.START;
			_stateVisitCounts[previousState.getID()]++;

			for (int stateIndex = 0; stateIndex < states.size(); stateIndex++) {
				State currentState = states.get(stateIndex);
				String word = words.get(stateIndex);

				_stateVisitCounts[currentState.getID()]++;
				_obsVisitCounts[currentState.getID()][DataLoader.getIndexOfWord(word)]++;
				_state2StateVisitCounts[previousState.getID()][currentState.getID()]++;
				previousState = currentState;
			}

			// Transition to END state
			_state2StateVisitCounts[previousState.getID()][State.END.getID()]++;
		}

		//////////////////////////////////////////////////////////////
		/**
		 * TODO: Fit the transition CPT
		 * Tip: use _transitionProbability.setValue to set the value of the CPT
		 */
		//////////////////////////////////////////////////////////////

		// fit the transition CPT
		for (int srcStateID = 0; srcStateID < State.values().length; srcStateID++) {
			for (int destStateID = 0; destStateID < State.values().length; destStateID++) {
				double probability = (double) _state2StateVisitCounts[srcStateID][destStateID] / (double) _stateVisitCounts[srcStateID];
				probability = (probability > 0) ? probability : EPSILON;
				_transitionProbability.setValue(srcStateID, destStateID, probability);
			}
		}

		// Handle special cases for transition probabilities
		for (int srcStateID = 0; srcStateID < State.values().length; srcStateID++) {
			_transitionProbability.setValue(srcStateID, State.START.getID(), 0);
			_transitionProbability.setValue(State.END.getID(), srcStateID, 0);
		}
		_transitionProbability.setValue(State.START.getID(), State.END.getID(), 0);

		//////////////////////////////////////////////////////////////
		/**
		 *
		 * TODO: Fit the emission CPT
		 * Tip: use _emissionProbability.setValue to set the value of the CPT
		 */
		//////////////////////////////////////////////////////////////

		for (int stateID = 0; stateID < State.values().length; stateID++) {
			for (int wordID = 0; wordID < DataLoader.numWords; wordID++) {
				double probability = (double) _obsVisitCounts[stateID][wordID] / (double) _stateVisitCounts[stateID];
				probability = (probability > 0) ? probability : EPSILON;

				// START and END states do not emit any observations
				if (stateID == State.START.getID() || stateID == State.END.getID()) {
					probability = 0;
				}

				_emissionProbability.setValue(stateID, wordID, probability);
			}
		}

		// Re-normalize the probabilities to account for numerical errors
		_emissionProbability.normalize();
		_transitionProbability.normalize();
	}

	/**
	 * 	Decodes the latent states (i.e. finds out the most likely path of the states)
	 * given a single document (i.e. a sequence of observations) via the Viterbi algorithm.
	 *
	 * @param doc	The list of word tokens of a single document
	 * @return		An array of state IDs representing the most likely path of state progression
	 */
	@Override
	public Integer[] decode(ArrayList<String> doc) {

		//////////////////////////////////////////////////////////////
		/**
		 *
		 * TODO: Decode the sequence of the most likely state IDs given the list of word tokens
		 * Note: the length of the returned sequence should be 'T + 2' if T is the total number
		 * of word tokens in doc.
		 *
		 */
		//////////////////////////////////////////////////////////////

		// sanity check
		if (doc.isEmpty()) {
			// handle the case when the document is empty
			return new Integer[]{START_STATE_ID, END_STATE_ID};
		}

		int T = doc.size(); // num of time steps (observations)
		int K = _sizeStateSpace; // size of observation space (num of states)
		double[][] delta = new double[T][K]; // viterbi trellis
		int[][] psi = new int[T][K]; // matrix of most likely assignments
		Integer[] mostLikelyPath = new Integer[T + 2]; // vector for the most likely path

		// initialization
		for (int i = 0; i < K; i++) {
			int ot = DataLoader.getIndexOfWord(doc.get(0)); // observation at time t=1
			double eProbability = ot != DataLoader.UNKNOWN ? _emissionProbability.getProbability(i, ot) : EPSILON; // emission probability
			delta[0][i] = _transitionProbability.getProbability(START_STATE_ID, i) * eProbability;
			psi[0][i] = START_STATE_ID;
		}

		// recursion
		for (int t = 1; t < T; t++) {
			for (int j = 0; j < K; j++) {
				delta[t][j] = Double.NEGATIVE_INFINITY;
				psi[t][j] = -1;
				int ot = DataLoader.getIndexOfWord(doc.get(t)); // observation at time t
				double eProbability = ot != DataLoader.UNKNOWN ? _emissionProbability.getProbability(j, ot) : EPSILON; // emission probability
				for (int i = 0; i < K; i++) {
					double score = delta[t - 1][i] * _transitionProbability.getProbability(i, j);
					if (score * eProbability > delta[t][j]) {
						delta[t][j] = score * eProbability;
						psi[t][j] = i;
					}
				}
			}
		}

		// termination and path backtracking
		double maxProb = Double.NEGATIVE_INFINITY;
		int bestLastState = -1;

		// find the state with the highest probability at time T
		for (int s = 0; s < K; s++) {
			if (delta[T - 1][s] > maxProb) {
				maxProb = delta[T - 1][s];
				bestLastState = s;
			}
		}

		// backtrack to retrieve the most likely sequence of states
		mostLikelyPath[T + 1] = END_STATE_ID; // set END state
		int currentState = bestLastState;
		for (int t = T; t >= 1; t--) {
			mostLikelyPath[t] = currentState;
			currentState = psi[t - 1][currentState];
		}
		mostLikelyPath[0] = START_STATE_ID; // set START state

		return mostLikelyPath;
	}
}