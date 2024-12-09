<!--  
 Omnissa Identity Manager SAML Toolkit

Copyright (c) 2016 Omnissa, LLC. All Rights Reserved.

This product is licensed to you under the BSD-2 license (the "License").  You may not use this product except in compliance with the BSD-2 License.

This product may include a number of subcomponents with separate copyright notices and license terms. Your use of these subcomponents is subject to the terms and conditions of the subcomponent's license, as noted in the LICENSE file.


-->
<%@ page contentType="text/html;charset=UTF-8" %>
<html>

	<!-- header -->
	<jsp:include page="headertpl.html"></jsp:include>
	<body id="page-top" data-spy="scroll" data-target=".navbar-fixed-top">
		<script>
			function logoutFromIDP(reqURL) {
				window.open(reqURL,'_blank');
			}
		</script>
		<!-- Navigation -->
    	<jsp:include page="navtpl.html"></jsp:include>

		<% 
			if(session.getAttribute("userName") != null) {
				//TODO: these should be moved to some jsp after receving logout response from vidm
		
				String logoutURL = MySSO.getSSOService().getLogoutURLRedirect();
				session.removeAttribute("userName");
				session.invalidate();
				if(logoutURL != null) {
					String scriptCall = String.format("<script>logoutFromIDP('%s')</script>", logoutURL);
					out.println(scriptCall);
				}
			}
		%>

    	<section id="logout" class="content-section text-center">
            <div class="container">
                <div class="col-lg-8 col-lg-offset-2">
                	 <div class="jumbotron" style="color:#000">
			            <h2>You have logged out from vIDM!</h2>
			        </div>
			        <div>
	                	<form action="index.jsp" method="get">
							<div><input type=submit value="Back to welcome page" class="btn btn-default btn-lg">
							</div>
						</form>
					</div>
               </div>
            </div>
    	</section>
	</body>
</html>