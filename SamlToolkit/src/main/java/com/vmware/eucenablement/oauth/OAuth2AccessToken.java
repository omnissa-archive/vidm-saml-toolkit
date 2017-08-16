package com.vmware.eucenablement.oauth;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by chenzhang on 2017-08-16.
 */
public class OAuth2AccessToken {

    private String access_token;
    private String refresh_token;
    private long expires_in;

    /**
     * All the extra data will save to this map
     */
    private Map<String, Object> extraInfo;

    /**
     * Timestamp of initialization
     */
    private long timestamp;

    public OAuth2AccessToken() {
        timestamp=System.currentTimeMillis();
        extraInfo=new HashMap<>();
    }

    public void setAccessToken(String access_token) {
        this.access_token = access_token;
    }
    public void setRefreshToken(String refresh_token) {
        this.refresh_token = refresh_token;
    }
    public void setExpiresIn(long expires_in) {
        this.expires_in=expires_in;
    }
    public void setInfos(Map<String, Object> extraInfo) {
        this.extraInfo=extraInfo;
    }
    public void addInfos(Map<String, Object> extraInfo) {
        this.extraInfo.putAll(extraInfo);
    }

    public boolean isValid() {
        return !OAuthUtil.isStringNullOrEmpty(access_token) && System.currentTimeMillis()-timestamp<=1000*expires_in;
    }

    public String getAccessToken() {
        return access_token;
    }

    public long getExpiresIn() {return expires_in;}

    public String getRefreshToken() {
        return refresh_token;
    }

    public Map<String, Object> getInfos() {return extraInfo;}

    public <T> T getValue(String key) {
        return getValue(key, null);
    }

    public <T> T getValue(String key, T defaultValue) {
        if (extraInfo==null || !extraInfo.containsKey(key))
            return defaultValue;
        try {
            return (T)extraInfo.get(key);
        }
        catch (Exception e) {
            return defaultValue;
        }
    }


}
