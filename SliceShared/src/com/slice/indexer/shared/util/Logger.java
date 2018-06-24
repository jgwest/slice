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

package com.slice.indexer.shared.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/** Simple class for search indexers to use during indexing. */
public class Logger {
	private static final boolean LOGGING = false;

	private static final Logger instance = new Logger();
	
	File file;
	FileWriter writer;

	public void setLogFile(File file) {
		this.file = file;
	}
	
	public static Logger getInstance() {
		return instance;
	}
	
	public void open() {
		if(!LOGGING) return;
		
		if(file == null) {
			throw new RuntimeException("setLogFile(...) must be called first. before opening the logger.");
		}
		
		try {
			writer = new FileWriter(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void close() {
		if(!LOGGING) return;
		
		try {
			writer.flush();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	public void newId(int id, String text) {
		if(!LOGGING) return;
		
		try {
			writer.write("["+id+"]-("+text+")\r\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void inFile(int id, File file, int debug) {
		if(!LOGGING) return;
		
		try {
			writer.write("{"+id+"}-&"+debug+"&<"+file.getPath()+">\r\n");
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}
}
