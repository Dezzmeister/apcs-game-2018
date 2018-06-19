package gui;

//>>>>>>> branch 'GUIteambranch' of https://github.com/Dezzmeister/apcs-game-2018.git



import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;



import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;


import message_loop.Messenger;





	public class Menu{

		private JFrame gameLoad;

	    private JPanel javaCups;

	    protected Container screen;

	    private JButton instructions;

	    protected JButton start;

	    private JLabel writtenInstructions;

	    private JButton back;

	

		

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
	        start.setFont(new Font("Consolas", Font.BOLD, 30));

	        

	        instructions= new JButton ("Instructions");
	        instructions.setFont(new Font("Consolas", Font.BOLD, 30));

	        

	        back = new JButton("Back");
	        back.addActionListener(new BListener()); 

	        

	        instructions.addActionListener(new IListener()); 


	        writtenInstructions = new JLabel ("INSTRUCTIONS FOR HOW TO PLAY HERE");
	        
	        screen.add(start);
	        screen.add(instructions);

	     
	    }

	    





	    @SuppressWarnings("deprecation")

		public void displayMe()  {

	       gameLoad.show();

	    }


	    public JPanel getPanel() {
	    	return javaCups;
	    	
	    }
	    
	    private class StartListener implements ActionListener  {


	        public void actionPerformed (ActionEvent event)  {
	        	 Messenger.post("start");

	    



	        }

	    }

	    private class IListener implements ActionListener  {
    

	        public void actionPerformed (ActionEvent event)  {

	        	screen.removeAll();
	        	screen.add(writtenInstructions);
	        	screen.add(back);
	        	screen.validate();
	        	screen.repaint();

	        	


	        }

	    }

	    private class BListener implements ActionListener  {
			    
		     public void actionPerformed (ActionEvent event)  {
		    	 
		    	screen.removeAll();
		        screen.validate();
		        screen.repaint();

		      

		      }

	    }

	    

}
	    
