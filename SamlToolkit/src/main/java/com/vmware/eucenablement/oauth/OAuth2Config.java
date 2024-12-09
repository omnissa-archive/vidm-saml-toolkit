package com.omnissa.eucenablement.oauth;

import java.io.IOException;
import java.net.URLEncoder;

/**
 * <p>OAuth2 config. Including APP_KEY, APP_SECRET, and your REDIRECT_URI.</p>
 * <p>Created by chenzhang on 2017-08-15.</p>
 */
public class OAuth2Config {

    private String APP_ID;
    private String APP_SECRET;
    private String REDIRECT_URI;

    public OAuth2Config(String APP_ID, String APP_SECRET, String REDIRECT_URI) {
        this.APP_ID=APP_ID;
        this.APP_SECRET=APP_SECRET;
        this.REDIRECT_URI = REDIRECT_URI;
    }

    public String get_APP_ID() {return APP_ID;}
    public String get_APP_SECRET() {return APP_SECRET;}
    public String get_REDIRECT_URI() {return REDIRECT_URI;}
    public String get_REDIRECT_URI_ENCODED() throws IOException {
        return URLEncoder.encode(REDIRECT_URI, "utf-8");
    }

}
