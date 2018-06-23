package gui;

/*
import java.awt.Color;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JTextField;
*/
import javax.swing.JLabel;
import javax.swing.JPanel;

public class Counter {

	private int countLives;
	private int countBeans;
	private JLabel beans;
	private JLabel lives;
	private JPanel onScreen;
	
	// Creating the counter labels, adding them to the onScreen jpanel
	public Counter(int beansNum, int livesNum) {
		countBeans = beansNum;
		countLives = livesNum;
		beans = new JLabel("Beans - " + getBeans());
		lives = new JLabel("Lives - " + getLives());
		onScreen = new JPanel();
		onScreen.add(lives);
		onScreen.add(beans);
	}
	
	// to be called each time you pick up a bean
	public void gotBean() {
		countBeans += 1;
	}
	
	// to be called when an enemy is hit
	public void lostLife() {
		countLives -= 1;
	}
	
	private int getBeans() {
		return countBeans;
	}
	
	private int getLives() {
		return countLives;
	}

}
