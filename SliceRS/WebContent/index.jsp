
<%@page import="java.util.regex.Pattern"%>
<%@page import="com.slice.indexer.constants.ISearchFileType"%>
<%@page import="com.slice.indexer.ui.SearchIndexUIUtil"%>
<%@ page import="java.util.List" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="com.slice.indexer.constants.IComponentPair" %>
<%@ page import="com.slice.indexer.ui.ProductSingleton" %>
<%@ page import="com.slice.indexer.shared.Product" %>

<%
	Product product = (Product)request.getAttribute("requestProduct");

	if(product == null) {
%>
	<jsp:include page="resource-list.jsp" />
	<%
		return;
	}

	com.slice.indexer.constants.IConfigConstants configConstants = product.getConstants();
	com.slice.indexer.constants.IProductMessages messages = configConstants.getProductMessages();
	List<IComponentPair> components = configConstants.getComponents();

	String resourceId = request.getParameter("resource");

	List<ISearchFileType> availableSearchFileTypes = configConstants.getSearchFileTypes();



	boolean isCookieSet = SearchIndexUIUtil.isCookieValid(product, request);
	%>

<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="utf-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title><%=  messages.getPageTitle() %></title>

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

<%

// Request



String requestQueryText = request.getParameter("queryText");
if(requestQueryText == null || requestQueryText.trim().length() == 0) {
	requestQueryText = "";
}

// // Defaults to true
// String requestShowJavaResults = request.getParameter("showJavaResults");
// if(requestShowJavaResults == null) {
// 	requestShowJavaResults = "checked";
// } else {
// 	requestShowJavaResults = requestShowJavaResults.equalsIgnoreCase("true") ? "checked" : ""; 
// }

// // Defaults to false
// String requestShowTextResults = request.getParameter("showTextResults");
// if(requestShowTextResults == null) {
// 	requestShowTextResults = "";
// } else {
// 	requestShowTextResults = requestShowTextResults.equalsIgnoreCase("true") ? "checked" : ""; 
// }

String searchFileTypeIdsStr = request.getParameter("searchFileTypeIds");
List<ISearchFileType> queryUrlSearchFileTypeResult = new ArrayList<ISearchFileType>();
if(searchFileTypeIdsStr != null) {
		String[] sfTypeIdsArr = searchFileTypeIdsStr.split(Pattern.quote(","));
		for(String str : sfTypeIdsArr) {
			str = str.trim();
			int id = Integer.parseInt(str);
			
			for(ISearchFileType sfType : configConstants.getSearchFileTypes()) {
				if(sfType.getFileTypeId() == id) {
					queryUrlSearchFileTypeResult.add(sfType);
					break;
				}
			}
		}
	
}


// Defaults to false
String requestWholeWordOnly = request.getParameter("wholeWordOnly");
if(requestWholeWordOnly == null) {
	requestWholeWordOnly = "";
} else {
	requestWholeWordOnly = requestWholeWordOnly.equalsIgnoreCase("true") ? "checked" : ""; 
}

// Defaults to false
String requestCaseSensitive = request.getParameter("caseSensitive");
if(requestCaseSensitive == null) {
	requestCaseSensitive = "";
} else {
	requestCaseSensitive = requestCaseSensitive.equalsIgnoreCase("true") ? "checked" : ""; 
}

List<Boolean> checkedComponents = new ArrayList<Boolean>(components.size());

for(IComponentPair pair : components) {
	String reqText = request.getParameter("show"+pair.getHelperQueryName());
	
	if(reqText != null && reqText.equalsIgnoreCase("true")) {
		checkedComponents.add(true);
	} else {
		checkedComponents.add(false);
	}
}


String requestfilterPatterns = request.getParameter("filterPatterns");
if(requestfilterPatterns == null || requestfilterPatterns.trim().length() == 0) {
	requestfilterPatterns = "";
}


String requestFilterIncludeOnlyPatterns = request.getParameter("filterIncludeOnlyPatterns");
if(requestFilterIncludeOnlyPatterns == null || requestFilterIncludeOnlyPatterns.trim().length() == 0) {
	requestFilterIncludeOnlyPatterns = "";
}

String startSearch = request.getParameter("startSearch");
boolean startSearchAction = startSearch != null && startSearch.equalsIgnoreCase("true"); 

%>

<body>

	<div class="navbar navbar-inverse navbar-fixed-top">
		<div class="container">
			<div class="navbar-header">
				<button type="button" class="navbar-toggle" data-toggle="collapse"
					data-target=".navbar-collapse">
					<span class="icon-bar"></span> <span class="icon-bar"></span> <span
						class="icon-bar"></span>
				</button>
				<a class="navbar-brand" href="/SliceRS/resources/<%=resourceId%>"><%= messages.getTopTitle() %></a>
			</div>
			<div class="collapse navbar-collapse">
				<ul class="nav navbar-nav">
					<li class="active"><a href="#">[ Code/Text Search ]</a></li>
					<li><a href="/SliceFilenameIndexerService/index.jsp?resourceId=<%=resourceId%>">[ Filename Search ]</a></li>
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
			<h1><%= messages.getContentTitle() %></h1>
		</div>


	<div class="row">
