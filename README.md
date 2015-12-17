### ChannelFinderService
A simple directory service

#####ChannelFinder Overview

  ChannelFinder is a directory server, implemented as a REST style web service.
Its intended use is within control systems, namely the EPICS Control system, for which it has been written.

* Motivation and Objectives

  High level applications tend to prefer an hierarchical view of the control system name space.
They group channel names by location or physical function. The name space of the EPICS Channel Access protocol,
on the other hand, is flat. A good and thoroughly enforced naming convention may solve the problem of creating
unique predictable names. It does not free every application from being configured explicitly,
so that it knows all channel names it might be interested in beforehand.

  ChannelFinder tries to overcome this limitation by implementing a generic directory service,
which applications can query for a list of channels that match certain conditions,
such as physical functionality or location. It also provides mechanisms to create channel name aliases,
allowing for different perspectives of the same set of channel names.

* Directory Data Structure

 Each directory entry consists of a channel <name>, an arbitrary set of <properties> (name-value pairs),
and an arbitrary set of <tags> (names).

* Basic Operation

 An application sends an HTTP query to the service, specifying an expression that references tags,
properties and their values, or channel names. The service returns a list of matching channels with their
properties and tags, as an XML or JSON document.


#####Installation

  ChannelFinder is a Java EE5 REST-style web service. The directory data is held in
a ElasticSearch index. Service authentication/authorization is done using the
methods provided by the web container (connecting to e.g. LDAP or PAM).
Application authorization, i.e. determining user/group relations, is either available
through an LDAP connection or through a command line script (default: "id") configured
in the web container.

* Prerequisites

  * Glassfish v3.1.2.2 application server

  * <For authN/authZ using LDAP:> LDAP server, e.g. OpenLDAP

* Installation Process

