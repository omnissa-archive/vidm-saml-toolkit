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

import java.io.Serializable;

public interface SAMLSsoResponse extends Serializable {

	/**
	 * Whether this SSO response is valid
	 *
	 * @return true if this response is valid; Otherwise, return false.
	 */
	public boolean isValid();

	/**
	 * Get the name id of login user
	 *
	 * @return name id of login user.
	 */
	public String getNameId();

	/**
	 * whether single sign on operation is successful.
	 *
	 * @return true if sso successfully.
	 */
	public boolean ssoSucceed();

	/**
	 * get the session index of SSO assertion
	 *
	 * @return session index of SSO assertion
	 */
	public String getSessionIndex();
}
