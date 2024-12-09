package com.omnissa.eucenablement.sample.servlet;

import java.io.IOException;

import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.eclipse.jetty.server.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.omnissa.eucenablement.sample.idp.MyIDP;
import com.omnissa.samltoolkit.idp.SAMLSsoRequest;

public class MyIDPServlet implements Servlet  {

	private static Logger log = LoggerFactory.getLogger(MyIDPServlet.class);

	@Override
	public void destroy() {


	}

	@Override
	public ServletConfig getServletConfig() {

		return null;
	}

	@Override
	public String getServletInfo() {

		return null;
	}

	@Override
	public void init(ServletConfig arg0) throws ServletException {


	}

	@Override
	public void service(ServletRequest req, ServletResponse res) throws ServletException, IOException {

		Request request = (Request) req;

		HttpServletResponse response = (HttpServletResponse) res;
		String s = request.getParameter("SAMLRequest");
		HttpSession session = request.getSession();

		if (s!=null && s.length()>0){
			//handle saml request
			try {

				String relay = request.getParameter("RelayState");
				SAMLSsoRequest ssoRequest = MyIDP.getIDPService().decodeSAMLRequest(s);
				if (relay!=null && relay.length()>0){
					ssoRequest.setRelay(relay);
				}


				if (ssoRequest != null && ssoRequest.isValid() ) {
					session.setAttribute("request", ssoRequest);
					response.sendRedirect("index.jsp");
					return;
				}

				log.error("Failed to get valid sso response!");
				throw new ServletException("Failed to login: Invalid SSO");
			} catch (Exception e) {
				log.error(e.getMessage(), e);
				throw new ServletException(e);
			}
		}else{
			// handle login request
			String ssoresponse = "";

			// TODO: Authorize the user by username & password
			// Here, we just regard ANY username is valid
			String user = request.getParameter("username");
			String pass = request.getParameter("password");
			log.info("user name is successful "+user);

			SAMLSsoRequest ssoRequest = (SAMLSsoRequest) session.getAttribute("request");
			if (ssoRequest==null){
				//IDP initiated sso, check vidm
				String vidm = request.getParameter("vidmURL");
				if (vidm ==null){
					throw new ServletException("Invalid Access!");
				}
				//TODO: relay
				String relay = "";
				ssoresponse = MyIDP.getIDPService().getSSOResponseByPostBinding(vidm, user, relay);
			}else{
				 ssoresponse = MyIDP.getIDPService().getSSOResponseByPostBinding(ssoRequest, user);

			}

			log.info(ssoresponse);
			response.setContentType("text/html;charset=UTF-8");
			response.setCharacterEncoding("UTF-8");
			response.getOutputStream().write(ssoresponse.getBytes());
		}


	}


}
