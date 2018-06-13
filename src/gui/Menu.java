package gui;

	import java.awt.Color;
	import java.awt.Container;
	import java.awt.FlowLayout;
	import java.awt.Font;
	import java.awt.event.ActionEvent;
	import java.awt.event.ActionListener;
	
	import java.awt.Toolkit;
	
	import javax.swing.JButton;
	import javax.swing.JFrame;
	//import javax.swing.JLabel;
	//import javax.swing.JTextField;

	public class Menu{
	    private JFrame javaCups;
	    protected Container screen;
	    private JButton instructions;
	    protected JButton start;
	    //private JButton resume;
	    //private JButton quit;
	    
	    public Menu()  {
	    	
	    	//https://stackoverflow.com/questions/9554636/the-use-of-multiple-jframes-good-or-bad-practice
	    	//perhaps we can make this a jpanel? because there will be one main jframe
	        javaCups= new JFrame ("javaCups");
	        screen = javaCups.getContentPane();
	        
	        Toolkit tk = Toolkit.getDefaultToolkit();
	        int xSize = ((int) tk.getScreenSize().getWidth());
	        int ySize = ((int) tk.getScreenSize().getHeight());
	      
	        javaCups.setSize(xSize, ySize);
	        screen.setLayout(new FlowLayout(FlowLayout.CENTER));

	        instructions= new JButton ("Instructions");
	        instructions.setFont(new Font("Cambria", Font.BOLD, 30));

	        start = new JButton ("Start");
	        //resume  = new JButton ("Resume");
	        //quit = new JButton ("Quit");
	 
	        start.addActionListener(new StartListener()); //?
	        //resume.addActionListener(new ResumeListener()); //?
	        //quit.addActionListener(new QuitListener()); //?
	        
	        screen.add(instructions);
	        screen.add(start);
	        //screen.add(resume);
	        //screen.add(quit);
	    }
	    


	    public void displayMe()  {
	        javaCups.show();
	    }

	    private class StartListener implements ActionListener  {
	    
	        public void actionPerformed (ActionEvent event)  {
	        
	            screen.setBackground(new Color ((int)(Math.random()*16777216)));
	            // the background should be the game //ask joe
	            
	            //start game from beginning
	        }    

	    }

	   /* private class ResumeListener implements ActionListener  {

	        public void actionPerformed (ActionEvent event)  {

	            //resume game from current point
	            
	        }

	    }

	    private class QuitListener implements ActionListener  {

	        public void actionPerformed (ActionEvent event)  {
	            
	            //quit the game

	        }

	    }*/
	    
}
