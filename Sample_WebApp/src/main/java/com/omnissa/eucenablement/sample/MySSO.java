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
package com.omnissa.eucenablement.sample;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.omnissa.samltoolkit.SAMLToolkitConf;
import com.omnissa.samltoolkit.SSOService;

/**
 *  A utility class for demo purpose.
 *
 */
public class MySSO {
	
	private static SSOService service;
	
	private static Logger log = LoggerFactory.getLogger(MySSO.class);

	public static SSOService initSsoService(String vidmURL, String consumerURL, boolean byPassCert) {
		
		log.info("Start to init SSO Service " + vidmURL + " consumer:" + consumerURL + " bypasscert:" + byPassCert);
		
		try {
			SAMLToolkitConf conf = new SAMLToolkitConf();
			conf.setIdpURL(vidmURL);
			conf.setConsumerURL(consumerURL);
			conf.setByPassSSLCertValidation(byPassCert);
			log.info("Config initiated:" + conf.isReady());
	
			service = new SSOService(conf);
		} catch (Exception e) {
			log.error("Error initializing service", e);
			service = null;
		}
		return service;
	}

	public static String getvIDMURL() {
	
		if (service == null)
			return null;

		return service.getVIDMURL();
	}

	public static SSOService getSSOService() {
		
		if (service == null)
			log.error("SSOFacade is null!! Please init it again!");
		
		return service;
	}
}
