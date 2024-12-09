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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.UUID;

import javax.annotation.Nonnull;
import javax.xml.namespace.QName;

import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.core.xml.XMLObjectBuilder;
import org.opensaml.core.xml.XMLObjectBuilderFactory;
import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.Audience;
import org.opensaml.saml.saml2.core.AudienceRestriction;
import org.opensaml.saml.saml2.core.AuthnContext;
import org.opensaml.saml.saml2.core.AuthnContextClassRef;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.AuthnStatement;
import org.opensaml.saml.saml2.core.Conditions;
import org.opensaml.saml.saml2.core.Issuer;
import org.opensaml.saml.saml2.core.LogoutRequest;
import org.opensaml.saml.saml2.core.NameID;
import org.opensaml.saml.saml2.core.NameIDPolicy;
import org.opensaml.saml.saml2.core.NameIDType;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.saml.saml2.core.SessionIndex;
import org.opensaml.saml.saml2.core.Status;
import org.opensaml.saml.saml2.core.StatusCode;
import org.opensaml.saml.saml2.core.Subject;
import org.opensaml.saml.saml2.core.SubjectConfirmation;
import org.opensaml.saml.saml2.core.SubjectConfirmationData;
import org.opensaml.saml.saml2.metadata.Endpoint;
import org.opensaml.saml.saml2.metadata.SingleSignOnService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.omnissa.samltoolkit.idp.SAMLSsoRequest;

public class SAMLUtil {

	private static Logger log = LoggerFactory.getLogger(SAMLUtil.class);

	/**
	 * Generate the ID for SAML message.
	 *
	 * @return ID for SAML message
	 */
	public static String generateSamlId() {
		return UUID.randomUUID().toString();
	}

	private static XMLObjectBuilderFactory builderFactory = XMLObjectProviderRegistrySupport.getBuilderFactory();

	/**
	 * Generate SAML object according to the element name.
	 *
	 * @param name
	 *            element name
	 * @return SAML object; This function will return null if cannot build SAML
	 *         object
	 */
	private static <T extends XMLObject> T buildSAMLObject(@Nonnull final QName name) {
		T object = null;
		if (name == null) {
			log.error("Failed to build SAML Object: QName is null!");
			return null;
		}

		try {

			@SuppressWarnings("unchecked")
			XMLObjectBuilder<T> builder = (XMLObjectBuilder<T>) builderFactory.getBuilder(name);

			if (builder != null) {
				object = builder.buildObject(name);
			} else {
				log.error("XMLObject for " + name.toString() + " is null!");
			}
		} catch (Exception e) {
			log.error("Build SAML Object failed", e);
			object = null;
		}

		return object;
	}

	/**
	 * convert key to pem format
	 *
	 */
	public static String convertCertToPemFormat(String cert) {
		return formatPEMString(VidmSamlConstants.BEGIN_CERT_FULL, VidmSamlConstants.END_CERT_FULL, cert);
	}

	private static String formatPEMString(final String head, final String foot, final String indata) {
		StringBuilder pem = new StringBuilder(head);
		pem.append("\n");

		String data;
		if (indata != null) {
			data = indata.replaceAll("\\s+", "");
		} else {
			data = "";
		}
		int lineLength = 64;
		int dataLen = data.length();
		int si = 0;
		int ei = lineLength;

		while (si < dataLen) {
			if (ei > dataLen) {
				ei = dataLen;
			}

			pem.append(data.substring(si, ei));
			pem.append("\n");
			si = ei;
			ei += lineLength;
		}

		pem.append(foot);

		return pem.toString();
	}

