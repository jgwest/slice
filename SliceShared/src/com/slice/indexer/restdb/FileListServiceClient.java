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

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

@Deprecated
public class FileListServiceClient {

	final String resourceUrl;

	public FileListServiceClient(String resourceUrl) {
		this.resourceUrl = resourceUrl;
	}

//	public FileIdListTypeDTO getFileList(String productId, List<Long> keyIds, long failAfterGivenTime) {
//		Client client = RestDbClientUtil.generateClient();
//		
//		WebTarget target = client.target(resourceUrl + "/resources").path("{ProductId}").path("FileList");
//		target = target.queryParam("ids", keyIds).queryParam("failAfterGivenTime", failAfterGivenTime);
//
//		target = target.resolveTemplate("ProductId", productId);
//
//		Invocation.Builder builder = target.request("application/json");
//		Response response = builder.get();
//		return response.readEntity(FileIdListTypeDTO.class);
//	}

	public long getLastIdListUpdateInMsecs(String productId) {
		Client client = RestDbClientUtil.generateClient();
		
		WebTarget target = client.target(resourceUrl + "/resources").path("{ProductId}").path("FileList")
				.path("LastUpdateInMsecs");
		target = target.resolveTemplate("ProductId", productId);

		Invocation.Builder builder = target.request("application/json");
		Response response = builder.get();
		return response.readEntity(long.class);
	}

}
