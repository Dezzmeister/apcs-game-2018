
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
	import javax.swing.JPanel;


	public class Menu{
	    private JPanel javaCups;
	    protected Container screen;
	    private JButton instructions;
	    protected JButton start;
	
	    
	    public Menu()  {

	        javaCups= new JPanel ();
	        screen = javaCups.getRootPane();
	        
	        Toolkit tk = Toolkit.getDefaultToolkit();
	        int xSize = ((int) tk.getScreenSize().getWidth());
	        int ySize = ((int) tk.getScreenSize().getHeight());
	      
	        javaCups.setSize(xSize, ySize);
	        screen.setLayout(new FlowLayout(FlowLayout.CENTER));

	        instructions= new JButton ("Instructions");
	        instructions.setFont(new Font("Cambria", Font.BOLD, 30));

	        start = new JButton ("Start");
	        
	        start.addActionListener(new StartListener()); 
	        
	        
	        screen.add(instructions);
	        screen.add(start);
	      
	    }
	    


	    public void displayMe()  {
	       // javaCups.show();
	    }

	    private class StartListener implements ActionListener  {
	    
	        public void actionPerformed (ActionEvent event)  {
	        
	            screen.setBackground(new Color ((int)(Math.random()*16777216)));
	            // the background should be the game //ask joe
	            
	            //start game from beginning
	        }    

	    }

	    
}
