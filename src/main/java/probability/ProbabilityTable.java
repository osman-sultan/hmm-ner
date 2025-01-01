package probability;

/**
 *  A generic class that represents a conditional probability table (CPT).
 * The table is a matrix of size (_size[0], _size[1]), which is stored in 
 * the member attribute _probTable as a 2D array. 
 *  
 *  The rows of the table represent the values of the conditioning variable. 
 * That is, P( ns=j | s=i ) will be stored in _probTable[i][j]; likewise, 
 * P( obs=k | s=i ) will be stored in _probTable[i][k], where i, j, k are indices.
 * 
 *  You can set the values of the table from other modules only by calling 
 * setProbability(id1, id2, val) method because _probTable is a private variable.
 * As such, getting the value from the probability table can be done via calling
 * getProbability(id1, id2).  
 * 
 *  When setting a value to the table or getting a value from the table, the method
 * checkIndices is called to check whether the query is valid.  
 * Specifically, it is impossible to retrieve a probability value when they are not
 * yet normalized.
 * 
 *  You can also directly get the log probability value by calling the 
 * getLogProbability(id1, id2) method. 
 *
 * @author jhjeong
 *
 */
public class ProbabilityTable {
	
	public int[] _size 				= new int[2];
	public boolean _normalized;
	private double[][] _probTable;
	
	/**
	 * 
	 * @param size1		The cardinality of the conditioning variable
	 * @param size2		The cardinality of the variable to query
	 */
	public ProbabilityTable(int size1, int size2) {
		_size[0] = size1;
		_size[1] = size2;
		_probTable = new double[size1][size2];
		_normalized = false;
	}
	
	/**
	 * Sets the value of the CPT at (index1, index2) after checking their validity
	 *  
	 * @param index1
	 * @param index2
	 * @param value
	 */
	public void setValue(int index1, int index2, double value) {
		checkIndices(index1, index2, false);
		
		_probTable[index1][index2] = value;		
	}
	
	/**
	 * Gets the value of the CPT at given indices after checking their validity
	 *  
	 * @param index1
	 * @param index2
	 */
	public double getProbability(int index1, int index2) {
		checkIndices(index1, index2, true);
	
		return _probTable[index1][index2];
	}
	
	/**
	 * Checks the validity of the given pair of indices for the following conditions:
	 * 	- Whether the indices fall within the sizes of the CPT
	 *  - Whether the CPT is normalized if you are querying a value from the CPT
	 *  	 
	 * @param index1
	 * @param index2
	 * @param isGet
	 */
	public void checkIndices(int index1, int index2, boolean isGet) {
		if (index1 >= _size[0] | index2 >= _size[1]) {
			System.out.println("Invalid indices (" + index1 + "," + index2 + ")" +
							   " for an array of size (" + _size[0] + "," + _size[1] + ")");
			System.exit(1);
		}
		
		if (isGet && !_normalized) {
			System.out.println("Attempted to query a value from an unnormalized probability table!");
			System.exit(1);
		}
	}
	
	/**
	 *  Gets the log value of the CPT at given indices after checking their validity.
	 * If the probability value is 0, this method returns Double.NEGATIVE_INFINITY and
	 * spits out a warning.  
	 * 
	 * @param index1
	 * @param index2
	 * @return			The log probability at (index1, index2)
	 */
	public double getLogProbability(int index1, int index2) {
		checkIndices(index1, index2, false);
		double prob = _probTable[index1][index2];
		
		if (prob == 0) {
			System.out.println("WARNING: Evaluating log on 0 probability");
			return (double)Double.NEGATIVE_INFINITY;
		}	
		return Math.log(prob);
	}
	
	/**
	 * Normalizes the probability for each row of the CPT
	 */
	public void normalize() {
		for (int i = 0; i < _size[0]; i++) {

			// Compute the sum of values
			double sumProb = 0;
			for (int j = 0; j < _size[1]; j++) {
				sumProb += _probTable[i][j];
			}
			
			// Normalize each element by dividing by the sum (only if the sum is positive)
			if (sumProb > 0) {
				for (int j = 0; j < _size[1]; j++) {
					_probTable[i][j] /= sumProb;
				}
			}
		}
		_normalized = true;
	}
}
