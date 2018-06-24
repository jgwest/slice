<%@page import="com.slice.indexer.constants.IConfigConstants"%>
<%@ page import="java.util.List" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="com.slice.indexer.constants.IComponentPair" %>
<%@ page import="com.slice.indexer.shared.Product" %>
<%@ page import="com.slice.indexer.ui.ProductSingleton" %>
<%@ page import="com.slice.indexer.ui.ProductUI" %>
<%

ProductUI product = ProductSingleton.getInstance().getProduct(request.getParameter("resourceId"));

String resourceId = request.getParameter("resourceId");

if(product == null) {
	out.write("<html><body>Unable to find resource "+resourceId+"</body></html>");
	return;
}


IConfigConstants constants = product.getProduct().getConstants();
%>


<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="utf-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">

<title><%= constants.getProductMessages().getPageTitle() %></title>

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
				<a class="navbar-brand" href="#"><%= constants.getProductMessages().getTopTitle() %></a>
			</div>
			<div class="collapse navbar-collapse">
				<ul class="nav navbar-nav">
					<li><a href="/SliceRS/resources/<%=resourceId%>">[ Code/Text Search ]</a></li>
					<li class="active"><a href="#">[ Filename Search ]</a></li>
					<!-- 
					<li><a href="#contact">Contact</a></li> -->
				</ul>
			</div>
			<!--/.nav-collapse -->
		</div>
	</div>

	<div class="container">

		<div class="starter-template">
			<h1><%= constants.getProductMessages().getContentTitle() %></h1>
			<p class="lead">
				Enter one or more search terms to find matching files.
			</p>
		</div>


		<div class="row">

			<div class="col-lg-6">
				<b>Enter a search term:</b><br/><br/>
				<div class="input-group">
					<input id="searchQuery" type="text" class="form-control"> <span
						class="input-group-btn">
						<button class="btn btn-default" type="button" id="id">Go!</button>
					</span>
				</div>
				<!-- /input-group -->
			</div>
			<!-- /.col-lg-6 -->
		</div>
		<!-- /.row -->


		<div class="inner"></div>

	</div>
	<!-- /.container -->


	<!-- Bootstrap core JavaScript
    ================================================== -->
	<!-- Placed at the end of the document so the pages load faster -->
	<script src="jquery/jquery-1.10.2.min.js"></script>
	<script src="dist/js/bootstrap.min.js"></script>

	<script>
		function pad(str) {

			if (str <= 9) {
				return "0" + str;
			} else {
				return str;
			}

		}

		function search() {
			var searchQuery = $("#searchQuery").val();

			var text = "<div class=\"inner\">";

			text += "		<br />";

			var currentdate = new Date();

			var datetime = "Seach started at: " + currentdate.getDate()
					+ "/" + pad(currentdate.getMonth() + 1) + "/"
					+ currentdate.getFullYear() + " @ "
					+ currentdate.getHours() + ":"
					+ pad(currentdate.getMinutes()) + ":"
					+ pad(currentdate.getSeconds());
			text += datetime;

			text += "<br/></div>";

			$(".inner").replaceWith(text);

			$
					.ajax({
						type : 'GET',

						url : '/SliceFilenameIndexerService/jaxrs/file/<%=product.getProduct().getProductId()%>/?name=' + searchQuery,
						dataType : 'json',
						error : function(data) {
							var text = "<div class=\"inner\">";

							text += "<br/><b>Results</b>:<br/><br/>";
							if(data.status == 413) {
								text += "Whoa there cowboy, your request would return way too many results. Please try again with a tighter query."
							} else if(data.status == 401) {
								text += "Unauthorized user. Go set your cookie! (" + JSON.stringify(data)+")";
							} else {
								text += "An error code response was returned." + JSON.stringify(data);
							}
							
							text += "</div>";

							$(".inner").replaceWith(text);

							
						}, 
						success : function(data) {
							// var output = JSON.stringify(data);
							// alert(output);

							var text = "<div class=\"inner\">";

							text += "<br/><b>Results</b>:<br/><br/>";

							for ( var x = 0; x < data.resultList.length; x++) {

								text += "<a href=\""+data.resultList[x].url+"\" target=\"_blank\">"
										+ data.resultList[x].path + "</a><br/>";

							}

							if (data.resultList.length == 0) {
								text += "N/A.";
							}

							text += "</div>";

							$(".inner").replaceWith(text);
						}
					});

		}

		$('#id').on('click', function(e) {
			search();
		})

		$('#searchQuery').bind("enterKey", function(e) {
			search();
		});

		$('#searchQuery').keyup(function(e) {
			if (e.keyCode == 13) {
				$(this).trigger("enterKey");
			}
		});
	</script>

</body>
</html>
