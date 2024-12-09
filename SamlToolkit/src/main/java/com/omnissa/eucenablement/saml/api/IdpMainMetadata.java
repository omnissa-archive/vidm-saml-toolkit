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
package com.omnissa.eucenablement.saml.api;

import java.util.List;

/**
 * 
 * IdpMainMetadata includes major information for SSO & SLO from vIDM
 *
 */
public interface IdpMainMetadata {
	public String getSigningKey();

	public String getEncrpytionKey();

	public String getIdpUri();

	List<IdpSsoMetadata> getSsoMetadata();

	List<IdpLogoutMetadata> getLogoutMetadata();
}
