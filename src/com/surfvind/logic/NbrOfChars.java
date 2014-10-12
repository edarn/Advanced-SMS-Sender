package com.surfvind.logic;

import android.widget.EditText;
import android.widget.TextView;

import com.surfvind.R;
import com.surfvind.listeners.CallbackMessage;
import com.surfvind.listeners.Listener;
import com.surfvind.listeners.ListenerManager;

/**
 * This class listens to mainInput1 and updates the character counter
 * 
 * @author Erik
 * 
 */
public class NbrOfChars implements Listener {

	private final String text = "Nbr of chars: ";
	private int nbrOfChars;
	private TextView tv;

	private int maxLenght;

	/**
	 * @param tv
	 */
	public NbrOfChars(TextView tv) {
		nbrOfChars = 0;
		this.tv = tv;
		ListenerManager.registerListener(R.id.MainInput1, this);
	}

	/**
	 * The text to display
	 * 
	 * @return
	 */
	public String getString() {
		return text + nbrOfChars;
	}

	/**
	 * When anything happens in mainInput one, we get to know here
	 */
	public void onEvent(Object o, CallbackMessage message) {
		if (o instanceof EditText) {
			nbrOfChars = ((EditText) o).getText().length();
			if(message.b1) {
				maxLenght = 160;
			} else {
				maxLenght = 140;
			}
			if (nbrOfChars > maxLenght) {
				tv.setTextColor(0xFFFF0000);
			} else {
				tv.setTextColor(0xFF000000);
			}
			tv.setText(getString());
		}
	}
}
