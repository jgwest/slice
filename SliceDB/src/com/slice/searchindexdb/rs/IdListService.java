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

import java.util.List;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import com.slice.indexer.shared.IReadableDatabase2;
import com.slice.searchindexdb.IdListTypeDTO;
import com.slice.searchindexdb.ObjectFactory;
import com.slice.searchindexdb.innerdb.DbDatabaseLock;
import com.slice.searchindexdb.innerdb.DbProductSingleton;
import com.slice.searchindexdb.innerdb.DbProductUI;

/**
 * JAX-RS service that provides some or all of the ID list for a particular product, as well as the size of the ID list. 
 */
@Path("/resources/{ProductId}/IdList")
public class IdListService {

	@HeaderParam("db-preshared-key") String headerDbPresharedKey;
	
	@Context HttpServletResponse response;
	
	/** end is inclusive */
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	public IdListTypeDTO get(@PathParam("ProductId") String productId, @QueryParam("start") Long start, @QueryParam("end") Long end) {

		SearchIndexDBApplication.handlePskCheck(productId, headerDbPresharedKey, response);
		
		DbProductSingleton ps = DbProductSingleton.getInstance();
		
		DbProductUI product = ps.getProduct(productId);
		
		DbDatabaseLock dl = product.getDatabase();

		ObjectFactory of = new ObjectFactory();
		
		IdListTypeDTO dto = of.createIdListTypeDTO();

		dl.acquireLock();
		try {
		
			IReadableDatabase2 readableDb = dl.getDbInst();

			List<String> idList = readableDb.getIdList();
			
			long currStart = 0;
			long currEnd = idList.size(); 
			
			if(start != null && start > 0) {
				currStart = start;
			}
			
			if(end != null && end >0) {
				currEnd = end;
			}
			
			for(long x = currStart; x <= currEnd && x >= 0 && x < idList.size(); x++) {
				dto.getValue().add( idList.get((int)x) );
			}
			
		} finally {
			dl.releaseLock();
		}
		
		return dto;
		
	}
	
	@GET
	@Path("/Size")
	@Produces(MediaType.APPLICATION_JSON)
	public long get(@PathParam("ProductId") String productId) {
		
		SearchIndexDBApplication.handlePskCheck(productId, headerDbPresharedKey, response);
		
		DbProductSingleton ps = DbProductSingleton.getInstance();
		
		DbProductUI product = ps.getProduct(productId);

		DbDatabaseLock dl = product.getDatabase();
		try {
			
			dl.acquireLock();
			
			IReadableDatabase2 readableDb = dl.getDbInst();
			return readableDb.getIdList().size();
			
		} finally {
			dl.releaseLock();
		}
	}
	
	
}
