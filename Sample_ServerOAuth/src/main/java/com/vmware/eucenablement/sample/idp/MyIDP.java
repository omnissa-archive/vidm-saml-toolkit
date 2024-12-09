package com.omnissa.eucenablement.sample.idp;

import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.omnissa.samltoolkit.idp.IDPService;
import com.omnissa.samltoolkit.idp.SAMLIDPConf;

public class MyIDP {

	private static IDPService service;

	private static Logger log = LoggerFactory.getLogger(MyIDP.class);

	/**
	 *
	 * @param issuer like https://localhost:8443/SamlSample/idp.xml
	 * @return
	 */
	public static IDPService initIDPService(String issuer, InputStream kestoreStream, String keystorepwd) {


		try {
			SAMLIDPConf conf = new SAMLIDPConf(issuer, kestoreStream, keystorepwd);



			service = new IDPService(conf);
		} catch (Exception e) {
			log.error("Error initializing service", e);
			service = null;
		}
		return service;
	}



	public static IDPService getIDPService() {

		if (service == null)
			log.error("IDPService is null!! Please init it again!");

		return service;
	}


}
