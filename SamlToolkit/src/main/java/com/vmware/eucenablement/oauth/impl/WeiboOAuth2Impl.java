package com.omnissa.eucenablement.oauth.impl;

import com.omnissa.eucenablement.oauth.OAuth2;
import com.omnissa.eucenablement.oauth.OAuth2Config;
import com.omnissa.eucenablement.oauth.OAuthException;
import com.omnissa.eucenablement.oauth.util.OAuthUtil;

import java.io.IOException;
import java.util.Map;

/**
 * <p>Weibo OAuth2 Implementation.</p>
 * <p>Created by chenzhang on 2017-08-16.</p>
 */
public class WeiboOAuth2Impl extends OAuth2 {

    public WeiboOAuth2Impl(OAuth2Config config) {
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
        builder.append(String.format("https://api.weibo.com/oauth2/authorize?client_id=%s&state=%s&response_type=code&redirect_uri=%s",
                oAuth2Config.get_APP_ID(), state, oAuth2Config.get_REDIRECT_URI_ENCODED()));
        OAuthUtil.additionalParamsToStringBuilder(builder, additionalParams);
        return builder.toString();
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
        StringBuilder builder=new StringBuilder();
        builder.append(String.format("https://api.weibo.com/oauth2/access_token?client_id=%s&client_secret=%s&grant_type=authorization_code&redirect_uri=%s&code=%s",
                oAuth2Config.get_APP_ID(), oAuth2Config.get_APP_SECRET(), oAuth2Config.get_REDIRECT_URI_ENCODED(), code));
        OAuthUtil.additionalParamsToStringBuilder(builder, additionalParams);
        return builder.toString();
    }

    /**
     * Weibo OAuth interface doesn't support refreshing token
     *
     * @param refresh_token
     * @param additionalParams
     * @return
     * @throws OAuthException
     * @throws IOException
     */
    @Override
    protected String getRefreshTokenUrl(String refresh_token, Map<String, String> additionalParams) throws OAuthException, IOException {
        return null;
    }

    @Override
    protected String getUserInfoUrl(Map<String, String> additionalParams) throws OAuthException, IOException {
        if (accessToken==null || !accessToken.isValid())
            throw new OAuthException("Access token is invalid!");
        StringBuilder builder=new StringBuilder();
        builder.append(String.format("https://api.weibo.com/2/users/show.json?access_token=%s&uid=%s",
                accessToken.getAccessToken(), getUid()));
        OAuthUtil.additionalParamsToStringBuilder(builder, additionalParams);
        return builder.toString();
    }

    /**
     * Get the user's UID
     * @return
     */
    public String getUid() {
        return accessToken==null?null:(String)accessToken.getValue("uid");
    }

}
