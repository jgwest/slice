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

import java.io.File;
import java.util.List;

import com.slice.indexer.constants.IComponentPair;
import com.slice.indexer.constants.IConfigConstants;

/** Stream refers to the component under which a file is located */
public class StreamUtil {

	
	public static String getStreamFromPath(File f, IConfigConstants constants) {
		String fpath = f.getPath();
		fpath = fpath.replace("\\", "/");
		String[] parts = fpath.split("/");
		
		for(String s : parts) {
			
			for(IComponentPair pair : constants.getComponents()) {
				if(s.equalsIgnoreCase(pair.getPath())) {
					return pair.getName();
				}
			}
					
		}
		
		return null;
		
		
	}
	
	public static int compareStreamOrder(String s1, String s2, IConfigConstants constants) {
		List<IComponentPair> list = constants.getComponents();
		
		int s1Pos = -1;
		int s2Pos = -1;
		
		for(int x = 0; x < list.size(); x++) {
			if(list.get(x).getName().equalsIgnoreCase(s1)) {
				s1Pos = list.size() - x - 1; 
				break;
			}
		}
		
		for(int x = 0; x < list.size(); x++) {
			if(list.get(x).getName().equalsIgnoreCase(s2)) {
				s2Pos = list.size() - x - 1; 
				break;
			}
			
		}
		
		return s2Pos - s1Pos;
		
	}
	
}
