////////////////////////////////////////////////////////////
// Checksum.java
//
// Terry Baume, 2009-2010
// terry@bogaurd.net
//
// This class provides methods for validating checkums
//
////////////////////////////////////////////////////////////

public class Checksum {

	//
	// Validate an XOR checksum
	//
	public static boolean validateXORSum(String inputString) {
		int c;	int xor = 0;
	
		// Read the checksum from the input string
		String sumString = inputString.substring(inputString.length()-2, inputString.length());
		int sum;
		try {
			sum = Integer.parseInt(sumString, 16);
		} catch (Exception e) {
			return false;
		}
		
		// Calculate what the checksum for the string should be
		for (int i = 0; i < inputString.length(); i++) {
			c = inputString.charAt(i);
			if (c == '*' && i == inputString.length() - 3) break;
			if (c != '$') xor ^= c;
		}
		
		// Does the sum validate?
		if (sum == xor) {
			return true;
		} else {
			return false;
		}
	}
	
	//
	// Calculate a CRC16 checksum
	//
	public static int CRC16Sum(String input) throws Exception {
		
		int crc = 0xFFFF;          // initial value
        int polynomial = 0x1021;   // 0001 0000 0010 0001  (0, 5, 12) 
		byte[] bytes = input.getBytes("ASCII");
		
		for (byte b : bytes) {
            for (int i = 0; i < 8; i++) {
                boolean bit = ((b   >> (7-i) & 1) == 1);
                boolean c15 = ((crc >> 15    & 1) == 1);
                crc <<= 1;
                if (c15 ^ bit) crc ^= polynomial;
             }
        }
        crc &= 0xffff;
		return crc;
	}
	
	//
	// Validate a CRC16 checksum
	//
	public static boolean validateCRC16Sum(String inputString) {
		
		// Read the checksum from the string
		String sumString = inputString.substring(inputString.length()-5, inputString.length());
		System.out.println("Read:" + sumString);
		int calcSum;
		
		try {
			System.out.println(inputString.substring(2, inputString.length()-6));
			calcSum = CRC16Sum(inputString.substring(2, inputString.length()-6));
			System.out.println(Integer.toHexString(calcSum));
		} catch (Exception e) {
			return false;
		}
		
		try {
			if (Integer.toHexString(calcSum).toLowerCase().equals(sumString.toLowerCase())) { 
				return true;
			} else {
				return false;
			}
		} catch (Exception e) {
			return false;
		}
		
	}

}