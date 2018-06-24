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

package com.slice.indexer.dice;

import java.io.File;
import java.util.Collections;
import java.util.Map;

import com.slice.indexer.shared.util.BaseConversionUtil;
import com.slice.indexer.shared.util.SearchIndexerUtil;

/** Used by ReadableFSDatabase. 
 * Not thread safe */
public class FSDatabasePathDecompressor {

	
	Map<Long, String> _databasePathDecompressor;
	
	public FSDatabasePathDecompressor(Map<Long, String> databasePathDecompressor) {
		_databasePathDecompressor = Collections.unmodifiableMap(databasePathDecompressor);
	}
	
		
	public String decompress(String path) {
//		String result = path;
		StringBuilder result = new StringBuilder();
		
		String[] components;
		if(SearchIndexerUtil.isWindows()) {
			components = path.split("\\\\");
		} else {
			components = path.split(File.separator);
		}
		
		
		for(String str : components) {
			long num = BaseConversionUtil.convertFromBase(str, 62);
				
			result.append(_databasePathDecompressor.get(num));
			
			result.append(File.separator);
			
		}
		
		
		String stringResult = result.toString();
		stringResult = stringResult.substring(0, stringResult.length()-1); // Remove trailing slash
		
		
//		while(result.contains("<")) {
//			int start = result.indexOf("<");
//			int end = result.indexOf(">", start);
//
//			if(end == -1) {
//				System.err.println("Error in decompress. This shouldn't happen.");
//				return null;
//			}
//
//			Long num = Long.parseLong(result.substring(start+1, end));
//			String replacementValue = _databasePathDecompressor.get(num);
//			
//			result = result.replace("<"+num+">", replacementValue);
//			
//		}
		
		return stringResult;
		
	}
	
	
}
