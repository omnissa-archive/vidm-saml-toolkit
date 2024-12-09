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

import java.io.IOException;

import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.omnissa.samltoolkit.SAMLSsoResponse;

/**
 * Demo of the consumer in SAML. Consumer serves actual application content after validating user identity against IDP (Identity Provider).
 *
 */
public class ConsumerServlet implements Servlet {

	private static Logger log = LoggerFactory.getLogger(ConsumerServlet.class);

	@Override
	public void init(ServletConfig config) throws ServletException {
	}

	@Override
	public ServletConfig getServletConfig() {
		return null;
	}

	@Override
	public void service(ServletRequest req, ServletResponse res) throws ServletException, IOException {
		
		HttpServletRequest request = (HttpServletRequest) req;
		HttpServletResponse response = (HttpServletResponse) res;
		String s = request.getParameter("SAMLResponse");

		HttpSession session = request.getSession();
		SAMLSsoResponse ssoResponse = null;
		
		try {
			
			ssoResponse = MySSO.getSSOService().decodeSSOResponse(s);
			
			if (ssoResponse != null && ssoResponse.isValid() && ssoResponse.ssoSucceed()) {
				session.setAttribute("userName", ssoResponse.getNameId());
				response.sendRedirect("userpage.jsp");
				return;
			}
			
			log.error("Failed to get valid sso response!");
			throw new ServletException("Failed to login: Invalid SSO");
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new ServletException(e);
		}
	}

	@Override
	public String getServletInfo() {
		return null;
	}

	@Override
	public void destroy() {
	}
}
