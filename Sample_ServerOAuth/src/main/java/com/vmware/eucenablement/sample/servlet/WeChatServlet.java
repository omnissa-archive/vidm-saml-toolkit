package com.omnissa.eucenablement.sample.servlet;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.HashMap;

import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.server.session.Session;
import org.eclipse.jetty.server.session.SessionHandler;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.omnissa.eucenablement.oauth.OAuth2Config;
import com.omnissa.eucenablement.oauth.util.OAuthUtil;
import com.omnissa.eucenablement.oauth.OAuthException;
import com.omnissa.eucenablement.oauth.impl.WeChatOAuth2Impl;
import com.omnissa.eucenablement.sample.idp.MyIDP;
import com.omnissa.samltoolkit.idp.SAMLSsoRequest;

/**
 * Created by chenzhang on 2017-08-03.
 */
public class WeChatServlet implements Servlet {

    private static Logger log = LoggerFactory.getLogger(WeChatServlet.class);

    public static final String APP_ID = "wxd9d651418ab66fdf";
    public static final String APP_SECRET = "5beceb1752824514e2a9bc8c09aa32e0";
    public static final String REDIRECT_PATH = "/MyAuthServer/wxLoginAction";

    private HashMap<String, String> usermap;

    @Override
    public void init(ServletConfig servletConfig) throws ServletException {
        usermap=new HashMap<>();
        try {
            BufferedReader reader=new BufferedReader(new FileReader("usermap.conf"));
            String line;
            while ((line=reader.readLine())!=null) {
                line=line.trim();
                if (line.startsWith("#") || !line.contains("=")) continue;
                int index=line.indexOf('=');
                String key=line.substring(0, index).trim();
                String value=line.substring(index+1).trim();
                usermap.put(key, value);
            }
        } catch (Exception e) {
            usermap.clear();
        }
    }

    @Override
    public ServletConfig getServletConfig() {
        return null;
    }

    @Override
    public void service(ServletRequest req, ServletResponse res) throws ServletException, IOException {
        Request request = (Request) req;
        Response response = (Response) res;

        WeChatOAuth2Impl weChatOAuth2=getWeChatOAuth(request);

        String samlRequest = request.getParameter("SAMLRequest");
        if (!OAuthUtil.isStringNullOrEmpty(samlRequest)) {
            redirectFromVIDM(request, response, samlRequest);
            return;
        }

        String code=request.getParameter("code");
        String state=request.getParameter("state");

        // query if it's valid
        if ("oauth".equals(request.getParameter("query"))) {
            query(request, response);
            return;
        }

        // login
        if ("login".equals(request.getParameter("action"))) {
            login(request, response);
            return;
        }

        // get jsessionid by state
        if (state!=null) {

            // get the browser's session
            String jsessionid=OAuthUtil.decode(state);
            HttpSession browserSession=jsessionid==null?null:getHttpSession(request, jsessionid);
            if (browserSession==null) {
                response.sendRedirect("wxLogin.jsp?errmsg="+
                    URLEncoder.encode("Invalid qrcode: refresh the qrcode page and re-scan.", "utf-8"));
                return;
            }

            // get openid
            if (code!=null) {
                try {
                    weChatOAuth2.getAccessTokenFromOAuthServer(code);
                    String openid=weChatOAuth2.getOpenId();

                    System.out.println("------------------- Login Succeeded! -------------------");
                    System.out.println("OpenID: "+openid);

                    String username=openid;

                    // TODO: Map the openid to username (according to database or configuration files)
                    if (usermap.containsKey(openid))
                        username=usermap.get(openid);


                    System.out.println("Map this openid to username: "+username);
                    System.out.println("--------------------------------------------------------");

                    // Get the session by jsessionid
                    browserSession.setAttribute("username", username);

                    // if it's the current session
                    if (jsessionid.equals(request.getRequestedSessionId())) {
                        response.sendRedirect("wxLoginAction?action=login");
                        return;
                    }

                    // TODO: Login successfully, just tell the user.
                    response.sendRedirect("wxLogin.jsp?openid="+openid+"&username="+username);
                }
                catch (OAuthException e) {
                    response.sendRedirect("wxLogin.jsp?errmsg="+URLEncoder.encode(e.getMessage(),"utf-8"));
                }
                return;
            }
        }

        response.sendRedirect("wxLogin.jsp");

    }

    private HttpSession getHttpSession(Request request, String extendedId) {
        // Method to handle method permission update for  request.getSessionHandler().getHttpSession(jsessionid);
        SessionHandler handler = request.getSessionHandler();
        String id = handler.getSessionIdManager().getId(extendedId);
        Session session = handler.getSession(id);
        if (session != null && !session.getExtendedId().equals(extendedId)) {
            session.setIdChanged(true);
        }

        return session;
    }

    private void redirectFromVIDM(Request request, Response response, String samlRequest) throws ServletException, IOException {
        try {
            HttpSession session=request.getSession();
            String relay = request.getParameter("RelayState");
            SAMLSsoRequest ssoRequest = MyIDP.getIDPService().decodeSAMLRequest(samlRequest);
            if (relay!=null && relay.length()>0){
                ssoRequest.setRelay(relay);
            }


            if (ssoRequest != null && ssoRequest.isValid() ) {
                session.setAttribute("request", ssoRequest);
                response.sendRedirect("wxLogin.jsp");
                return;
            }

            log.error("Failed to get valid sso response!");
            throw new ServletException("Failed to login: Invalid SSO");
        }
        catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new ServletException(e);
        }
    }

    private void query(Request request, Response response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        String username=(String)session.getAttribute("username");
        if (username!=null) {
            response.getWriter().write(new JSONObject().put("code", 0).toString());
        }
        else {
            response.getWriter().write(new JSONObject().put("code", -1).toString());
        }
    }

    private void login(Request request, Response response) throws ServletException, IOException {
        // try to login...
        HttpSession session = request.getSession();
        String username=(String)session.getAttribute("username");

        // login failed
        if (username==null) {
            response.sendRedirect("wxLogin.jsp?errmsg="+ URLEncoder.encode("You haven't passed the WeChat OAuthUtil!", "utf-8"));
            return;
        }

        // oauth success: login
        SAMLSsoRequest ssoRequest = (SAMLSsoRequest) session.getAttribute("request");
        if (ssoRequest==null) {
            // invalid sso: need to set SAML
            response.sendRedirect("wxLogin.jsp?errmsg="+ URLEncoder.encode("Invalid SSO request: Have you set the vIDM server correctly?", "utf-8"));
            return;
        }

        // confirm login
        response.setContentType("text/html;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        response.getOutputStream().write(MyIDP.getIDPService().getSSOResponseByPostBinding(ssoRequest, username).getBytes());
        response.getOutputStream().flush();

        // Login finished: set username to invalid
        session.removeAttribute("username");

    }

    @Override
    public String getServletInfo() {
        return null;
    }

    @Override
    public void destroy() {

    }

    public static WeChatOAuth2Impl getWeChatOAuth(HttpServletRequest req) {
        if (req==null) return null;
        HttpSession session=req.getSession();
        WeChatOAuth2Impl oAuth2=(WeChatOAuth2Impl)session.getAttribute("oauth");
        if (oAuth2 ==null) {
            oAuth2 =new WeChatOAuth2Impl(new OAuth2Config(APP_ID, APP_SECRET,
                    String.format("%s://%s:%d%s", req.getScheme(),
                            req.getServerName(), req.getServerPort(), REDIRECT_PATH)
            ));
            session.setAttribute("oauth", oAuth2);
        }
        return oAuth2;
    }

}
