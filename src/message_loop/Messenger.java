package message_loop;

import java.util.ArrayList;

/**
 * This class is meant purely for accessing the only instance of MessageBoard,
 * m.
 *
 * @author Dan Hagborg
 * @version 1.0
 * @since 2018-06-07
 */
@SuppressWarnings("serial")
public class Messenger {

	private static MessageBoard m = new MessageBoard();

	/**
	 * Appends the given message to m
	 *
	 * @param String
	 *            s The message to be appended
	 * @retun Nothing
	 */
	public static void post(String s) {
		m.add(s);
	}

	/**
	 * Returns an ArrayList of all of the messages in m
	 *
	 * @return ArrayList<String> All of the messages in m
	 */
	public static ArrayList<String> getMessages() {
		return new ArrayList<String>(m);
	}

	/**
	 * Returns true if a given String is contained in m, false otherwise
	 *
	 * @param String
	 *            message the String in question
	 * @return boolean Boolean representing whether or not message is contained in m
	 */
	public static boolean containsMessage(String message) {
		return m.stream().anyMatch(s -> s == message);
	}

	/**
	 * Returns ArrayList of Strings for every message pertaining to a specific type
	 * (keypress, event, etc.), "<typeName>:" not included (see message format)
	 *
	 * @param String
	 *            type Type of message needed
	 * @return ArrayList<String> list of messages pertaining to a certain type
	 */
	public ArrayList<String> getOfType(String type) {
		return new ArrayList<String>() {

			/**
			*
			*/
			private static final long serialVersionUID = 6387753689642356466L;

			{
				for (String s : m) {
					if (s.indexOf(type) == 0) {
						add(s.substring(s.indexOf(":") + 1));
					}
				}
			}
		};

	}

}
