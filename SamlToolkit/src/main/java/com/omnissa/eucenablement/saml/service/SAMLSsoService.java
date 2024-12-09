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
package com.omnissa.eucenablement.saml.service;

import org.opensaml.core.config.InitializationException;
import org.opensaml.core.config.InitializationService;
import org.opensaml.messaging.context.MessageContext;
import org.opensaml.messaging.decoder.SimpleSAMLMessageDecoder;
import org.opensaml.saml.common.SAMLObject;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.LogoutRequest;
import org.opensaml.saml.saml2.metadata.Endpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.omnissa.eucenablement.saml.impl.SAMLSsoResponseImpl;
import com.omnissa.eucenablement.saml.impl.SAMLUtil;
import com.omnissa.samltoolkit.SAMLSsoResponse;
import com.omnissa.samltoolkit.SAMLToolkitConf;

/**
 * Core service to handle Single Sign On and Single Log Out.
 *
 */
public class SAMLSsoService extends AbstractSAMLService{
	private static Logger log = LoggerFactory.getLogger(SAMLSsoService.class);

	private final SAMLToolkitConf _conf;

	public SAMLSsoService(SAMLToolkitConf conf) throws InitializationException {
		this._conf = conf;

		try {
			InitializationService.initialize();
		} catch (InitializationException e) {
			log.error(e.getMessage(), e);
			throw e;
		}
	}

	public SAMLToolkitConf getConfig() {
		return _conf;
	}

	//TODO: refer to how vIDM validates the response, quite a lot of fields to be validated
	public SAMLSsoResponse decodeSAMLResponse(String encodedSAMLResponse) throws Exception {
		// Just for debug
		/// String s = request.getParameter("SAMLResponse");
		log.debug("SAML Response is:" + encodedSAMLResponse);

		SimpleSAMLMessageDecoder decoder = new SimpleSAMLMessageDecoder();
		decoder.setSAMLMessage(encodedSAMLResponse);
		// decoder.setParserPool(parserPool);

		decoder.initialize();

		// Decode the message
		SAMLSsoResponseImpl ssoRespone = null;
		try {
			decoder.decode();

			MessageContext<SAMLObject> messageContext = decoder.getMessageContext();
			ssoRespone = new SAMLSsoResponseImpl(messageContext, _conf);
		} catch (Exception e) {
			log.error("Error during SAML decoding", e);
			return null;
		}

		return ssoRespone;
	}

	public String getSAMLRequestURLRedirect(String relayState) throws Exception {
		if (!_conf.isReady()) {
			log.error("Config not initiated!");
			return null;
		}
		log.info("Reply with SAMLRequest HTTP Redirect Binding");
		AuthnRequest authnReqeust = generateAuthRequest(SAMLConstants.SAML2_REDIRECT_BINDING_URI);

		if (authnReqeust == null) {
			log.error("Cannot generate saml authn request for redirect binding!");
			return null;
		}
		Endpoint endpoint = SAMLUtil.generateEndpoint(_conf.getSSOTargetURL(SAMLConstants.SAML2_REDIRECT_BINDING_URI),
				_conf.getConsumerURL(), SAMLConstants.SAML2_REDIRECT_BINDING_URI);
		if (endpoint == null) {
			log.error("Cannot generate saml endPoint for redirect binding!");
			return null;
		}

		return samlHTTPRedirect(authnReqeust, relayState, endpoint);

	}

	public String getSSOHtmlPost(String relayState) {
		if (!_conf.isReady()) {
			log.error("Config not initiated!");
			return null;
		}

		log.info("SAMLRequest HTTP Post Binding");
		Endpoint endpoint = SAMLUtil.generateEndpoint(_conf.getSSOTargetURL(SAMLConstants.SAML2_POST_BINDING_URI),
				_conf.getConsumerURL(), SAMLConstants.SAML2_POST_BINDING_URI);
		if (endpoint == null) {
			log.error("Cannot generate saml endPoint for post binding!");
			return null;
		}

		AuthnRequest authnReqeust = generateAuthRequest(SAMLConstants.SAML2_POST_BINDING_URI);
		if (authnReqeust == null) {
			log.error("Cannot generate saml authn request for post binding!");
			return null;
		}

		return samlHTTPPost(authnReqeust, relayState, endpoint);
	}

	// Just add post binding for SLO
	// Actually from idp.xml of vIDM, this binding type is not supported now
	public String getSLOHtmlPost(SAMLSsoResponse response, String relayState) throws Exception {
		if (!_conf.isReady()) {
			log.error("Config not initiated!");
			return null;
		}

		log.info("log out with HTTP Post Binding");
		String logoutSrvLocation = this._conf.getSignoutTargetURL(SAMLConstants.SAML2_POST_BINDING_URI);
		if (logoutSrvLocation == null) {
			log.error("Cannot get the location of singlelogout service for post binding!");
			return null;
		}

		Endpoint endpoint = SAMLUtil.generateEndpoint(logoutSrvLocation, _conf.getConsumerURL(),
				SAMLConstants.SAML2_POST_BINDING_URI);
		if (endpoint == null) {
			log.error("Cannot generate saml logout endPoint for post binding!");
			return null;
		}

		LogoutRequest logoutRequest = generateLogoutRequest(response.getNameId(), response.getSessionIndex(),
				logoutSrvLocation);
		if (logoutRequest == null) {
			log.error("Cannot generate the single logout request!");
			return null;
		}

		return samlHTTPPost(logoutRequest, relayState, endpoint);
	}

	public String getSAMLLogoutURLRedirect() throws Exception {
		return _conf.getSignoutTargetURL(SAMLConstants.SAML2_REDIRECT_BINDING_URI);
	}

	private AuthnRequest generateAuthRequest(String binding) {
		if (!_conf.isReady()) {
			log.error("Config not initiated!");
			return null;
		}

		return SAMLUtil.createAuthRequest(_conf.getConsumerURL(), _conf.getSSOTargetURL(binding), _conf.getIssuerName(), binding);

	}

	private LogoutRequest generateLogoutRequest(String nameId, String sessionIndex, String logoutURL)
			throws IllegalArgumentException, SecurityException, IllegalAccessException {
		if (!_conf.isReady()) {
			log.error("Config not initiated!");
			return null;
		}

		return SAMLUtil.genererateLogoutRequest(nameId, sessionIndex, logoutURL, _conf.getIssuerName());
	}



}
