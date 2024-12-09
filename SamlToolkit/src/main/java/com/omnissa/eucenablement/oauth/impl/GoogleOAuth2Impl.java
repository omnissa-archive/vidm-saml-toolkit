package com.omnissa.eucenablement.oauth.impl;

import com.omnissa.eucenablement.oauth.util.HttpRequest;
import com.omnissa.eucenablement.oauth.OAuth2;
import com.omnissa.eucenablement.oauth.OAuth2AccessToken;
import com.omnissa.eucenablement.oauth.OAuth2Config;
import com.omnissa.eucenablement.oauth.OAuthException;
import com.omnissa.eucenablement.oauth.util.OAuthUtil;

import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>Google OAuth2 Implementation.</p>
 * <p>Created by chenzhang on 2017-08-16.</p>
 */
public class GoogleOAuth2Impl extends OAuth2 {

    public GoogleOAuth2Impl(OAuth2Config config) {
        super(config);
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
        StringBuilder builder=new StringBuilder();
        builder.append(String.format("https://accounts.google.com/o/oauth2/v2/auth?client_id=%s&state=%s" +
                        "&scope=openid%%20email%%20profile&response_type=code&redirect_uri=%s&access_type=offline",
                oAuth2Config.get_APP_ID(), state, oAuth2Config.get_REDIRECT_URI_ENCODED()));
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
     * <p>Get access_token url.</p>
     * <p>As we need to post the request, we have to put all the parameters into
     * <code>additionalParams</code>, and return the base url.</p>
     * @param code
     * @param additionalParams
     * @return
     * @throws OAuthException
     * @throws IOException
     */
    @Override
    protected String getAccessTokenUrl(String code, Map<String, String> additionalParams) throws OAuthException, IOException {
        if (additionalParams==null)
            throw new OAuthException("Additional Params cannot be null!");
        // post
        additionalParams.put("code", code);
        additionalParams.put("client_id", oAuth2Config.get_APP_ID());
        additionalParams.put("client_secret", oAuth2Config.get_APP_SECRET());
        additionalParams.put("redirect_uri", oAuth2Config.get_REDIRECT_URI());
        additionalParams.put("grant_type", "authorization_code");
        return "https://www.googleapis.com/oauth2/v4/token";
    }

    protected HttpRequest.Method refreshAccessTokenMethod() {
        return HttpRequest.Method.POST;
    }

    /**
     * <p>Get refresh access_token url.</p>
     * <p>As we need to post the request, we have to put all the parameters into
     * <code>additionalParams</code>, and return the base url.</p>
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
        additionalParams.put("client_id", oAuth2Config.get_APP_ID());
        additionalParams.put("client_secret", oAuth2Config.get_APP_SECRET());
        additionalParams.put("refresh_token", refresh_token);
        additionalParams.put("grant_type", "refresh_token");
        return "https://www.googleapis.com/oauth2/v4/token";
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

    @Override
    protected String getUserInfoUrl(Map<String, String> additionalParams) throws OAuthException, IOException {
        return null;
    }

    @Override
    public Map<String, Object> getUserInfo(Map<String, String> additionalParams) throws OAuthException, IOException {
        // In google oauth: the user info is returned..
        return accessToken.getInfos();
    }

    public String getUid() {
        return accessToken==null?null:(String)accessToken.getValue("sub");
    }

}
