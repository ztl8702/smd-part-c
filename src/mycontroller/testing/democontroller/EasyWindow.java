

/** TODO: POTENTIALL TO REMOVE **/


/*
 * Group number: 117
 * Therrense Lua (782578), Tianlei Zheng (773109)
 */

package mycontroller.testing.democontroller;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;

public class EasyWindow implements ActionListener {
	private Frame f;
	private Button btnSetSpeed0;
	private Button btnSetSpeed1;
	private Button btnSetSpeed2;
	private Button btnSetTurn;
	
	public Runnable onSetSpeed1;
	public Runnable onSetSpeed2;
	public Runnable onSetSpeed0;
	public Runnable onSetTurn;
	public EasyWindow() {
		f = new Frame();
		f.setTitle("Easy Control");
		f.setSize(200, 400);
		f.setLayout(new BoxLayout(f, BoxLayout.Y_AXIS));
	
		btnSetSpeed0 = new Button();
		btnSetSpeed0.setLabel("Set Speed to 0");
		btnSetSpeed0.addActionListener(this);
		f.add(btnSetSpeed0);
		
		btnSetSpeed1 = new Button();
		btnSetSpeed1.setLabel("Set Speed to 1");
		btnSetSpeed1.addActionListener(this);
		f.add(btnSetSpeed1);
		
		btnSetSpeed2 = new Button();
		btnSetSpeed2.setLabel("Set Speed to 2");
		btnSetSpeed2.addActionListener(this);
		f.add(btnSetSpeed2);
		
		btnSetTurn = new Button();
		btnSetTurn.setLabel("Turn from (6,3)->(7,4)");
		btnSetTurn.addActionListener(this);
		f.add(btnSetTurn);
		
		f.show();
		
	}
	@Override
	public void actionPerformed(ActionEvent arg0) {
		if (arg0.getSource() == this.btnSetSpeed1) {
			System.out.print("clicked 1!");
			this.onSetSpeed1.run();
		} else if (arg0.getSource() == this.btnSetSpeed2) {
			this.onSetSpeed2.run();
		} else if (arg0.getSource() == this.btnSetSpeed0) {
			this.onSetSpeed0.run();
		} else if (arg0.getSource() == this.btnSetTurn) {
			this.onSetTurn.run();
		}
		
	}
}
