<!--  
 Omnissa Identity Manager SAML Toolkit

Copyright (c) 2016 Omnissa, LLC. All Rights Reserved.

This product is licensed to you under the BSD-2 license (the "License").  You may not use this product except in compliance with the BSD-2 License.

This product may include a number of subcomponents with separate copyright notices and license terms. Your use of these subcomponents is subject to the terms and conditions of the subcomponent's license, as noted in the LICENSE file.


-->

<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="com.omnissa.eucenablement.sample.VIDMServlet" %>
<%@ page import="com.omnissa.eucenablement.oauth.impl.VIDMOAuth2Impl" %>
<%@ page import="com.omnissa.eucenablement.oauth.util.OAuthUtil" %>
<%@ page import="com.omnissa.eucenablement.oauth.util.HttpRequest" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
	<!-- header -->
	<jsp:include page="headertpl.html"></jsp:include>
	<body id="page-top" data-spy="scroll" data-target=".navbar-fixed-top">
		 <!-- Navigation -->
    	<jsp:include page="navtpl.html"></jsp:include>
    	
    	<!-- get related configuration-->
		<%
		String idpDomainURI = request.getParameter("vidmhost");
		VIDMOAuth2Impl vidmoAuth2=VIDMServlet.getVIDMOAuth(request);

		if (!VIDMServlet.isValidHost(idpDomainURI))
		    idpDomainURI=null;
		else {
			if (idpDomainURI.endsWith("/")) idpDomainURI=idpDomainURI.substring(0, idpDomainURI.length()-1);
			vidmoAuth2.setHost(idpDomainURI);
		}

		%>		
		
    	<section id="ConfigDone" class="content-section text-center" style="height:100%;width:100%;">
        	<div class="ConfigDone-section">
            	<div class="container">
                <div class="col-lg-8 col-lg-offset-2">
					<%if(idpDomainURI!=null) {
					%>         
						<div class="jumbotron" style="color:#000" style="padding: 2rem;">
							<p>Congratulations. You are almost ready.</p>
							<p>Please go to your vIDM administration console, and follow this web page to add a new web application.</p>
							<p>Click the white arrow button for guidance.</p>
							<p>Click "SINGLE SIGN ON" button if you are ready.</p>
						</div>
								<div class="row">
								 <a href="#ConfigVIDM" class="btn btn-circle page-scroll">
			                            <i class="fa fa-angle-double-down animated"></i>
			                     </a>
	                    	</div>
						
						 <div class="row">
							 <div class="col-xs-12 col-lg-6">

								 <p><a class="btn btn-default btn-lg" target="_new" href="http://pubs.omnissa.com/vidm/topic/com.omnissa.vidm_workspace-one.doc/GUID-C1A2F5E4-E117-4CD7-A672-C19040527C4B.html" role="button">vIDM Document >>></a></p>

							 </div>
	            <div class="col-xs-12 col-lg-6" >
					<p><a type="button" class="btn btn-default btn-lg" href="<%
						if ("".equals(VIDMServlet.APP_ID) || "".equals(VIDMServlet.APP_SECRET)) {
						    out.print("javascript:alert('Set APP_ID and APP_SECRET in VIDMServlet.java, and restart the application!')");
						}
						else {
						    out.print(vidmoAuth2.getAuthorizationUrl(""));
						}
					%>">Single Sign On >>></a></p>
	            </div>

	          </div><!--/row-->
						
					<%} else {%>
						<div class="jumbotron" style="color:#000">
				            <h2>Cannot fetch data from VIDM!</h2>
				            <p>Hints: Check your vIDM URL and vIDM service.</p>
				         </div>
				         <div>
		                	<form action="idpDiscovery.jsp" method="get">
								<div><input type=submit value="<<< Back" class="btn btn-default btn-lg">
								</div>
							</form>
						</div>
					<%} %>
				</div>
				</div>
			</div>
		</section>
		<%if(idpDomainURI!=null) {
					%>   
		<section id="ConfigVIDM" class="content-section text-center" style="height:100%;width:100%;">
				       <div class="row row-offcanvas">
				        <div class="col-xs-12 col-sm-12">
				          <div class="row"> 
				              <p>Step 1: Catalog --> Settings --> Remote App Access --> Create Client</p>
				              <p><img src="img/step1.png" style="width: 75%"> </p>
							
				          </div><!--/row-->
				          
				          
				          <div class="row">
				              <p>Step 2: Fill the blanks and save.</p>
				              <p><img src="img/step2.png" > </p>
				           
				          </div>
				          
				          <div class="row">
				              <p>Step 3: Modify APP_ID (Client_ID), APP_SECRET (Shared Secret) and REDIRECT_URI in MyWebServer. <br/>
							  Be sure they are exactly same as you fill.</p>
				              <p><img src="img/step3.png" > </p>	            
					       
				          </div>
				          
				          
				          <div class="row">
				              <p>Step 4: Restart the server.</p>
					       
				          </div>
				        </div><!--/.col-xs-12.col-sm-12-->
				      </div>
		</section>
		<%} %>
	</body>
</html>