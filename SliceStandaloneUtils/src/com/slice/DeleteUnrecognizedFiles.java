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

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;

import com.slice.indexer.constants.IConfigConstants;
import com.slice.indexer.constants.ISearchFileType;
import com.slice.indexer.shared.Product;
import com.slice.indexer.shared.util.SearchIndexerUtil;

/** 
 * Delete any files in a product's directory that will not be indexed by the indexer. 
 * 
 *  This utility requires that 'CLEANSE' appear in the directory path, in order to prevent accidental 
 *  invocation on/deletion of unrelated directories. 
 *  
 *  An example use of this utility: imagine you have a large directory of projects 
 *  containing both .java files (which will be indexed) and .jar files (which will not be indexed). 
 *  Since the indexer only cares about the .java files, you may run this utility on that directory. This utility will delete all
 *  files that will not be used by the index (the .jar files), leaving only the .java files.
 *  
 *  This can be helpful for reducing the size of large source repositories, by removing all but what is required for indexing.
 *  
 **/
public class DeleteUnrecognizedFiles {

	public static final String SL = File.separator;
	
	public static void main(String[] args) {
	
		if(args.length != 1) {
			System.out.println("first argument is path to configuration XML file.");
			return;
		}
		
		InputStream xsdIs = SearchIndexerUtil.class.getResourceAsStream("/META-INF/FileConfiguration.xsd");
		
		File pathToProductConfigXML = new File(args[0]);
		try {
			List<Product> products = SearchIndexerUtil.readProductConfigFile(new FileInputStream(pathToProductConfigXML), xsdIs);
			for(Product p : products) {
				IConfigConstants constants = p.getConstants();
				
				String javaSrcPathToSource = constants.getPathToSourceDir();
				
				if(javaSrcPathToSource != null && javaSrcPathToSource.contains("CLEANSE")) {
					processDirectory(new File(javaSrcPathToSource), constants);
				}
				
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		
	}
	
	
	private static void processDirectory(File dir, IConfigConstants p) {
		String path = dir.getPath();
		
		if(	!(path.contains(SL+"CLEANSE"+SL) || path.endsWith(SL+"CLEANSE")) ) {
			return;
		}

		if(path.contains(SL+SL) || path.contains("..")) {
			return;
		}
		
		for(File f : dir.listFiles()) {
			if(f.isDirectory()) {
				processDirectory(f, p);
				
			} else {
				processFile(f, p);
			}
		}
		

		
	}
	
	private static void processFile(File f, IConfigConstants p) {
		String path = f.getPath();
		
		if(!path.contains(SL+"CLEANSE"+SL)) {
			return;
		}
		
		if(path.contains(SL+SL) || path.contains("..")) {
			return;
		}

		boolean match = false;
		for(ISearchFileType sfType : p.getSearchFileTypes()) {
			for(String str : sfType.getSupportedExtensionsList()) {
				if(path.toLowerCase().endsWith(str)) {
					match = true;
					break;
				}
			}
			
		}
		
		if(!match) {
			f.delete();
			System.out.println("delete: "+f.getPath());
		}
		
	}
}
