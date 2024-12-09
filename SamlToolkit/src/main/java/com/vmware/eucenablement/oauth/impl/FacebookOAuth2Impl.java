package com.omnissa.eucenablement.oauth.impl;

import com.omnissa.eucenablement.oauth.OAuth2;
import com.omnissa.eucenablement.oauth.OAuth2Config;
import com.omnissa.eucenablement.oauth.OAuthException;
import com.omnissa.eucenablement.oauth.util.OAuthUtil;

import java.io.IOException;
import java.util.Map;

/**
 * <p>Facebook OAuth2 Implementation.</p>
 * <p>Created by chenzhang on 2017-08-16.</p>
 */
public class FacebookOAuth2Impl extends OAuth2 {

    public FacebookOAuth2Impl(OAuth2Config config) {
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
        builder.append(String.format("https://www.facebook.com/v2.10/dialog/oauth?client_id=%s&state=%s&response_type=code&redirect_uri=%s",
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
        builder.append(String.format("https://graph.facebook.com/v2.10/oauth/access_token?" +
                        "client_id=%s&client_secret=%s&redirect_uri=%s&code=%s",
                oAuth2Config.get_APP_ID(), oAuth2Config.get_APP_SECRET(), oAuth2Config.get_REDIRECT_URI_ENCODED(), code));
        OAuthUtil.additionalParamsToStringBuilder(builder, additionalParams);
        return builder.toString();
    }

    /**
     * Facebook doesn't support refreshing tokens
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
        builder.append(String.format("https://graph.facebook.com/me?access_token=%s",
                accessToken.getAccessToken()));
        OAuthUtil.additionalParamsToStringBuilder(builder, additionalParams);
        return builder.toString();
    }
}
