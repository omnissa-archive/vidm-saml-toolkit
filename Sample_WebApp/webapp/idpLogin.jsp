<!--  
 Omnissa Identity Manager SAML Toolkit

Copyright (c) 2016 Omnissa, LLC. All Rights Reserved.

This product is licensed to you under the BSD-2 license (the "License").  You may not use this product except in compliance with the BSD-2 License.

This product may include a number of subcomponents with separate copyright notices and license terms. Your use of these subcomponents is subject to the terms and conditions of the subcomponent's license, as noted in the LICENSE file.


-->
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Login</title>
</head>

<jsp:include page="headertpl.html"></jsp:include>
<body id="page-top" data-spy="scroll" data-target=".navbar-fixed-top">

	<jsp:include page="navtpl.html"></jsp:include>
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
						    <div class="clearfix bottom-section">
						        <input type="submit" name="loginFormSubmit" class="btn btn-default btn-lg" value="Sign in">
						    </div>
						</form>
	               </div>
	            </div>
	        </div>
	    </section>
	
	


</body>
</html>