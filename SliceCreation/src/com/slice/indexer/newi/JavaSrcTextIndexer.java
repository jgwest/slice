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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.slice.indexer.constants.IConfigConstants;
import com.slice.indexer.shared.IWritableDatabase2;
import com.slice.indexer.shared.util.SearchIndexerUtil;


/** Generate a unique list of valid white-space separated tokens contained within a given file, and write 
 * them to the database. */
public class JavaSrcTextIndexer {
	
	private static final boolean DEBUG = false; 
	
	private static Writer DEBUG_WRITER = null;
	
	private static long _filesRead = 0;
	
	public static void indexJavaSrc(IConfigConstants constants, IWritableDatabase2 dbInst) {
		try {
			
			File directoryToRecurse = new File(constants.getPathToSourceDir());
		
			if(DEBUG) {
				DEBUG_WRITER = new FileWriter(new File("/tmp/standalone-javasrc-text-indexer.log"));
			}
			
			boolean ignoreCase = false;
			
			
			recurseDirectory(directoryToRecurse, ignoreCase, dbInst, constants);
			
			dbInst.writeDatabase();
			
			if(DEBUG_WRITER != null) {
				DEBUG_WRITER.close();
			}
			
		} catch(IOException e) {
			e.printStackTrace();
		}
		
	}
	
	
	private static void recurseDirectory(File dir, boolean ignoreCase, IWritableDatabase2 dbInst, IConfigConstants constants) throws IOException {
		
		for(File f : dir.listFiles()) {
			if(f.isDirectory()) {
				recurseDirectory(f, ignoreCase, dbInst, constants);
			} else if(f.isFile()) {
				recurseFile(f, ignoreCase, dbInst, constants);
			}
			
		}
		
	}


	private static void recurseFile(File file, boolean ignoreCase, IWritableDatabase2 dbInst, IConfigConstants constants) throws IOException {
		
		String fpath = file.getPath().toLowerCase();
		
		List<String> indexerIgnoreList = constants.getIndexerIgnoreList();
		if(indexerIgnoreList != null) {
			// Don't index files that we have told the indexer to ignore
			for(String str : indexerIgnoreList) {
				if(fpath.contains(str)) {
					return;
				}
			}
			
		}
				
		if(SearchIndexerUtil.isSearchSupportedFile(fpath, constants.getSearchFileTypes())) {
			
			if(DEBUG) { DEBUG_WRITER.write(fpath+":\n"); }
			
			_filesRead++;
			
			if(_filesRead % 10000 == 0) {
				System.out.println("Files read:"+_filesRead);
			}
			
			
			try {
				FileReader fr = new FileReader(file);
				BufferedReader br = new BufferedReader(fr);
				
				List<String> fileContents = new ArrayList<String>();
				String str;
				while( (str = br.readLine()) != null) {
					fileContents.add(str);
				}
				br.close();
				fr.close();

		
				// A unique list of valid white-space separated tokens contained in the file 
				List<String> result = parseFile(fileContents, file);

				dbInst.addBulkLinks(result, ignoreCase, file, 0);
								
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
		
	}
	

	/** Convert the contents of a file into a unique list of tokens contained in that file.
	 * 
	 * Additional rules exist for whether a string is a valid token.
	 **/
	private static List<String> parseFile(List<String> fileContents, File file) {
		
		Map<String, Boolean> tokensFoundMap = new HashMap<String, Boolean>(512);
		List<String> tokensFound = new ArrayList<String>();
		
		for(String s : fileContents) {
			
			String str = s.trim();
			
			
			String currToken = "";
			for(int x = 0; x < str.length(); x++) {
				char ch = str.charAt(x);
				if(Character.isJavaIdentifierPart(ch) || ch == '.') {
					
					currToken += ch;
					
				} else {
					
					if(currToken.length() > 0) {
						
						if(!tokensFoundMap.containsKey(currToken) && SearchIndexerUtil.isValidTerm(currToken)) {
							tokensFound.add(currToken);
							tokensFoundMap.put(currToken, true);
						}
						
						currToken = "";
						
					}
				}
				
				
			}
			
			// Handle the last token still in currToken 
			if(currToken.length() > 0) {
				
				if(!tokensFoundMap.containsKey(currToken) && SearchIndexerUtil.isValidTerm(currToken)) {
					tokensFound.add(currToken);
					tokensFoundMap.put(currToken, true);
				}
				
				currToken = "";				
			}

			
		}
		
		return tokensFound;
		
	}
	
}
