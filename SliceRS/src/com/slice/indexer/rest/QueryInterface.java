/*
 * Copyright 2018 Jonathan West
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
*/

package com.slice.indexer.rest;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.slice.datatypes.ObjectFactory;
import com.slice.datatypes.QueryDTO;
import com.slice.datatypes.QueryStatusDTO;
import com.slice.datatypes.ResourceCreateResponseDTO;
import com.slice.datatypes.ResultEntryDTO;
import com.slice.datatypes.SearchResultListDTO;
import com.slice.indexer.constants.IComponentPair;
import com.slice.indexer.constants.ISearchFileType;
import com.slice.indexer.shared.util.JavaSrcSearchQuery;
import com.slice.indexer.ui.ProductSingleton;
import com.slice.indexer.ui.ProductUI;
import com.slice.indexer.ui.SearchIndexUIUtil;

/** JAX-RS service for Query, used to create queries, get their current status, and retrieve query results/content. */
@Path("/resources/{resourceid}/query")
public class QueryInterface {
	
	@PathParam("resourceid")
	String _resourceId;
	
	@Context
	HttpServletRequest _request;
		
	
	/** Filter the query and return true if it's still valid; otherwise errors will be stored in sb. */
	private static boolean filterSearchQueryIsValid(JavaSrcSearchQuery query, StringBuilder sb) {

		List<String> keys = query.getSearchTerm();
		
		for(int x = 0; x < keys.size(); x++) {
			boolean removeTerm = false;
			String key = keys.get(x).replace("-", " ").replace("*", " ").trim();
			
			if(key.length() == 0) {
				removeTerm = true;
			}
			
			if(!removeTerm && key.length() <= 2) {			
				sb.append("* Skipped search term '"+key+"' as it is 2 or less characters. <br/>\n");
				removeTerm = true;
			}
			
			if(removeTerm) {
				keys.remove(x);
				x--;
				removeTerm = false;
			} else {
				keys.set(x, key);
			}
			
		}
		
		if(keys.size() == 0) {
			sb.append("<br/>No search terms left. :) <br/>");
			
			return false;
		} else {
			return true;
		}
				
	}
	
	private static String generateSearchTermUrl(ProductUI p, JavaSrcSearchQuery query) {
		StringBuilder sb = new StringBuilder();
		
		String productId = p.getProduct().getProductId();
		
		sb.append("/SliceRS/resources/"+productId+"/?queryText=");
		
		for(int x = 0; x < query.getSearchTerm().size(); x++) {
			String str = query.getSearchTerm().get(x);
			sb.append(str);
			// Append space for all but the last
			if(x+1 < query.getSearchTerm().size()) {
				sb.append(" ");
			}
		}
		
		
		sb.append("&searchFileTypeIds=");
		boolean isFirst = true;
		for(ISearchFileType sfType : query.getSearchFileTypes()) {
			if(isFirst) {
				isFirst = false;
			} else {
				sb.append(",");
			}
			sb.append(sfType.getFileTypeId());			
		}
		
		sb.append("&caseSensitive="+query.isCaseSensitive());

		for(IComponentPair pair : p.getProduct().getConstants().getComponents()) {
			
			boolean pairFound = false;
			
			for(String str : query.getPathIncludeComponentFilterPatterns()) {
				str = str.toLowerCase().trim();
				if(str.contains(pair.getPath().toLowerCase())) {
					pairFound = true;
				}
			}
			
			if(pairFound) {
				sb.append("&show"+pair.getHelperQueryName()+"=true");
			}
		}
		
		if(query.getPathExcludeFilterPatterns().length > 0) {
			sb.append("&filterPatterns=");
			for(int x = 0; x < query.getPathExcludeFilterPatterns().length; x++) {
				String str = query.getPathExcludeFilterPatterns()[x];
				sb.append(str);
				// Append space for all but the last
				if(x+1 < query.getPathExcludeFilterPatterns().length) {
					sb.append(" ");
				}
			}
		}

		
		if(query.getPathIncludeOnlyFilterPatterns().length > 0) {
			sb.append("&filterIncludeOnlyPatterns=");
			for(int x = 0; x < query.getPathIncludeOnlyFilterPatterns().length; x++) {
				String str = query.getPathIncludeOnlyFilterPatterns()[x];
				sb.append(str);
				// Append space for all but the last
				if(x+1 < query.getPathIncludeOnlyFilterPatterns().length) {
					sb.append(" ");
				}
			}
		}	
		
		
		return sb.toString();
	}
	
	
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	@Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	@POST
	public Response newQuery(QueryDTO param) { 

		ObjectFactory of = new ObjectFactory();

		ResourceCreateResponseDTO result = of.createResourceCreateResponseDTO();
		
		ProductUI p = null;		
		if(_resourceId != null) {
			p = ProductSingleton.getInstance().getProduct(_resourceId);
		}

		if(p == null) {			
			result.setUserMessage("Invalid resource id:"+_resourceId);
			return Response.status(403).entity(result).build();
		}
		
		
		StringBuilder errorMsgs = new StringBuilder();
		if(!SearchIndexUIUtil.checkCookieInRequest(p.getProduct(), _request, errorMsgs)) {
			result.setUserMessage(errorMsgs.toString());
			return Response.status(403).entity(result).build();
		}

		JavaSrcSearchQuery query = RestUtil.convertQueryDTO(param, p.getProduct().getConstants());
		
		boolean isValid = filterSearchQueryIsValid(query, errorMsgs);
		if(!isValid) {
			result.setUserMessage(errorMsgs.toString());
			return Response.status(403).entity(result).build();
		}
		
		int queryId = CentralQueryManager.getInstance(p).newQuery(query);
		
		query.setQueryId(queryId);
		
		query.setSearchTermUrl(generateSearchTermUrl(p, query));
		
		result.setUri("/SliceRS/jaxrs/resources/"+_resourceId+"/query/"+queryId);

		// Log before returning
		Log.getInstance().logNewQuery(param, _request.getRemoteHost());

		return Response.ok(result).build();
		
	}
	

