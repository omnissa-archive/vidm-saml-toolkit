/*
 * Omnissa Identity Manager SAML Toolkit
 * 
 * Copyright (c) 2016 Omnissa, LLC. All Rights Reserved.
 * 
 * This product is licensed to you under the BSD-2 license (the "License").  You may not use this product except in compliance with the BSD-2 License. 
 * 
 * This product may include a number of subcomponents with separate copyright notices and license terms. Your use of these subcomponents is subject to the terms and conditions of the subcomponent's license, as noted in the LICENSE file. 
 * 
 */
package com.omnissa.samltoolkit;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SAMLToolkitConf {

	private String issuerName = null;

	private String consumerURL = null;

	//contains only two bindings for now, one for redirect, one for post
	private Map<String, String> loginBindings = new ConcurrentHashMap<String, String>();

	// this map contain only one binding for redirect, may contain more in the future
	private Map<String, String> logoutBindings = new ConcurrentHashMap<String, String>();

	private String certificate = null;
	private String idpURL = null;

	private boolean byPassSSLCertValidation = false;

	public String getIssuerName() {
		return issuerName == null ? consumerURL : issuerName;
	}

	/**
	 *
	 * @param issuerName
	 *            optional, use consumer as issuer if not set
	 */
	public void setIssuerName(String issuerName) {
		this.issuerName = issuerName;
	}

	public String getConsumerURL() {
		return consumerURL;
	}

	/**
	 *
	 * @param consumerURL
	 *            must-have, normally this is the consumer URL developed by the
	 *            customer
	 */
	public void setConsumerURL(String consumerURL) {
		this.consumerURL = consumerURL;
	}

	public String getSSOTargetURL(String binding) {
		return loginBindings.get(binding.toLowerCase());
	}

	public String getSignoutTargetURL(String binding) {
		return this.logoutBindings.get(binding.toLowerCase());
	}

	/**
	 * Optional. If not set, the bindings will be retrived from IDP metadata
	 *
	 * @param binding
	 * @param targetURL
	 */
	public void setLoginBinding(String binding, String targetURL) {
		this.loginBindings.put(binding.toLowerCase(), targetURL);
	}

	/**
	 * Optional. If not set, the bindings will be retrived from IDP metadata
	 *
	 * @param binding
	 * @param targetURL
	 */
	public void setLogoutBinding(String binding, String targetURL) {
		this.logoutBindings.put(binding.toLowerCase(), targetURL);
	}

	public String getCertificate() {
		return certificate;
	}

	/**
	 *
	 * @param certificate
	 *            optinal, if not set, will be retrived from vidm idp meta data
	 *            with a http request
	 */
	public void setCertificate(String certificate) {
		this.certificate = certificate;
	}

	public String getIdpURL() {
		return idpURL;
	}

	/**
	 *
	 * @param idpURL
	 *            must-have, normally, this is a root URL for your vidm, like
	 *            https://vidm.stengdomain.fvt The idp metadata url should be
	 *            like
	 *            https://vidm.stengdomain.fvt/SAAS/API/1.0/GET/metadata/idp.xml
	 */
	public void setIdpURL(String idpURL) {
		// remove "/"
		if ((idpURL != null) && idpURL.endsWith("/")) {
			this.idpURL = idpURL.substring(0, idpURL.length() - 1);
		} else {
			this.idpURL = idpURL;
		}
	}

	/**
	 *
	 * @return true if the config is setup correctly with all must-have values
	 */
	public boolean isReady() {
		return this.consumerURL != null && this.idpURL != null;
	}

	boolean shouldRequestVIDMMetaData() {
		return this.certificate == null || this.loginBindings.isEmpty();
	}

	public boolean isByPassSSLCertValidation() {
		return byPassSSLCertValidation;
	}

	/**
	 *
	 * @param byPassSSLCertValidation
	 *            Optional value, if true, then ignore SSL certificate error,
	 *            this is useful if vIDM is using self signed certificate
	 */
	public void setByPassSSLCertValidation(boolean byPassSSLCertValidation) {
		this.byPassSSLCertValidation = byPassSSLCertValidation;
	}

	public Map<String, String> getLoginBindings() {
		return Collections.unmodifiableMap(this.loginBindings);
	}

	public Map<String, String> getLogoutBindings() {
		return Collections.unmodifiableMap(this.logoutBindings);
	}
}
