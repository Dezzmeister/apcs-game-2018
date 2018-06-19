package gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;



import javax.swing.JButton;



public class EndMenu extends Menu{
	
    private JButton quit;
 
	
	public EndMenu() {
		
		super();
        start.setText("Restart");
		quit = new JButton ("Quit");
		
		quit.addActionListener(new QuitListener()); 
		 	
	}
	
	 private class QuitListener implements ActionListener  {

	     public void actionPerformed (ActionEvent event)  {
	         
	         //quit the game

	     }

	 }
	
	
}
