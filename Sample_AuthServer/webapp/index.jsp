<!--  
 Omnissa Identity Manager SAML Toolkit

Copyright (c) 2016 Omnissa, LLC. All Rights Reserved.

This product is licensed to you under the BSD-2 license (the "License").  You may not use this product except in compliance with the BSD-2 License.

This product may include a number of subcomponents with separate copyright notices and license terms. Your use of these subcomponents is subject to the terms and conditions of the subcomponent's license, as noted in the LICENSE file.


-->
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%@ page import="com.omnissa.eucenablement.sample.idp.MyIDP" %>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
	<!-- header -->
	<jsp:include page="headertpl.html"></jsp:include>
	<body id="page-top" data-spy="scroll" data-target=".navbar-fixed-top">
		 <!-- Navigation -->
    	<jsp:include page="navtpl.html"></jsp:include>
    	<% if (MyIDP.getIDPService()==null || MyIDP.getIDPService().getSAMLIDPConf()==null || !MyIDP.getIDPService().getSAMLIDPConf().isConfigured()){
    		//if IDP service has not been configured, you must config it first
    	%>
    	<section id="idpDiscovery" class="content-section text-center">
	        <div class="idpDiscovery-section">
	            <div class="container">
	                <div class="col-lg-8 col-lg-offset-2">
	                
	                <div><h2>Configure VIDM as SP</h2></div>
	                <br>
	                <br>
	                	<form action="configSPURI.jsp" method="post">
	                	<p>Your vIDM (act as SP1) ROOT URL:</p> 
	                	<br/>
	                	
                		<div id="vidmSp1"><input name="vidmSp1" type="text" style="width: 300px;color:#fff;background-color:#000;" placeholder="https://yourcompany.omnissaidentity.com"></div>
	                	
						<br/>
						<br/>
						<br/>
						
						<div><input type=submit value="NEXT" class="btn btn-default btn-lg"></div>
						</form>
	               </div>
	            </div>
	        </div>
	    </section>
    
	    
		
		<%
		} else{
			//idp service has been configured, we just go to login page
			
			
			
    	%>
    	
    	<section id="userLogin" class="content-section text-center">
	        <div class="idpDiscovery-section">
	            <div class="container">
	                <div class="col-lg-8 col-lg-offset-2">
	                
	                <div><h2>Login page</h2></div>
	                <br>
	                <br>
	                	<form id="loginForm" class="form-login" action="saml2postlogin" method="POST" autocomplete="off">
						    <div >
						        <div >
						            <input name="username" type="text" style="width: 300px;color:#fff;background-color:#000;" placeholder="username">
						        </div>
						    </div>
						    <br>
						    <div>
						        <div >
						            <input name="password" type="password" style="width: 300px;color:#fff;background-color:#000;" placeholder="password">
						        </div>
						    </div>
						    <br>
						    <div class="clearfix bottom-section"style="margin-top: 10px;">
						        <input type="submit" name="loginFormSubmit" class="btn btn-default btn-lg" value="Sign in">
						    </div>
						</form>
	               </div>
	            </div>
	        </div>
	    </section>
    	
    	<%
		}
    	%>
	</body>
</html>
