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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Stack;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/** This class creates a directory and file listing of all the files in 'directoryToIndex', and stores
 * them in a ZIP file, which is compressed using the standard ZIP file algorithm. */
public class FilenameIndexerNew {

	public static void createIndex(File directoryToIndex, File resultIndexFile) throws IOException {
	
		resultIndexFile.getParentFile().mkdirs();
		
		ZipOutputStream zfos = new ZipOutputStream(new FileOutputStream(resultIndexFile));
		zfos.putNextEntry(new ZipEntry("filename-index.idx"));
		
		OutputStreamWriter osw = new OutputStreamWriter(zfos);
		
		osw.write(directoryToIndex.getPath()+"\n");
		

		long count = 0;
		Stack<File> stack = new Stack<File>();		
		stack.push(directoryToIndex);
		
		while(stack.size() > 0) {
			File f = stack.pop();
			
			if(f.isDirectory()) {
				
				for(File fe : f.listFiles()) {
					stack.push(fe);
				}
				
			} else {
		
				
				String path = f.getPath();
				path = path.replace(directoryToIndex.getPath(), "");
				
				
				osw.write(path+"\n");
				count++;
				
				if(count % 100000 == 0) {
					System.out.println(count);
				}
				
			}
			
		}
		

		osw.close();
	}
	
}