<%= messages.getContentDescription()  %>
 
<br/>
<br/>

<!--  begin  -->




<form id="form1" action="" name="example" method="post">
<b><%= messages.getTextToSearchFor() %></b>:<br/><br/>
<input id="query-text" type="text"  
    value="<%=requestQueryText %>" 
    style="width: 400px;" 
    class="form-control"
    />

<br/>

<br/>

<%
for(ISearchFileType availableType:  availableSearchFileTypes) {
	int id = availableType.getFileTypeId();
	boolean checked = false;
	if(queryUrlSearchFileTypeResult != null && queryUrlSearchFileTypeResult.contains(availableType)) {
		checked = true;
	}
	if(availableType.isSelectedByDefault()) {
		checked = true;
	}
	
	%>
	<input type="checkbox" name="searchFileType2_<%=id%>" id="cbSearchFileType<%=id%>" <%=(checked ? "checked" : "")%>/>
	<label for="cbSearchFileType<%=id%>" class="jgwLabel"><%=availableType.getSearchNameText() %></label>
	<br/>
	<%
}
%>
<br/>
<br/>





<input type="checkbox" name="cbwholeword2" id="cbwholeword" <%=requestWholeWordOnly %>/>
<label for="cbwholeword" class="jgwLabel">Show whole-word matches only.</label>


<br/>

<input type="checkbox" name="cbshowcasesensitiveonly2" id="cbshowcasesensitiveonly" <%=requestCaseSensitive %>/>
<label for="cbshowcasesensitiveonly" class="jgwLabel">Show case-sensitive matches only.</label>
<br/>



<br/>
<% if(components != null && components.size() > 0) { %>
	<b>(Optional) Restrict results to <i>only</i> these components:</b><br/>
<% } 

	int c = -1;
	for(IComponentPair component : components) { 
		c++;
		boolean isCheckedFromQuery = checkedComponents.get(c);
	%>
	
		
		<input type="checkbox" id="<%= component.getStrippedName() %>-component-cb" <%= (isCheckedFromQuery ? "checked" : "" ) %>/>
		
		<label for="<%= component.getStrippedName() %>-component-cb" class="jgwLabel"><%= component.getName() %></label>
		<br/>
		<%
	}

%>

<br/>
<b><%=messages.getSpecifyExclude() %></b>:<br/><br/>


<input id="filterpatterns" type="text" 
    value="<%=requestfilterPatterns %>" 
    style="width: 500px;"
    class="form-control"
     />
<br/>
<br/>
<b><%=messages.getSpecifyOnlyInclude() %></b>:<br/><br/>

<input id="filterincludeonlypatterns" type="text" 
    value="<%=requestFilterIncludeOnlyPatterns %>" 
    style="width: 500px;" 
    class="form-control"
     />

<br/>

<%if(isCookieSet) { %>
	<button class="btn btn-default" type="button" id="search-button">Search</button>

<% }  else { %>
	
	<div class="indexbox">
		<% String email = "";
	
		if(product != null) { 
			email = product.getConstants().getSearchAdminEmail();		
		}
		%>
		In order to see search results you need to log-in with your username/password (you can email your administrator for details &nbsp;<a href="mailto:<%=email%>"><%=email %></a>), and you must also be authorized to view the content.
		<br/>
		<br/>
		<a href="/SliceWeb/auth?resourceId=<%=product.getProductId() %>" target="_blank" >Click here to log-in here.</a> 

	</div>

<% } %>



<br/>
</form>
<br/>

<br/>
<br/>
<h3>Results:</h3>

	



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

$.ajaxSetup({
    cache: false
});

