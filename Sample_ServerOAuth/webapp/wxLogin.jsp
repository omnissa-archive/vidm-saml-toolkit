<%@ page language="java" contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8"%>
<%@ page import="com.omnissa.eucenablement.sample.idp.MyIDP"%>
<%@ page import="java.util.*" %>
<%@ page import="com.omnissa.eucenablement.oauth.util.OAuthUtil" %>
<%@ page import="com.omnissa.eucenablement.sample.servlet.WeChatServlet" %>
<%@ page import="java.net.URLEncoder" %>
<%@ page import="org.opensaml.xml.signature.Q" %>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>WeChat Login</title>
</head>

<jsp:include page="headertpl.html"></jsp:include>
<body id="page-top" data-spy="scroll" data-target=".navbar-fixed-top">
    <jsp:include page="navtpl.html"></jsp:include>

    <section id="userLogin" class="content-section text-center">
        <div class="idpDiscovery-section">
            <div class="container">
                <%
                    String header = request.getHeader("User-Agent");
                    String openid = request.getParameter("openid");
                    String errmsg = request.getParameter("errmsg");
                    if (errmsg!=null) {
                        // login failed
                        %>
                            <div><h2>Oops...</h2></div>
                            <div style="margin-top: 40px"><p><% out.print(errmsg); %></p></div>
                            <div>
                            <button class="btn btn-primary" style="margin-top: -15px"
                                onclick="window.location='wxLogin.jsp'">Retry</button>
                            </div>
                        <%
                    }
                    else if (openid!=null) {
                        // login Success
                        %>
                            <div><h2>Login Success!</h2></div>
                            <div style="margin-top: 40px">
                                <p>
                                    Your OpenId: <% out.print(openid); %>
                                </p>
                                <p>
                                    Your Username: <% out.print(request.getParameter("username")); %>
                                </p>
                            </div>

                        <%
                    }

                    //########### Redirect to WeChat QRCode generation directly. ###########//
                    // If you have the authorization of sns_login, You can jump to WeChat directly
                    // See https://open.weixin.qq.com/cgi-bin/showdocument?action=dir_list&t=resource/res_list&id=open1419316505
/*
                    else if (true) {
                        response.sendRedirect(WeChatServlet.getWeChatOAuth(request).getAuthorizationQrcodeUrl(
                                OAuthUtil.encode(request.getRequestedSessionId())));
                    }
*/
                    //########### Generate QRCode by myself. ###########//
                    else if (!header.contains("MicroMessenger")) {
                        // show qrCode
                        %>
                            <div><h2>WeChat Login</h2></div>
                            <div style="margin-top: -8px"><img src="http://api.qrserver.com/v1/create-qr-code/?data=<%=
                                request.getRequestURL().toString()+"?state="+OAuthUtil.encode(request.getRequestedSessionId())
                            %>&size=180x180" /></div>

                            <div style="margin-top: 20px"><p style="font-size: 22px">Use your WeChat to scan the QRCode.</p></div>
                <script type="text/javascript">
                    function check() {
                        $.get("wxLoginAction?query=oauth", function (result) {
                            if (result.code==0) {
                                window.location="wxLoginAction?action=login";
                            }
                        }, "json");
                    }
                    $(document).ready(function () {
                        setInterval(check, 1500);
                    });
                </script>
                        <%
                    }
                    else {
                        // login

                        String state=request.getParameter("state");
                        if (state==null || "".equals(state))
                            state=OAuthUtil.encode(request.getRequestedSessionId());

                        response.sendRedirect(WeChatServlet.getWeChatOAuth(request).getAuthorizationUrl(state));

                    }

                %>

            </div>
        </div>
    </section>
</body>
</html>
