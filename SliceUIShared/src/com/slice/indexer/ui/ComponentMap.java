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

package com.slice.indexer.ui;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Parse a component file. A component file maps individual project/plug-ins to a overarching 'parent' component. 
 * 
 * The format of the file is:
 * (child plugin/project) -> (parent component)
 * 
 */
public class ComponentMap {

	Map<String, String> _componentMap = new HashMap<String, String>(); 
	
	File _componentFile;
	
	public ComponentMap(File componentFile) throws IOException {
		_componentFile = componentFile;
		loadFile();
	}
	
	private void loadFile() throws IOException {
		
		BufferedReader br = new BufferedReader(new FileReader(_componentFile));
		try {
			String str;
			while(null != (str = br.readLine())	 ) {
				
				int c = str.indexOf("->");
				
				if(c != -1) {
					String plugin = str.substring(0, c).trim().toLowerCase();
					String component = str.substring(c+2).trim();
					
					if(!plugin.equalsIgnoreCase("Plugin") && !component.equalsIgnoreCase("Component")) {
						_componentMap.put(plugin, component);
					}
					
				}
				
			}
		} finally {
			br.close();
		}
		
		
	}
	
	public String getComponent(String plugin) {
		return _componentMap.get(plugin.toLowerCase());
	}
	
}
