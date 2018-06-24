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

package com.slice.indexer.generation;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import com.slice.indexer.constants.IConfigConstants;
import com.slice.indexer.shared.util.SearchIndexerUtil;

/** 
 * This class recursively scans a directory structure by path, breaking it up as follow:
 *  /home/user/mypath/dir/thing
 *  -> [ "home", "user", "mypath", "dir", thing"]
 *  with each of the above being path tokens.
 *  
 *  In occursMap is keeps track of how many times each path token occurs in the total structure (for example,
 *  home would be present in every path, if it is the root of the search.)
 * 
 * The return of this class is a map containing an id for each path token. The path tokens with the lowest
 * ids are the ones that would benefit most from compression (either they occur often, or are very long, usually both) 
 * 
 * */
public class FSDatabasePathCompressorPrefetch {
	/** Used by FSDatabase. */
	public static enum SupportedFiles{ JAR_SEARCH, JAVA_SRC_SEARCH };
	
	public static Map<String /* path token */, Long /*id*/> generatePrefetchMap(File fin, SupportedFiles type, IConfigConstants constants) {
		Queue<File> q = new LinkedList<File>();
		
		Map<String /* path token */, Long /* number of paths the path token occurs in*/ > occursMap = new HashMap<String, Long>();
		
		q.add(fin);
		
		long x = 0;
		
		while(!q.isEmpty()) {
			File dir = q.remove();
			
			File[] dirFileList = dir.listFiles();
			
			if(dirFileList == null) { continue; }
			
			for(File f : dirFileList) {
				
				if(f.isDirectory()) {
					q.add(f);
				} else {
					
					if(type == SupportedFiles.JAR_SEARCH) {
						if(!SearchIndexerUtil.isJarSearchSupportedFile(f.getPath().toLowerCase(), constants.getJarSearchSupportedExtensions() )) {
							continue;
						}
						
					} else if(type == SupportedFiles.JAVA_SRC_SEARCH) {
						if(!SearchIndexerUtil.isSearchSupportedFile(f.getPath().toLowerCase(), constants.getSearchFileTypes() )) {
							continue;
						}
						
					} else {
						return null;
					}
					
					String[] sarg;
					if(SearchIndexerUtil.isWindows()) {
						sarg = f.getPath().split("\\\\");
					} else {
						sarg = f.getPath().split(File.separator);
					}
					
					for(String str : sarg ) {
						Long occurs = occursMap.get(str);
						if(occurs == null) {
							occurs = 0l;
						} 
						occurs++;
						occursMap.put(str, occurs);
					}
					
					x++;
					
					if(x % 10000 == 0) {
						System.out.println(x);
					}
					
				}
			}
			
			
		}
		
		System.out.println();
		
		List<FSPrefetchEntry> r = new ArrayList<FSPrefetchEntry>();
		
		for(Map.Entry<String /* path token*/, Long /* occurrences*/> e : occursMap.entrySet()) {
			FSPrefetchEntry re = new FSPrefetchEntry();
			re._name = e.getKey();
			re._occurrences = e.getValue();
			re.calc();
			r.add(re);
		}
		
		// Sort by descending(?) by length * # of occurrences 
		Collections.sort(r);
		
		Map<String /* path token*/, Long /*id*/> prefetchMap = new HashMap<String, Long>();
		
		long id = 0;
		for(FSPrefetchEntry e : r) {
			prefetchMap.put(e._name, id);
			id++;
		}
		
		return prefetchMap;
	}
	

	/** This class is used to sort path tokens by the number of times they occur, and then calculating how
	 * expensive they are to store (and thus would benefit most from compression) */
	private static class FSPrefetchEntry implements Comparable<FSPrefetchEntry> {
		String _name;
		long _occurrences;
		
		/** How expensive the path token is (how many times it occurs * length), and thus how 
		 * beneficial compression would be. */
		long _calc;
		
		public void calc() {
			_calc = _name.length() * _occurrences;		
		}
		

		@Override
		public int compareTo(FSPrefetchEntry o) {
			return (int)(o._calc - _calc);
		}
		
		@Override
		public boolean equals(Object o) {
			return (this == o);
		}
	}
	
}

