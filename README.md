
# vIDM Integration Toolkit

## Overview

vIDM (Omnissa Identity Manager) Integration Toolkit is a simple Java SDK (software development 
kit) for web developers who want to integrate vIDM with their web services, with either 
SAML or OAuth protocol.

VIDM provides Single-Sign-On (SSO) to SAML (Security Assertion Markup Language) compliant
applications, but it is not a small task for web developers to make their applications
SAML compliant. Many developers prefer OAuth other than SAML.

This toolkit can save developers' effort by providing some easy to use functions, supporting both SAML
and OAtuh. With this toolkit, a web developer can implement SSO integration with vIDM, even if he has no
knowledge about SAML/OAuth. This toolkit is platform independent, since it is written in Java. 

vIDM can play two different roles: identity provider (IDP) or service provider (SP).

### Role 1: Use vIDM as IDP

Your web application can delegate authentication to vIDM. When user accesses your web application without been authenticated, 
he will be redirected to vIDM first. VIDM will tell your web application the user's ID if the user has been authenticated.

You can choose either OAuth or SAML to use vIDM as IDP.

Please read [Sample_WebApp](Sample_WebApp/) for SAML, and read [Sample_AppOAuth](Sample_AppOAuth/) for OAuth.

### Role 2: Use vIDM as SP

vIDM can delegate authentication to your web application. In this case, your web application is IDP, vIDM is SP. 
Once a user has been authenticated by your web application, he can access vIDM without been authenticated again.

You can choose either OAuth or SAML to use vIDM as SP.

Please read [Sample_AuthServer](Sample_AuthServer/) for SAML, and read [Sample_ServerOAuth](Sample_ServerOAuth/) for OAuth.


## Samples

We provide the following four samples, as a quick guide for developers to use this toolkit.

### Sample_AuthServer
[Sample_AuthServer](Sample_AuthServer/) is a demo of using vIDM as SP with SAML. You can start your
web application as an IDP, config your vIDM, and authenticate users through your web application by yourself.

### Sample_WebApp
[Sample_WebApp](Sample_WebApp/) is a demo of using vIDM as IDP with SAML. You can start your web
application as SP, and authenticate users through vIDM.

### Sample_ServerOAuth
[Sample_ServerOAuth](Sample_ServerOAuth/) is also a demo of using vIDM as SP, but with OAuth.
In this sample, vIDM delegates authentication to your web application with SAML, but your web application communicates with some OAuth server with OAuth. In this way, users can login vIDM through any OAuth server, like Google, Facebook, WeChat, Github...... 

### Sample_AppOAuth
[Sample_AppOAuth](Sample_AppOAuth/) is also a demo of using vIDM as IDP, but with OAuth. 
In this sample, your web application can authenticate users through vIDM with OAuth protocol.

## Try it out

### Prerequisites

* A valid vIDM administrator account.
* JDK 1.6+, Maven.

### Build & Run

This is a maven project, just install the toolkit on the project base directory:

```
mvn clean install       # Reinstall the project
```

Then, you may enter the sample directory, and start the sample:
```
cd Sample_AuthServer/   # Enter the sample directory you want to run
mvn compile exec:java   # Start the sample
```

There will be some configuration, just check ReadMe.md in each sample directory for more details.


## Contributing

This vIDM Integration Toolkit project team welcomes contributions from the community.

## License

This product is licensed to you under the BSD-2 license (the "License").  You may not use this product except in compliance with the BSD-2 License.

This product may include a number of subcomponents with separate copyright notices and license terms. Your use of these subcomponents is subject to the terms and conditions of the subcomponent's license, as noted in the LICENSE file.

