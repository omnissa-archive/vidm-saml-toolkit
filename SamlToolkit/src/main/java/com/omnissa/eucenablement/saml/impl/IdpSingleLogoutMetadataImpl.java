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

import com.omnissa.eucenablement.saml.api.IdpLogoutMetadata;

public class IdpSingleLogoutMetadataImpl implements IdpLogoutMetadata {

	/** Supported binding type of single logout service */
	private String binding;

	/** URI of single logout service */
	private String location;
	private String responseLocation;

	public IdpSingleLogoutMetadataImpl(String binding, String location, String responseLocation) {
		this.binding = binding;
		this.location = location;
		this.responseLocation = responseLocation;
	}

	@Override
	public String getBinding() {
		return this.binding;
	}

	@Override
	public String getLocation() {
		return this.location;
	}

	@Override
	public String getResponseLocation() {
		return this.responseLocation;
	}
}
