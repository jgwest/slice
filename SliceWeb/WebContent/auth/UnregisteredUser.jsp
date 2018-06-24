<%@ page language="java" contentType="text/html" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="com.slice.indexer.constants.IComponentPair" %>
<%@ page import="com.slice.indexer.shared.Product" %>
<%@ page import="com.slice.indexer.ui.ProductSingleton" %>
<%@ page import="com.slice.indexer.ui.ProductUI"%>

<%
	String resourceId = request.getParameter("resourceId");

	ProductUI productUI = ProductSingleton.getInstance().getProduct(request.getParameter("resourceId"));
	
	Product product = productUI.getProduct();

	
	if(product == null) {
		out.write("<html><body>Unable to find resource.</body></html>");
		return;
	}
 
	// This JSP page should only be displayed on a secure URL
	if(!request.isSecure()) {
		response.sendError(426, "SSL/TLS connection required for password checking. Ensure the server is configured to use HTTPS URL.");
		return;
	}

%>

<!DOCTYPE html>
<html>
<head>
		<link rel="stylesheet" href="../css/slice.css" media="screen">
		<link rel="stylesheet" href="http://ajax.googleapis.com/ajax/libs/dojo/1.6/dijit/themes/claro/claro.css">
		<script src="http://ajax.googleapis.com/ajax/libs/dojo/1.6/dojo/dojo.xd.js"
			data-dojo-config="isDebug:true, parseOnLoad:true">
		</script>
		
	<title>Log-in</title>
</head>
<body class="claro">

<div class="indexbox">

	<p>
		Sorry!<br/>
		<br>
		Your log-in and password were valid, but your email address wasn't in the authorized users list.<br/>
		<br/>
		Talk to your administrator to resolve this issue: <a href="mailto:<%=product.getConstants().getSearchAdminEmail()%>"><%=product.getConstants().getSearchAdminEmail()%></a>.
		<br/>
		<br/>
		Cheers. 
	</p>
	</div>
</body>
</html>