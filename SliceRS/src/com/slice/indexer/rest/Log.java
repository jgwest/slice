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

import java.io.FileWriter;

import com.slice.datatypes.QueryDTO;
import com.slice.models.ModelUtils;

/** Simple log utility */
public class Log {

	private static final Log instance = new Log();
	
	private Log() {}
	
	public static Log getInstance() {
		return instance;
	}
	
	// ---------------------------------------------------
	
	private final Object lock = new Object();
	
	private FileWriter fw;
	
	private static String obfuscateRemoteAddr(String remoteAddr) {
		return ""+remoteAddr.trim().toLowerCase().hashCode();
	}
	
	private boolean initIfNeeded() {
//		synchronized(lock) {
//			if(fw == null) {
//				List<ProductUI> products = ProductSingleton.getInstance().getProductList();
//				if(products.size() > 0) {
//					
//					String pathToLogFileStr = products.get(0).getProduct().getConstants().getLogFileOutPath(); // .getConfigEntries().getPathToLogFile();
//					
//					if(pathToLogFileStr != null && pathToLogFileStr.trim().length() > 0) {
//						File pathToLogFile = new File(pathToLogFileStr);
//						try {
//							fw = new FileWriter(pathToLogFile, true);
//							return true;
//						} catch (IOException e) {
//							e.printStackTrace();
//						}						
//					}
//				} 
//				
//				return false;
//			}
//
//			return true;
//		}

		return true;
	}
	
	public void logNewQuery(QueryDTO param, String remoteAddr)  {
		if(!initIfNeeded()) { return; }
		
		try {
			String query = ModelUtils.debugMarshallQueryDTO(param, false);
			query = query.replace("\r", "");
			query = query.replace("\n", "||");

			System.out.println(System.currentTimeMillis()+") host: "+obfuscateRemoteAddr(remoteAddr)+"  query: "+query+"\n");
//			fw.write(System.currentTimeMillis()+") host: "+obfuscateRemoteAddr(remoteAddr)+"  query: "+query+"\n");
//			fw.flush();
		} catch(Throwable t) {
			t.printStackTrace();
			// First, do no harm.
		}
		
	}
	
}
