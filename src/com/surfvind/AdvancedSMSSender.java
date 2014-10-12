package com.surfvind;

import java.io.File;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SubMenu;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnTouchListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.surfvind.listeners.CallbackMessage;
import com.surfvind.listeners.ListenerManager;
import com.surfvind.logic.FileSystemManager;
import com.surfvind.logic.MessageStructure;
import com.surfvind.logic.NbrOfChars;
import com.surfvind.logic.PDU;
import com.surfvind.sms.MessageSender;

/**
 * Engine
 * 
 * @author Erik Creates all needed views from the xml
 */
public class AdvancedSMSSender extends Activity {

	/* Text views */
	private TextView title;
	private TextView charCounterText;

	/* Text inputs */
	private EditText phoneNoInput;
	private EditText smsPidInput;
	private EditText mainInput1;
	private EditText mainInput2;

	/* Spinners */
	private Spinner smsClassSpinner;

	/* Radio buttons */
	private RadioButton bit7Radio;
	private RadioButton bit8Radio;
	
	/* Button */
	private Button sendBtn;
	private Button generateTc65;
	private Button generateMessageBtn;

	/* Message sender */
	private MessageSender ms;

	private String pduMessage;

	/* Menu options */
	private final int MENU_NEW = 100;
	private final int MENU_SAVE = 101;
	private final int MENU_LOAD = 102;
	private final int MENU_QUIT = 103;

	/* Load menu options as submenu */
	private SubMenu loadMenu;

	/* filesystem path */
	private final String FS_PATH = "/data/data/com.surfvind/saveFiles/";

	/* If unable to create the path flag for this */
	private boolean pathProblems;

	/* For save dialog */
	private AlertDialog.Builder alert;

	/* Input field for saving */
	private EditText saveInput;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		/* setup filesystem */
		if (!FileSystemManager.createDir(FS_PATH)) {
			pathProblems = true;
		} else {
			pathProblems = false;
		}

