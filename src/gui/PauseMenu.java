package gui;

//import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

	
import javax.swing.JButton;




public class PauseMenu extends Menu{
	
	 private JButton resume;
     private JButton quit;
     
	 public PauseMenu() {
		super();
	    resume  = new JButton ("Resume");
        quit = new JButton ("Quit");
 
        resume.addActionListener(new ResumeListener()); 
        quit.addActionListener(new QuitListener()); 
        
        resume.setFont(new Font("Cambria", Font.BOLD, 30));
        
        super.screen.add(resume);
        super.screen.add(quit);
        
        
	}
	
	 private class ResumeListener implements ActionListener  {

		 public void actionPerformed (ActionEvent event)  {

         //resume game from current point
         
     }

 }

 private class QuitListener implements ActionListener  {

     public void actionPerformed (ActionEvent event)  {
         
         //quit the game

     }

 }
 
	

}
