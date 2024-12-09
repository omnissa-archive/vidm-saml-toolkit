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

import java.util.List;

import org.opensaml.messaging.context.MessageContext;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.AuthnStatement;
import org.opensaml.saml.saml2.core.Issuer;
import org.opensaml.saml.saml2.core.NameID;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.saml.saml2.core.Status;
import org.opensaml.saml.saml2.core.StatusCode;
import org.opensaml.saml.saml2.core.Subject;
import org.opensaml.security.x509.BasicX509Credential;
import org.opensaml.xmlsec.signature.Signature;
import org.opensaml.xmlsec.signature.support.SignatureException;
import org.opensaml.xmlsec.signature.support.SignatureValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.omnissa.samltoolkit.SAMLSsoResponse;
import com.omnissa.samltoolkit.SAMLToolkitConf;

/**
 * The implementation of SSO response. This class will be generated according to
 * the message context of SSO response.
 *
 */
public class SAMLSsoResponseImpl implements SAMLSsoResponse {

	private static final long serialVersionUID = 4508967938573947130L;

	private static Logger log = LoggerFactory.getLogger(SAMLSsoResponseImpl.class);

	/** Whether response content is a valid SSO response */
	private boolean isValidResponse = false;

	/** SSO login user */
	private String username = null;

	/** SSO session id */
	private String sessionid = null;

	/** Login result of SSO */
	private boolean loginSuccess = false;

	/** Configuration of current service provider */
	private SAMLToolkitConf _conf;

	public SAMLSsoResponseImpl(MessageContext<?> context, SAMLToolkitConf conf) throws SignatureException {

		this._conf = conf;
		Response responseContent = null;
		boolean validResult = check(context);
		Object obj = context.getMessage();
		if (obj instanceof Response) {
			responseContent = (Response) obj;
		}

		if (responseContent == null || responseContent.getAssertions() == null) {
			throw new SignatureException("Content is null");
		}

		if (responseContent.getAssertions() != null && responseContent.getAssertions().size() > 0) {

			Assertion assertion = responseContent.getAssertions().get(0);

			// getname id
			if (assertion != null) {
				Subject subject = assertion.getSubject();
				if (subject != null) {
					NameID nameId = subject.getNameID();
					if (nameId != null) {
						this.username = nameId.getValue();
					}
				}
			}

			// get session id

			if (assertion != null && assertion.getAuthnStatements().size() > 0) {
				AuthnStatement authStatement = assertion.getAuthnStatements().get(0);
				if (authStatement != null) {
					sessionid = authStatement.getSessionIndex();
				}
			}
		}

		Status st = responseContent.getStatus();
		if (st != null && st.getStatusCode() != null) {
			if (st.getStatusCode().getValue().compareToIgnoreCase(StatusCode.SUCCESS) == 0) {
				loginSuccess = true;
			}
		}

		this.isValidResponse = validResult && responseContent != null;
	};

	@Override
	public String getNameId() {
		return this.username;
	}

	@Override
	public String getSessionIndex() {

		return this.sessionid;
	}

	@Override
	public boolean ssoSucceed() {
		return loginSuccess;
	}

	@Override
	public boolean isValid() {
		return this.isValidResponse;
	}

	private boolean validateSignature(Signature signature) {

		String certValue = _conf.getCertificate();
		java.security.cert.X509Certificate jX509Cert = SAMLUtil.transfer2X509Certificate(certValue);
		if (null == jX509Cert) {
			return false;
		}

		// Setup validation
		BasicX509Credential publicCredential = new BasicX509Credential(jX509Cert);
		try {
			SignatureValidator.validate(signature, publicCredential);
		} catch (SignatureException e) {
			log.error("Exception when validating signature", e);
			return false;
		}

		return true;
	}

	private boolean validateIssuer(Issuer issuer) {
		return true;
	}

	private boolean validateInResponseTo(String inResponseTo) {
		return true;
	}

	private boolean check(MessageContext<?> context) {

		Object obj = context.getMessage();

		// whether is valid response
		Response resp = null;
		if (!(obj instanceof Response)) {
			log.error("SAML response format is not valid");
			return false;
		}

		resp = (Response) obj;

		// validate responseto
		// In sso, the field should be the UUID of authnrequest
		if (resp.getInResponseTo() != null) {
			if (validateInResponseTo(resp.getInResponseTo()) == false) {
				log.error("ResponseTo id doesn't match the sender.");
				return false;
			}
		}

		// validate signature
		if (resp.getSignature() != null) {
			if (validateSignature(resp.getSignature()) == false) {
				log.error("Failed to validate response signature");
				return false;
			}
		}

		// validate issuer
		if (validateIssuer(resp.getIssuer()) == false) {
			log.error("The result of validating issuer is failure.");
			return false;
		}

		// validate assertion signature
		List<Assertion> assertions = resp.getAssertions();
		if (assertions == null) {
			log.error("No assertions");
			return false;
		}

		for (Assertion assertion : resp.getAssertions()) {
			if (assertion != null && assertion.getSignature() != null) {
				boolean validateRes = validateSignature(assertion.getSignature());
				if (validateRes == false) {
					log.warn("validate assertion failed!");
					return false;
				}
			}
		}

		return true;
	}
}
