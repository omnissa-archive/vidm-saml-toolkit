package com.omnissa.samltoolkit.idp;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

import com.omnissa.eucenablement.saml.service.AbstractSAMLService;

public class SAMLIDPConf {
	private String _issuer ;

	private Map<String, String> spEntity2XMLMap;
	public String getIssuer(){
		return this._issuer;
	}



	/**
	 *
	 * @param issuer like https://localhost:8443/SamlSample/idp.xml
	 * @param keystorepath
	 * @param keystorepwd
	 * @throws KeyStoreException
	 * @throws IOException
	 * @throws CertificateException
	 * @throws NoSuchAlgorithmException
	 */
	public SAMLIDPConf(String issuer, InputStream keystoreStream, String keystorepwd) throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException{
		this._issuer = issuer;
		spEntity2XMLMap = new HashMap<String, String>();

		 KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());

		    // get user password and file input stream
		    char[] password = keystorepwd.toCharArray();

		    try {

		        ks.load(keystoreStream, password);
		    } finally {
		        if (keystoreStream != null) {
		        	keystoreStream.close();
		        }
		    }
		    AbstractSAMLService.setPrivateCredential( ks, ks.aliases().nextElement(), keystorepwd);
	}

	public void registerSpConfig(String spConfig) {
		try {
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();

			DocumentBuilder db = dbFactory.newDocumentBuilder();
			Document doc = db.parse(new InputSource(new StringReader(spConfig)));
			Element e = doc.getDocumentElement();

			String entityID = e.getAttribute("entityID");
			spEntity2XMLMap.put(entityID, spConfig);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public boolean isValidSpConfig(String spConfigEntity) {
		for(String entity : spEntity2XMLMap.keySet()) {
			if(entity.equals(spConfigEntity))
				return true;
		}
		return false;
	}


	public boolean isConfigured(){
		return !this.spEntity2XMLMap.isEmpty();
	}

}
