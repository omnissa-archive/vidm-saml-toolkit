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
package com.omnissa.eucenablement.saml.impl;

import java.util.ArrayList;
import java.util.List;

import com.omnissa.eucenablement.saml.api.IdpLogoutMetadata;
import com.omnissa.eucenablement.saml.api.IdpMainMetadata;
import com.omnissa.eucenablement.saml.api.IdpSsoMetadata;

public class IdpMainMetadataImpl implements IdpMainMetadata {

	/** X509 certificate of vIDM for signing */
	private String signingKey = null;

	/** X509 certificate of vIDM for encrpytion */
	private String encrpytionKey = null;

	/** URI of vIDM for idp */
	private String idpUri = null;

	/** metadata list of single sign on service for vIDM */
	private List<IdpSsoMetadata> ssoMetadataList = new ArrayList<IdpSsoMetadata>();

	/** metadata list of single log out service for vIDM */
	private List<IdpLogoutMetadata> logoutMetadataList = new ArrayList<IdpLogoutMetadata>();

	@Override
	public String getSigningKey() {
		return this.signingKey;
	}

	@Override
	public String getEncrpytionKey() {
		return this.encrpytionKey;
	}

	@Override
	public List<IdpSsoMetadata> getSsoMetadata() {
		return ssoMetadataList;
	}

	@Override
	public List<IdpLogoutMetadata> getLogoutMetadata() {
		return this.logoutMetadataList;
	}

	/**
	 * Sets the X509 certificate of vIDM for signing.
	 *
	 * @param signingKey
	 *            X509 certificate of vIDM for signing
	 */
	public void setSigningKey(String signingKey) {
		this.signingKey = signingKey;
	}

	/**
	 * Sets the X509 certificate of vIDM for encryption.
	 *
	 * @param signingKey
	 *            X509 certificate of vIDM for encryption
	 */
	public void setEncrpytionKey(String encrpytionKey) {
		this.encrpytionKey = encrpytionKey;
	}

	/**
	 * Add the SSO information.
	 *
	 * @param binding
	 *            binding type of SSO service on vIDM
	 * @param location
	 *            URI, usually a URL, for the location of the SSO service on vIDM
	 */
	public void addSsoMetadata(String binding, String location) {
		IdpSsoMetadata ssoMetadata = new IdpSsoMetadataImpl(binding, location);
		if (this.ssoMetadataList != null) {
			this.ssoMetadataList.add(ssoMetadata);
		}
	}

	/**
	 * Add the single logout information on vIDM.
	 *
	 * @param binding
	 *            binding type of SLO service on vIDM
	 * @param location
	 *            URI, usually a URL, for the location of the SLO service on vIDM
	 * @param responseLocation
	 *            the SLO responses should be sent to this location
	 */
	public void addLogoutMetadata(String binding, String location, String responseLocation) {
		IdpSingleLogoutMetadataImpl logoutMetadata = new IdpSingleLogoutMetadataImpl(binding, location,
				responseLocation);
		this.logoutMetadataList.add(logoutMetadata);
	}

	/**
	 * set the Idp URI.
	 *
	 * @param uri
	 *            IDP URI
	 */
	public void setIdpUri(String uri) {
		this.idpUri = uri;
	}

	@Override
	public String getIdpUri() {
		return idpUri;
	}

}
