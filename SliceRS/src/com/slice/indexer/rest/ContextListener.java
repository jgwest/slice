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

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.slice.indexer.shared.util.SearchIndexerUtil;
import com.slice.indexer.ui.ProductSingleton;

/** When the application is loaded by the application server, this class will be called, and this 
 * call will automatically initialize the product singleton. */
public class ContextListener implements ServletContextListener {

	@Override
	public void contextInitialized(ServletContextEvent arg0) {
		
		// In debug mode, wait for the first HTTP request to load the servlet.
		if(SearchIndexerUtil.isDebugTestModeEnabled()) { return; }
		
		// Load the singleton instance on servlet start;
		ProductSingleton.getInstance();
	}
	
	@Override
	public void contextDestroyed(ServletContextEvent arg0) {
		
		ProductSingleton.getInstance().unload();
	}


}
