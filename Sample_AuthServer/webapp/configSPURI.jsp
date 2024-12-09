<!--  
 Omnissa Identity Manager SAML Toolkit

Copyright (c) 2016 Omnissa, LLC. All Rights Reserved.

This product is licensed to you under the BSD-2 license (the "License").  You may not use this product except in compliance with the BSD-2 License.

This product may include a number of subcomponents with separate copyright notices and license terms. Your use of these subcomponents is subject to the terms and conditions of the subcomponent's license, as noted in the LICENSE file.


-->

<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List" %>
<%@ page import="com.omnissa.eucenablement.sample.idp.MyIDP" %>
<%@ page import="com.omnissa.eucenablement.sample.idp.SslUtilities" %>
<%@ page import="com.omnissa.samltoolkit.idp.IDPService"%>
<%@ page import="com.omnissa.samltoolkit.idp.SAMLIDPConf"%>
<%@ page import="com.omnissa.samltoolkit.SAMLToolkitConf"%>
<%@ page import="java.util.Map" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
	<!-- header -->
	<jsp:include page="headertpl.html"></jsp:include>
	<body id="page-top" data-spy="scroll" data-target=".navbar-fixed-top">
		 <!-- Navigation -->
    	<jsp:include page="navtpl.html"></jsp:include>
    	
    	<!-- get related configuration-->
		<%
			IDPService idpService = MyIDP.getIDPService();
			SAMLIDPConf conf = idpService.getSAMLIDPConf();
			String vidmSp1Url = request.getParameter("vidmSp1");
			SslUtilities.trustAllCertificates();//Trust all certificate for sample usage. Certificates should always be managed in production environment
			String spXML = idpService.getSpConfigfromUrl(vidmSp1Url);
			if(spXML != null)
				conf.registerSpConfig(spXML);
		%>
		
    	<section id="ConfigDone" class="content-section text-center" style="height:100%;width:100%;">
        	<div class="ConfigDone-section">
            	<div class="container">
                <div class="col-lg-8 col-lg-offset-2">
					<%if((spXML !=null) && (idpService != null) && (idpService != null)) {
					%>         
						<div class="jumbotron" style="color:#000" style="padding: 2rem;">
							<p>Congratulations. You are almost ready.</p>
							<p>Please go to your vIDM administration console, and follow this web page to add a new web applciation.</p>
							<p>Click the white arrow button for guidance.</p>
							<p>Click "LOGIN" button if you are ready.</p>
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
		            <form action=<%=vidmSp1Url %> method="post">
						<p><input type=submit value="LOGIN >>>" class="btn btn-default btn-lg"></p>
					</form>
	            </div>
	            
	            
	                    	
	           
	          </div><!--/row-->

	    	<div id="signcert" class="modal fade">
		        <div class="modal-dialog" style="boder-color:#fff">
		            <div class="modal-content">
		                <div class="modal-header black-background green-font">
		                    <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
		                    <h4 class="modal-title">Signing Certificate</h4>
		                </div>
		                <div class="modal-body black-background">
		                    <div></div>
		                </div>
		            </div>
		        </div>
		    </div>
						
					<%} else {%>
						<div class="jumbotron" style="color:#000">
				            <h2>Cannot fetch data from SP Service!</h2>
				            <p>Hints: Check your vIDM URL and vIDM service.</p>
				         </div>
				         <div>
		                	<form action="spDiscovery.jsp" method="get">
								<div><input type=submit value="<<< Back" class="btn btn-default btn-lg">
								</div>
							</form>
						</div>
					<%} %>
				</div>
				</div>
			</div>
		</section>
		<%if((idpService != null) && (conf != null)) {
					%>   
		<section id="ConfigVIDM" class="content-section text-center" style="height:100%;width:100%;">
				       <div class="row row-offcanvas">
				        <div class="col-xs-12 col-sm-12">
				          
				          <div class="row">
				              <p>Step 1: Select "Identity & Access Management" --> "Identity Providers" --> "Add Identity Provider" --> "Create Third Party IDP"</p>
				              <p><img src="img/idp_config_step1.png" style="width: 1040px;"> </p>
				           
				          </div>
				          
				          <div class="row">
				              <p>Step 2: Fill in the blanks with proper values. Make sure IdP metadata is parsed properly </p>
				              <p><img src="img/idp_config_step2.png" style="width: 1040px;"> </p>	            
					       
				          </div>
				          
				          
				          <div class="row">
				              <p>Step 3: Edit policies with accessing "Policies" --> "Edit Default Policy/Add Policy"</p>
				              <p><img src="img/idp_config_step3.png" style="width: 1040px;"> </p>	            
					       
				          </div>
				          
				          <div class="row">
				              <p>Step 4: Continue Editing policies</p>
				              <p><img src="img/idp_config_step4.png" style="width: 1040px;"> </p>	            
					       
				          </div>
				          
				          <div class="row">
				              <p>Step 5: Add a policy rule with clicking on "+" </p>
				              <p><img src="img/idp_config_step5.png" style="width: 1040px;"> </p>
				           
				          </div>
				          
				          <div class="row">
				              <p>Step 6: Edit policy rules with proper values which just added</p>
				              <p><img src="img/idp_config_step6.png" > </p>
				           
				          </div>
				          
				          <div class="row">
				              <p>Step 7: Drag the rule to the first place and save</p>
				              <p><img src="img/idp_config_step7.png" style="width: 1040px;"> </p>
				           
				          </div>
				          
				          <div class="row">
				              <p>Step 8: Review Summay and save policy</p>
				              <p><img src="img/idp_config_step8.png" style="width: 1040px;"> </p>
				           
				          </div>
				        </div><!--/.col-xs-12.col-sm-12-->
				      </div>
		</section>
		<%} %>
	</body>
</html>