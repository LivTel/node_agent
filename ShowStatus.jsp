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
<% 	String telescopeName = NodeAgentProperties.getInstance().getProperty(NodeAgentProperties.NODE_AGENT_NAME);
	String currentPeriod = "13";
	String currentAvailability="45";
%>

<h2><%= telescopeName %> Availability Prediction</h2>
<form action="submitAvailability">
<table>
	<tr>
		<td>Availability predicted for 
			&nbsp;
			<input type="text" name="period" size="3" value="<%= currentPeriod %>">
			&nbsp;
			<select name="periodicity">
	  			<option value="hours">hours
	  			<option value="days">days
			</select>
		</td>
	</tr>
	<tr>
		<td>Is &nbsp;
			<input type="text" size="3" name="availability" value="<%= currentAvailability %>">%
		</td>
	</tr>
	<tr>
		<td colspan="1" valign="middle">
			<input type="submit" name="submit" value="Submit">
		</td>
	</tr>
</table>

</form>

<br><br>
<h2><%= telescopeName %> Status</h2>
<br><br>
Project Aliases:
<table border=1>
<tr><td>Key</td><td>Value</td></tr>
<%      PersistentMap projectAliasMapStore = PersistanceController.getInstance().getProjectAliasMapStore();
        Enumeration paKeysE = projectAliasMapStore.keys();
        while (paKeysE.hasMoreElements()) {
                String key = (String)paKeysE.nextElement();
                String value = projectAliasMapStore.getProperty(key);
        %>
        <tr><td><%=key %></td><td><%=value %></td></tr>
        <%
        } %>
</table>
<br><br>
User Aliases:
<table border=1>
<tr><td>Key</td><td>Value</td></tr>
<%      PersistentMap userAliasMapStore = PersistanceController.getInstance().getUserAliasMapStore();
        Enumeration uaKeysE = userAliasMapStore.keys();
        while (uaKeysE.hasMoreElements()) {
                String key = (String)uaKeysE.nextElement();
                String value = userAliasMapStore.getProperty(key);
        %>
        <tr><td><%=key %></td><td><%=value %></td></tr>
        <%
        } %>
</table>
<br><br>
		<%
			TestClass1 testClass = new TestClass1(); 
		%>
		<%=testClass %>
        
</body>
</html>
