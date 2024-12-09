
# Sample_AuthServer

This is a sample of using vIDM as SP and your web application as IDP. Any user who wants to
access vIDM without authorization will be redirected to your web application, and you 
may verify the user and notify the vIDM, and the user will login to vIDM successfully
without enter username and password.

## How to use this sample

### Prerequisites

* A valid vIDM administrator account.
* Java 1.6+, Maven.

### Configuration

You need to configure your vIDM to use this sample.  

1. Go to your vIDM administrator console, "Identity Manager" --> "Identity Providers"
--> "Add Identity Provider" --> "Create Third Party IDP".  
![Create Third Party IDP](webapp/img/idp_config_step1.png)

2. Fill the blanks with proper values. Just copy the content of [idp.xml](webapp/idp.xml) to the 
SAML Metadata.  
![Fill the blanks](webapp/img/idp_config_step2.png)

3. Configure the polices by "Policies" --> "Edit Default Policy" --> 
"+". Then edit policy rules with proper values.  
![Edit policy](webapp/img/idp_config_step6.png)

4. Drag the rule to the first place and save.

### Build & Run

Be sure you have installed the SamlToolkit by `mvn clean install` on the project base directory.  
Run the following command to start your web application.
```
mvn compile exec:java
```
Then, open [https://127.0.0.1:8443/MyAuthServer](https://127.0.0.1:8443/MyAuthServer), and just
follow the instructions displayed.

## What you need to do

Just refer [MyIDPServlet](src/main/java/com/omnissa/eucenablement/sample/servlet/MyIDPServlet.java) 
to see how it works. You just need to authorize the user in your own way.

If you want to deploy it on web server, please modify the IP Address in [idp.xml](webapp/idp.xml#L66) 
to the actual IP address, and re-configure the third party IDP on your vIDM.

## Documentation

To use vIDM as SP, you need to do the following:

Step 1. Create a new IDPService when your web server is started.
```
SAMLIDPConf conf = new SAMLIDPConf(issuer, kestoreStream, keystorepwd);
service = new IDPService(conf);
```

Step 2. (Optional) Process the SSO request from vIDM.
If necessary, redirect the user to your login page.
```
String relay = request.getParameter("RelayState");
SAMLSsoRequest ssoRequest = service.decodeSAMLRequest(s);
if (ssoRequest != null && ssoRequest.isValid() ) {
    session.setAttribute("request", ssoRequest);
    response.sendRedirect("idpLogin.jsp");
    return;
}
```

Step 3. Verify the user. You may authorize by username/password, or by [OAuth](../Sample_ServerOAuth),
or just any ways you like.

Step 4. If the user has logged in, redirect the user back to vIDM with SAMLSsoResponse.
* If the login is initiated by vIDM and you have received the SSO request,
use the request to generate SSO response.
    ```
    ssoresponse = MyIDP.getIDPService().getSSOResponseByPostBinding(ssoRequest, user);
    response.getOutputStream().write(ssoresponse.getBytes());
    ```
* You can also generate SSO response directly with vIDM URL, and redirect the user to vIDM web page.
    ```
    ssoresponse = MyIDP.getIDPService().getSSOResponseByPostBinding(vidm, user, relay);
    response.getOutputStream().write(ssoresponse.getBytes());	
    ```
