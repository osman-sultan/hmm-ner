package data;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import model.State;

/**
 * A class which loads the BIO tagging dataset and stores tokens and their corresponding BIO labels 
 *  in separate lists.
 *  
 * This class has to be instantiated separately for the train set and the test set.
 * When you load the test data, make sure to pass in 'isTrain=false' in the constructor.
 * 
 * @author jhjeong
 *
 */
public class DataLoader {
	
	public static final int UNKNOWN = -1; 						// ID for an unknown word token in the test set
	public static final int NUM_ENTITIES = 12;					// Total number of entities to consider
	
	public static HashMap<Integer, String> hmId2EntityName;		// Maps an ID to the corresponding entity name
	public static HashMap<String, Integer> hmEntityName2Id;		// Maps an entity name to the corresponding ID
	
	public static HashMap<String, Integer> hmWord2Id;			// Maps a word to its ID
	public static HashMap<Integer, String> hmId2Word;			// Maps an ID to the associated word
	public static int numWords;									// Total number of unique words seen during training
	
	public int _numDocs;										// The number of documents in the datatset
	public int _numWordCount;									// Total number of tokens in the dataset
	public int[][] _aEntityCounts;								// Stores the count of an entity in each document
	public ArrayList<ArrayList<String>> _alDocs;				// The list of documents (each document is a list of word tokens)
	public ArrayList<ArrayList<State>> _alStates;				// The list of labels 
	public boolean _isTrain;
	public String _dataPath;
		
	public DataLoader(String dataPath) {
		this(dataPath, true);
	}
	
	public DataLoader(String dataPath, boolean isTrain) {
		_isTrain = isTrain;
		_dataPath = dataPath;
		
		if (isTrain) {
			hmWord2Id = new HashMap<String, Integer>();
			hmId2Word = new HashMap<Integer, String>();
			setup();
		}
		
		loadData(dataPath);
	}
	
	
	/**
	 * Sets up entity name and IDs
	 * 
	 * @param probPath	The path to the problem definition file 
	 */
	public void setup() {
		hmId2EntityName = new HashMap<Integer, String>();
		hmEntityName2Id = new HashMap<String, Integer>();
		
		int id = 0;
		for (State state : State.values()) {
			switch (state) {
				case START:
					break;
				case END:
					break;
				case O:
					break;
				default:
					String label = state.toString();
					String[] labelSplit = label.split("_");
					String entity = String.join("_", Arrays.copyOfRange(labelSplit, 1, labelSplit.length)).intern();
					Integer entityID = hmEntityName2Id.get(entity);
					if (entityID == null) {
						hmEntityName2Id.put(entity, id);
						hmId2EntityName.put(id++, entity);	
					}
			}
		}
		
		// Print out some information
		System.out.println("Number of entities: " + NUM_ENTITIES);
		System.out.println("Entites: ");
		for (int i = 0; i < NUM_ENTITIES; i++) {
			String entity = hmId2EntityName.get(i);
			System.out.println("\t" + entity);
		}
		
		System.out.println();
	}
	
	/**
	 * Loads the dataset given in the file path. 
	 * 
	 * Each line in the data should either be a line break or a pair of tokens. 
	 *   (1) When the line is a line break, it denotes the end of the previous document. 
	 * 	 (2) Each line consists of two tokens: the first one corresponding to the BIO label, 
	 * 		 while the other is the word token.
	 * 
	 * All word tokens are stored as lower-case letters. 
	 * In contrast, the labels other than "O" (Outside) have the form "{Tag}-{EntityName}",
	 * with "Tag" being "B" or "I", and "EntityName" being, for example, "Actor", "Director", etc.
	 * 
	 * @param filePath	The path to the file containing word tokens and their BIO labels
	 */
	public void loadData(String filePath) {

		_numDocs 	= 0;
		_alStates 	= new ArrayList<ArrayList<State>>();
		_alDocs 	= new ArrayList<ArrayList<String>>();
		int[][] entityCounts = new int[50000][NUM_ENTITIES];
		
		ArrayList<State> alStates = new ArrayList<State>(); 
		ArrayList<String> alWords = new ArrayList<String>();
		
		// Reads in data in String type
		System.out.println("Read data from " + filePath);
		
		try (BufferedReader br = new BufferedReader(new FileReader(filePath))){
			String line;
			while ((line = br.readLine()) != null) {
				line = line.trim();
				
				// There's a line break between documents
				if (line.isEmpty()) {
					_alStates.add(alStates);
					_alDocs.add(alWords);
					_numDocs++;
					alStates = new ArrayList<State>();
					alWords = new ArrayList<String>();
					continue;
				}
				
				String[] tokens = line.split("\\s+");
				if (tokens.length != 2) {
					throw new Exception("Invalid data");
				}
				
				// Add the label as a State object and the word String to separate lists
				String tag = tokens[0].intern();
				String word = tokens[1].toLowerCase().intern();
				if (tag.equalsIgnoreCase("O")) {
					alStates.add(State.valueOf(tag));
				} else {
					String[] tagSplit = tag.split("-");
					String entity = tagSplit[1];
					
					if (hmEntityName2Id.containsKey(entity)) {
						alStates.add(State.valueOf(tag.replace("-", "_")));
						int entityId = hmEntityName2Id.get(entity);
						entityCounts[_numDocs][entityId]++;
					} else {
						alStates.add(State.valueOf("O"));
					}
				}
				alWords.add(word);
				
				// Update the vocabulary only during training
				Integer id = getIndexOfWord(word);
				if (id == UNKNOWN && _isTrain) {
					hmId2Word.put(numWords, word);
					hmWord2Id.put(word, numWords++);
				}
				_numWordCount++;
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		// Update the entity counts for performance evaluation
		_aEntityCounts = new int[_numDocs][NUM_ENTITIES];
		for (int d = 0; d < _numDocs; d++) {
			for (int e = 0; e < NUM_ENTITIES; e++) {
				_aEntityCounts[d][e] = entityCounts[d][e];
			}
		}
		
		System.out.println("Data loaded successfully");
		System.out.println("Number of documents: " + _numDocs + "\tTotal number of unique words (train set): " + numWords + "\n");
	}
	
	/**
	 * Returns the list of Strings of word tokens given the ID of a document
	 * 
	 * @param id	The ID of a document
	 * @return		The list of word tokens in the corresponding document
	 */
	public ArrayList<String> getDoc(Integer id) {
		return _alDocs.get(id);
	}
	
	/**
	 * Returns the list of States given the ID of a document
	 * 
	 * @param id	The ID of a document
	 * @return		The list of States in the corresponding document
	 */
	public ArrayList<State> getStates(Integer docID) {
		return _alStates.get(docID);
	}
	
	/**
	 * Returns the ID of a word from the vocabulary set.  
	 * If the word has not been encountered before, this method returns UNKNOWN. 
	 * 
	 * @param word	The word to look up in the vocabulary set
	 * @return		The ID of the word or UNKNOWN 
	 */
	public static Integer getIndexOfWord(String word) {
		Integer id = hmWord2Id.get(word.intern());
		if (id == null) {
			return UNKNOWN;
		} else {
			return id;
		}
	}
	
	/**
	 * Returns the word associated with an ID from the vocabulary set.  
	 * If the word has not been encountered before, this method returns null. 
	 * 
	 * @param id 	The ID of the word
	 * @return		The word from the vocabulary or null
	 */
	public static String getWordFromIndex(Integer id) {
		String word = hmId2Word.get(id);
		if (word == null) {
			return null;
		} else {
			return word;
		}
	}
}
