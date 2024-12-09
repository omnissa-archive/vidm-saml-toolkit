/*
 * Omnissa Identity Manager SAML Toolkit
 *
 * Copyright (c) 2016 Omnissa, LLC. All Rights Reserved.
 *
 * This product is licensed to you under the BSD-2 license (the "License").  You may not use this product except in compliance with the BSD-2 License.
 *
 * This product may include a number of subcomponents with separate copyright notices and license terms. Your use of these subcomponents is subject to the terms and conditions of the subcomponent's license, as noted in the LICENSE file.
 *
 */
package com.omnissa.eucenablement.sample;

import java.net.URL;
import java.security.KeyStoreException;

import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.webapp.WebAppContext;

import com.omnissa.eucenablement.sample.idp.MyIDP;
import com.omnissa.eucenablement.sample.servlet.WeChatServlet;


/**
 * Simple HTTP server for demo purpose.
 */
public class MyAuthServer {

    public static void main(String[] args) throws KeyStoreException {

        Server server = new Server();


        // HTTPS configuration
        HttpConfiguration https = new HttpConfiguration();
        https.addCustomizer(new SecureRequestCustomizer());

        // Configuring SSL
        SslContextFactory.Server sslContextFactoryServer = new SslContextFactory.Server();
        URL keystoreurl = MyAuthServer.class.getResource("/sslkeystore");

        System.out.println("keystore path:" + keystoreurl.getPath());
        // Defining keystore path and passwords
        sslContextFactoryServer.setKeyStorePath(keystoreurl.getPath());
        String keystorepwd = "123456";
        sslContextFactoryServer.setKeyStorePassword(keystorepwd);

        sslContextFactoryServer.setTrustAll(true);
        sslContextFactoryServer.setNeedClientAuth(false);

        // Configuring the connector
        ServerConnector sslConnector = new ServerConnector(server, new SslConnectionFactory(sslContextFactoryServer, "http/1.1"), new HttpConnectionFactory(https));
        sslConnector.setPort(8443);

        server.addConnector(sslConnector);

        server.setStopAtShutdown(true);

        // Set JSP to use Standard JavaC always
        System.setProperty("org.apache.jasper.compiler.disablejsr199", "false");

        WebAppContext webAppContext = new WebAppContext();

        String webapp = "webapp";

        webAppContext.setDescriptor(webapp + "/WEB-INF/web.xml");
        webAppContext.setResourceBase(webapp);
        webAppContext.setContextPath("/MyAuthServer");
        webAppContext.setParentLoaderPriority(true);
        webAppContext.setClassLoader(Thread.currentThread().getContextClassLoader());

        webAppContext.addServlet(WeChatServlet.class.getCanonicalName(), "/wxLoginAction");

        server.setHandler(webAppContext);
        try {
            server.start();

            String url = "https://localhost:8443/MyAuthServer";
            System.out.println("Open your browser to view the demo: " + url);

            // https://127.0.0.1:8443/MyAuthServer/idp.xml
            MyIDP.initIDPService(url + "/idp.xml", keystoreurl.openStream(), keystorepwd);
            server.join();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}