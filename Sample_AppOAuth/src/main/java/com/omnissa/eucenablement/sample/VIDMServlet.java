package com.omnissa.eucenablement.sample;

import com.omnissa.eucenablement.oauth.OAuth2Config;
import com.omnissa.eucenablement.oauth.impl.VIDMOAuth2Impl;
import com.omnissa.eucenablement.oauth.util.HttpRequest;
import com.omnissa.eucenablement.oauth.util.OAuthUtil;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URLEncoder;

import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * Created by chenzhang on 2017-08-25.
 */
public class VIDMServlet implements Servlet {

    private static Logger log = LoggerFactory.getLogger(VIDMServlet.class);

    public static final String APP_ID = "Sample_AppOAuth";
    public static final String APP_SECRET = "bnDqMk8j25LeZLYgTr76KurM0lzpVBJ1cJHfJkGV2ECMOs7h";
    public static final String REDIRECT_URI = "https://127.0.0.1:8443/WebApp/oauth";

    @Override
    public void init(ServletConfig servletConfig) throws ServletException {

    }

    @Override
    public ServletConfig getServletConfig() {
        return null;
    }

    @Override
    public void service(ServletRequest req, ServletResponse res) throws ServletException, IOException {

        Request request=(Request)req;
        Response response=(Response)res;
        VIDMOAuth2Impl vidmoAuth2=getVIDMOAuth(request);

        String code=request.getParameter("code");

        if (OAuthUtil.isStringNullOrEmpty(code)) {
            response.sendRedirect("userpage.jsp?errmsg="+ URLEncoder.encode("Return code is null!", "utf-8"));
            return;
        }
        try {
            vidmoAuth2.getAccessTokenFromOAuthServer(code);
            String username=vidmoAuth2.getUsername(), email=vidmoAuth2.getEmail();
            request.getSession().setAttribute("username", username);
            request.getSession().setAttribute("email", email);
            response.sendRedirect("userpage.jsp");
        }
        catch (Exception e) {
            response.sendRedirect("userpage.jsp?errmsg="+URLEncoder.encode(e.getMessage(), "utf-8"));
        }

    }

    @Override
    public String getServletInfo() {
        return null;
    }

    @Override
    public void destroy() {

    }

    public static VIDMOAuth2Impl getVIDMOAuth(HttpServletRequest req) {
        if (req==null) return null;
        HttpSession session=req.getSession();
        VIDMOAuth2Impl vidmoAuth2=(VIDMOAuth2Impl)session.getAttribute("oauth");
        if (vidmoAuth2==null) {
            vidmoAuth2=new VIDMOAuth2Impl(new OAuth2Config(APP_ID, APP_SECRET, REDIRECT_URI));
            session.setAttribute("oauth", vidmoAuth2);
        }
        return vidmoAuth2;
    }

    public static boolean isValidHost(String host) {
        try {
        	SslUtilities.trustAllCertificates(); //Trust all certificate in sample. This should be removed in a real production environment.
            return host!=null && HttpRequest.get(host+"/SAAS/API/1.0/GET/metadata/idp.xml").code()==200;
        }
        catch (Exception e) {return false;}
    }

}
