package com.omnissa.eucenablement.oauth;

import com.omnissa.eucenablement.oauth.util.HttpRequest;
import com.omnissa.eucenablement.oauth.util.OAuthUtil;

import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>The base class. All OAuth implementations should extend this class.</p>
 * <p>Created by chenzhang on 2017-08-15.</p>
 */
public abstract class OAuth2 {

    protected OAuth2Config oAuth2Config;
    protected OAuth2AccessToken accessToken;

    public OAuth2(OAuth2Config config) {
        oAuth2Config=config;
    }

    /**
     * Get authorization url for redirect.
     * @param state
     * @return
     * @throws OAuthException
     * @throws IOException
     */
    public String getAuthorizationUrl(String state) throws OAuthException, IOException {
        return getAuthorizationUrl(state, new HashMap<String, String>());
    }

    /**
     * Get authorization url for redirect.
     * @param state
     * @param additionalParams
     * @return
     * @throws OAuthException
     * @throws IOException
     */
    public abstract String getAuthorizationUrl(String state, Map<String, String> additionalParams) throws OAuthException, IOException;

    /**
     * Get access_token url
     * @param code
     * @param additionalParams
     * @return
     * @throws OAuthException
     * @throws IOException
     */
    protected abstract String getAccessTokenUrl(String code, Map<String, String> additionalParams) throws OAuthException, IOException;

    /**
     * Require access_token from OAuth Server
     * @param code
     * @return
     * @throws OAuthException
     * @throws IOException
     */
    public OAuth2AccessToken getAccessTokenFromOAuthServer(String code) throws OAuthException, IOException {
        return getAccessTokenFromOAuthServer(code, new HashMap<String, String>());
    }

    /**
     * <p>The method (GET/POST) to require access token from OAuth Server.</p>
     * <p>If it's a post request, then <code>getAccessTokenUrl(code, additionalParams)</code> must return
     * the base url, and put all the parameters into <code>additionalParams</code>.</p>
     * <p>See {@linkplain com.omnissa.eucenablement.oauth.impl.GoogleOAuth2Impl#getAccessTokenUrl(String, Map)
     * GoogleOAuthImpl#getAccessTokenUrl(String, Map)}.</p>
     * @return
     */
    protected HttpRequest.Method getAccessTokenMethod() {
        return HttpRequest.Method.GET;
    }

    /**
     * Add header to request if needed
     * @param request
     */
    protected void addHeader(HttpRequest request) {

    }

    /**
     * Require access_token from OAuth Server
     * @param code
     * @param additionalParams
     * @return
     * @throws OAuthException
     * @throws IOException
     */
    public OAuth2AccessToken getAccessTokenFromOAuthServer(String code, Map<String, String> additionalParams) throws OAuthException, IOException {
        HttpRequest.Method method=getAccessTokenMethod();
        String url=getAccessTokenUrl(code, additionalParams);

        // for post, we put all the parameters into additionalParams.
        HttpRequest request=HttpRequest.http(url, method);
        addHeader(request);
        if (method== HttpRequest.Method.POST)
            request.form(additionalParams);
        String response=request.body();
        JSONObject jsonObject=new JSONObject(response);
        String errmsg= decodeErrorMessage(jsonObject);
        if (!OAuthUtil.isStringNullOrEmpty(errmsg))
            throw new OAuthException(errmsg);
        return accessToken=decodeAccessToken(jsonObject);
    }

    /**
     * Get error message from JSON response. Return null if no error occurs.
     * @param jsonObject
     * @return
     */
    protected String decodeErrorMessage(JSONObject jsonObject) {
        if (jsonObject.has("error_description"))
            return jsonObject.getString("error_description");
        if (jsonObject.has("error")) {
            if (jsonObject.has("message"))
                return jsonObject.getString("message");
            try {
                JSONObject error=jsonObject.getJSONObject("error");
                return error.getString("message");
            }
            catch (Exception e) {}
            return jsonObject.getString("error");
        }
        if (jsonObject.has("errmsg"))
            return jsonObject.getString("errmsg");
        return null;
    }

    /**
     * Decode access_token from JSON response
     * @param jsonObject
     * @return
     */
    protected OAuth2AccessToken decodeAccessToken(JSONObject jsonObject) {
        OAuth2AccessToken oAuth2AccessToken=new OAuth2AccessToken();
        oAuth2AccessToken.setAccessToken(jsonObject.optString("access_token", null));
        oAuth2AccessToken.setExpiresIn(jsonObject.optLong("expires_in", 300L));
        oAuth2AccessToken.setRefreshToken(jsonObject.optString("refresh_token", null));

        Map<String, Object> map=new HashMap<>();
        for (String key: jsonObject.keySet()) {
            map.put(key, jsonObject.opt(key));
        }

        oAuth2AccessToken.setInfos(map);
        return oAuth2AccessToken;
    }

