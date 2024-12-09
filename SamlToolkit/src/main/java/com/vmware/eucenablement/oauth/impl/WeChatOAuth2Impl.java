package com.omnissa.eucenablement.oauth.impl;

import com.omnissa.eucenablement.oauth.OAuth2;
import com.omnissa.eucenablement.oauth.OAuth2AccessToken;
import com.omnissa.eucenablement.oauth.OAuth2Config;
import com.omnissa.eucenablement.oauth.OAuthException;
import com.omnissa.eucenablement.oauth.util.OAuthUtil;

import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * <p>WeChat OAuth2 Implementation.</p>
 * <p>Created by chenzhang on 2017-08-15.</p>
 */
public class WeChatOAuth2Impl extends OAuth2 {

    public WeChatOAuth2Impl(OAuth2Config config) {
        super(config);
    }

    @Override
    public String getAuthorizationUrl(String state, Map<String, String> additionalParams) throws OAuthException, IOException {
        StringBuilder builder=new StringBuilder();
        builder.append(String.format("https://open.weixin.qq.com/connect/oauth2/authorize?" +
                "appid=%s&redirect_uri=%s&response_type=code&scope=snsapi_userinfo&state=%s",
                oAuth2Config.get_APP_ID(), oAuth2Config.get_REDIRECT_URI_ENCODED(), state));
        OAuthUtil.additionalParamsToStringBuilder(builder, additionalParams);
        builder.append("#wechat_redirect");
        return builder.toString();
    }

    public String getAuthorizationQrcodeUrl(String state) throws OAuthException, IOException{
        return String.format("https://open.weixin.qq.com/connect/qrconnect?" +
                "appid=%s&redirect_uri=%s&response_type=code&scope=snsapi_login&state=%s#wechat_redirect",
                oAuth2Config.get_APP_ID(), oAuth2Config.get_REDIRECT_URI_ENCODED(), state);
    }

    @Override
    protected String getAccessTokenUrl(String code, Map<String, String> additionalParams) throws OAuthException, IOException {
        StringBuilder builder=new StringBuilder();
        builder.append(String.format("https://api.weixin.qq.com/sns/oauth2/access_token?appid=%s&secret=%s&code=%s&grant_type=authorization_code",
                oAuth2Config.get_APP_ID(), oAuth2Config.get_APP_SECRET(), code));
        OAuthUtil.additionalParamsToStringBuilder(builder, additionalParams);
        return builder.toString();
    }

    @Override
    protected String getRefreshTokenUrl(String refresh_token, Map<String, String> additionalParams) throws OAuthException, IOException {
        StringBuilder builder=new StringBuilder();
        builder.append(String.format("https://api.weixin.qq.com/sns/oauth2/refresh_token?appid=APPID&grant_type=refresh_token&refresh_token=%s",
                refresh_token));
        OAuthUtil.additionalParamsToStringBuilder(builder, additionalParams);
        return builder.toString();
    }

    @Override
    protected String getUserInfoUrl(Map<String, String> additionalParams) throws OAuthException, IOException {
        if (accessToken==null || !accessToken.isValid())
            throw new OAuthException("Access token is invalid!");
        StringBuilder builder=new StringBuilder();
        builder.append(String.format("https://api.weixin.qq.com/sns/userinfo?access_token=%s&openid=%s",
                accessToken.getAccessToken(), getOpenId()));
        OAuthUtil.additionalParamsToStringBuilder(builder, additionalParams);
        return builder.toString();
    }

    public String getOpenId() {
        return accessToken==null?null:(String)accessToken.getValue("openid");
    }

    /**
     * <p>We can get different languages according to the locale. Default is Locale.English.</p>
     * <p>See {@link #getUserInfo(Locale)}</p>
     * @return
     * @throws OAuthException
     * @throws IOException
     */
    @Override
    public Map<String, Object> getUserInfo() throws OAuthException, IOException {
        return getUserInfo(Locale.ENGLISH);
    }

    /**
     * <p>Return the user's profile according to the locale.</p>
     * <p>Frequently used: Locale.English, Locale.SIMPLIFIED_CHINESE, Locale.TRADITIONAL_CHINESE.</p>
     * @param locale
     * @return
     * @throws OAuthException
     * @throws IOException
     */
    public Map<String, Object> getUserInfo(Locale locale) throws OAuthException, IOException {
        Map<String, String> map=new HashMap<>();
        map.put("lang", locale.toString());
        return getUserInfo(map);
    }

}
