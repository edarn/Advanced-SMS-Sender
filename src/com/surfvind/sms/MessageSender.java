package com.surfvind.sms;

import java.lang.reflect.Method;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.telephony.SmsManager;

import com.surfvind.AdvancedSMSSender.Result;

/**
 * 
 * Sends the sms
 * 
 * @author Erik
 * 
 */
public class MessageSender {

	private SmsManager smsManager;

	private PendingIntent sentPI;
	private PendingIntent deliveredPI;

	private Result send;
	private Result deliver;

	private String SENT = "SMS_SENT";
	private String DELIVERED = "SMS_DELIVERED";

	public MessageSender(Result send, Result deliver) {
		this.send = send;
		this.deliver = deliver;

		smsManager = SmsManager.getDefault();
	}

	/**
	 * Send an sms message. Get the method by reflection (since it isn't public)
	 * and invoke it.
	 * 
	 * @param pdu
	 * @param context
	 * @return
	 */
	public boolean sendSMS(String pdu, Context context) {
		context.registerReceiver(send, new IntentFilter(SENT));
		context.registerReceiver(deliver, new IntentFilter(DELIVERED));

		sentPI = PendingIntent.getBroadcast(context, 0, new Intent(SENT), 0);
		deliveredPI = PendingIntent.getBroadcast(context, 0, new Intent(
				DELIVERED), 0);

		/* Ok, hack time!! */
		try {
			Class c = smsManager.getClass();
			Method[] allMethods = c.getDeclaredMethods();
			int i = 0;
			for (Method m : allMethods) {
				String mname = m.getName();
				m.setAccessible(true);
				System.out.println(mname);
				if (mname.equalsIgnoreCase("sendRawPdu"))
				{
					break;
				}
				i++;
			}
			Method sendMethod = allMethods[i];
			sendMethod.setAccessible(true);
			sendMethod.invoke(smsManager, null, pduAsByte(pdu), sentPI,
					deliveredPI, true, true);
		} catch (Exception e) {
			System.out.println("Unable to send sms.");
			e.printStackTrace();
			return false;
		}

		return true;
	}

	/**
	 * Convert a string to byte values
	 * 
	 * @param pdu
	 *            to convert
	 * @return the pdu as bytes
	 */
	private byte[] pduAsByte(String pdu) {
		if (pdu == null || pdu.length() == 0) {
			return new byte[0];
		}
		byte[] bytes = new byte[pdu.length() / 2];
		
		int j = 0;
		for (int i = 0; i < bytes.length; i++) {
			bytes[i] = Integer.decode("0x" + pdu.substring(j, j + 2))
					.byteValue();
			j += 2;
		}

		System.out.println("Pdu as byte: ");
		for (int i = 0; i < bytes.length; i++) {
			System.out.print(bytes[i] + " ");
		}
		System.out.println();

		return bytes;
	}

}
