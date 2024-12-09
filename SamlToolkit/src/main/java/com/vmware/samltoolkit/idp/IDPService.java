package com.omnissa.samltoolkit.idp;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.omnissa.eucenablement.saml.impl.SAMLSsoRequestImpl;
import com.omnissa.eucenablement.saml.service.SAMLIDPService;

/**
*
* This is the identity provider service for vIDM.
* vIDM plays the role of service provider, and this IDPService should be created and managed by some identity provider
* Step 1, This IDPService decodes SAML request (containing service info) from vIDM,
* Step 2, The identity provider handles authentication as is
* Step 3, This IDPService generates SAML response (with user info) to vIDM
*
*/
public class IDPService {

	private final SAMLIDPConf config;

	private SAMLIDPService _service;

	public SAMLIDPConf getSAMLIDPConf() {
		return config;
	}

	/**
	 *
	 * @param conf
	 * @throws Exception
	 */
	public IDPService(SAMLIDPConf conf) throws Exception {

		this.config = conf;
		this._service = new SAMLIDPService(conf);

	}

	/**
	 *
	 * @param samlrequest from the http request parameter like ?SAMLRequest=
	 * @return
	 * @throws Exception
	 */
	public SAMLSsoRequest decodeSAMLRequest(String samlrequest) throws Exception {

		SAMLSsoRequest ssoRequest = this._service.decodeSAMLRequest(samlrequest);
		String issuer = ssoRequest.getIssuer();
		if(config.isValidSpConfig(issuer))
			return ssoRequest;
		return null;
	}


	/**
	 *
	 * @param request
	 * @param userID
	 * @return
	 */
	public String getSSOResponseByPostBinding(SAMLSsoRequest request, String userID)  {
		return this._service.getSSOResponseByPostBinding(request, userID);
	}

	public String getSpConfigfromUrl(String vidmUrl) {
		String line;
		StringBuffer sb = new StringBuffer();
		BufferedReader br = null;
		try {
			if(!vidmUrl.startsWith("https"))
				vidmUrl = "https://" + vidmUrl;
			URL url = new URL(vidmUrl + "/SAAS/API/1.0/GET/metadata/sp.xml");
			HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
			connection.setRequestMethod("GET");
			InputStream stream = connection.getInputStream();
			int responseCode = connection.getResponseCode();
			System.out.println(responseCode);


			br = new BufferedReader(new InputStreamReader(stream));
			while((line = br.readLine()) != null ){
				sb.append(line);
			}

		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} finally {
			if(br != null) {
				try {
					br.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return sb.toString();

	}

	/**
	 *
	 * @param vidmURL like "https://steng.omnissaidentity.asia"
	 * @param userID
	 * @param relay
	 * @return
	 */
	public String getSSOResponseByPostBinding(String vidmURL, String userID,  String relay)  {

		//TODO:
		SAMLSsoRequest request = new SAMLSsoRequestImpl(vidmURL, "urn:oasis:names:tc:SAML:1.1:nameid-format:unspecified", "urn:oasis:names:tc:SAML:2.0:ac:classes:Password", relay);
		return getSSOResponseByPostBinding(request, userID);

	}




	private static Logger log = LoggerFactory.getLogger(IDPService.class);

}
