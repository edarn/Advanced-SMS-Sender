package com.surfvind.logic;

/**
 * Creates a PDU message
 * 
 * @author Erik
 * 
 */
public class PDU {

	// Things I won't change:
//	private static final String smsc = "00";
	private static final String smsSubmitMsg = "11";
	private static final String tpMessageRef = "00";
	private static final String typeOfAddress = "91";
	private static final String tpValidityPeriod = "AA";

	// Things based on the application
	private static String addressLength;
	private static String phoneNoAsSemiOctets;
	private static String tpPid;
	private static String tpDcs;
	private static String tpUserDataLenght;
	private static String tpUserData;

	/**
	 * Constructs a raw PDU message based on text, phone number, bit encoding,
	 * sms class and pid
	 * 
	 * @param message
	 *            message
	 * @param phoneNo
	 *            phone number to destination
	 * @param bit7
	 *            7 bit encoding or 8
	 * @param smsClass
	 *            sms class, 0, 1, 2 or 3
	 * @param pid
	 *            sms pid
	 */
	public static String constructAPDUMessage(String message, String phoneNo,
			boolean bit7, int smsClass, String pid) {
		String pduMessage;

		if (!argsOK(phoneNo, bit7, smsClass, pid)) {
			return "";
		}

		if (message == null) {
			message = "";
		}

		// parse the '+' away if any
		if (phoneNo.charAt(0) == '+') {
			phoneNo = new String(phoneNo.substring(1));
		}

		addressLength = convertHexToTwoBytes(Integer.toHexString(phoneNo
				.length()));
		phoneNoAsSemiOctets = convertPhoNoToSemiOctets(phoneNo);
		tpUserDataLenght = convertHexToTwoBytes(Integer.toHexString(message
				.length()));
		try {
			tpUserData = convertMessageToHex(message, bit7);
			System.out.println(tpUserData);
		} catch (Exception e) {
			System.err.println("Unable to code the message...");
			return "<message containts illegal characters>";
		}
		tpDcs = getTPDCS(bit7, smsClass);
		tpPid = pid;

		pduMessage = new String();
		// I've removed smsc information completely
		pduMessage += smsSubmitMsg + tpMessageRef + addressLength
				+ typeOfAddress + phoneNoAsSemiOctets + tpPid + tpDcs
				+ tpValidityPeriod + tpUserDataLenght + tpUserData;

		
		return pduMessage;
	}

	public static String constructAPDUMessage(String smsc, String message,
			String phoneNo, boolean bit7, int smsClass, String pid) {
		StringBuffer pdu = new StringBuffer(constructAPDUMessage(message,
				phoneNo, bit7, smsClass, pid));
		pdu.delete(0, 2);

		if (smsc.startsWith("+")) {
			smsc = new String(smsc.substring(1));
		}

		String encoded;
		String length;
		String smscEncoded;

		int smscLength;

		smscEncoded = convertPhoNoToSemiOctets(smsc);

		smscLength = smscEncoded.length() + 2; // plus two for the 91
		length = convertHexToTwoBytes(Integer.toHexString(smscLength / 2));

		encoded = length;
		encoded += "91";
		encoded += smscEncoded;

		pdu.insert(0, encoded);

		return pdu.toString();

	}

	public static String addSmscToPdu(String smsc, String pdu) {
		StringBuffer newPdu = new StringBuffer(pdu);
		newPdu.delete(0, 2);

		if (smsc.startsWith("+")) {
			smsc = new String(smsc.substring(1));
		}

		String encoded;
		String length;
		String smscEncoded;

		int smscLength;

		smscEncoded = convertPhoNoToSemiOctets(smsc);

		smscLength = smscEncoded.length() + 2; // plus two for the 91
		length = convertHexToTwoBytes(Integer.toHexString(smscLength / 2));

		encoded = length;
		encoded += "91";
		encoded += smscEncoded;

		newPdu.insert(0, encoded);

		return newPdu.toString();
	}

	public static String getTPUserData(String message, boolean bit7) {
		return convertMessageToHex(message, bit7);
	}

	/**
	 * Check the arguments, return false in case any arguments are faulty.
	 * 
	 * @param phoneNo
	 * @param bit7
	 * @param smsClass
	 * @param pid
	 * @return
	 */
	private static boolean argsOK(String phoneNo, boolean bit7, int smsClass,
			String pid) {
		if (phoneNo == null || phoneNo.length() == 0) {
			System.err.println("phoneNo not OK!");
			return false;
		}
		if (smsClass < 0 || smsClass > 3) {
			System.err.println("smsClass not OK!");
			return false;
		}
		if (pid == null || pid.length() == 0) {
			System.err.println("PID not OK!");
			return false;
		}

		return true;
	}

