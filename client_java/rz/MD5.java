package rz;

import java.io.*;
import java.security.*;

public class MD5 {
    private static final int BUFFER_SIZE = 1024;

    private MD5() {
    }

    private static MessageDigest getMD5Instance() {
        MessageDigest md = null;
        try {
	    md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
	    Log.abort("Cannot compute md5: " + e.toString());
        }
	return md;
    }

    private static byte[] DigestInputStream(FileInputStream in,
					    MessageDigest md) {
	byte[] dataBytes = new byte[BUFFER_SIZE];
        int nread = 0;
        try {
            while ((nread = in.read(dataBytes)) != -1) {
                md.update(dataBytes, 0, nread);
            }
        } catch (IOException e) {
            Log.abort("can not read file" + in);
        }
	return md.digest();
    }

    private static String byteArrayToHex(byte[] ba) {
        StringBuffer sb = new StringBuffer("");
        for (int i = 0; i < ba.length; i++) {
            String tmp = Integer.toString((ba[i] & 0xff) + 0x100, 16);
            sb.append(tmp.substring(1));
        }
	return sb.toString();	
    }

    public static String hash(FileInputStream in) {
        MessageDigest md = getMD5Instance();
	byte[] mdbytes = DigestInputStream(in, md);
        return byteArrayToHex(mdbytes);
    }
}
