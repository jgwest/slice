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
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.Map;

import com.slice.indexer.dice.MemIntToIntMap;

/** Further compress the previously crunched file. */
public class StandaloneFSDatabasePathRandomAccessCompressorNew {

	public static void compressNew(File inputPath, File outputFile, File outputMapData) {
		
		try {
			
			Map<Long/*id*/, Integer/*file size*/> generateSizeMap = generateSizeMap(inputPath);
	
			/** id => file position */
			MemIntToIntMap idToStartPosMap = new MemIntToIntMap(generateSizeMap.size());
			
			Long[] currPosArray = new Long[generateSizeMap.size()];
			
			long curr = 0;
			for(long id = 0; id < currPosArray.length; id++) {
			
				idToStartPosMap.put((Integer)(int)id, (Integer)(int)curr);
				currPosArray[(int)id] = curr;
				
				curr += generateSizeMap.get((Long)id)  + 4;
				
			}
						
			if(outputFile.exists()) {
				if(!outputFile.delete()) {
					throw new RuntimeException("Unable to delete.");
				}
			}
			
						
			RandomAccessFile out = new RandomAccessFile(outputFile, "rw");
			
			BufferedReader br = new BufferedReader(new FileReader(inputPath));
			String str;
			while(null != (str = br.readLine())) {
				
				int hyphenPos = str.indexOf("-");
				
				long id = Long.parseLong(str.substring(0, hyphenPos));
			
				String text = str.substring(hyphenPos+1).trim()+"\n";

				byte[] textBinary = text.getBytes();
				
				long currFilePos = currPosArray[(int)id];
				
				
				out.seek(currFilePos);
				
				int startPosForId = idToStartPosMap.get((int)id);
				
				// Is this the first entry for this file?
				if(currFilePos == startPosForId) {
					// If so, the first 4 bytes are the length 
					out.writeInt(generateSizeMap.get((Long)id));
					currPosArray[(int)id]+= 4;
				} 
				
				// Write the text
				
				out.write(textBinary);
				
				currPosArray[(int)id]+= textBinary.length;				
			}
			
			br.close();
			
			out.close();
			
			idToStartPosMap.writeToFile(outputMapData);
			
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static Map<Long/*id*/, Integer/*file size*/> generateSizeMap(File inputPath) throws IOException {
		
		Map<Long/*id*/, Integer/*file size*/> map = new HashMap<Long, Integer>();
		
		BufferedReader br = new BufferedReader(new FileReader(inputPath));
		String str;
		
		while(null != (str = br.readLine()) ) {
			
			int hyphenPos = str.indexOf("-");
			
			long id = Long.parseLong(str.substring(0, hyphenPos));
		
			
			String text = str.substring(hyphenPos+1).trim()+"\n";
			
			byte[] textInBytes = text.getBytes();
			
			Integer size = map.get((Long)id);
			if(size == null) {
				size = 0;
			}
			
			size = size + textInBytes.length;
			
			map.put((Long)id, size);
			
		}
		
		br.close();
		
		return map;
		
		
	}

}
