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

package com.slice.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.zip.ZipFile;

/** Static utility class that reads the filename-index.idx file, locates paths in the file that 
 * match the given keywords, and returns those paths. */
public class FileIndexSearchNew {

	public static SearchIndexFileReturn searchIndexFile(File indexFile, String[] keywords, boolean ignoreCase) throws IOException {

		List<String> keywordList = new ArrayList<String>();
		for(int x = 0; x < keywords.length; x++) {
			String str = keywords[x];
			if(ignoreCase) { str = str.toLowerCase(); }
			keywordList.add(  str);
		}
		
		// Sort the keywords such that the longest keyword is first in the list
		Collections.sort(keywordList, new Comparator<String>() {

			@Override
			public int compare(String o1, String o2) {
				return o2.length() - o1.length();
			}
			
		});
		
		String[] keywordArr = keywordList.toArray(new String[keywordList.size()]);
		
		
		SearchIndexFileReturn result = new SearchIndexFileReturn();
		
		
		ZipFile zf = new ZipFile(indexFile);
		InputStream is = zf.getInputStream(zf.getEntry("filename-index.idx"));
		
		InputStreamReader fr = new InputStreamReader(is);

		BufferedReader br = new BufferedReader(fr);
		String str;
		
		result._fileRoot = br.readLine();
		result._result = new ArrayList<String>();
		
		while(null != (str = br.readLine())) {
			
			String origStr = str;
			
			if(ignoreCase) {
				str = str.toLowerCase();
			}
			
			boolean match = true;
			
			for(String k : keywordArr) {
				if(!str.contains(k)) {
					match = false;
					break;
				}
				
			}
			
			if(match) {
				result._result.add(origStr);
			}
			
		}
		
		fr.close();
		zf.close();
		
		return result;
		
	}

	
	/** Return value for searchIndexFile(...) method */
	public static class SearchIndexFileReturn {
		// root of fs, which is the first line in the filename-index.idx file
		String _fileRoot;
		
		// paths in the index file that match the search query
		List<String> _result;
	}

}

