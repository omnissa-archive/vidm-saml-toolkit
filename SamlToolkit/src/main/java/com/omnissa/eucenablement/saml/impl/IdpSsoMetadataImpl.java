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

import com.omnissa.eucenablement.saml.api.IdpSsoMetadata;

public class IdpSsoMetadataImpl implements IdpSsoMetadata {

	/** Supported binding type of single sign on service */
	private String binding;

	/** URI of single sign on service */
	private String location;

	public IdpSsoMetadataImpl(String binding, String location) {
		this.binding = binding;
		this.location = location;
	}

	@Override
	public String getBinding() {
		return this.binding;
	}

	@Override
	public String getLocation() {
		return this.location;
	}
}
