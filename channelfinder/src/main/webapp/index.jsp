<%-- 
    Document   : index
    Created on : Sep 9, 2009, 2:36:42 PM
    Author     : rlange
--%>

<%@page language="java" contentType="text/html" pageEncoding="UTF-8"%>
<%
java.util.jar.Manifest manifest = new java.util.jar.Manifest();
java.io.InputStream is = getServletContext().getResourceAsStream("/META-INF/MANIFEST.MF");
if (is == null) {
    out.println("Can't find /META-INF/MANIFEST.MF");
} else {
    manifest.read(is);
}
java.util.jar.Attributes attributes = manifest.getMainAttributes();
%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
   "http://www.w3.org/TR/html4/loose.dtd">

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>ChannelFinder Directory Service</title>
    </head>
    <body>
        <h1>ChannelFinder Directory Service</h1>
        <h3>Version: <%=attributes.getValue("Implementation-Version")%> &nbsp;&nbsp;&nbsp;&nbsp;
            Build: <%=attributes.getValue("Build-Number")%></h3>
        <h2>External Design Documents</h2>
        <ul>
            <li><a href="ChannelFinder-Design.pdf">Design Document</a> (Jan 2010)</li>
            <li><a href="ChannelFinder-API.pdf">API Description</a> (Jun 2010)</li>
        </ul>

        <h2>ChannelFinder Project on SourceForge</h2>
        <ul>
            <li><a href="http://channelfinder.sourceforge.net/">Homepage</a></li>
            <li><a href="http://sourceforge.net/projects/channelfinder/">Project Page</a></li>
            <li><a href="http://sourceforge.net/apps/trac/channelfinder/">Wiki / Issue Tracker</a></li>
            <li><a href="http://channelfinder.hg.sourceforge.net/hgweb/channelfinder/">Repository Browser</a></li>
        </ul>
        <h3>Build Info</h3>
        Number: <%=attributes.getValue("Build-Number")%><br/>
        Id: <%=attributes.getValue("Build-Id")%><br/>
        Tag: <%=attributes.getValue("Build-Tag")%>
    </body>
</html>
