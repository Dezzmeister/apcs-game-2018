package gui;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;

public class EndMenu extends Menu {
	
	private JButton quit;
	private JLabel lose;
	private JLabel win;
	
	public EndMenu() {

		super();
		start.setText("Restart");
		quit = new JButton("Quit");
		quit.addActionListener(new QuitListener());
		
		lose = new JLabel("U LOSE");
		lose.setFont(new Font("Cambria", Font.BOLD, 70));
		
		win = new JLabel("U WIN");
		win.setFont(new Font("Cambria", Font.BOLD, 70));
		
	}
	
	private class QuitListener implements ActionListener {
		
		@Override
		public void actionPerformed(ActionEvent event) {
			
			// quit the game
			
		}
		
	}
	
	public void outcome(int countLives) {
		
		if (countLives > 0) {
			screen.add(win);
		} else {
			screen.add(lose);
		}
		
	}
	
}