    /**
     * Get the refresh_token url
     * @param refresh_token
     * @param additionalParams
     * @return
     * @throws OAuthException
     * @throws IOException
     */
    protected abstract String getRefreshTokenUrl(String refresh_token, Map<String, String> additionalParams) throws OAuthException, IOException;

    /**
     * Refresh the access_token
     * @return
     * @throws OAuthException
     * @throws IOException
     */
    public OAuth2AccessToken refreshAccessToken() throws OAuthException, IOException {
        return refreshAccessToken(new HashMap<String, String>());
    }

    /**
     * <p>The method (GET/POST) to refresh access token from OAuth Server.</p>
     * <p>If it's a post request, then <code>getRefreshTokenUrl(refresh_token, additionalParams)</code> must return
     * the base url, and put all the parameters into <code>additionalParams</code>.</p>
     * <p>See {@linkplain com.omnissa.eucenablement.oauth.impl.GoogleOAuth2Impl#getRefreshTokenUrl(String, Map)
     * GoogleOAuthImpl#getRefreshTokenUrl(String, Map)}.</p>
     * @return
     */
    protected HttpRequest.Method refreshAccessTokenMethod() {
        return HttpRequest.Method.GET;
    }



    /**
     * Refresh the access_token
     * @param additionalParams
     * @return
     * @throws OAuthException
     * @throws IOException
     */
    public OAuth2AccessToken refreshAccessToken(Map<String, String> additionalParams)
            throws OAuthException, IOException {
        if (accessToken==null || OAuthUtil.isStringNullOrEmpty(accessToken.getAccessToken()))
            throw new OAuthException("Access Token is Invalid!");

        HttpRequest.Method method=refreshAccessTokenMethod();

        String url=getRefreshTokenUrl(accessToken.getRefreshToken(), additionalParams);
        if (url==null)
            throw new OAuthException("Refreshing token is not supported!");

        // post request: put all the parameters into additionalParams
        HttpRequest request=HttpRequest.http(url, method);
        if (method== HttpRequest.Method.POST)
            request.form(additionalParams);
        addHeader(request);
        String response=request.body();
        JSONObject jsonObject=new JSONObject(response);

        String errmsg= decodeErrorMessage(jsonObject);
        if (!OAuthUtil.isStringNullOrEmpty(errmsg))
            throw new OAuthException(errmsg);
        OAuth2AccessToken accessToken1 = decodeAccessToken(jsonObject);
        accessToken.setAccessToken(accessToken1.getAccessToken());
        accessToken.setExpiresIn(accessToken1.getExpiresIn());
        accessToken.addInfos(accessToken1.getInfos());
        return accessToken;
    }

    protected abstract String getUserInfoUrl(Map<String, String> additionalParams) throws OAuthException, IOException;

    /**
     * Get user info
     * @return
     */
    public Map<String, Object> getUserInfo() throws OAuthException, IOException {
        return getUserInfo(new HashMap<String, String>());
    }

    protected HttpRequest.Method getUserInfoMethod() {
        return HttpRequest.Method.GET;
    }

    /**
     * Get user info
     * @return
     */
    public Map<String, Object> getUserInfo(Map<String, String> additionalParams) throws OAuthException, IOException {
        String url=getUserInfoUrl(additionalParams);
        if (url==null)
            throw new OAuthException("Userinfo is not supported!");
        HttpRequest.Method method=getUserInfoMethod();
        HttpRequest request=HttpRequest.http(url, method);
        if (method==HttpRequest.Method.POST)
            request.form(additionalParams);
        addHeader(request);
        String response=request.body();
        JSONObject jsonObject=new JSONObject(response);
        String errmsg=decodeErrorMessage(jsonObject);
        if (!OAuthUtil.isStringNullOrEmpty(errmsg))
            throw new OAuthException(errmsg);
        Map<String, Object> map=new HashMap<>();
        for (String key: jsonObject.keySet()) {
            map.put(key, jsonObject.opt(key));
        }
        // accessToken.addInfos(map);
        return map;
    }

    /**
     * Get the access token from local.
     * @return
     */
    public OAuth2AccessToken getCurrentAccessToken() {
        return accessToken;
    }

}
