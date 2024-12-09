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
package org.opensaml.messaging.decoder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.List;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;

import org.opensaml.core.xml.XMLObject;
import org.opensaml.core.xml.io.MarshallingException;
import org.opensaml.core.xml.util.XMLObjectSupport;
import org.opensaml.messaging.context.MessageContext;
import org.opensaml.messaging.encoder.AbstractMessageEncoder;
import org.opensaml.messaging.encoder.MessageEncodingException;
import org.opensaml.saml.common.SAMLObject;
import org.opensaml.saml.common.SignableSAMLObject;
import org.opensaml.saml.common.binding.BindingException;
import org.opensaml.saml.common.binding.SAMLBindingSupport;
import org.opensaml.saml.common.messaging.SAMLMessageSecuritySupport;
import org.opensaml.saml.saml2.core.RequestAbstractType;
import org.opensaml.saml.saml2.core.StatusResponseType;
import org.opensaml.security.SecurityException;
import org.opensaml.security.credential.Credential;
import org.opensaml.security.credential.CredentialSupport;
import org.opensaml.xmlsec.SignatureSigningParameters;
import org.opensaml.xmlsec.crypto.XMLSigningUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

import net.shibboleth.utilities.java.support.codec.Base64Support;
import net.shibboleth.utilities.java.support.collection.Pair;
import net.shibboleth.utilities.java.support.net.URLBuilder;
import net.shibboleth.utilities.java.support.xml.SerializeSupport;

/**
 * Implements to encode redirect message and just return the content redirect
 * URI This class will not send redirect message directly
 */
public class SAMLRedirectMessageEncoder extends AbstractMessageEncoder<SAMLObject> {

	private String redirectURL;
	private static Logger log = LoggerFactory.getLogger(SAMLRedirectMessageEncoder.class);

	@Override
	protected void doEncode() throws MessageEncodingException {

		MessageContext<SAMLObject> messageContext = getMessageContext();
		SAMLObject outboundMessage = messageContext.getMessage();

		String endpointURL = getEndpointURL(messageContext).toString();

		removeSignature(outboundMessage);

		String encodedMessage = deflateAndBase64Encode(outboundMessage);

		redirectURL = buildRedirectURL(messageContext, endpointURL, encodedMessage);

	}

	/**
	 * Removes the signature from the protocol message.
	 *
	 * @param message
	 *            current message context
	 */
	protected void removeSignature(SAMLObject message) {
		if (message instanceof SignableSAMLObject) {
			SignableSAMLObject signableMessage = (SignableSAMLObject) message;
			if (signableMessage.isSigned()) {
				log.debug("Removing SAML protocol message signature");
				signableMessage.setSignature(null);
			}
		}
	}

	/**
	 * Builds the URL to redirect the client to.
	 *
	 * @param messageContext
	 *            current message context
	 * @param endpoint
	 *            endpoint URL to send encoded message to
	 * @param message
	 *            Deflated and Base64 encoded message
	 *
	 * @return URL to redirect client to
	 *
	 * @throws MessageEncodingException
	 *             thrown if the SAML message is neither a RequestAbstractType
	 *             or Response
	 */
	protected String buildRedirectURL(MessageContext<SAMLObject> messageContext, String endpoint, String message)
			throws MessageEncodingException {
		log.debug("Building URL to redirect client to");

		URLBuilder urlBuilder = null;
		try {
			urlBuilder = new URLBuilder(endpoint);
		} catch (MalformedURLException e) {
			throw new MessageEncodingException("Endpoint URL " + endpoint + " is not a valid URL", e);
		}

		List<Pair<String, String>> queryParams = urlBuilder.getQueryParams();
		queryParams.clear();

		SAMLObject outboundMessage = messageContext.getMessage();

		if (outboundMessage instanceof RequestAbstractType) {
			queryParams.add(new Pair<String, String>("SAMLRequest", message));
		} else if (outboundMessage instanceof StatusResponseType) {
			queryParams.add(new Pair<String, String>("SAMLResponse", message));
		} else {
			throw new MessageEncodingException(
					"SAML message is neither a SAML RequestAbstractType or StatusResponseType");
		}

		String relayState = SAMLBindingSupport.getRelayState(messageContext);
		if (SAMLBindingSupport.checkRelayState(relayState)) {
			queryParams.add(new Pair<String, String>("RelayState", relayState));
		}

		SignatureSigningParameters signingParameters = SAMLMessageSecuritySupport
				.getContextSigningParameters(messageContext);
		if (signingParameters != null && signingParameters.getSigningCredential() != null) {
			String sigAlgURI = getSignatureAlgorithmURI(signingParameters);
			Pair<String, String> sigAlg = new Pair<String, String>("SigAlg", sigAlgURI);
			queryParams.add(sigAlg);
			String sigMaterial = urlBuilder.buildQueryString();

			queryParams.add(new Pair<String, String>("Signature",
					generateSignature(signingParameters.getSigningCredential(), sigAlgURI, sigMaterial)));
		} else {
			log.debug("No signing credential was supplied, skipping HTTP-Redirect DEFLATE signing");
		}

		return urlBuilder.buildURL();
	}