// 	function createNewRequest() {
// 		return {
// 				"queryUri" : null,
// 				"preInnerText" : "",
// 				"activeQuery" : false,
// 				"searchStartedTime" : -1	
// 		};		
// 	}
	
	
	// var currentRequest = null;
	
	var queryUri = null;
	var preInnerText = "";
	var activeQuery = false;
	var activeInterval = null;
	var searchStartedTime = -1;
	var insideUAQ = false;
	var lastResult = -1;
	var updateUIStatus = null;
	var resultsReceivedYet = false;
	
	function clearState() {
		queryUri = null;
		preInnerText = "";
		activeQuery = false;
		activeInterval = null;
		searchStartedTime = -1;
		insideUAQ = false;
		lastResult = -1;
		updateUIStatus = null;
		resultsReceivedYet = false;
	}
	
	// ----------------------------------
	
	function startSearch() {
		clearState();
		createQuery();		
	}

	$('#query-text').bind("enterKey", function(e) {
		startSearch();
	});

	$('#query-text').keyup(function(e) {
		if (e.keyCode == 13) {
			$(this).trigger("enterKey");
		}
	});

	
	// ----------------------------------
	
	$( "#search-button" ).bind("click", function() {
		startSearch();
		// if(!activeQuery) {
		// } else {
		// 	alert("Error: Query is currently active.");
		// }
	});

	
		function pad(str) {

			if (str <= 9) {
				return "0" + str;
			} else {
				return str;
			}

		}

		var TAB = "&nbsp;&nbsp;&nbsp;&nbsp;";
		
		
		function createQuery() {
			// Reset
			preInnerText = "";
			activeQuery = true;
			searchStartedTime = -1;
			$(".inner").replaceWith("<div class=\"inner\"></div>");
			$(".preinner").replaceWith("<div class=\"preinner\"></div>");

			
			// Build Query
			
			var pathIncludeComponentFilterPatterns = [];

			<% for(IComponentPair component : components) { %>
			
				if($("#<%=component.getStrippedName()%>-component-cb").is(":checked")) {
					pathIncludeComponentFilterPatterns.push("<%=component.getPath()%>");
				}

			<% } %>
			
			
			var searchFileTypeList = [];
			
			<% for(ISearchFileType sfType : availableSearchFileTypes) {
				int id = sfType.getFileTypeId(); 
			%>
				if( $("#cbSearchFileType<%=id%>").is(":checked") ) {
				
					searchFileTypeList.push(<%=id%>);
					isFirst = false;					
				}
				
			<% } %>
			
			var splitQuery = $("#query-text").val().split(" "); 
			
			var postRequest =  {
					"wholeWordOnly": $("#cbwholeword").is(":checked"), 
					"caseSensitive" : $("#cbshowcasesensitiveonly").is(":checked"),
					"searchFileTypes" : searchFileTypeList,
					"searchTerm": splitQuery
			};
						
			if(pathIncludeComponentFilterPatterns.length >0) {
				postRequest.pathIncludeComponentFilterPatterns = pathIncludeComponentFilterPatterns;
			}

			// Convert filter patterns field to array of strings
			var filterPatterns = $("#filterpatterns").val();
			filterPatterns = filterPatterns.replace(/,/g," ");
			
			if(filterPatterns != null) {
				var filterPatternsSplit = filterPatterns.split(" ");
				var filterPatternsResult = [];
				
				for(var x = 0; x < filterPatternsSplit.length; x++) {
					filterPatternsResult.push(filterPatternsSplit[x]);
				}
				postRequest.pathExcludeFilterPatterns = filterPatternsResult; 
			}
			
			// Convert include only filter patterns field to array of strings
			var filterIncludeOnlyPatterns = $("#filterincludeonlypatterns").val();
			filterIncludeOnlyPatterns = filterIncludeOnlyPatterns.replace(/,/g," ");
			if(filterIncludeOnlyPatterns != null) { 
				var filterIncludeOnlyPatternsSplit = filterIncludeOnlyPatterns.split(" ");
				var filterIncludeOnlyPatternsResult = [];
				
				for(var x = 0; x < filterIncludeOnlyPatternsSplit.length; x++) {
					filterIncludeOnlyPatternsResult.push(filterIncludeOnlyPatternsSplit[x]);
				}
				postRequest.pathIncludeOnlyFilterPatterns = filterIncludeOnlyPatternsResult;
			}
			
			
			$
			.ajax({
				type : 'POST',
				data : JSON.stringify(postRequest),
				contentType : "application/json",
				url : '/SliceRS/jaxrs/resources/<%=resourceId%>/query',
				dataType : 'json',

				success : function(data) {

					lastResult = -1;
					
					// clear inner
					$(".inner").replaceWith("<div class=\"inner\"></div>");
					
					// $(".inner").append("uri:"+data.uri+"<br/><br/>");
					
						preInnerText = "";
					
					queryUri = data.uri;
					
					activeInterval = setInterval(updateUI, 100);
					updateUI();
					
					
				},
				
				error : function(data) {
					if(data != null && data.responseJSON.userMessage != null) {
						
						$(".inner").append(data.responseJSON.userMessage);
					} else {
						alert(JSON.stringify(data));
					}
					
					activeQuery = false;
				}
			});
			
		}
		
		
		function updateUI_latestResults(from, to) {
			$
			.ajax({
				type : 'GET',
				async : false,
				url : queryUri+'/result?from='+from+'&to='+to,
				dataType : 'json',

				success : function(data) {

					var newText = "";
										
					for(var x = 0; x < data.searchResultListEntry.length; x++) {
						var e = data.searchResultListEntry[x];
						newText += "\n";
						newText += "<b>"+e.path+"</b>";
						if(e.fileUrl != null) {
							newText += " [<a href=\""+e.fileUrl+"\" target=\"_blank\">"+"file"+"</a>]";
						}
						newText += ":<br/>\n"+e.content;
					}
					
					$(".inner").append(newText);
					
				},
				error : function(data) {
					$(".inner").append("error :(. details:"+JSON.stringify(data)+" from:"+from+" to:"+to);
				}
			});

		}

		
		function updateUI_getCurrQueryStatus() {
			$
			.ajax({
				type : 'GET',

				url : queryUri+'/status',
				dataType : 'json',
				async: false,

				success : function(data) {

					updateUIStatus = data;
				
					if(searchStartedTime == -1 && updateUIStatus.startTime > 0 ) {
						searchStartedTime = updateUIStatus.startTime;
					}
					
					if(searchStartedTime > 0) {
						var d = new Date(searchStartedTime);
						
						var dateString = d.toLocaleTimeString();
						preInnerText = "Search started at: "+dateString+"<br/><br/>";
					} else {
						preInnerText = "";
					}

					
					if(data.userMessage != null){
						preInnerText += data.userMessage;
					}
					
					if(data.errorMessage != null){
						preInnerText += data.errorMessage+"<br/><br/>";
					}
					
					// $(".inner").replaceWith("<div class=\"inner\">"+innerText+"</div>");
					$(".preinner").replaceWith("<div class=\"preinner\">"+preInnerText+"</div>");

				}, 
				error : function(data) {
					$(".inner").append("error :(. details:"+JSON.stringify(data));
				}
				
				
			});			
		}
		
		function updateUI() {
			
			if(insideUAQ) { return; }
			if(!activeQuery) { return; }
			
			insideUAQ = true;
			
			updateUIStatus = null;
			
			updateUI_getCurrQueryStatus();
			
			if(updateUIStatus != null) {
			
				if((updateUIStatus.status == "COMPLETE_SUCCESS" ||
						updateUIStatus.status == "COMPLETE_ERROR") && (updateUIStatus.numResults - (lastResult+1)) <= 0 ) {
					
					if(updateUIStatus.numResults == 0) {
						$(".inner").append("No results found.<br/>");
					}
					
					if(updateUIStatus.searchTermsUrl != null) {
						$(".inner").append("<br/>[ URL for this search result: <a href=\""+updateUIStatus.searchTermsUrl+"\" target=\"_blank\">"
						                           + "<%= request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() %>"
						                           + updateUIStatus.searchTermsUrl+"</a>]");
					}
										
					// We've received everything we'll get, so stop querying.
					activeQuery = false;
					clearInterval(activeInterval);
					insideUAQ = false;
					
					return;
				}
			}
			
			
			if(updateUIStatus != null) {
			
				if(updateUIStatus.numResults > 0) {
				
					if(!resultsReceivedYet) {
						// If this is the first time we have seen that results are available, adjust our refresh
						// rate higher.
						clearInterval(activeInterval);
						activeInterval = setInterval(updateUI, 250);
						
						resultsReceivedYet = true;						
					}
				
					var from = -1;
					var to = -1;
					
					if(lastResult == -1 || (updateUIStatus.numResults - (lastResult+1)) > 0){
						var numResultsAddedInThisCall = 0;
						
						var newResults;
						if(lastResult == -1) {
							newResults = updateUIStatus.numResults;
						} else {
							newResults = (updateUIStatus.numResults - (lastResult+1));
						}
						
						var recordsToGet = updateUIStatus.numResults > 1000 ? 10 : 100;
							
						// innerText += "-----------<br/>newresults: "+newResults+", totalnum results:"+updateUIStatus.numResults+" <br/>";
						
						// while(there are still results left to process AND (we haven't received more than 1000 result in total; but if we have only process RECORDS_TO_GET per call)   ))
						while(newResults > 0 && !(updateUIStatus.numResults > 1000 && numResultsAddedInThisCall > 20)) {
							from = lastResult+1;
							to = (lastResult+1)+Math.min(newResults, recordsToGet)-1;
							newResults -= (to-from)+1;
							
							numResultsAddedInThisCall += (to-from)+1;
							
							// innerText += "from:"+from+" to:"+to+" newResults:"+newResults+" numResults:"+updateUIStatus.numResults+" <br/>";
							lastResult = to;
							
							// $(".inner").replaceWith("<div class=\"inner\">"+innerText+"</div>");
							
							updateUI_latestResults(from, to);
							
						}
						// innerText += "exit: "+lastResult+"<br/>";
					}
				} else {
					// no results yet.
				}
								
				insideUAQ = false;
			} else {
				insideUAQ = false;
			} // end updateUIStatus if
			
			
		}
		



		
	</script>

</body>
</html>
	