<%@ page language="java" contentType="text/html" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html lang="en">
<head>
<meta name="viewport" content="width=device-width, initial-scale=1.0">

<% 
String resourceId = request.getParameter("resource");
 %>

<title>Search Index and File Index REST API</title>

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
				<a class="navbar-brand" href="#">Search Index and File Index REST API</a>
			</div>
			<div class="collapse navbar-collapse">
				<ul class="nav navbar-nav">
					<li class="active"><a href="#">[ API ]</a></li>
					<li><a href="/SliceRS">[ Code/Text Search ]</a></li>
					<li><a href="#">[ Filename Search ]</a></li>
			
				</ul>
			</div>
			<!--/.nav-collapse -->
		</div>

	</div>


	<div style="margin-top: 10px; margin-left: 30px; margin-right: 30px;">


	<div class="starter-template">
		
	<h3>Search Index and File Index REST API</h3>		
		
	</div>

		<div id="main">

		
<pre>

--------------------------------------------
Query
--------------------------------------------

Create a Query using POST, and then access the query URL to determine the status of the current query, and the number of results that are currently available.

POST:
/SliceRS/jaxrs/resources/{resourceId}/query
o Creates a new query to begin search of resource 'resourceId'.
- post request: Query resource representation (XML/JSON)
- post response: ResourceCreateResponse resource representation (XML/JSON)


GET:
/SliceRS/jaxrs/resources/{resourceId}/query/{id} 
- GET response: Query resource representation (XML/JSON)

GET:
/SliceRS/jaxrs/resources/{resourceId}/query/{id}/status
- GET response: QueryStatus resource representation (XML/JSON)


GET:
/SliceRS/jaxrs/resources/{resourceId}/query/{id}/result?from={from}&to={to}
o Returns results from a given query, beginning with result 'from' and ending with result 'to' (inclusive)
- GET response: SearchResultList resource representation (XML/JSON)


--------------------------------------------
File Contents
--------------------------------------------

Access the contents of a File that was previously identified in a query. 

GET:
/SliceRS/jaxrs/resources/{resourceId}/file?file={file path}&queryId={queryId}
o Get the contents of the specified file
- PARAM: queryId is optional.
- GET Response: FileContents resource representation (XML/JSON)


File Path Indexer:
------

GET:
o 
/SliceFilenameIndexerService/jaxrs/file/{resourceId}/?name={search terms separated by spaces or %20}
- GET Response: FileSearchResultList resource representation (XML/JSON)



--------------------------------------------
Resources and their Representations
--------------------------------------------

All resource representations may be specified in XML or JSON. 
Use the dataType in the header of the request to indicate which is being used, and the result will be returned in the specified format.

Representations in JSON are below. XML are similar, see the listed XML schema for the exact definition.

Query:
{
    "wholeWordOnly": true,
    "caseSensitive": true,
    "showJavaResults": true,
    "showTextResults": true,
    "searchTerm": [
        "one",
        "two",
        "three"
    ],
    "pathIncludeComponentFilterPatterns": [
        "Eclipse"
    ],
    "pathExcludeFilterPatterns": [
        "jhkjhjk"
    ],
    "pathIncludeOnlyFilterPatterns": [
        "hjkhjk"
    ]
}
[Defined in Datatypes.xsd]


QueryStatus:
{
    "status": "COMPLETE_SUCCESS" or "INCOMPLETE",
    "numResults": 10,
    "userMessage": "...",
    "startTime": 1390954675508,
    "searchTermsUrl": "/SliceRS/resources/eclipse/?queryText=one two three&showJavaResults=true&showTextResults=true&wholeWordOnly=true&caseSensitive=true&filterPatterns=jhkjhjk&filterIncludeOnlyPattern=hjkhjk"
}
[Defined in Datatypes.xsd]


ResourceCreateResponse:
{
    "uri": "/SliceRS/jaxrs/resources/eclipse/query/1",
    "userMessage": "..."
}
[Defined in Datatypes.xsd]


SearchResultList:
{"searchResultListEntry":

[{
    "path": "eclipse\\org.eclipse.jaxrs.common\\src\\org\\eclipse\\jaxrs\\ui\\internal\\project\\facet\\JAXRSUserLibraryProviderInstallPanel.java",
    "content": "...",
    "fileUrl": "/SliceRS/fileview.jsp?file=eclipse\\org.eclipse.jaxrs.common\\src\\org\\eclipse\\jaxrs\\ui\\internal\\project\\facet\\JAXRSUserLibraryProviderInstallPanel.java&queryId=2&resourceId=eclipse"
}, ... ] }
[Defined in Datatypes.xsd]


FileContents:
{
    "path": "Eclipse/org.eclipse.jaxrs.common/src/org/eclipse/jaxrs/ui/internal/project/facet/JAXRSUserLibraryProviderInstallPanel.java",
    "filename": "JAXRSUserLibraryProviderInstallPanel.java",
    "contents": "...",
    "component": "Eclipse Web Services"
}
[Defined in Datatypes.xsd]


FileSearchResultList

{
    "resultList": [
        {
            "path": "\\eclipse\\org.eclipse.jaxws.emitter\\src\\org\\eclipse\\jaxws\\finder\\AbstractBinaryFinder.java",
            "url": "/SliceRS/fileview.jsp?file=\\eclipse\\org.eclipse.jaxws.emitter\\src\\org\\eclipse\\jaxws\\finder\\AbstractBinaryFinder.java&resourceId=eclipse"
        },
        ...
    ]
}

[Defined in FilenameIndexerService.xsd]

</pre>
		
		
		</div>
		
	</div>


	<script src="/SliceRS/highlight.js/highlight.pack.js"></script>
	


	<script src="jquery/jquery-1.10.2.min.js"></script>
	<script src="dist/js/bootstrap.min.js"></script>




</body>
</html>
	