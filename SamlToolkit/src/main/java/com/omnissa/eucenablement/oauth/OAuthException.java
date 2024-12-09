package com.omnissa.eucenablement.oauth;

/**
 * <p>OAuth2 Exception. It's a RuntimeException, you don't need to handle it.</p>
 * <p>Created by chenzhang on 2017-08-07.</p>
 */
public class OAuthException extends RuntimeException {

    public OAuthException(String msg) {
        super(msg);
    }

}
