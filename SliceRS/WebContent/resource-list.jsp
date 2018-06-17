
<%@ page import="java.util.List" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="com.slice.indexer.constants.IComponentPair" %>
<%@ page import="com.slice.indexer.ui.ProductSingleton" %>
<%@ page import="com.slice.indexer.shared.Product" %>

<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="utf-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>Resource list</title>

<!-- Bootstrap core CSS -->
<link href="dist/css/bootstrap.css" rel="stylesheet">

<!-- Custom styles for this template -->
<link href="starter-template.css" rel="stylesheet">

<!-- Just for debugging purposes. Don't actually copy this line! -->
<!--[if lt IE 9]><script src="../../docs-assets/js/ie8-responsive-file-warning.js"></script><![endif]-->

<!-- HTML5 shim and Respond.js IE8 support of HTML5 elements and media queries -->
<!--[if lt IE 9]>
      <script src="https://oss.maxcdn.com/libs/html5shiv/3.7.0/html5shiv.js"></script>
      <script src="https://oss.maxcdn.com/libs/respond.js/1.3.0/respond.min.js"></script>
    <![endif]-->
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
				<a class="navbar-brand" href="#">Available Resources</a>
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

	<div class="container" >

		<div class="starter-template">
			<h1>Available Resources</h1>
		</div>


	<div class="row">
<br/>
<br/>

<!--  begin  -->

<h4>The following resources are available to be searched: </h4><br/>
Click below to go to the search page for the selected resource.<br/>
<br/>

<%
	for(com.slice.indexer.ui.ProductUI p : ProductSingleton.getInstance().getProductList()) {

	com.slice.indexer.constants.IConfigConstants configConstants = p.getProduct().getConstants();
	
	//List<IComponentPair> components = configConstants.getComponents();

	String productId = p.getProduct().getProductId();
%>
	
	<a href="resources/<%=productId%>"><%=productId%> </a>
	
	<br/>
	
	
	<%

}


 %>

<!-- end -->




			<div class="col-lg-6">
				<div class="input-group">
					<span class="input-group-btn">
<!-- 						<button class="btn btn-default" type="button" id="id">Go!</button> -->
					</span>
				</div>
				<!-- /input-group -->
			</div>
			<!-- /.col-lg-6 -->
		</div>
		<!-- /.row -->

		<div class="preinner"></div>
		<div class="inner"></div>

	</div>
	<!-- /.container -->


	<!-- Bootstrap core JavaScript
    ================================================== -->
	<!-- Placed at the end of the document so the pages load faster -->
	<script src="jquery/jquery-1.10.2.min.js"></script>
	<script src="dist/js/bootstrap.min.js"></script>

	<script>

		
	</script>

</body>
</html>
	