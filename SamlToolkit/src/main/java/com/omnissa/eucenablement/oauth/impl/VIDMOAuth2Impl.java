package com.omnissa.eucenablement.oauth.impl;

import com.omnissa.eucenablement.oauth.OAuth2;
import com.omnissa.eucenablement.oauth.OAuth2AccessToken;
import com.omnissa.eucenablement.oauth.OAuth2Config;
import com.omnissa.eucenablement.oauth.OAuthException;
import com.omnissa.eucenablement.oauth.util.HttpRequest;
import com.omnissa.eucenablement.oauth.util.OAuthUtil;

import org.json.JSONObject;
import org.opensaml.xml.util.Base64;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>VIDM OAuth2 Implementation.</p>
 * <p>Created by chenzhang on 2017-08-25.</p>
 */
public class VIDMOAuth2Impl extends OAuth2 {

    private String host;

    public VIDMOAuth2Impl(OAuth2Config config) {
        super(config);
    }

    /**
     * Set the VIDM Host
     * @param _host
     */
    public void setHost(String _host) {
        host=_host;
    }

    /**
     * Get authorization url for redirect.
     *
     * @param state
     * @param additionalParams
     * @return
     * @throws OAuthException
     * @throws IOException
     */
    @Override
    public String getAuthorizationUrl(String state, Map<String, String> additionalParams) throws OAuthException, IOException {
        if (host==null) throw new OAuthException("VIDM host is invalid!");
        StringBuilder builder=new StringBuilder();
        builder.append(String.format("%s/SAAS/auth/oauth2/authorize?response_type=code&client_id=%s&" +
                "redirect_uri=%s&state=%s&scope=openid+user+email",
                host, oAuth2Config.get_APP_ID(), oAuth2Config.get_REDIRECT_URI_ENCODED(), state));
        OAuthUtil.additionalParamsToStringBuilder(builder, additionalParams);
        return builder.toString();
    }

    /**
     * Google OAuth needs post method to get access token
     * @return
     */
    @Override
    protected HttpRequest.Method getAccessTokenMethod() {
        return HttpRequest.Method.POST;
    }

    /**
     * Get access_token url
     *
     * @param code
     * @param additionalParams
     * @return
     * @throws OAuthException
     * @throws IOException
     */
    @Override
    protected String getAccessTokenUrl(String code, Map<String, String> additionalParams) throws OAuthException, IOException {
        if (host==null) throw new OAuthException("VIDM host is invalid!");
        if (additionalParams==null)
            throw new OAuthException("Additional Params cannot be null!");
        additionalParams.put("grant_type", "authorization_code");
        additionalParams.put("code", code);
        additionalParams.put("redirect_uri", oAuth2Config.get_REDIRECT_URI());
        return host+"/SAAS/auth/oauthtoken";
    }

    @Override
    protected void addHeader(HttpRequest request) {
        request.header("Authorization", "Basic "+
                Base64.encodeBytes((oAuth2Config.get_APP_ID()+":"+oAuth2Config.get_APP_SECRET()).getBytes(), Base64.DONT_BREAK_LINES));
    }

    /**
     * As the user profile is directly returned in
     * <code>id_token</code>, we may decode them.
     * @param code
     * @param additionalParams
     * @return
     * @throws OAuthException
     * @throws IOException
     */
    @Override
    public OAuth2AccessToken getAccessTokenFromOAuthServer(String code, Map<String, String> additionalParams) throws OAuthException, IOException {
        super.getAccessTokenFromOAuthServer(code, additionalParams);

        // decode "id_token" to user info
        JSONObject jsonObject = OAuthUtil.decodeJWT((String)accessToken.getValue("id_token"));
        Map<String, Object> map=new HashMap<>();
        for (String key: jsonObject.keySet()) {
            map.put(key, jsonObject.opt(key));
        }
        accessToken.addInfos(map);
        return accessToken;
    }

    protected HttpRequest.Method refreshAccessTokenMethod() {
        return HttpRequest.Method.POST;
    }

    /**
     * Get the refresh_token url
     *
     * @param refresh_token
     * @param additionalParams
     * @return
     * @throws OAuthException
     * @throws IOException
     */
    @Override
    protected String getRefreshTokenUrl(String refresh_token, Map<String, String> additionalParams) throws OAuthException, IOException {
        if (additionalParams==null)
            throw new OAuthException("Additional Params cannot be null!");
        // post
        additionalParams.put("refresh_token", refresh_token);
        additionalParams.put("grant_type", "refresh_token");
        return host+"/SAAS/auth/oauthtoken";
    }

    @Override
    protected String getUserInfoUrl(Map<String, String> additionalParams) throws OAuthException, IOException {
        return null;
    }

    @Override
    public Map<String, Object> getUserInfo(Map<String, String> additionalParams) throws OAuthException, IOException {
        // In VIDM oauth: the user info is returned..
        return accessToken.getInfos();
    }

    public String getUsername() {
        String sub=accessToken.getValue("sub");
        if (sub==null) return null;
        int index=sub.indexOf('@');
        if (index==-1) return sub;
        return sub.substring(0, index);
    }

    public String getEmail() {
        return accessToken.getValue("email");
    }

}
