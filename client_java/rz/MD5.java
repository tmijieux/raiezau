package rz;

import java.io.FileInputStream;
import java.security.*;

public class MD5 {
    private MD5() {}

    public static String hash(FileInputStream in) {
        MessageDigest md;
        try {
             md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("cannot compute md5");
        }

        byte[] dataBytes = new byte[1024];

        int nread = 0;
        try {
            while ((nread = in.read(dataBytes)) != -1) {
                md.update(dataBytes, 0, nread);
            };
        } catch (Exception e) {
            Log.severe("can not read file" + in);
        }

        byte[] mdbytes = md.digest();

        //convert the byte to hex format
        StringBuffer sb = new StringBuffer("");
        for (int i = 0; i < mdbytes.length; i++) {
            String tmp = Integer.toString((mdbytes[i] & 0xff) + 0x100, 16);
            sb.append(tmp.substring(1));
        }
        return sb.toString();
    }
}