	/**
	 * transfer certificate content to X509Certificate object
	 *
	 * @param cert
	 *            content of certificate
	 * @return X509Certificate object
	 */
	public static X509Certificate transfer2X509Certificate(String cert) {

		// return null if no certificate content
		if (null == cert) {
			log.error("The input cert for transfer2X509Certificate is null!");
			return null;
		}
		/*
		 * Test cert = "-----BEGIN CERTIFICATE-----\n" +
		 * "MIID7DCCAtSgAwIBAgIFFHYYEzIwDQYJKoZIhvcNAQELBQAwgawxCzAJBgNVBAYT\n"
		 * "qNHgsx8lHUoenasijd4sJPnj3YKz2Q9lHjSIOgMK41PSgVymOY2W7y2ANoNNKR0Q\n"
		 * + "-----END CERTIFICATE-----";
		 */
		if (!cert.contains(VidmSamlConstants.BEGIN_CERT)) {
			cert = convertCertToPemFormat(cert);
		}

		InputStream certinputstream = new ByteArrayInputStream(cert.getBytes());
		CertificateFactory cf = null;
		X509Certificate x509 = null;
		try {
			cf = CertificateFactory.getInstance("X.509");
			x509 = (X509Certificate) cf.generateCertificate(certinputstream);
		} catch (CertificateException e) {
			log.error("Caught CertificateException", e);
			return null;
		}

		return x509;
	}

	/**
	 * generate issuer
	 *
	 * @param issuerName
	 *            name of issuer
	 * @return issuer with this name
	 */
	public static Issuer generateIssuer(String issuerName) {
		Issuer issuer = SAMLUtil.buildSAMLObject(Issuer.DEFAULT_ELEMENT_NAME);

		issuer.setValue(issuerName);
		issuer.setFormat(NameIDType.ENTITY);

		return issuer;
	}

	/**
	 * Generate endpoint based on the input parameters.
	 *
	 * @param service
	 *            Element name of service
	 * @param location
	 *            URI, usually a URL, for the location of the Endpoint
	 * @param responseLocation
	 *            The URI responses should be sent to this for this Endpoint
	 * @param binding
	 *            The URI identifier for the binding supported by the Endpoint
	 */
	public static Endpoint generateEndpoint(String location, String responseLocation, String binding) {
		Endpoint samlEndpoint = SAMLUtil.buildSAMLObject(SingleSignOnService.DEFAULT_ELEMENT_NAME);

		samlEndpoint.setLocation(location);
		if (binding != null) {
			samlEndpoint.setBinding(binding);
		}

		if (StringUtils.isNotEmpty(responseLocation))
			samlEndpoint.setResponseLocation(responseLocation);

		return samlEndpoint;
	}

	/**
	 * Generate SLO request
	 *
	 * @param nameIdValue
	 *            user name
	 * @param sessionIndex
	 *            Session index of SSO
	 * @param logoutURL
	 *            the location of SLO
	 * @param issuerName
	 *            request sender
	 * @return SAML object; This function will return null if cannot build SAML
	 *         object
	 */
	public static LogoutRequest genererateLogoutRequest(String nameIdValue, String sessionIndex, String logoutURL,
			String issuerName) {
		LogoutRequest logoutRequest = (LogoutRequest) SAMLUtil.buildSAMLObject(LogoutRequest.DEFAULT_ELEMENT_NAME);

		logoutRequest.setID(SAMLUtil.generateSamlId());
		logoutRequest.setDestination(logoutURL);

		logoutRequest.setIssueInstant(new DateTime());
		logoutRequest.setIssuer(SAMLUtil.generateIssuer(issuerName));
		SessionIndex sessionIndexElement = (SessionIndex) SAMLUtil.buildSAMLObject(SessionIndex.DEFAULT_ELEMENT_NAME);
		sessionIndexElement.setSessionIndex(sessionIndex);
		logoutRequest.getSessionIndexes().add(sessionIndexElement);

		NameID nameId = (NameID) SAMLUtil.buildSAMLObject(NameID.DEFAULT_ELEMENT_NAME);
		nameId.setValue(nameIdValue);
		logoutRequest.setNameID(nameId);

		return logoutRequest;
	}