	@GET
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	@Path("/{id}")
	public Response getQueryContent(@PathParam("id") int id) {
		
		ProductUI p = null;		
		if(_resourceId != null) {
			p = ProductSingleton.getInstance().getProduct(_resourceId);
		}

		if(p == null) {			
			return Response.status(403).entity(	("Invalid resource id:"+_resourceId)	).build();
		}

		
		IQueryDatabaseResult dbResult = CentralQueryManager.getInstance(p).getQueryDatabaseResult(id);
		if(dbResult == null) {
			return Response.status(404).build();			
		}

		QueryDTO result = RestUtil.convertJavaSrcSearchQuery(dbResult.getQuery());
		
		return Response.ok(result).build();
		
	}

	
	@GET
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	@Path("/{id}/status")
	public Response queryStatus(@PathParam("id") int id) {

		ObjectFactory of = new ObjectFactory();
		QueryStatusDTO qs = of.createQueryStatusDTO();

		ProductUI p = null;		
		if(_resourceId != null) {
			p = ProductSingleton.getInstance().getProduct(_resourceId);
		}

		if(p == null) {			
			qs.setErrorMessage("Invalid resource id:"+_resourceId);
			return Response.status(403).entity(	qs ).build();
		}
		
		if(!SearchIndexUIUtil.checkCookieInRequest(p.getProduct(), _request, null)) {			
			return Response.status(403).build();
		}
		
		IQueryDatabaseResult dbResult = CentralQueryManager.getInstance(p).getQueryDatabaseResult(id);
		if(dbResult == null) {
			return Response.status(404).build();			
		}
		
//		generateSearchTermUrl
		
		qs.setSearchTermsUrl(dbResult.getQuery().getSearchTermUrl());
		qs.setStatus(RestUtil.convertStatusToString(dbResult.getStatus()));
		qs.setNumResults(dbResult.getNumResultsAvailable());
		qs.setStartTime(dbResult.getStartTime());
		
		if(dbResult.getErrorText() != null) {
			qs.setErrorMessage(dbResult.getErrorText());
		}
		
		if(dbResult.getUserText() != null) {
			qs.setUserMessage(dbResult.getUserText());
		}
	
		return Response.ok(qs).build();
		
	}

	@GET
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	@Path("/{id}/result")
	public Response queryResult(@PathParam("id") int id, @QueryParam("from") int from, @QueryParam("to") int to) {

		System.out.println("\nQuery result request in:" + System.currentTimeMillis());
		
		ProductUI p = null;		
		if(_resourceId != null) {
			p = ProductSingleton.getInstance().getProduct(_resourceId);
		}
		if(p == null) {			
			return Response.status(403).entity(	("Invalid resource id:"+_resourceId)	).build();
		}
		
		if(!SearchIndexUIUtil.checkCookieInRequest(p.getProduct(), _request, null)) {			
			return Response.status(403).build();
		}

		ObjectFactory of = new ObjectFactory();		
		SearchResultListDTO result = of.createSearchResultListDTO();
		
		IQueryDatabaseResult dbResult = CentralQueryManager.getInstance(p).getQueryDatabaseResult(id);
		if(dbResult == null) {
			return Response.status(404).build();
		}
		
		if( to - from < 0 || to - from >= 201) {
			return Response.status(413).entity("Request must be <200 results").build();
		}
		
		try {
			List<QueryResultEntry> list = dbResult.getResults(from, to);
			
			for(QueryResultEntry qre : list) {
				ResultEntryDTO red = of.createResultEntryDTO();
				red.setContent(qre.getContent());
				red.setPath(qre.getPath());
				red.setFileUrl(qre.getFileUrl());
				result.getSearchResultListEntry().add(red);
			}

		} catch(IndexOutOfBoundsException ioobe) {
			return Response.status(416).build();
		}
		
		System.out.println("Query result request out:" + System.currentTimeMillis());
		
		return Response.ok(result).build();
		
	}

}
