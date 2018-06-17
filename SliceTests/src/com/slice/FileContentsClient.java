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

package com.slice;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import com.slice.datatypes.FileContentsDTO;

/** Used to retrieve the HTML-formatted contents of a particular file from the filesystem, optionally with 
 * search terms highlighted using a specific query (by id). */
public class FileContentsClient {

	final String resourceUrl;
	
	final LoginInfo loginInfo; 

	
	public FileContentsClient(String resourceUrl, LoginInfo loginInfo) {
		this.resourceUrl = resourceUrl;
		this.loginInfo = loginInfo;
	}

	
	public FileContentsDTO invokeGetFileContents(String file, Integer queryId, String productId){
		Client client = RestClientUtil.generateClient();
		
		WebTarget target = client.target(resourceUrl + "/SliceRS/jaxrs/resources").path("{resourceid}").path("file");
		
		target = target.queryParam("file", file);
		
		if(queryId != null) {
			target = target.queryParam("queryId", queryId);
		}
		
		target = target.resolveTemplate("resourceid", productId);
		
		
		
		Invocation.Builder builder = target.request("application/xml", "application/json");
		loginInfo.addCookieToBuilder(builder);
		
		Response response = builder.get();
				
		return response.readEntity(FileContentsDTO.class);
	}
	

}