1. Install Glassfish v3

  Download and install Glassfish v3 from
  {{{https://glassfish.dev.java.net/public/downloadsindex.html}java.net}}
  following the instructions for your platform.

2. Install elastic search

  * Download and install elasticsearch from {{{https://www.elastic.co/downloads/elasticsearch}elastic.com}}
    following the instructions for your platform.\
    <Alternatively:> Install the elastic server from your distribution using a package manager.

3. Create the elastic indexes and set up their mapping

  * The Mapping_definitions script (which is avaiable under /channelfinder/src/main/resources) contains the curl commands to setup the 3 elastic indexes associated with channelfinder.
  
  * For more information of how Index and mappings can be setup using any rest client as described here {{{https://www.elastic.co/guide/en/elasticsearch/reference/1.4/_create_an_index.html}create elastic index}}
  

4. Authentication/Authorization using PAM

*** Create the PAM Realm for Service Authentication/Authorization

  * Login to the Glassfish admin console.

  * Open "Common Tasks" / "Configuration" / "server-config" / "Security" / "Realms".

  * Create a new realm called "channelfinder", setting the "Class Name" to
    "com.sun.enterprise.security.auth.realm.pam.PamRealm", and the "JAAS Context" to "pamRealm".

*** Setup Script for Determining Group Membership

  * By default, the "id" command is used. If you are using the same user you have
    from your (Linux) OS, no setting is needed.

  * If you want to change the command (for Windows use or different user database):
    
    * Open "Common Tasks" / "Resources" / "JNDI" / "Custom Resources".

    * Create a new resource called "channelfinder/idManagerCommand", setting the "Resource Type" to
      "java.lang.String", and the "Factory Class" to "org.glassfish.resources.custom.factory.PrimitivesAndStringFactory".

    * Add an additional property with name "Value" and with the script name as value (e.g. "id.bat").

** Authentication/Authorization using LDAP

*** Install an LDAP Server

  * If your site is running an LDAP server, you can skip the next step, and have
    the LDAP manager show you the structure and how to query it.

  * Download and install slapd from {{{http://www.openldap.org/}OpenLDAP.org}}
    following the instructions for your platform.\
    <Alternatively:> Install the slapd daemon from your distribution using a package manager.

  * Set up users and groups. The <<<ldif/cftest.ldif>>> file in the distribution shows
    the LDAP entries I create to run the integration tests. That should give you an idea
    about the structures that ChannelFinder expects.

*** Create the LDAP Realm for Service Authentication/Authorization

  * Login to the Glassfish admin console.

  * Open "Common Tasks" / "Configuration" / "server-config" / "Security" / "Realms".

  * Create a new realm called "channelfinder", setting the "Class Name" to
    "com.sun.enterprise.security.auth.realm.ldap.LDAPRealm", and the "JAAS Context" to "ldapRealm".
    "Directory" and "Base DN" should reflect your LDAP configuration. (My integration tests use
    "ldap://localhost:389" as "Directory" and "dc=cf-test,dc=local" as "Base DN" which connects
    to a slapd on localhost that has loaded the definitions from <<<cftest.ldif>>>.

  * Add a property called "group-search-filter" with the value "memberUid=%s" to make
    authentication work using the usual posixGroup definitions in the LDAP server.

*** Create the LDAP Connection for Determining Group Membership

  * Login to the Glassfish admin console.

  * Open "Common Tasks" / "Resources" / "JNDI" / "Custom Resources".

  * Create a new resource called "channelfinder/ldapManagerConnection",
    setting the "Resource Type" to "javax.naming.directory.Directory",
    and the "Factory Class" to "com.sun.jndi.ldap.LdapCtxFactory".

  * Add the additional properties "URL" to specify the LDAP connection,
    "javax.naming.security.principal" and
    "javax.naming.security.credentials" to specify the name and password used
    to bind to LDAP.
    (My integration test setup uses "URL" = "ldap://localhost/dc=cf-test,dc=local",
    "javax.naming.security.principal" = "cn=channelfinder,dc=cf-test,dc=local", and
    and "javax.naming.security.credentials" = "1234".)

  * Create a new resource called "channelfinder/userManager", setting the "Resource Type" to
    "java.lang.String", and the "Factory Class" to "org.glassfish.resources.custom.factory.PrimitivesAndStringFactory".

  * Add an additional property with name "Value" and with "gov.bnl.channelfinder.LDAPUserManager" as value.

5. Deploy the ChannelFinder Application

  * Temporary step
    Currently the v2 of channelfinder is under active development and requires installation from the source
    Checkout the elasticsearch branch from github
    git clone -b ElasticSearch https://github.com/ChannelFinder/ChannelFinderService.git
    change the configuration in the elasticsearch.yml to match the installation details of your elastic search index.
    build the war 
    mvn clean install
    
    If you do not wish to run the Integration tests add the following
    -DSkipTests=True -DskipCargoAndRun=True -DskipNoseTests=True

  * Drop the ChannelFinder WAR file <<<war/ChannelFinder.war>>> into
    <<<GLASSFISH_HOME/domains/domain1/autodeploy>>>.
    (You may have to create the autodeploy directory if it doesn't exist yet.)\
    <Alternatively:> Call <<<asadmin deploy <WAR-file>>>> (<<<asadmin.bat>>> on Windows)
    inside <<<GLASSFISH_HOME/bin>>>.

* Checking the Installation

6. Using a Browser

  Once deployed, ChannelFinder should be listening for requests.

  For a Glassfish default installation on localhost, the URL "http://localhost:8080/ChannelFinder/"
should get you to the top page of the embedded web site, showing documentation, links to the
SourceForge project site, and Build information.

  For a list of all channels in your database, try pointing your browser to
"http://localhost:8080/ChannelFinder/resources/channels".

7. Using a Generic Client

  For in-depth testing of the service, I recommend using the
{{{http://code.google.com/p/rest-client/}rest-client}} application, that allows a complete
detailed specification of a request and its payload, giving a lot more options than using a browser.
To use rest-client for SSL connections, you will have to download the server certificate of your
Glassfish server (e.g. using a web browser), then use <<<keytool>>> to load it into a trust store,
which then can be configured in rest-client.
{{{http://xcitestudios.com/blog/2011/03/04/using-ssl-in-restclient/}These instructions}} will give
you a general idea.

8 Using the Integration Tests

  If you have set up the necessary users and groups, you can try running the python
integration tests in <<<test/cftest.py>>> against your server.
