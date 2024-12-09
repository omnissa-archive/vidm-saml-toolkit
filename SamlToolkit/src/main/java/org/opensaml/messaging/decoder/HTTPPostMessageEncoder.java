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

import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URI;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.core.xml.io.MarshallingException;
import org.opensaml.core.xml.util.XMLObjectSupport;
import org.opensaml.messaging.context.MessageContext;
import org.opensaml.messaging.encoder.AbstractMessageEncoder;
import org.opensaml.messaging.encoder.MessageEncodingException;
import org.opensaml.saml.common.SAMLObject;
import org.opensaml.saml.common.binding.BindingException;
import org.opensaml.saml.common.binding.SAMLBindingSupport;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.saml2.core.RequestAbstractType;
import org.opensaml.saml.saml2.core.StatusResponseType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

import net.shibboleth.utilities.java.support.codec.Base64Support;
import net.shibboleth.utilities.java.support.codec.HTMLEncoder;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.component.ComponentSupport;
import net.shibboleth.utilities.java.support.xml.SerializeSupport;

/**
 * Implements to encode post message and just return the html content for
 * post-binding. This class will not send html content directly
 */
public class HTTPPostMessageEncoder extends AbstractMessageEncoder<SAMLObject> {
	/** Default template ID. */
	public static final String DEFAULT_TEMPLATE_ID = "/templates/saml2-post-binding.vm";

	/** Class logger. */
	private final Logger log = LoggerFactory.getLogger(HTTPPostMessageEncoder.class);

	/**
	 * Velocity engine used to evaluate the template when performing POST
	 * encoding.
	 */
	private VelocityEngine velocityEngine;

	/** ID of the Velocity template used when performing POST encoding. */
	private String velocityTemplateId;

	/** The HTML content to post */
	private String htmlPostContent = null;

	/** Constructor. */
	public HTTPPostMessageEncoder() {
		setVelocityTemplateId(DEFAULT_TEMPLATE_ID);
	}

	/** {@inheritDoc} */
	public String getBindingURI() {
		return SAMLConstants.SAML2_POST_BINDING_URI;
	}

	/**
	 * Get the VelocityEngine instance.
	 *
	 * @return return the VelocityEngine instance
	 */
	public VelocityEngine getVelocityEngine() {
		return velocityEngine;
	}

	/**
	 * Set the VelocityEngine instance.
	 *
	 * @param newVelocityEngine
	 *            the new VelocityEngine instane
	 */
	public void setVelocityEngine(VelocityEngine newVelocityEngine) {
		ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
		ComponentSupport.ifDestroyedThrowDestroyedComponentException(this);
		velocityEngine = newVelocityEngine;
	}

	/**
	 * Get the Velocity template id.
	 *
	 * <p>
	 * Defaults to {@link #DEFAULT_TEMPLATE_ID}.
	 * </p>
	 *
	 * @return return the Velocity template id
	 */
	public String getVelocityTemplateId() {
		return velocityTemplateId;
	}

	/**
	 * Set the Velocity template id.
	 *
	 * <p>
	 * Defaults to {@link #DEFAULT_TEMPLATE_ID}.
	 * </p>
	 *
	 * @param newVelocityTemplateId
	 *            the new Velocity template id
	 */
	public void setVelocityTemplateId(String newVelocityTemplateId) {
		ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
		ComponentSupport.ifDestroyedThrowDestroyedComponentException(this);
		velocityTemplateId = newVelocityTemplateId;
	}

	/** {@inheritDoc} */
	@Override
	protected void doDestroy() {
		velocityEngine = null;
		velocityTemplateId = null;
		super.doDestroy();
	}

	/** {@inheritDoc} */
	@Override
	protected void doInitialize() throws ComponentInitializationException {
		super.doInitialize();
		if (velocityEngine == null) {
			throw new ComponentInitializationException("VelocityEngine must be supplied");
		}
		if (velocityTemplateId == null) {
			throw new ComponentInitializationException("Velocity template id must be supplied");
		}
	}

	/** {@inheritDoc} */
	@Override
	protected void doEncode() throws MessageEncodingException {
		MessageContext<SAMLObject> messageContext = getMessageContext();

		SAMLObject outboundMessage = messageContext.getMessage();
		if (outboundMessage == null) {
			throw new MessageEncodingException("No outbound SAML message contained in message context");
		}

		String endpointURL = getEndpointURL(messageContext).toString();

		postEncode(messageContext, endpointURL);
	}