	/**
	 * Generates the signature over the query string.
	 *
	 * @param signingCredential
	 *            credential that will be used to sign query string
	 * @param algorithmURI
	 *            algorithm URI of the signing credential
	 * @param queryString
	 *            query string to be signed
	 *
	 * @return base64 encoded signature of query string
	 *
	 * @throws MessageEncodingException
	 *             there is an error computing the signature
	 */
	protected String generateSignature(Credential signingCredential, String algorithmURI, String queryString)
			throws MessageEncodingException {

		log.debug(String.format("Generating signature with key type '%s', algorithm URI '%s' over query string '%s'",
				CredentialSupport.extractSigningKey(signingCredential).getAlgorithm(), algorithmURI, queryString));

		String b64Signature = null;
		try {
			byte[] rawSignature = XMLSigningUtil.signWithURI(signingCredential, algorithmURI,
					queryString.getBytes("UTF-8"));
			b64Signature = Base64Support.encode(rawSignature, Base64Support.UNCHUNKED);
			log.debug("Generated digital signature value (base64-encoded) {}", b64Signature);
		} catch (final SecurityException e) {
			log.error("Error during URL signing process", e);
			throw new MessageEncodingException("Unable to sign URL query string", e);
		} catch (final UnsupportedEncodingException e) {
			// UTF-8 encoding is required to be supported by all JVMs
		}

		return b64Signature;
	}

	/**
	 * Gets the signature algorithm URI to use.
	 *
	 * @param signingParameters
	 *            the signing parameters to use
	 *
	 * @return signature algorithm to use with the associated signing credential
	 *
	 * @throws MessageEncodingException
	 *             thrown if the algorithm URI is not supplied explicitly and
	 *             could not be derived from the supplied credential
	 */
	protected String getSignatureAlgorithmURI(SignatureSigningParameters signingParameters)
			throws MessageEncodingException {

		if (signingParameters.getSignatureAlgorithm() != null) {
			return signingParameters.getSignatureAlgorithm();
		}

		throw new MessageEncodingException("The signing algorithm URI could not be determined");
	}

	/**
	 * Helper method that marshalls the given message.
	 *
	 * @param message
	 *            message the marshall and serialize
	 *
	 * @return marshalled message
	 *
	 * @throws MessageEncodingException
	 *             thrown if the give message can not be marshalled into its DOM
	 *             representation
	 */
	protected Element marshallMessage(XMLObject message) throws MessageEncodingException {
		log.debug("Marshalling message");

		try {
			return XMLObjectSupport.marshall(message);
		} catch (MarshallingException e) {
			log.error("Error marshalling message", e);
			throw new MessageEncodingException("Error marshalling message", e);
		}
	}

	/**
	 * DEFLATE (RFC1951) compresses the given SAML message.
	 *
	 * @param message
	 *            SAML message
	 *
	 * @return DEFLATE compressed message
	 *
	 * @throws MessageEncodingException
	 *             thrown if there is a problem compressing the message
	 */
	protected String deflateAndBase64Encode(SAMLObject message) throws MessageEncodingException {
		log.debug("Deflating and Base64 encoding SAML message");
		try {
			String messageStr = SerializeSupport.nodeToString(marshallMessage(message));

			ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
			Deflater deflater = new Deflater(Deflater.DEFLATED, true);
			DeflaterOutputStream deflaterStream = new DeflaterOutputStream(bytesOut, deflater);
			deflaterStream.write(messageStr.getBytes("UTF-8"));
			deflaterStream.finish();

			return Base64Support.encode(bytesOut.toByteArray(), Base64Support.UNCHUNKED);
		} catch (IOException e) {
			throw new MessageEncodingException("Unable to DEFLATE and Base64 encode SAML message", e);
		}
	}

	/**
	 * Gets the response URL from the message context.
	 *
	 * @param messageContext
	 *            current message context
	 *
	 * @return response URL from the message context
	 *
	 * @throws MessageEncodingException
	 *             throw if no relying party endpoint is available
	 */
	protected URI getEndpointURL(MessageContext<SAMLObject> messageContext) throws MessageEncodingException {
		try {
			return SAMLBindingSupport.getEndpointURL(messageContext);
		} catch (BindingException e) {
			throw new MessageEncodingException("Could not obtain message endpoint URL", e);
		}
	}

	public String getRedirectURL() {
		return redirectURL;
	}

}
