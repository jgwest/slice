<%@ page language="java" contentType="text/html" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="com.slice.indexer.constants.IComponentPair" %>
<%@ page import="com.slice.indexer.shared.Product" %>
<%@ page import="com.slice.indexer.ui.ProductSingleton" %>
<%@ page import="com.slice.indexer.ui.ProductUI"%>

<%

ProductUI product = ProductSingleton.getInstance().getProduct(request.getParameter("resourceId"));

if(product == null) {
	out.write("<html><body>Unable to find resource "+request.getParameter("resourceId")+"</body></html>");
	return;
}

%>
<!DOCTYPE html>
<html lang="en">
<head>
<meta name="viewport" content="width=device-width, initial-scale=1.0">

<% 
String resourceId = request.getParameter("resourceId");
 %>

<title><%= product.getProduct().getConstants().getProductMessages().getContentTitle() %></title>

<!-- Bootstrap core CSS -->
<link href="dist/css/bootstrap.css" rel="stylesheet">

<link rel="stylesheet" title="Default" href="/SliceRS/highlight.js/styles/monokai.css" type="text/css" />
	
<!-- Custom styles for this template -->
<link href="starter-template.css" rel="stylesheet">

<!-- Just for debugging purposes. Don't actually copy this line! -->
<!--[if lt IE 9]><script src="../../docs-assets/js/ie8-responsive-file-warning.js"></script><![endif]-->

<!-- HTML5 shim and Respond.js IE8 support of HTML5 elements and media queries -->
<!--[if lt IE 9]>
      <script src="https://oss.maxcdn.com/libs/html5shiv/3.7.0/html5shiv.js"></script>
      <script src="https://oss.maxcdn.com/libs/respond.js/1.3.0/respond.min.js"></script>
    <![endif]-->
    
    
    <style type="text/css">
			.boldstyle2 {
			color: #F04444;
			background-color: #000000;
			font-weight:bold;
			}
	</style>
	
	<style type="text/css">
	    pre {
	      counter-reset: lines;
	    }
	    pre .line {
	      counter-increment: lines;
	    }
	    pre .line::before {
	      content: counter(lines); text-align: right;
	      display: inline-block; width: 4em;
	      padding-right: 0.5em; margin-right: 0.5em;
	      color: #BBB; border-right: solid 1px;
	    }
  </style>
	
	    
</head>

<body>

	<div class="navbar navbar-inverse navbar-fixed-top">
		<div class="container">
			<div class="navbar-header">
				<button type="button" class="navbar-toggle" data-toggle="collapse"
					data-target=".navbar-collapse">
					<span class="icon-bar"></span> <span class="icon-bar"></span> <span
						class="icon-bar"></span>
				</button>
				<a class="navbar-brand" href="#"><%= product.getProduct().getConstants().getProductMessages().getTopTitle() %></a>
			</div>
			<div class="collapse navbar-collapse">
				<ul class="nav navbar-nav">
					<!-- 
					<li class="active"><a href="#">Home</a></li>
					<li><a href="#about">About</a></li>
					<li><a href="#contact">Contact</a></li> -->
				</ul>
			</div>
			<!--/.nav-collapse -->
		</div>

	</div>


	<div style="margin-top: 60px; margin-left: 30px; margin-right: 30px;">


	<div id="premain">
		
		
	</div>

		<div id="main">
		
		
		</div>
		
	</div>


	<script src="/SliceRS/highlight.js/highlight.pack.js"></script>

	<script src="jquery/jquery-1.10.2.min.js"></script>
	<script src="dist/js/bootstrap.min.js"></script>

	<script>
	
		hljs.initHighlightingOnLoad();		
		
		function endsWith(str, suffix) {
 		   return str.indexOf(suffix, str.length - suffix.length) !== -1;
		}
	
		function updateUI() {
		
		<% 
		String path = request.getParameter("file");
		if(path != null) {
			path = path.replace("\\", "\\\\");
		}
		%>
		
			var path = "<%=  path %>";
			var queryId = "<%= request.getParameter("queryId") %>";
			if(queryId == "null") { queryId = null; }
			
		
			$
			.ajax({
				type : 'GET',
				async : false,
				url : '/SliceRS/jaxrs/resources/<%=resourceId%>/file?file='+path +(queryId != null ? '&queryId='+queryId : '') ,
				dataType : 'json',

				success : function(data) {

					var newText = "";

					var contents = data.contents;
					
					var codeType = "";
					
					var isJava = endsWith(path.trim().toLowerCase(), ".java") ? "java " : "";
					var isXml = endsWith(path.trim().toLowerCase(), ".xml") ? "xml " : "";
					
					codeType = isJava+isXml;
					
					newText ="<pre><code class=\""+codeType+"lineNumbers\">"+contents+"</code></pre>";
					
					
					$("#main").append(newText);
		
					newText = "<b>Path</b>: "+data.path+"<br/>";
					
					if(data.component != null) {
						newText += "<i>Component</i>: "+data.component+"<br/><br/>";
					} else {
						newText += "<br/>";
					}
					
					$("#premain").append(newText);
								
					
				},
				error : function(data) {
					$("#main").append("<a href=\"/SliceWeb/auth/?resourceId=<%=resourceId%>\">You need to log-in here.</a>");
					// $("#main").append("error :(. details:"+JSON.stringify(data.responseText));
					// $("#main").append("error :(. details:"+JSON.stringify(data.responseText));
				}
			});

		}

		$( document ).ready(updateUI);
	
	</script>

</body>
</html>
	