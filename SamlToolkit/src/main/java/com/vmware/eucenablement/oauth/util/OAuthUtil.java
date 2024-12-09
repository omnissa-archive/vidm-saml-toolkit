package com.omnissa.eucenablement.oauth.util;

import com.omnissa.eucenablement.oauth.OAuthException;

import org.json.JSONObject;
import org.opensaml.xml.util.Base64;

import java.io.IOException;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Map;

/**
 * Created by chenzhang on 2017-08-07.
 */
public class OAuthUtil {

    /**
     * Encode jsessionid with current timestamp, to get "state"
     * @param jsessionid
     * @return
     */
    public static String encode(String jsessionid) {
        // md5 the current timestamp to get keys
        String time=String.valueOf(System.currentTimeMillis()/10000);
        byte[] keys=md5(time.substring(time.length()-6));

        // encode the bytes by keys
        byte[] bytes=jsessionid.getBytes().clone();
        for (int i=0,j=0;i<bytes.length;i++) {
            bytes[i]=(byte)((bytes[i]+keys[j]));
            j++;
            if (j>=keys.length) j=0;
        }

        return Base64.encodeBytes(bytes).replace('+','-').replace('=','_');
    }

    /**
     * decode state to jsessionid
     * @param state
     * @return
     */
    public static String decode(String state) {
        state=state.replace('-','+').replace('_','=');
        long currtime=System.currentTimeMillis()/10000;

        // 3 minutes, 180 seconds
        for (int t=0;t<18;t++) {
            // get keys
            String time=String.valueOf(currtime-t);
            byte[] keys=md5(time.substring(time.length()-6));

            // decode
            byte[] bytes=Base64.decode(state);
            for (int i=0,j=0;i<bytes.length;i++) {
                bytes[i]=(byte)((bytes[i]-keys[j]));
                j++;
                if (j>=keys.length) j=0;
            }

            String jsessionid=new String(bytes);
            // check if it's an valid jsessionid
            if (jsessionid.matches("[a-z0-9]{20,40}\\.[a-z0-9]{5}")) {
                return jsessionid;
            }
        }

        // can not decode: invalid
        return null;
    }

    private static byte[] md5(String s) {
        try {
            MessageDigest digest=MessageDigest.getInstance("MD5");
            return Arrays.copyOfRange(digest.digest(s.getBytes()), 0, 6);
        }
        catch (Exception e) {
            return Base64.encodeBytes(s.getBytes()).substring(0, 6).getBytes();
        }
    }

    public static StringBuilder additionalParamsToStringBuilder(StringBuilder builder, Map<String, String> additionalParams) throws IOException {
        if (additionalParams!=null && !additionalParams.isEmpty()) {
            if (!builder.toString().contains("?"))
                builder.append('?');
            for (Map.Entry<String, String> entry: additionalParams.entrySet()) {
                if (builder.charAt(builder.length()-1)!='?')
                    builder.append('&');
                builder.append(URLEncoder.encode(entry.getKey(), "utf-8"));
                builder.append('=');
                builder.append(URLEncoder.encode(entry.getValue(), "utf-8"));
            }
        }
        return builder;
    }

    public static boolean isStringNullOrEmpty(String string) {
        return string==null || "".equals(string);
    }

    public static JSONObject decodeJWT(String jwtString) throws OAuthException {
        String[] strings=jwtString.split("\\.");
        if (strings.length!=3) throw new OAuthException("Invalid JWT String!");
        try {
            String body=strings[1].replace('-','+').replace('_','/');
            switch (body.length()%4) {
                case 0: break;
                case 2: body+="==";break;
                case 3: body+="=";break;
            }
            return new JSONObject(new String(Base64.decode(body)));
        }
        catch (Exception e) {
            throw new OAuthException(e.getMessage());
        }
    }
}
