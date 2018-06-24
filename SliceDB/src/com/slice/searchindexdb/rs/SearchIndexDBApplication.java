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

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import com.slice.searchindexdb.innerdb.DbProductSingleton;
import com.slice.searchindexdb.innerdb.DbProductUI;

/** JAX-RS application class for the IdListService and QueryService resources */
@ApplicationPath("/jaxrs")
public class SearchIndexDBApplication extends Application {

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Set<Class<?>> getClasses() {
		HashSet hs = new HashSet();
		
		hs.add(IdListService.class);
		hs.add(QueryService.class);
		
		return hs;
	}
	
	public static void handlePskCheck(String productId, String headerContents, HttpServletResponse response) {
		
		if(headerContents != null) {
			DbProductUI product = DbProductSingleton.getInstance().getProduct(productId);
			if(product != null) {
				String correctPsk = product.getProduct().getConstants().getDatabasePresharedKey();
				if(correctPsk == null || correctPsk.length() == 0 || correctPsk.trim().length() == 0) {
					try {
						response.setStatus(HttpServletResponse.SC_NON_AUTHORITATIVE_INFORMATION);
						response.getWriter().print("Missing PSK in config");
					} catch (IOException e) {
						/** Ignore, we are throwing an exception below */
					}
					throw new RuntimeException("Missing PSK in config");
				} else {
					
					if(!correctPsk.equals(headerContents)) {
						headerContents = null;
					} else {
						/* it is correct */
					}
					
				}
				
			} else {
				headerContents = null;
			}
			
		}
		
		// Do NOT join this if-clause with the above one
		if(headerContents == null) {
			response.setStatus(HttpServletResponse.SC_FORBIDDEN);
			try {
				response.getWriter().print("Missing or invalid PSK");
			} catch (IOException e) {
				/** Ignore, we are throwing an exception below */ 
			}
			
			throw new RuntimeException("Missing PSK");
		}
		
	}
}
