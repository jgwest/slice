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

package com.slice.searchindexdb.rs;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.slice.searchindexdb.NewQueryIdListDTO;
import com.slice.searchindexdb.ObjectFactory;
import com.slice.searchindexdb.QueryIDResultDTO;
import com.slice.searchindexdb.QueryIdStatusDTO;
import com.slice.searchindexdb.innerdb.DbQuerySingleton;

/** 
 * JAX-RS service uses to create DB queries, get current status, and read DB query results. 
 **/
@Path("/resources/{ProductId}/Query")
public class QueryService {
	
	@HeaderParam("db-preshared-key") String headerDbPresharedKey;
	
	@Context HttpServletResponse response;
	
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public long post(@PathParam("ProductId") String productId, NewQueryIdListDTO queryReq) {

		SearchIndexDBApplication.handlePskCheck(productId, headerDbPresharedKey, response);

		if(queryReq.getId() != null && queryReq.getId().size() > 0) {
		
			return DbQuerySingleton.getInstance().createQuery(queryReq, productId);
			
		}
		
		return -1;
	}
	
	@GET
	@Path("/{Id}/Status")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	public QueryIdStatusDTO getStatus(@PathParam("ProductId") String productId, @PathParam("Id") long queryId) {
		
		SearchIndexDBApplication.handlePskCheck(productId, headerDbPresharedKey, response);
		
		ObjectFactory of = new ObjectFactory();
		
		QueryIdStatusDTO result = DbQuerySingleton.getInstance().createStatusDTO(queryId, of);
		return result;
	}
	
	@GET
	@Path("/{Id}/Result")
	@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	public QueryIDResultDTO getResults(@PathParam("ProductId") String productId, @PathParam("Id") long queryId, @QueryParam("ids") String ids ) {

		SearchIndexDBApplication.handlePskCheck(productId, headerDbPresharedKey, response);
		
		ObjectFactory of = new ObjectFactory();
		
		List<Long> fileIds = null;
		
		if(ids != null && ids.trim().length() > 0) {
			fileIds = new LinkedList<Long>();
			
			String[] idStrArr = ids.split(Pattern.quote(","));
			for(String str : idStrArr) {
				if(str.trim().length() == 0) { continue; }
				
				long l = Long.parseLong(str.trim());
				fileIds.add(l);
			}
			
		}
		
		return DbQuerySingleton.getInstance().getResult(queryId, fileIds, of);
		
	}
	
	@DELETE
	@Path("/{Id}")
	public Response delete(@PathParam("ProductId") String productId, @PathParam("Id") long queryId) {
		
		SearchIndexDBApplication.handlePskCheck(productId, headerDbPresharedKey, response);
		
		DbQuerySingleton.getInstance().deleteQuery(queryId);
		
		return Response.ok().build();
	}
}
