
package gui;
//>>>>>>> branch 'GUIteambranch' of https://github.com/Dezzmeister/apcs-game-2018.git

	import java.awt.Color;
	import java.awt.Container;
	import java.awt.FlowLayout;
	import java.awt.Font;
	import java.awt.event.ActionEvent;
	import java.awt.event.ActionListener;
	
	import java.awt.Toolkit;
	
	import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;


	public class Menu{
		private JFrame gameLoad;
	    private JPanel javaCups;
	    protected Container screen;
	    private JButton instructions;
	    protected JButton start;
	    private JLabel writtenInstructions;
	
	    
	    public Menu()  {
	    	
	    	gameLoad = new JFrame("JavaCups");
	    	
	    	screen = gameLoad.getContentPane();
	    	
	    	Toolkit tk = Toolkit.getDefaultToolkit();
		    int xSize = ((int) tk.getScreenSize().getWidth());
		    int ySize = ((int) tk.getScreenSize().getHeight());
		      
		    gameLoad.setSize(xSize, ySize);

			
	        javaCups= new JPanel ();
	        gameLoad.add(javaCups);
	        //screen = javaCups.getRootPane();
	        
	      
	    
	        screen.setLayout(new FlowLayout(FlowLayout.CENTER));
	        
	        start = new JButton ("Start");
	        start.addActionListener(new StartListener()); 
	        start.setFont(new Font("Cambria", Font.BOLD, 30));
	        
	        instructions= new JButton ("Instructions");
	        instructions.setFont(new Font("Cambria", Font.BOLD, 30));
	        
	        instructions.addActionListener(new IListener()); 
	        
	        
	        writtenInstructions = new JLabel ("INSTRUCTIONS FOR HOW TO PLAY HERE");

	       
	        
	        
	        screen.add(instructions);
	        screen.add(start);
	      
	    }
	    


	    @SuppressWarnings("deprecation")
		public void displayMe()  {
	       gameLoad.show();
	    }

	    private class StartListener implements ActionListener  {
	    
	        public void actionPerformed (ActionEvent event)  {
	        
	            screen.setBackground(new Color ((int)(Math.random()*16777216)));
	            // the background should be the game //ask joe
	            
	            //start game from beginning
	        }    

	    }
	    private class IListener implements ActionListener  {
		    
	        public void actionPerformed (ActionEvent event)  {
	        	screen.removeAll();
	        	screen.add(writtenInstructions);
	        	screen.validate();
	        	screen.repaint();
	        	

	        }
	    }
	    
}
