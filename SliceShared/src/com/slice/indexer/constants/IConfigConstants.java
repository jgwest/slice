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

package com.slice.indexer.constants;

import java.util.List;

/** Corresponds to values in the <Product></Product> entry in the XML configuration files. Each product will
 * have various values for each of these constants, which are configured in the XML file. */
public interface IConfigConstants {
	
	public String getProductId();
	
	public String getJarMemDatabaseFilePath(); 
	
	public String getJarFsDatabaseOutputPath(); 
	
	
	public String getJarPathToPluginsDir(); 

	public String getPathToSourceDir(); 

		
	public String getSearchComponentMapFile();
	
	public String getCookieName();
	
	public String getLogFileOutPath(); 
		

	public String getJarSearchWhoaCowboyFilesNumberText(); //  = "20,000";
	public int getJarSearchWhoaCowboyFilesNumber(); //  = 20000;
	
	public String getSearchWhoaCowboyFilesNumberText(); //  = "20,000";
	public int getSearchWhoaCowboyFilesNumber(); //  = 20000;
	
	public int getSearchWhoaCowboyQueryTimeoutInSecs(); //  = 120;
	
	public String getSearchAdminEmail();
	
	public String[] getJarSearchSupportedExtensions(); //  = { ".javapout.txt" };

	
	public List<IComponentPair> getComponents();
	
	public IProductMessages getProductMessages();
	
	public List<IAllowRule> getAllowRules();
	
	public ISearchIndex getSearchIndex();

	public List<String> getIndexerIgnoreList();
	
	public String getFileIndexerPath();
	
	public List<ISearchFileType> getSearchFileTypes();
	
	public String getDatabasePresharedKey();
}
