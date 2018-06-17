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

package com.slice.service;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.slice.filenameindexerservice.FileSearchResult;
import com.slice.filenameindexerservice.FileSearchResultList;
import com.slice.filenameindexerservice.ObjectFactory;
import com.slice.indexer.constants.IConfigConstants;
import com.slice.indexer.constants.ISearchFileType;
import com.slice.indexer.shared.util.SearchIndexerUtil;
import com.slice.indexer.ui.ProductSingleton;
import com.slice.indexer.ui.ProductUI;
import com.slice.indexer.ui.SearchIndexUIUtil;
import com.slice.service.FileIndexSearchNew.SearchIndexFileReturn;

/** JAX-RS class for file indexer service */
@Path(value = "/file/{resourceid}/")
public class FileInterface {

	@PathParam("resourceid")
	String _resourceId;

	@Context
	HttpServletRequest servletRequest;


	private static boolean isFileSupported(String path, IConfigConstants constants) {
		boolean matchFound = false;
		
		if(!SearchIndexerUtil.allowPathAccess(constants, new File(path))) {
			return false;
		}
		
		for(ISearchFileType sfType :constants.getSearchFileTypes()) {
			
			for(String s : sfType.getSupportedExtensionsList()) {
				if(path.toLowerCase().endsWith(s)) {
					matchFound = true;
					break;
				}
			}
		}
		
		
		return matchFound;
	}
	
	
	@GET
	@Produces({"application/xml","application/json"})
	public Response get(@QueryParam("name") String query, @Context final HttpServletResponse response) {

		ProductUI p = null;
		if(_resourceId != null) {
			p = ProductSingleton.getInstance().getProduct(_resourceId);
		} else {
			return Response.status(400).type("text/plain").entity("resourceId not specified.").build();
		}

	
		if(!SearchIndexUIUtil.isCookieValid(p.getProduct(), servletRequest)) {
			return Response.status(401).type("text/plain").entity("Authorization cookie is not set.").build();
		}
		
		
		String[] searchTerms = query.split(Pattern.quote(" "));

		System.out.println();
		System.out.print("search terms: ");
		for(String str : searchTerms) {
			System.out.print("["+str+"] ");
		}
		System.out.println();
		
		
		ObjectFactory of = new ObjectFactory();

		FileSearchResultList resultList = of.createFileSearchResultList();
		
		final String VIEW_URL = "/SliceRS/fileview.jsp?file=";
		
		List<String> paths;
		try {
			
			if(p.getProduct().getConstants().getFileIndexerPath() == null) {
				// Need to add the file indexer path to the database
				return Response.status(Status.NO_CONTENT).type("text/plain").entity("File indexer path is not set in config file.").build();
			}
			
			SearchIndexFileReturn ret = FileIndexSearchNew.searchIndexFile(new File(p.getProduct().getConstants().getFileIndexerPath(), "filename-path-index.zip.idx"), searchTerms, true);
			
			paths =  ret._result;
			
			if(paths == null) {
				// Null is returned if the query was too large.
				return Response.status(413).entity(resultList).build();
			}
			
			Collections.sort(paths);
			
			for(String str : paths) {
								
				// Skip unsupported file extensions
				if(!isFileSupported(ret._fileRoot+File.separator+str, p.getProduct().getConstants())) {
					continue;
				}
				
				String rootlessStr = str.replace(ret._fileRoot, "");
				
				FileSearchResult entry = of.createFileSearchResult();
				entry.setPath(rootlessStr);
				entry.setUrl(VIEW_URL+rootlessStr+"&resourceId="+p.getProduct().getProductId());
				
				resultList.getResultList().add(entry);
			}
			
			System.out.println("result size:"+resultList.getResultList().size());
			
		} catch (Exception e) {
			e.printStackTrace();
			return Response.status(400).build();
		}

		return Response.ok(resultList).build();
		
	}
	

}

