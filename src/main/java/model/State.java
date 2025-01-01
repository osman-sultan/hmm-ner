package model;

import java.util.HashMap;

/**
 * The latent state names and IDs are fixed as constants in this file.
 * 
 * Note: DO NOT MODIFY THIS FILE AS THE AUTO-GRADER RELIES ON THESE SPECIFIC STATE NAMES AND IDS DEFINED HERE. 
 * 
 * @author jhjeong
 *
 */
public enum State {
	START(0),
	B_Actor(1),
	I_Actor(2),
	B_Plot(3),
	I_Plot(4),
	B_Opinion(5),
	I_Opinion(6),
	B_Award(7),
	I_Award(8),
	B_Year(9),
	B_Genre(10),
	B_Origin(11),
	I_Origin(12),
	B_Director(13),
	I_Director(14),
	I_Genre(15),
	I_Year(16),
 	B_Soundtrack(17),
 	I_Soundtrack(18),
 	B_Relationship(19),
 	I_Relationship(20),
 	B_Character_Name(21),
 	I_Character_Name(22),
 	B_Quote(23),
 	I_Quote(24),
 	O(25),
 	END(26);
	
	// The total size of the state space
	public static final int SIZE;		
	
	// Mapping from a state ID to a State object
	private static final HashMap<Integer, State> HM_STATE_ID_TO_STATE = new HashMap<Integer, State>();
	
	// Initialize static variables
	static {
		SIZE = values().length;
		for (State s : State.values()) {
			HM_STATE_ID_TO_STATE.put(s.getID(), s);
		}
	}
	
	// Each State object is associated with an integer ID 
	private final Integer id;	
	private State(Integer id) {
		this.id = id;
	}
	
	/**
	 * Returns the ID of a State object
	 */
	public Integer getID() {
		return id;
	}
	
	/**
	 * Returns the State object given its ID
	 */
	public static State getStateFromID(Integer id) {
		return HM_STATE_ID_TO_STATE.get(id);
	}
}
