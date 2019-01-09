<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"
    import="java.util.Iterator,
    org.estar.rtml.RTMLIntelligentAgent,
    java.util.Enumeration,
    org.estar.storage.PersistentMap,
    org.estar.storage.PersistanceController,
    org.estar.client.NodeTester,
    org.apache.soap.rpc.Parameter,
	org.apache.soap.rpc.Response,
	org.apache.soap.SOAPException,
	org.estar.configuration.NodeAgentProperties,
	org.estar.test.TestClass1"%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Service Status and Availability Prediction</title>
<body>

<br><br>
		<%
			TestClass1 testClass = new TestClass1(); 
		%>
		<%=testClass %>
        
</body>
</html>
