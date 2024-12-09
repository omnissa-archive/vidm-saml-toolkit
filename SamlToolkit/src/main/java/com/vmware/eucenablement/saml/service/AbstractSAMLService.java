package com.omnissa.eucenablement.saml.service;

import java.security.KeyStore;

import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.opensaml.messaging.context.MessageContext;
import org.opensaml.messaging.decoder.HTTPPostMessageEncoder;
import org.opensaml.messaging.decoder.SAMLRedirectMessageEncoder;
import org.opensaml.messaging.encoder.MessageEncodingException;
import org.opensaml.saml.common.SAMLObject;
import org.opensaml.saml.common.SignableSAMLObject;
import org.opensaml.saml.common.binding.SAMLBindingSupport;
import org.opensaml.saml.common.binding.impl.SAMLOutboundDestinationHandler;
import org.opensaml.saml.common.messaging.SAMLMessageSecuritySupport;
import org.opensaml.saml.common.messaging.context.SAMLEndpointContext;
import org.opensaml.saml.common.messaging.context.SAMLPeerEntityContext;
import org.opensaml.saml.saml2.metadata.Endpoint;
import org.opensaml.security.x509.impl.KeyStoreX509CredentialAdapter;
import org.opensaml.xml.signature.SignatureConstants;
import org.opensaml.xmlsec.SignatureSigningParameters;
import org.opensaml.xmlsec.context.SecurityParametersContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.shibboleth.utilities.java.support.component.ComponentInitializationException;


public class AbstractSAMLService {

	private static KeyStoreX509CredentialAdapter credential;
	public static void setPrivateCredential(KeyStore keystore, String alias, String password ){
		 credential = new KeyStoreX509CredentialAdapter(keystore, alias, password.toCharArray());

	}
	private static Logger log = LoggerFactory.getLogger(AbstractSAMLService.class);
	protected String samlHTTPRedirect(SAMLObject request, String relayState, Endpoint endpoint) {

		MessageContext<SAMLObject> messageContext = new MessageContext<SAMLObject>();
		messageContext.setMessage(request);
		if (relayState != null)
			SAMLBindingSupport.setRelayState(messageContext, relayState);
		messageContext.getSubcontext(SAMLPeerEntityContext.class, true).getSubcontext(SAMLEndpointContext.class, true)
				.setEndpoint(endpoint);

		SAMLRedirectMessageEncoder encoder = new SAMLRedirectMessageEncoder();
		encoder.setMessageContext(messageContext);

		try {
			encoder.initialize();
			encoder.encode();

			String url = encoder.getRedirectURL();
			log.info("Redirect URL:" + url);
			return url;
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}

		return null;
	}


	@SuppressWarnings("unchecked")
	protected String samlHTTPPost(SignableSAMLObject object, String relayState, Endpoint endpoint) {
		String htmlPostContent = null;

		VelocityEngine velocityEngine = new VelocityEngine();
		velocityEngine.setProperty(RuntimeConstants.ENCODING_DEFAULT, "UTF-8");
		velocityEngine.setProperty(RuntimeConstants.OUTPUT_ENCODING, "UTF-8");
		velocityEngine.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
		velocityEngine.setProperty("classpath.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
		velocityEngine.setProperty("runtime.log.logsystem.class", "org.apache.velocity.runtime.log.NullLogSystem");
		velocityEngine.init();

		MessageContext<SAMLObject> messageContext = new MessageContext<SAMLObject>();
		try {
			messageContext.setMessage(object);
			messageContext.getSubcontext(SAMLPeerEntityContext.class, true)
					.getSubcontext(SAMLEndpointContext.class, true).setEndpoint(endpoint);
			if (relayState != null) {
				SAMLBindingSupport.setRelayState(messageContext, relayState);
			}




			SignatureSigningParameters signatureSigningParameters = new SignatureSigningParameters();
			signatureSigningParameters.setSigningCredential(credential);

			signatureSigningParameters.setSignatureAlgorithm(SignatureConstants.ALGO_ID_SIGNATURE_RSA_SHA256);
			signatureSigningParameters.setSignatureCanonicalizationAlgorithm(SignatureConstants.ALGO_ID_C14N_OMIT_COMMENTS);
			messageContext.getSubcontext(SecurityParametersContext.class, true).setSignatureSigningParameters(signatureSigningParameters);

			SAMLMessageSecuritySupport.signMessage(messageContext);
			log.info("Message signed");

			SAMLOutboundDestinationHandler handler = new SAMLOutboundDestinationHandler();
			handler.invoke(messageContext);
		} catch (Exception e) {
			log.error("caught MessageHandlerException: ", e);
			return htmlPostContent;
		}

		HTTPPostMessageEncoder encoder = new HTTPPostMessageEncoder();
		encoder.setMessageContext(messageContext);
		encoder.setVelocityEngine(velocityEngine);

		try {
			encoder.initialize();
			encoder.prepareContext();
			encoder.encode();
			htmlPostContent = encoder.getHtmlPostContent();
		} catch (ComponentInitializationException e) {
			log.error("caught ComponentInitializationException: ", e);
		} catch (MessageEncodingException e) {
			log.error("caught MessageEncodingException: ", e);
		} catch (Exception e) {
			log.error("caught MessageEncodingException: ", e);
		}

		return htmlPostContent;
	}
}
