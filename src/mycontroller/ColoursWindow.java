package mycontroller;

import java.awt.Color;
import java.awt.Font;
import java.awt.Frame;
import java.awt.TextArea;
import javax.swing.JTextPane;
import javax.swing.text.AttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;

public class ColoursWindow {

	private JTextPane tC;
	public ColoursWindow() {
		
		tC = new JTextPane();
		tC.setFont(new Font("Consolas",Font.PLAIN,16));
		Frame fC = new Frame();
		fC.setTitle("StateManager - Colours");
		fC.setSize(1000,1200);
		fC.add(tC);
		fC.show();
	}
	
	public void setText(String s) {
		this.tC.setText(s);
	}
	
	public void clear() {
		this.tC.setText("");
	}
	
    public void appendText(String msg, Color c)
    {
    	
        StyleContext sc = StyleContext.getDefaultStyleContext();
        AttributeSet aset = sc.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, c);

        //aset = sc.addAttribute(aset, StyleConstants.FontFamily, "Lucida Console");
        //aset = sc.addAttribute(aset, StyleConstants.Alignment, StyleConstants.ALIGN_JUSTIFIED);

        int len = tC.getDocument().getLength();
        tC.setCaretPosition(len);
        tC.setCharacterAttributes(aset, false);
        tC.replaceSelection(msg);
    }

}
