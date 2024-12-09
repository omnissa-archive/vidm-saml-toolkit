package com.omnissa.eucenablement.saml.service;

import org.opensaml.core.config.InitializationException;
import org.opensaml.core.config.InitializationService;
import org.opensaml.messaging.context.MessageContext;
import org.opensaml.messaging.decoder.SimpleSAMLMessageDecoder;
import org.opensaml.saml.common.SAMLObject;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.saml.saml2.metadata.Endpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.omnissa.eucenablement.saml.impl.SAMLSsoRequestImpl;
import com.omnissa.eucenablement.saml.impl.SAMLUtil;
import com.omnissa.samltoolkit.idp.SAMLIDPConf;
import com.omnissa.samltoolkit.idp.SAMLSsoRequest;

public class SAMLIDPService extends AbstractSAMLService {
	private static Logger log = LoggerFactory.getLogger(SAMLIDPService.class);
	private final SAMLIDPConf _conf;

	public SAMLIDPService(SAMLIDPConf conf) throws InitializationException {
		this._conf = conf;

		try {
			InitializationService.initialize();
		} catch (InitializationException e) {
			log.error(e.getMessage(), e);
			throw e;
		}
	}

	public SAMLIDPConf getConfig() {
		return _conf;
	}


	/**
	 * TODO: refer to vIDM to validate the request, a lot of fields to be validated
	 * @param samlrequest
	 * @return
	 * @throws Exception
	 */
	public SAMLSsoRequest decodeSAMLRequest(String samlrequest) throws Exception {
		log.info("SAML Request is:" + samlrequest);

		SimpleSAMLMessageDecoder decoder = new SimpleSAMLMessageDecoder();

		//TODO: check whether the message is deflated or not. It seems that vIDM deflates the message by default
		decoder.setDefaltedSAMLMessage(samlrequest);
		// decoder.setParserPool(parserPool);

		decoder.initialize();

		// Decode the message
		SAMLSsoRequest ssoRequest = null;
		try {
			decoder.decode();

			MessageContext<SAMLObject> messageContext = decoder.getMessageContext();
			ssoRequest = new SAMLSsoRequestImpl(messageContext, _conf);
		} catch (Exception e) {
			log.error("Error during SAML decoding", e);
			return null;
		}

		return ssoRequest;

	}


	public SAMLSsoRequest decodeSAMLRequestWithRelay(String samlrequest, String relay) throws Exception {
		log.info("SAML Request is:" + samlrequest);
		SAMLSsoRequest request = this.decodeSAMLRequest(samlrequest);

		request.setRelay(relay);

		return request;


	}

/**
 *
 * @param request
 * @return a SAML Response for browser; IDP should return this response to the browser
 * https://steng.omnissaidentity.asia//SAAS/API/1.0/GET/metadata/sp.xml
 * NOTICE: call this function only after the authentication has been processed by yourself successfully
 * @throws Exception
 */
	public String getSSOResponseByPostBinding(SAMLSsoRequest request ,  String userID)  {
		log.info("Getting SSO Response");


       Endpoint endpoint = SAMLUtil.generateEndpoint(request.getConsumer(),
				request.getConsumer(), SAMLConstants.SAML2_POST_BINDING_URI);
		if (endpoint == null) {
			log.error("Cannot generate saml endPoint for post binding!");
			return null;
		}

		Response response = SAMLUtil.createAuthResponse(request,this._conf.getIssuer(), userID);
		if (response == null) {
			log.error("Cannot generate saml authn request for post binding!");
			return null;
		}

		return samlHTTPPost(response, request.getRelay(), endpoint);
	}



}
