package com.omnissa.eucenablement.sample.idp;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class SslUtilities {
	/*
	 * This is only for usage of the sample to trust all certificates. This
	 * should never be used on production environment
	 */
	public static TrustManager[] getTrustManager() {

		TrustManager[] certs = new TrustManager[] { new X509TrustManager() {
			public X509Certificate[] getAcceptedIssuers() {
				return null;
			}

			public void checkClientTrusted(X509Certificate[] certs, String t) {
			}

			public void checkServerTrusted(X509Certificate[] certs, String t) {
			}
		} };
		return certs;
	}

	public static void trustAllCertificates() throws NoSuchAlgorithmException, KeyManagementException {

		SSLContext sslCtx = SSLContext.getInstance("TLS");
		TrustManager[] trustMgr = getTrustManager();
		sslCtx.init(null, trustMgr, new SecureRandom());
		HttpsURLConnection.setDefaultSSLSocketFactory(sslCtx.getSocketFactory());
	}

}
