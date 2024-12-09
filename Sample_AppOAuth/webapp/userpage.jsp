<!--  
 Omnissa Identity Manager SAML Toolkit

Copyright (c) 2016 Omnissa, LLC. All Rights Reserved.

This product is licensed to you under the BSD-2 license (the "License").  You may not use this product except in compliance with the BSD-2 License.

This product may include a number of subcomponents with separate copyright notices and license terms. Your use of these subcomponents is subject to the terms and conditions of the subcomponent's license, as noted in the LICENSE file.


-->
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
	<!-- header -->
	<jsp:include page="headertpl.html"></jsp:include>
	<body id="page-top" data-spy="scroll" data-target=".navbar-fixed-top">
		<!-- Navigation -->
    	<jsp:include page="navtpl.html"></jsp:include>
		
    	<section id="User info" class="content-section text-center">
	        <div class="indexconf-section">
	            <div class="container">
	                <div class="col-lg-8 col-lg-offset-2">

						<%
							String errmsg = request.getParameter("errmsg");
							if (errmsg!=null) {
							    %>
						<div><h2>Oops...</h2></div>
						<div style="margin-top: 40px"><p><% out.print(errmsg); %></p></div>
						<div>
							<button class="btn btn-primary" style="margin-top: -15px"
									onclick="window.location='idpDiscovery.jsp'">Retry</button>
						</div>
								<%
							}
							else {
						%>

	                	 <div class="jumbotron" style="color:#000">
				            <h2>Welcome, <%=session.getAttribute("username")%>!</h2>
							 <p>Your email: <%=session.getAttribute("email")%></p>
				          </div>
						<%
							}

						%>
	               </div>
	            </div>
	        </div>
    	</section>
	</body>
</html>