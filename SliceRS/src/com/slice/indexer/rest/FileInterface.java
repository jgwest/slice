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

import java.io.File;
import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.slice.datatypes.FileContentsDTO;
import com.slice.datatypes.ObjectFactory;
import com.slice.indexer.constants.IConfigConstants;
import com.slice.indexer.constants.ISearchFileType;
import com.slice.indexer.shared.util.JavaSrcSearchQuery;
import com.slice.indexer.shared.util.SearchIndexerUtil;
import com.slice.indexer.ui.DatabaseLock;
import com.slice.indexer.ui.ProductSingleton;
import com.slice.indexer.ui.ProductUI;
import com.slice.indexer.ui.SearchIndexUIUtil;
import com.slice.indexer.urlgen.GenerateInnerFileHtmlUtil;

/** Return the contents (HTML-formatted) of a particular file from the filesystem, optionally with 
 * search terms highlighted using a specific query. */
@Path("/resources/{resourceid}/file")
public class FileInterface {

	@PathParam("resourceid")
	String _resourceId;
	
	@Context
	HttpServletRequest _request;
	
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	@GET
	public Response getFileContents(@QueryParam("file") String file, @QueryParam("queryId") Integer queryId) {
		ObjectFactory of = new ObjectFactory();
		FileContentsDTO fc = of.createFileContentsDTO();		

		ProductUI p = null;
		
		if(_resourceId != null) {
			p = ProductSingleton.getInstance().getProduct(_resourceId);
		}

		if(p == null) {
			fc.setErrorText("Invalid resource: "+_resourceId);			
			return Response.status(403).entity(fc).build();
		}
		
		// Check if the proper cookie is set
		StringBuilder errorText = new StringBuilder();
		if(!SearchIndexUIUtil.checkCookieInRequest(p.getProduct(), _request, errorText)) {
			fc.setErrorText(errorText.toString());			
			return Response.status(403).entity(fc).build();
		}

		file = SearchIndexerUtil.sanitizePathTraversalChars(file);
		
		IConfigConstants constants = p.getProduct().getConstants();
		
		File fileToRead = new File(constants.getPathToSourceDir()+File.separator+file);


		boolean supportedFileExtension = false;
		
		for(ISearchFileType sfType : constants.getSearchFileTypes()) {		
			for(String s : sfType.getSupportedExtensionsList()) {
				if(fileToRead.getPath().toLowerCase().endsWith(s.toLowerCase())) {
					supportedFileExtension = true;
				}
			}
		}
		
		if(!supportedFileExtension) {
			fc.setErrorText("ERROR: File extension is not supported.");			
			return Response.status(403).entity(fc).build();
		}
		
		if(!SearchIndexerUtil.allowPathAccess(constants, fileToRead)) {
			fc.setErrorText("ERROR: Access denied.");
			return Response.status(403).entity(fc).build();
		}

		if(!fileToRead.exists()) {
			fc.setErrorText("ERROR: File does not exist: "+fileToRead.getPath().replace(constants.getPathToSourceDir(), ""));
			return Response.status(403).entity(fc).build();
		}
		
		DatabaseLock db = p.getDatabase();
		
		JavaSrcSearchQuery query = null;
		String componentOfPlugin = null;
		try {
			db.acquireLock();

			componentOfPlugin = SearchIndexUIUtil.getComponentOfPath(fileToRead, db);
			
			if(queryId != null) {
				IQueryDatabaseResult queryDbResult = CentralQueryManager.getInstance(p).getQueryDatabaseResult(queryId);
				if(queryDbResult != null) {
					query = queryDbResult.getQuery();
				}
			}
			
		} finally {
			db.releaseLock();
		}
		

		StringBuilder contents = new StringBuilder();
		try {

			contents.append(GenerateInnerFileHtmlUtil.generateInnerFileHTML(fileToRead, query, true));
			
		} catch (IOException e) {
			fc.setErrorText("ERROR: IO Exception thrown during file reading.");
			return Response.status(500).entity(fc).build();
		}

		// Construct the response
		fc.setFilename(fileToRead.getName());
		fc.setPath(file);
		fc.setContents(contents.toString());
		fc.setComponent(componentOfPlugin);
		
		
		return Response.ok(fc).build();
		
	}


}