	/**
	 * Base64 and POST encodes the outbound message and writes it to the
	 * outbound transport.
	 *
	 * @param messageContext
	 *            current message context
	 * @param endpointURL
	 *            endpoint URL to which to encode message
	 *
	 * @throws MessageEncodingException
	 *             thrown if there is a problem encoding the message
	 */
	protected void postEncode(MessageContext<SAMLObject> messageContext, String endpointURL)
			throws MessageEncodingException {
		log.debug("Invoking Velocity template to create POST body");
		try {
			VelocityContext context = new VelocityContext();

			populateVelocityContext(context, messageContext, endpointURL);

			/*
			 * HttpServletResponse response = getHttpServletResponse();
			 * 
			 * HttpServletSupport.addNoCacheHeaders(response);
			 * HttpServletSupport.setUTF8Encoding(response);
			 * HttpServletSupport.setContentType(response, "text/html");
			 * 
			 * Writer out = new OutputStreamWriter(response.getOutputStream(),
			 * "UTF-8");
			 */
			StringWriter content = new StringWriter();
			velocityEngine.mergeTemplate(velocityTemplateId, "UTF-8", context, content);
			this.htmlPostContent = content.toString();
		} catch (Exception e) {
			log.error("Error invoking Velocity template", e);
			throw new MessageEncodingException("Error creating output document", e);
		}
	}

	/**
	 * Populate the Velocity context instance which will be used to render the
	 * POST body.
	 *
	 * @param velocityContext
	 *            the Velocity context instance to populate with data
	 * @param messageContext
	 *            the SAML message context source of data
	 * @param endpointURL
	 *            endpoint URL to which to encode message
	 * @throws MessageEncodingException
	 *             thrown if there is a problem encoding the message
	 */
	protected void populateVelocityContext(VelocityContext velocityContext, MessageContext<SAMLObject> messageContext,
			String endpointURL) throws MessageEncodingException {

		String encodedEndpointURL = HTMLEncoder.encodeForHTMLAttribute(endpointURL);
		log.debug("Encoding action url of '{}' with encoded value '{}'", endpointURL, encodedEndpointURL);
		velocityContext.put("action", encodedEndpointURL);
		velocityContext.put("binding", getBindingURI());

		SAMLObject outboundMessage = messageContext.getMessage();

		log.debug("Marshalling and Base64 encoding SAML message");
		Element domMessage = marshallMessage(outboundMessage);

		try {
			String messageXML = SerializeSupport.nodeToString(domMessage);
			String encodedMessage = Base64Support.encode(messageXML.getBytes("UTF-8"), Base64Support.UNCHUNKED);
			if (outboundMessage instanceof RequestAbstractType) {
				velocityContext.put("SAMLRequest", encodedMessage);
			} else if (outboundMessage instanceof StatusResponseType) {
				velocityContext.put("SAMLResponse", encodedMessage);
			} else {
				throw new MessageEncodingException(
						"SAML message is neither a SAML RequestAbstractType or StatusResponseType");
			}
		} catch (UnsupportedEncodingException e) {
			log.error("UTF-8 encoding is not supported, this VM is not Java compliant.");
			throw new MessageEncodingException("Unable to encode message, UTF-8 encoding is not supported");
		}

		String relayState = SAMLBindingSupport.getRelayState(messageContext);
		if (SAMLBindingSupport.checkRelayState(relayState)) {
			String encodedRelayState = HTMLEncoder.encodeForHTMLAttribute(relayState);
			log.debug("Setting RelayState parameter to: '{}', encoded as '{}'", relayState, encodedRelayState);
			velocityContext.put("RelayState", encodedRelayState);
		}
	}

	public String getHtmlPostContent() {
		return this.htmlPostContent;
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

	/** {@inheritDoc} */
	@Override
	public void encode() throws MessageEncodingException {
		if (log.isDebugEnabled() && getMessageContext().getMessage() != null) {
			log.debug("Beginning encode of message of type: {}", getMessageContext().getMessage().getClass().getName());
		}

		super.encode();

		log.debug("Successfully encoded message.");
	}

}