		/* Make it full screen */
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);

		this.setContentView(R.layout.main);

		/* create the message sender */
		ms = new MessageSender(new Result("Message sent!"), new Result(
				"Message delivered"));

		/* Initialize everyting and add listeners */
		initView(this);

		
		/* Update the pdu message according to the selected settings */
		updatePDUMessage();
	}

	/**
	 * Create everything and add listeners
	 * 
	 * @param context
	 */
	private void initView(Context context) {
		/* Text views */
		title = (TextView) findViewById(R.id.Title);
		title.setGravity(Gravity.CENTER_HORIZONTAL); /* Center the text */

		charCounterText = (TextView) findViewById(R.id.CharCounterText);

		/* Text inputs */
		phoneNoInput = (EditText) findViewById(R.id.PhoneNoInput);
		smsPidInput = (EditText) findViewById(R.id.SMSPIDInput);
		mainInput1 = (EditText) findViewById(R.id.MainInput1);
		mainInput2 = (EditText) findViewById(R.id.MainInput2);

		/* Spinners */
		smsClassSpinner = (Spinner) findViewById(R.id.SMSClassSpinner);

		/* populate the spinners */
		Integer[] items = new Integer[] { 0, 1, 2, 3 };
		ArrayAdapter<Integer> adapter = new ArrayAdapter<Integer>(this,
				android.R.layout.simple_spinner_item, items);
		adapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		smsClassSpinner.setAdapter(adapter);
		smsClassSpinner.setSelection(1); /* Set default to point to sms class 1 */

		/* Radio buttons */
		bit7Radio = (RadioButton) findViewById(R.id.bit7Radio);
		bit8Radio = (RadioButton) findViewById(R.id.bit8Radio);
		/* Set 7bit checked */
		bit7Radio.setChecked(true);

		
		/* Buttons */
		sendBtn = (Button) findViewById(R.id.SendButton);
		generateTc65 = (Button) findViewById(R.id.GenerateTc65);
		
		/* LISTENERS */
		// Phone number listener
		phoneNoInput.addTextChangedListener(new TextWatcher() {

			public void afterTextChanged(Editable s) {
				updatePDUMessage();
			}

			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				/* ignore */
			}

			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				/* ignore */
			}
		});

		/* When anything is changed, update the pdu message */
		smsPidInput.addTextChangedListener(new TextWatcher() {
			public void afterTextChanged(Editable s) {
				updatePDUMessage();
			}

			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				/* ignore */
			}

			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				/* ignore */
			}

		});

		// sms class listener
		smsClassSpinner.setOnFocusChangeListener(new OnFocusChangeListener() {

			public void onFocusChange(View v, boolean hasFocus) {
				updatePDUMessage();
			}

		});

		/*
		 * add radio button listeners and make 7 and 8 bit radio buttons
		 * mutually exclusive
		 */
		bit7Radio.setOnTouchListener(new OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				bit7Radio.setChecked(true);
				bit8Radio.setChecked(false);
				
				CallbackMessage message = new CallbackMessage();
				message.b1 = bit7Radio.isChecked();
				ListenerManager.onEvent(R.id.MainInput1, mainInput1, message);
				
				updatePDUMessage();
				return true;
			}
		});

		bit8Radio.setOnTouchListener(new OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				bit8Radio.setChecked(true);
				bit7Radio.setChecked(false);
				
				CallbackMessage message = new CallbackMessage();
				message.b1 = bit7Radio.isChecked();
				ListenerManager.onEvent(R.id.MainInput1, mainInput1, message);
				
				updatePDUMessage();
				return true;
			}
		});

		
		/*
		 * When the main text changes we need to update the character counter
		 * and the pdu message itself
		 */
		mainInput1.addTextChangedListener(new TextWatcher() {
			public void afterTextChanged(Editable s) {
				CallbackMessage message = new CallbackMessage();
				message.b1 = bit7Radio.isChecked();
				ListenerManager.onEvent(R.id.MainInput1, mainInput1, message);
				updatePDUMessage();
			}

			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				/* ignore */
			}

			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				/* ignore */
			}

		});

		/*
		 * Add listener to send button. On press event we update the pdu message
		 * and try to send the sms.
		 */
		sendBtn.setOnTouchListener(new OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					updatePDUMessage();
					if (ms.sendSMS(pduMessage, AdvancedSMSSender.this)) {
						Toast.makeText(getBaseContext(), "Message sent!",
								Toast.LENGTH_SHORT).show();
					} else {
						Toast.makeText(getBaseContext(), "Send failed!",
								Toast.LENGTH_SHORT).show();
					}
					return true;
				}
				return false;
			}

		});
		
		/*
		 * Add listener to generate TC65 button. On press event we will display the cinterionsmsotap.xml view
		 */
		generateTc65.setOnTouchListener(new OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					setContentView(R.layout.cinterionsmsotap);
					
					
					generateMessageBtn = (Button) findViewById(R.id.generateMessage);
					
					generateMessageBtn.setOnTouchListener(new OnTouchListener() {
						public boolean onTouch(View v, MotionEvent event) {
							if (event.getAction() == MotionEvent.ACTION_DOWN) {
								String phoneNbrText = ((EditText) findViewById(R.id.phoneNbrEdit)).getText().toString();
								String passText = ((EditText) findViewById(R.id.passwordEdit)).getText().toString();
								String urlText = ((EditText) findViewById(R.id.urlEdit)).getText().toString();
								String apnText = ((EditText) findViewById(R.id.apnEdit)).getText().toString();
								String dirText = ((EditText) findViewById(R.id.directoryEdit)).getText().toString();
								setContentView(R.layout.main);
								initView(v.getContext());
								phoneNoInput.setText(phoneNbrText);
								mainInput1.setText("OTAP_IMPNG\nPWD:"+passText+"\nJADURL:"+urlText+"\nAPPDIR:"+dirText+"\nBEARER:gprs\nAPNORNUM:"+apnText+"\nSTART:install\n");
								bit7Radio.setChecked(false);
								bit8Radio.setChecked(true);
								smsPidInput.setText("7d");
								
								return true;
							}
							return false;
						}
					});
					return true;
				}
				return false;
			}
		});
		
		

		/* Dialog for save */
		alert = new AlertDialog.Builder(this);
		alert.setTitle("Save As");
		alert.setMessage("Choose file name");

		alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				if (save()) {
					Toast.makeText(getBaseContext(), "Save successful!!",
							Toast.LENGTH_SHORT).show();
				} else {
					Toast.makeText(getBaseContext(), "Save failed!",
							Toast.LENGTH_SHORT).show();
				}
			}
		});

		alert.setNegativeButton("Cancel",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {

					}
				});
		
		/* The charCounterText is updated through here */
		new NbrOfChars(charCounterText);

	}

	/**
	 * Update the pdu message according to the settings. Set mainInput2 to
	 * contain the pdu message
	 */
	private void updatePDUMessage() {
		String message = mainInput1.getText().toString();
		String phoNo = phoneNoInput.getText().toString();
		StringBuffer pdu = new StringBuffer();
		pduMessage = "";

		{
			boolean bit7 = bit7Radio.isChecked();
			int smsClass = (Integer) smsClassSpinner.getSelectedItem();
			String pid = smsPidInput.getText().toString();
			
			pdu.append(PDU.constructAPDUMessage(message, phoNo, bit7, smsClass,
					pid));
			
		}

		pduMessage = pdu.toString();

		mainInput2.setText(pduMessage);
	}

	/**
	 * Creates the menu items
	 */
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, MENU_NEW, 0, "New");
		menu.add(0, MENU_QUIT, 0, "Quit");
		loadMenu = menu.addSubMenu(0, MENU_LOAD, 0, "Load");

		if (pathProblems) {
			menu.add(0, MENU_SAVE, 0, "Save").setEnabled(false);
		} else {
			menu.add(0, MENU_SAVE, 0, "Save");
		}

		return true;
	}

	/**
	 * Handles menu item selections
	 */
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MENU_NEW: {
			clear();
			return true;
		}
		case MENU_SAVE: {
			saveInput = new EditText(this);
			alert.setView(saveInput);
			alert.show();
			return true;
		}
		case MENU_LOAD: {
			showLoadChoices();
			return true;
		}
		case MENU_QUIT: {
			quit();
			return true;
		}
		default: {
			// guessing we want to load :)
			if (load(item.getItemId())) {
				Toast.makeText(getBaseContext(), "Load successful!",
						Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(getBaseContext(), "Load failed",
						Toast.LENGTH_SHORT).show();
			}
		}
		}
		return true;
	}

	/**
	 * Clear everything and revert the settings back to initial
	 */
	private void clear() {
		phoneNoInput.setText("");
		smsClassSpinner.setSelection(0);
		smsPidInput.setText("");
		bit7Radio.setChecked(true);
		bit8Radio.setChecked(false);
		mainInput1.setText("");
		updatePDUMessage();
	}

	/**
	 * Save the current settings to a file.
	 * 
	 * @return true if the file was saved, false otherwise
	 */
	private boolean save() {
		String phoNo;
		int smsClass;
		String smsPid;
		boolean bit7;
		String message;

		String encodedMessage;

		String fileName;

		fileName = saveInput.getText().toString();
		if (fileName == null || fileName.length() == 0) {
			System.out.println("Illegal filename");
			return false;
		}

		phoNo = phoneNoInput.getText().toString();
		smsClass = (Integer) smsClassSpinner.getSelectedItem();
		smsPid = smsPidInput.getText().toString();
		bit7 = bit7Radio.isChecked();
		message = mainInput1.getText().toString();

		encodedMessage = MessageStructure.encodeToString(phoNo, smsClass,
				smsPid, bit7, message);

		return FileSystemManager.save(encodedMessage, FS_PATH, fileName);
	}

	/**
	 * List all the files under FS_PATH. If not files are found a pop-up is
	 * displayed and the action is canceled. Otherwise the files are listed.
	 */
	private void showLoadChoices() {
		String[] files;

		files = FileSystemManager.listFilesInAsString(FS_PATH);

		if (files.length == 0) {
			Toast.makeText(getBaseContext(), "No files to load",
					Toast.LENGTH_SHORT).show();
			return;
		}

		loadMenu.clear();
		for (int i = 0; i < files.length; i++) {
			loadMenu.add(0, i, 0, files[i]);
		}
	}

	/**
	 * Loads a file. The file is decoded and the settings are set to reflect the
	 * content of the file.
	 * 
	 * @param file
	 *            to be loaded
	 * @return true if load was successful. False otherwise
	 */
	private boolean load(File file) {
		MessageStructure settings;

		settings = MessageStructure.decodeFile(file);
		if (settings == null) {
			return false;
		}

		phoneNoInput.setText(settings.phoneNo);
		smsClassSpinner.setSelection(settings.smsClass, true);
		smsPidInput.setText(settings.smsPid);
		bit7Radio.setChecked(settings.bit7);
		bit8Radio.setChecked(!settings.bit7);
		mainInput1.setText(settings.message);

		updatePDUMessage();

		return true;
	}

	/**
	 * Load a file based on item id (of load menu)
	 * 
	 * @param itemId
	 * @return
	 */
	private boolean load(int itemId) {
		String fileName;
		File file;

		fileName = (String) loadMenu.getItem(itemId).getTitle();

		file = FileSystemManager.load(FS_PATH + fileName);

		return load(file);
	}

	/**
	 * Quit. Should perhaps do some cleaning :)
	 */
	private void quit() {
		System.exit(0);
	}

	/**
	 * Display the result from sending the sms
	 * 
	 * @param status
	 * @param okMessage
	 */
	private void displaySendResult(int status, String okMessage) {
		switch (status) {
		case Activity.RESULT_OK:
			Toast.makeText(getBaseContext(), okMessage, Toast.LENGTH_SHORT)
					.show();
			break;
		case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
			Toast.makeText(getBaseContext(), "Generic failure",
					Toast.LENGTH_SHORT).show();
			break;
		case SmsManager.RESULT_ERROR_NO_SERVICE:
			Toast.makeText(getBaseContext(), "No service", Toast.LENGTH_SHORT)
					.show();
			break;
		case SmsManager.RESULT_ERROR_NULL_PDU:
			Toast.makeText(getBaseContext(), "Null PDU", Toast.LENGTH_SHORT)
					.show();
			break;
		case SmsManager.RESULT_ERROR_RADIO_OFF:
			Toast.makeText(getBaseContext(), "Radio off", Toast.LENGTH_SHORT)
					.show();
			break;
		default: {
			Toast.makeText(getBaseContext(), "Code: " + status,
					Toast.LENGTH_SHORT).show();
			break;
		}
		}
	}

	/**
	 * Send and delivered intents are set when sending sms, so we sohuld expect
	 * a callback here about the status. We pass this information to {@link}
	 * displaySendResult()
	 * 
	 */
	public class Result extends BroadcastReceiver {

		private String action;

		public Result(String action) {
			this.action = action;
		}

		@Override
		public void onReceive(Context context, Intent intent) {
			displaySendResult(getResultCode(), action);
		}

	}
}
