package mycontroller;

import java.awt.Font;
import java.awt.Frame;
import java.awt.TextArea;

public class MapWindow {
	private TextArea t;
	
	public MapWindow() {
		Frame f = new Frame();
		t = new TextArea();
		t.setFont(new Font("Consolas",Font.PLAIN,20));
		t.setText("a\nb");
		f.add(t);
		f.setTitle("MapManager - Map");
		f.setSize(1000, 1000);
		f.show();
	}
	
	public void setText(String s) {
		t.setText(s);
	}

}