	/**
	 * TPDS is represented with 8 bits and tells if 7 bit or 8 bit and what sms
	 * class is used. bit 4 tells if sms has class meaning (it always has in our
	 * case) bit 2 & 3 tells the alphabet being used. bit 1 & 2 tells the sms
	 * class
	 * 
	 * @param bit7
	 * @param smsClass
	 * @return
	 */
	private static String getTPDCS(boolean bit7, int smsClass) {
		String ret;
		int bitsAndClass;

		ret = "1";
		if (bit7) {
			bitsAndClass = 0;
		} else {
			bitsAndClass = 4;
		}
		bitsAndClass += smsClass;

		ret += Integer.toString(bitsAndClass);
		return ret;
	}

	/**
	 * @param value
	 * @return
	 */
	private static String convertHexToTwoBytes(String value) {
		if (value.length() == 1) {
			return new String("0" + value).toUpperCase();
		}
		return value;
	}

	/**
	 * Convert phone number to semi octets. The converting is done by swapping
	 * every pair. If the number is odd a trailing F is added E.g. number
	 * 1234567890 becomes: 214365870 and number 123456789 becomes: 214365879F
	 * i.e. 12 34 56 78 90 -> 21 43 65 87 09
	 * 
	 * @param number
	 * @return
	 */
	private static String convertPhoNoToSemiOctets(String number) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < number.length(); i++) {
			if (i == number.length() - 1) {
				// last step, insert trailing F
				sb.insert(i, 'F');
				sb.insert(i + 1, number.charAt(i));
			} else {
				sb.insert(i, number.charAt(i + 1));
				sb.insert(i + 1, number.charAt(i));
				i++;
			}
		}
		return sb.toString().toUpperCase();
	}

	/**
	 * Converts a message to hex. If 7bit we need to code the septets into
	 * octets
	 * 
	 * @param message
	 * @param bit7
	 * @return
	 */
	private static String convertMessageToHex(String message, boolean bit7) {
		String asBin = new String();
		for (int i = 0; i < message.length(); i++) {
			if (message.charAt(i) == '\n') {
				asBin += toBinaryString('\12', 7);
			} else if(message.charAt(i) == '_') {
				/* I don't really know why, but the underscore is shown as '§' otherwise */
				asBin += toBinaryString('\21', 7);
			} else {
				asBin += toBinaryString(message.charAt(i), 7);
			}
		}

		if (bit7) {
			return codeSeptetIntoOctets(asBin).toUpperCase();
		} else {
			return binToHex(asBin, 7).toUpperCase();
		}
	}

	/**
	 * Codes septets into octets. See: http://www.dreamfabric.com/sms/hello.html
	 * for a step by step how that works
	 * 
	 * @param s
	 * @return
	 */
	private static String codeSeptetIntoOctets(String s) {
		int nbrOfSeptets;
		StringBuffer messageAsBin = new StringBuffer();
		StringBuffer[] septets;

		// calc number of septets
		nbrOfSeptets = (int) Math.ceil(s.length() / 7f);
		septets = new StringBuffer[nbrOfSeptets];

		for (int i = 0; i < nbrOfSeptets; i++) {
			septets[i] = new StringBuffer(s.substring(i * 7, (i * 7) + 7));
		}

		int next = 0;
		for (int i = 0; i < nbrOfSeptets; i++) {
			if (i == nbrOfSeptets - 1) {
				messageAsBin.append(septets[i]);
			} else {
				if (septets[i].length() == 0) {
					continue;
				}
				messageAsBin.append(septets[i + 1].substring(6 - (next % 7))
						+ septets[i]);
				septets[i + 1].delete(6 - (next % 7), 7);
				next++;
			}
		}

		return binToHex(messageAsBin.toString(), 8);
	}

	/**
	 * Converts binary strings to hex.
	 * 
	 * @param messageAsBin
	 * @param step
	 * @return
	 */
	private static String binToHex(String messageAsBin, int step) {
		StringBuffer ret = new StringBuffer();
		int i = 0;
		while (i < messageAsBin.length()) {
			if (i + step >= messageAsBin.length()) {
				ret.append(convertHexToTwoBytes(Integer
						.toHexString(binToInt(messageAsBin.substring(i)))));
			} else {
				ret.append(convertHexToTwoBytes(Integer
						.toHexString(binToInt(messageAsBin.substring(i, i
								+ step)))));
			}
			i += step;
		}
		return ret.toString();
	}

	/**
	 * convert binary string to integer
	 * 
	 * @param bin
	 * @return
	 */
	private static int binToInt(String bin) {
		int val, exp;
		exp = 1;
		val = 0;
		for (int i = bin.length() - 1; i >= 0; i--) {
			val += exp * Integer.valueOf(bin.charAt(i) - 48);
			exp += exp;
		}
		return val;
	}

	/**
	 * Returns a string represented as binaries. The method makes sure the
	 * length will always be @length long (fills with leading zeroes)
	 * 
	 * @param bin
	 * @param length
	 * @return
	 */
	private static String toBinaryString(char bin, int length) {
		StringBuffer ret = new StringBuffer(Integer.toBinaryString(bin));
		while (ret.length() < length) {
			ret.insert(0, "0");
		}
		return ret.toString();
	}
}
