<!--  
 Omnissa Identity Manager SAML Toolkit

Copyright (c) 2016 Omnissa, LLC. All Rights Reserved.

This product is licensed to you under the BSD-2 license (the "License").  You may not use this product except in compliance with the BSD-2 License.

This product may include a number of subcomponents with separate copyright notices and license terms. Your use of these subcomponents is subject to the terms and conditions of the subcomponent's license, as noted in the LICENSE file.


-->
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List" %>
<%@ page import="com.omnissa.eucenablement.sample.MySSO" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html lang="en">
<!-- header -->
<jsp:include page="headertpl.html"></jsp:include>
<!-- TODO: support both IDP and SP now, we need to add a UI flow for IDP -->
<body id="page-top" data-spy="scroll" data-target=".navbar-fixed-top">
    <!-- Navigation -->
    <jsp:include page="navtpl.html"></jsp:include>

    <!-- Intro Header -->
    <header class="intro">
        <div class="intro-body">
            <div class="container">
                <div class="row">
                    <div class="col-md-8 col-md-offset-2">
                        <h1 class="brand-heading">SAML TOOLKIT</h1>
                        <p class="intro-text">A toolkit to facilitate the vIDM users to have their business application SAML enabled to play with vIDM.</p>
                        <form action="idpDiscovery.jsp" method="get" style="padding: 2rem;">
							<div align="middle">
							<input type=submit value="CONFIGURE" class="btn btn-default btn-lg" title="Configure your vIDM (as Identity Provider)">
							</div>
						</form>
						
                    </div>
                </div>
            </div>
        </div>
    </header>

   
</body>
</html>
