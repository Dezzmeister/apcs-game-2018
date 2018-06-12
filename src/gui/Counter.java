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


public class Counter {

	private int countLives;
	private int countBeans;
	private JLabel beans;
	private JLabel lives;
	
	public Counter(int beansNum, int livesNum) {
		countBeans = beansNum;
		countLives = livesNum;
		beans = new JLabel("Beans - " + countBeans);
		lives = new JLabel("Lives - " + countLives);
	}
	
	public void addCount(int count) {
		count+=1;
	}
	public void downCount(int count) {
		count-=1;		
	}
	
	
	
	
}
