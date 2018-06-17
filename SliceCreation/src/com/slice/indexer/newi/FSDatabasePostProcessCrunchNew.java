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
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.slice.indexer.shared.util.BaseConversionUtil;

/** The purpose of this class is to take an exiting id-to-files-map.bigstore file created by WritableFSDatabaseNew, and
 * convert it into a much smaller on-disk file. To compress or "crunch" the data, the previous line of a file is used as a template for 
 * the current line of the file (as the succeeding line tends to be similar to the preceding line), 
 * which prevents needing to repeat some parts of the current line, thus reducing disk space
 *
 * A factor when writing this class was to reduce the maximum heap (-Xmx value) that is required for indexing large source code repositories (those with tens or hundreds of thousands of files):
 * - The bigstore file is split into 50 MB pieces, and each of these pieces is sorted separately, so as to not require loading the full file into memory at once
 * - DefferedStringFactory is used to create the strings, which will map large strings (>4 chars) to a temporary on-disk file. 
 *  
 *  */
public class FSDatabasePostProcessCrunchNew {
	
	/** Input is 'id-to-files-map.bigstore' file */
	public static void crunch(File originalFile) throws IOException {
		
		long startTime = System.currentTimeMillis();

		// Split into 50MB chunks
		int splitNum =  (int) (	originalFile.length() / (50*1024* 1024) 	);
		if(splitNum == 0) { splitNum = 1; } 
		
		File newFile = new File(originalFile.getPath()+".new");
		
		System.out.println("Using splitnum:"+splitNum);
		
		// Split into 'splitNum' separate files. id-to-files-map.bigstore.*, for each split, I think, and sort the text by alphabetical order 
		splitAndSortFile(originalFile, splitNum);
		
		// Write the split files, in order, to a new file 'newFile', id-to-files-map.new
		// Each split is sorted by alphabetical order 
		combineSortedFiles(originalFile, newFile, splitNum);
		
		// Crunch the new file
		File crunchedFile = new File(originalFile.getPath()+".crunched");
		innerCrunch(newFile, crunchedFile);
		
		newFile.delete();
		
		System.out.println("totaltime:"+(System.currentTimeMillis() - startTime));
		
	}
	
	private static void combineSortedFiles(File originalFile, File newFile, int sortSplitNum) throws IOException {
		FileWriter fw = new FileWriter(newFile);
		
		for(int x = 0; x < sortSplitNum; x++) {
			
			System.out.println("combining - x:"+x);
			
			File f = new File(originalFile.getPath()+"."+x);
			
			BufferedReader br = new BufferedReader(new FileReader(f));
			
			String str;
			while(null !=(str = br.readLine())) {
				fw.write(str+"\n");
			}
			
			br.close();
			
			f.delete();
			
		}
		
		fw.close();
	}
	
	/** 
	 * Take the original file and split it into (splitNum) separate files. Each new file will
	 * have the values (not ids) sorted. 
	 * 
	 * The new files will have the same name as the original file, except .(number) appended.
	 * 
	 **/
	private static void splitAndSortFile(File originalFile, int splitNum) throws IOException {

		for(int x = 0; x < splitNum; x++) {

			HashMap<Long /* id from originalFile*/, ArrayList<String /* text with that id*/>> idMap = new HashMap<Long, ArrayList<String>>();

			BufferedReader br = new BufferedReader(new FileReader(originalFile));

			System.out.println("sort - x:"+x);
			
			String str;
			while(null != (str = br.readLine())) {
			
				// line format: (id) - (text)
				
				int hyphenPos = str.indexOf("-");
				
				long id = Long.parseLong(str.substring(0, hyphenPos));
				
				if(id % splitNum == x) {

					String text = str.substring(hyphenPos+1);

					ArrayList<String> list = idMap.get(id);
					if(list == null) {
						
						list = new ArrayList<String>();
						idMap.put(id, list);
					}
					list.add(text);
					
				}
				
			}

			FileWriter fw = new FileWriter(new File(originalFile.getPath()+"."+x));

			for(Map.Entry<Long, ArrayList<String>> e : idMap.entrySet()) {
				
				long id = e.getKey();
				// Sort the strings in the list 
				Collections.sort(e.getValue());
				
				// Write the sorted strings back to new file
				for(String str2 : e.getValue()) {
					fw.write(id+"-"+str2+"\n");
				}
				
			}
			fw.close();
			br.close();
			
		}
		
	}
	

	/**
	 * This method assumes that 'path' file has been sorted by text, eg that each id has its text sorted in alphabetical
	 * order.
	 */
	private static void innerCrunch(File inputFile, File outputFile) throws IOException {

		File factoryPath = new File(inputFile.getPath() + ".deferred");

		DeferredStringFactory factory = new DeferredStringFactory(factoryPath);

		FileWriter fw = new FileWriter(outputFile);

		HashMap<Long /* id from file*/, IDeferredString> lastLineOfIdMap = new HashMap<Long, IDeferredString>();

		BufferedReader br = new BufferedReader(new FileReader(inputFile));
		try {
			String str;
			while (null != (str = br.readLine())) {

				// format: (id) - (text) 
				
				int hyphenPos = str.indexOf("-");

				long id = Long.parseLong(str.substring(0, hyphenPos));

				String text = str.substring(hyphenPos + 1);

				IDeferredString lastLineForId = lastLineOfIdMap.get((Long) id);
				if (lastLineForId == null) {
					lastLineOfIdMap.put((Long) id, factory.createString(text));
					fw.write(id + "-*" + text + "\n");
				} else {
					String lastLineForIdText = lastLineForId.getValue();

					int lastMatch = -1;
					for (int x = 0; x < text.length() && x < lastLineForIdText.length(); x++) {
						if (text.charAt(x) == lastLineForIdText.charAt(x)) {
							lastMatch = x;
						} else {
							break;
						}
					}
					if (lastMatch != -1 && lastMatch <= 60) {
						fw.write(id + "-" + BaseConversionUtil.convertToBase(lastMatch, 62)
								+ text.substring(lastMatch + 1) + "\n");
					} else {
						fw.write(id + "-*" + text + "\n");
					}

					lastLineOfIdMap.put((Long) id, factory.createString(text));

				}

			}
		} finally {
			br.close();

			fw.close();
			factoryPath.delete();
			factory.closeFactory();

		}
	}

}
