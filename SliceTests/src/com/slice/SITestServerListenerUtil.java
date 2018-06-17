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

package com.slice;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/** This utility class will read from the Liberty messages.log, and output it to the screen. */
public class SITestServerListenerUtil {

	public static void addServerMessagesListenerThread(String libertyRootPath, String libertyServerName) {
		
		ServerMessagesListenerThread t = new ServerMessagesListenerThread(libertyRootPath, libertyServerName);
		t.start();
		
	}

	/** The actual thread that listens on the messages.log */
	private static class ServerMessagesListenerThread extends Thread {

		/** lines that we have already read from the file */
		private final List<String> seenLines = new ArrayList<String>();
		
		private final String libertyRootPath;
		private final String liberytServerName;
		
		
		public ServerMessagesListenerThread(String libertyRootPath, String liberytServerName) {
			
			this.libertyRootPath = libertyRootPath;
			this.liberytServerName = liberytServerName;
		}


		@Override
		public void run() {
			
			try {

				File messagesLog = new File(libertyRootPath, "usr/servers/" + liberytServerName+"/logs/messages.log");
				
				if(!messagesLog.exists()) {
					System.err.println("Unable to location message.log at: "+messagesLog.getPath());
					return;
				}
				
				System.out.println("Reading data from: "+messagesLog.getPath());

				while(true) {
					
					Iterator<String> seenIterator = seenLines.iterator();
					
					List<String> fileContents = readFile(messagesLog);
					
					Iterator<String> fileContentsIt = fileContents.iterator();
					
					boolean fullMatch = true;
					
					while(seenIterator.hasNext() && fileContentsIt.hasNext()) {
						String fromSeen = seenIterator.next();
						String fromFileContents = fileContentsIt.next();
						
						if(!fromSeen.equals(fromFileContents)) {
							fullMatch = false;
							break;
						}
						
					}
					
					if(fullMatch) {
						
						if(seenIterator.hasNext()) {
							// If there are more lines in seenLines, then we are reading a different file.
							fullMatch = false;
						}
						
						if(fileContentsIt.hasNext()) {
							// If there are more lines in the fileContents, then we should print them and add them to seenLines
							
							while(fileContentsIt.hasNext()) {
								String str = fileContentsIt.next();
								System.out.println("["+liberytServerName+"] > "+str);
								seenLines.add(str);
								
							}
							
						}
						
						
					}
					
					if(!fullMatch) {
						seenLines.clear();
						
						seenLines.addAll(fileContents);
						
						for(String str : fileContents) {
							System.out.println("["+liberytServerName+"] > "+str);
						}						
					}
								
					Thread.sleep(1 * 1000);
				}
				
			} catch(Exception e) {
				e.printStackTrace();
			}
						
		}
	
	
		private List<String> readFile(File serversDir) throws IOException {
			List<String> result = new ArrayList<String>();
			
			BufferedReader br = new BufferedReader(new FileReader(serversDir));
			
			String str;
			while(null != (str = br.readLine())) {
				
				result.add(str);
			}
			
			br.close();

			return result;
		}
	}

}
