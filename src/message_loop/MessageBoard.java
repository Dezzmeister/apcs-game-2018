package message_loop;

import java.util.ArrayList;
/*
 * Package-private class containing messages regarding inputs, interactions, and game 
 * events, such as pausing or losing.
 * 
 * It has a customizable maximum length, MAX_LENGTH.
 * 
 * It extends ArrayList<String> and overrides the add(String s) method in order to maintain
 * maximum length, if need be.
 * 
 * The methods in Messenger are used for access of the one and only instance of this class.
 * 
 * @author Dan Hagborg
 * @version 1.0
 * @since 2018-06-07
 */
@SuppressWarnings("serial")
class MessageBoard extends ArrayList<String>{
	static final int MAX_LENGTH = 10;
	@Override
	public boolean add(String s){
		if(size() == MAX_LENGTH){
			remove(0);
		}
		super.add(s);
		System.out.println(s);
		return false;
	}
	

}
/*
 * How to use:
 * 
 * Add message using Messenger.addMessage("message goes here")
 * Read messages using Messenger.getMessages() to get an ArrayList<String> of them
 * Check for a message with Messenger.containsMessage("message goes here")
 * 
 * Message Format: <type:specification>
 * 
 * Types and relevant specification examples:
 * 
 * "keypress"
 * "a", "ENTER", "CTRL" (lowercase for letters, uppercase for words)
 * 
 * "mousemove"
 * "100", "-100" (positive for right, negative for left)
 * 
 * "interaction"
 * "coworker", "coffeebean"
 * 
 * "event"
 * "pause", "win", "lose"
 */
