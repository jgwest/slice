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

import java.util.concurrent.TimeUnit;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import com.slice.datatypes.ObjectFactory;
import com.slice.datatypes.QueryDTO;
import com.slice.datatypes.QueryStatusDTO;
import com.slice.datatypes.ResourceCreateResponseDTO;
import com.slice.datatypes.SearchResultListDTO;
import com.slice.models.ModelConstants.QDRStatus;

/** JAX-RS client to invoke the Query service */
public class QueryInterfaceClient {

	final LoginInfo loginInfo;

	final String resourceUrl;

	final ObjectFactory objectFactory = new ObjectFactory();
	
	public QueryInterfaceClient(String resourceUrl, final LoginInfo loginInfo) {
		this.resourceUrl = resourceUrl;
		this.loginInfo = loginInfo;
	}

	public QueryDTO invokeGetQueryContent(int id, String resourceid) {
		Client client = RestClientUtil.generateClient();
		WebTarget target = client.target(resourceUrl + "/SliceRS/jaxrs/resources").path("{resourceid}")
				.path("query").path("{id}");
		
		target = target.resolveTemplate("id", id);
		target = target.resolveTemplate("resourceid", resourceid);
		
		Invocation.Builder builder = target.request("application/xml", "application/json");
		loginInfo.addCookieToBuilder(builder);
		Response response = builder.get();
		
		return response.readEntity(QueryDTO.class);
	}

	public ResourceCreateResponseDTO invokeNewQuery(QueryDTO q, String resourceid) {
		Client client = RestClientUtil.generateClient();
		WebTarget target = client.target(resourceUrl + "/SliceRS/jaxrs/resources").path("{resourceid}")
				.path("query");
		
		target = target.resolveTemplate("resourceid", resourceid);
		
		Invocation.Builder builder = target.request("application/xml", "application/json");
		loginInfo.addCookieToBuilder(builder);
		
		Response response = builder.post(Entity.entity(q, "application/xml"));
		// Optionally, select an alternative media type for the preceding POST request.
		// Response response = builder.post(Entity.entity(q, "application/json"));

		return response.readEntity(ResourceCreateResponseDTO.class);
	}

	public SearchResultListDTO invokeQueryResult(long queryId, int from, int to, String resourceid) {
		Client client = RestClientUtil.generateClient();
		WebTarget target = client.target(resourceUrl + "/SliceRS/jaxrs/resources").path("{resourceid}")
				.path("query").path("{id}").path("result");
		
		target = target.queryParam("from", from).queryParam("to", to);
		target = target.resolveTemplate("resourceid", resourceid);
		target = target.resolveTemplate("id", queryId);

		Invocation.Builder builder = target.request("application/xml", "application/json");
		loginInfo.addCookieToBuilder(builder);
		
		Response response = builder.get();
		
		return response.readEntity(SearchResultListDTO.class);
	}

	public QueryStatusDTO invokeQueryStatus(long queryId, String resourceid) {
		Client client = RestClientUtil.generateClient();
		WebTarget target = client.target(resourceUrl + "/SliceRS/jaxrs/resources").path("{resourceid}")
				.path("query").path("{id}").path("status");
		
		target = target.resolveTemplate("id", queryId);
		target = target.resolveTemplate("resourceid", resourceid);
		
		Invocation.Builder builder = target.request("application/xml", "application/json");
		loginInfo.addCookieToBuilder(builder);
		
		Response response = builder.get();
		return response.readEntity(QueryStatusDTO.class);
	}

	
	public ObjectFactory getObjectFactory() {
		return objectFactory;
	}
	
	public static QueryStatusDTO waitForQueryComplete(long queryId, QueryInterfaceClient client, String productId) {
		long expireTime = System.nanoTime() + TimeUnit.NANOSECONDS.convert(240, TimeUnit.SECONDS);
		
		QueryStatusDTO result = null;

		while(result == null && System.nanoTime() < expireTime) {

			QueryStatusDTO queryStatus = client.invokeQueryStatus(queryId, productId);
			
			String statusName = queryStatus.getStatus().toLowerCase();
			
			if(statusName.equalsIgnoreCase(QDRStatus.COMPLETE_SUCCESS.name()) || statusName.equalsIgnoreCase(QDRStatus.COMPLETE_ERROR.name())) {
				result = queryStatus;
			}
				
			if(result == null) {
				try {
					TimeUnit.MILLISECONDS.sleep(250);
				} catch (InterruptedException e) {
					throw new RuntimeException(e);
				}
			}
		}
		
		return result;
	}
}