	/**
	 * generate simple saml sso request
	 *
	 * @param assertionConsumerServiceURL
	 *            consumer URL to handle the SSO response
	 * @param ssoTargetUrl
	 *            SSO service URL on IDP
	 * @param issuerName
	 *            issuer of this application
	 * @param binding
	 *            binding type to communicate with IDP for SSO feature
	 */
	public static AuthnRequest createAuthRequest(String assertionConsumerServiceURL, String ssoTargetUrl,
			String issuerName, String binding) {

		AuthnRequest request = (AuthnRequest) SAMLUtil.buildSAMLObject(AuthnRequest.DEFAULT_ELEMENT_NAME);

		request.setAssertionConsumerServiceURL(assertionConsumerServiceURL);
		request.setID(SAMLUtil.generateSamlId());
		request.setIssueInstant(new DateTime());
		request.setDestination(ssoTargetUrl);
		request.setIssuer(SAMLUtil.generateIssuer(issuerName));

		// protocol binding
		request.setProtocolBinding(binding);
		request.setNameIDPolicy(buildNameIdPolicy());
		return request;
	}

/**
 *
 * @param request
 * @param issuerStr like https://localhost:8443/SamlSample/idp.xml
 * @param userID like "steng"
 * @return
 */
	public static Response createAuthResponse(SAMLSsoRequest request, String issuerStr,  String userID) {

		Response response = (Response) SAMLUtil.buildSAMLObject(Response.DEFAULT_ELEMENT_NAME);

		response.setID(SAMLUtil.generateSamlId());
		response.setInResponseTo(request.getID());
		response.setIssueInstant(new DateTime());

		response.setDestination(request.getConsumer());


		Status st = SAMLUtil.buildSAMLObject(Status.DEFAULT_ELEMENT_NAME);
		StatusCode stcode= SAMLUtil.buildSAMLObject(StatusCode.DEFAULT_ELEMENT_NAME);
		stcode.setValue(StatusCode.SUCCESS);
		st.setStatusCode(stcode);

		response.setStatus(st);

	    Issuer issuer= SAMLUtil.generateIssuer(issuerStr);
		response.setIssuer(issuer);

		List<Assertion> assertions = response.getAssertions();


		Assertion assertion = SAMLUtil.buildSAMLObject(Assertion.DEFAULT_ELEMENT_NAME);
		 Issuer aissuer= SAMLUtil.generateIssuer(issuerStr);
		assertion.setIssuer(aissuer);



		AuthnContext acontext = SAMLUtil.buildSAMLObject(AuthnContext.DEFAULT_ELEMENT_NAME);
		AuthnContextClassRef classref = SAMLUtil.buildSAMLObject(AuthnContextClassRef.DEFAULT_ELEMENT_NAME);
		classref.setAuthnContextClassRef(request.getAuthnContextClassRef());
		acontext.setAuthnContextClassRef(classref);
		AuthnStatement state = SAMLUtil.buildSAMLObject(AuthnStatement.DEFAULT_ELEMENT_NAME);
		state.setAuthnContext(acontext);
		assertion.getAuthnStatements().add(state);

		Subject subject=  SAMLUtil.buildSAMLObject(Subject.DEFAULT_ELEMENT_NAME);
		SubjectConfirmation confirm =  SAMLUtil.buildSAMLObject(SubjectConfirmation.DEFAULT_ELEMENT_NAME);
		SubjectConfirmationData data = SAMLUtil.buildSAMLObject(SubjectConfirmationData.DEFAULT_ELEMENT_NAME);
		data.setRecipient(request.getConsumer());
		confirm.setSubjectConfirmationData(data);

		subject.getSubjectConfirmations().add(confirm);
		NameID id =   SAMLUtil.buildSAMLObject(NameID.DEFAULT_ELEMENT_NAME);

		id.setFormat(request.getNameidpolicy());
		id.setValue(userID);




		subject.setNameID(id);

        Conditions cons =  SAMLUtil.buildSAMLObject(Conditions.DEFAULT_ELEMENT_NAME);
		AudienceRestriction restriction = SAMLUtil.buildSAMLObject(AudienceRestriction.DEFAULT_ELEMENT_NAME);
		cons.getAudienceRestrictions().add(restriction);
		Audience aud =  SAMLUtil.buildSAMLObject(Audience.DEFAULT_ELEMENT_NAME);
		aud.setAudienceURI(request.getIssuer());
		restriction.getAudiences().add(aud);

		assertion.setConditions(cons);


		assertion.setSubject(subject);

		assertions.add(assertion);


		return response;
	}

	/**
	 * build name id policy
	 */
	private static NameIDPolicy buildNameIdPolicy() {
		NameIDPolicy nameIDPolicy = SAMLUtil.buildSAMLObject(NameIDPolicy.DEFAULT_ELEMENT_NAME);
		nameIDPolicy.setAllowCreate(true);
		nameIDPolicy.setFormat(NameIDType.TRANSIENT);

		return nameIDPolicy;
	}
}
