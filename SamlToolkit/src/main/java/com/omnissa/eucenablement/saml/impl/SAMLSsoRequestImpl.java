package com.omnissa.eucenablement.saml.impl;

import org.opensaml.messaging.context.MessageContext;
import org.opensaml.saml.common.SignableSAMLObject;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.RequestAbstractType;
import org.opensaml.xmlsec.signature.support.SignatureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.omnissa.samltoolkit.idp.SAMLIDPConf;
import com.omnissa.samltoolkit.idp.SAMLSsoRequest;

public class SAMLSsoRequestImpl implements SAMLSsoRequest{

	private static Logger log = LoggerFactory.getLogger(SAMLSsoRequestImpl.class);

	/** Whether response content is a valid SSO response */
	private boolean isValid = false;

	/** Configuration of current identity provider */
	private SAMLIDPConf _conf;

	private String _issuer;
	private String _nameidpolicy;
	private String _consumer;
	private String _authnContextClass;

	private String _id;

	public SAMLSsoRequestImpl(String vidmURL, String nameidpolicy, String authncontext, String relay){
		this._issuer = vidmURL + "/SAAS/API/1.0/GET/metadata/sp.xml";

		this._consumer = vidmURL + "/SAAS/auth/saml/response";

		this._nameidpolicy = nameidpolicy;
		this._authnContextClass = authncontext;
		this.isValid = true;
		this.relay=relay;
		this._id="";

	}
	public SAMLSsoRequestImpl(MessageContext<?> context, SAMLIDPConf conf) throws SignatureException {

		this._conf = conf;
		RequestAbstractType requestContent = null;
		SignableSAMLObject obj = (SignableSAMLObject) context.getMessage();

		AuthnRequest request;
		log.info("obj type:"+obj );
		if (obj instanceof AuthnRequest) {

			request = (AuthnRequest) obj;
			this._issuer = request.getIssuer().getValue();
			this._nameidpolicy = request.getNameIDPolicy().getFormat();
			this._id= request.getID();
			this._consumer = request.getAssertionConsumerServiceURL();

			this._authnContextClass = request.getRequestedAuthnContext().getAuthnContextClassRefs().get(0).getAuthnContextClassRef();
			this.isValid = true;
			log.info("request:"+this.toString());
		}else{
			this.isValid = false;
		}
	};

	@Override
	public String toString(){
		return this._id+ " issuer: "+ this._issuer+" nameid:"+this._nameidpolicy;
	}

	@Override
	public boolean isValid() {
		return this.isValid;
	}


	@Override
	public String getIssuer() {
		return _issuer;
	}


	@Override
	public String getNameidpolicy() {
		return _nameidpolicy;
	}



	@Override
	public String getID() {

		return _id;
	}

	@Override
	public String getConsumer() {
		return _consumer;
	}

	@Override
	public String getAuthnContextClassRef() {

		return this._authnContextClass;
	}

	private String relay;

	@Override
	public void setRelay(String relay){
		this.relay = relay;
	}
	@Override
	public String getRelay() {
		// TODO Auto-generated method stub
		return relay;
	}

}
