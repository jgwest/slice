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

package com.slice.indexer.restdb;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.slice.indexer.constants.IConfigConstants;
import com.slice.searchindexdb.IdListTypeDTO;

/** Query the database service to retrieve the list of IDs, using the JAX-RS Client. */
public class KeyListServiceClient {

	final String resourceUrl;

	public KeyListServiceClient(String resourceUrl) {
		this.resourceUrl = resourceUrl;
	}

	
	private String readResponse(Response response)  {
		InputStream is = (InputStream)response.getEntity();
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		
		StringBuilder sb = new StringBuilder();
		String str;
		try {
			while(null != (str = br.readLine())) {
				sb.append(str);
			}
			
			return sb.toString();
		} catch(IOException ioe) {
			ioe.printStackTrace();
			return null;
		}
		
	}
	
	public long getIdListSize(IConfigConstants product) {
		Client client = RestDbClientUtil.generateClient();
		
		WebTarget target = client.target(resourceUrl + "/resources").path("{ProductId}").path("IdList").path("Size");
		target = target.resolveTemplate("ProductId", product.getProductId());

		Invocation.Builder builder = target.request("application/json");
		RestDbClientUtil.addPskHeader(builder, product);
		Response response = builder.get();
		
		return Long.parseLong(readResponse(response));
		
	}

	public IdListTypeDTO getIdList(IConfigConstants product, Long start, Long end) {
		Client client = RestDbClientUtil.generateClient();
		
		WebTarget target = client.target(resourceUrl + "/resources").path("{ProductId}").path("IdList");
		target = target.queryParam("start", start).queryParam("end", end);

		target = target.resolveTemplate("ProductId", product.getProductId());
		
		Invocation.Builder builder = target.request(MediaType.APPLICATION_XML);

		RestDbClientUtil.addPskHeader(builder, product);
			
		Response response = builder.get();
		
		return response.readEntity(IdListTypeDTO.class);
	}

}
