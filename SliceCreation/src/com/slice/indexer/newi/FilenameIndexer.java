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

package com.slice.indexer.newi;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Stack;
import java.util.regex.Pattern;

/**
 * This class is deprecated, see FilenameIndexerNew.
 *  
 * The FilenameIndexer allows the user to quickly find a file whose path matches a given substring. For example:
 * 
 * c:\dir\my-stuff\my-file.txt
 * 
 * could be found using:
 * "my-stuff"
 * "stuff"
 * "my-file"
 * "my-stuff" "my-file"
 * "stuff" "file"
 * 
 * The purpose of this class is to create the index file that speeds up the above search process. We do this
 * by recursively iterating over a target directory, compressing the individual path components, and writing the 
 * full file/directory listing to a file.
 * 
 * A 'path component' is this:
 * - for "c:\dir\my-stuff\my-file.txt"
 * - the path components would be: "c", "dir", "my-stuff", "my-file.txt"
 *
 */
@Deprecated
public class FilenameIndexer {
	
	public static void createIndex(File directoryToIndex, File resultIndexFile) {
		IndexResult ir = indexDirectory(directoryToIndex);

		try {
			
			writeIndexFile(ir, resultIndexFile);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	private static void writeIndexFile(IndexResult result, File f) throws IOException {
		FileWriter fw = new FileWriter(f);
		
		fw.write("index root:\n");
		fw.write(result.indexRoot+"\n");
		fw.write("ids:\n");
		
		for(String id : result.compressedIdToKey) {
			
			int numMatches = result.keyToPathMap.get(id).size();
			
			fw.write(id+"|"+numMatches+"\n");
		}
		fw.write("paths:\n");
		
		
		Set<Entry<String, List<String>>> s = result.keyToPathMap.entrySet();
		
		for(Entry<String, List<String>> e : s) {
			
			for(String value : e.getValue()) {
				
				fw.write(e.getKey()+"|"+value+"\n");				
			}
			
		}
		
		fw.close();
		
	}
	

	private static List<String> splitPath(String path) {
		List<String> result = new LinkedList<String>();
		
		String[] pathList = path.split(Pattern.quote(File.separator));
	
		for(String str : pathList) {
			if(str.length()>=4) {
				result.add(str);
			}
			
		}
		
		return result;
		
	}
	

	private static IndexResult indexDirectory(File start) {
		
		IndexState state = new IndexState();
		
		IndexResult result = new IndexResult();
		
		result.indexRoot = start.getPath();
		
		Stack<File> stack = new Stack<File>();		
		stack.push(start);
		
		
		while(stack.size() > 0) {
			File f = stack.pop();
			
			if(f.isDirectory()) {
				
				for(File fe : f.listFiles()) {
					stack.push(fe);
				}
				
			} else {
				
				String fpath = f.getPath().replace(start.getPath(), "");
				
				// If a path happens to contain this character, ignore it; we use this character for special bsns
				if(fpath.contains("|")) { continue; }
				
				// See definition of path components in class
				List<String> pathComponents = splitPath(fpath);
				
				for(String str : pathComponents) {

					List<String> list = result.keyToPathMap.get(str);
					if(list == null) {
						list = new ArrayList<String>();
					}
					
					String path = convertPathToString(state, result, fpath) ;
					list.add(path);
					
					result.keyToPathMap.put(str, list);
					
				}
				
			}
			
		}
		
		return result;
	}

	
	private static String convertPathToString(IndexState state, IndexResult ir, String newPath) {
		String result = "";
		
		Map<String, Integer> pathToId = ir.keyToCompressedId;
		
		
		String[] pathComponents = newPath.split(Pattern.quote(File.separator));
		
		int pos = 0;
		for(String pc : pathComponents) {
			
			String addition = "";
			if(pos > 0) {
				addition += File.separator;				
			}
			
			if(pc.length() <= 3) {
				addition += pc;
				
			} else {
				
				Integer i = pathToId.get(pc);
				if(i == null) {
					i = state._nextId;
					state._nextId++;
					pathToId.put(pc, i);
					ir.compressedIdToKey.add(pc);
				}
				
				addition += i;
			}
			
			result += addition;
			pos++;
		}
		
		return result;
	}

	/** The path component id starts 0 and increases by 1. All unique. */
	private static class IndexState {
		int _nextId = 0;
	}

	/** The result of the indexDirectory(...) call; this is then written to file by writeIndexFile(...). */
	private static class IndexResult {
		
		// read
		String indexRoot;
		
		// id -> key, where id is the position in the list:
		// o key = compressedIdToKey.get(id);
		ArrayList<String> compressedIdToKey = new ArrayList<String>();
		
		Map<String /* path component */, List<String> /* compressed list of file paths that contain that component*/ > keyToPathMap = new HashMap<String, List<String>>();
		
		// write
		Map<String /* path component*/, Integer /* id, increments from 0 */> keyToCompressedId = new HashMap<String, Integer>(10, 0.90f); 
		
	}

}


