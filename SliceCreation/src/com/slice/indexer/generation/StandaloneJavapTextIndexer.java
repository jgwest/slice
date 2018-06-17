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
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.slice.indexer.shared.IWritableDatabase;

/** Parse class bytecode generated by the javap utility and write to a database. */
public class StandaloneJavapTextIndexer {

	private static long _filesRead = 0;
	
//	private static IWritableDatabase _dbInst = null;
	
	
	/**
	 * @param args
	 */
	@SuppressWarnings("null")
	public static void main(String[] args) {

		File directoryToRecurse = new File(args[0]);
		
		IWritableDatabase dbInst = null; // WritableFSDatabase.generateFSDatabaseForWrite(new File(ConfigConstants.getJarFsDatabaseOutputPath()), directoryToRecurse, SupportedFiles.JAR_SEARCH);
//		_dbInst = new InMemoryDatabase(new File(SearchIndexerConstants.DATABASE_FILE_PATH));
		
		recurseDirectory(directoryToRecurse, dbInst);
		System.out.println("Writing db");
		
		dbInst.writeDatabase();

	}
	
	private static boolean isValidClassChar(char c) {
		if(c == '/') return true;
		return Character.isJavaIdentifierPart(c);
		
	}
	
	private static List<String> extractClassesFromLine(String str) {
		int start = -1;
		List<String> result = new ArrayList<String>();
		
		for(int x = 0; x < str.length(); x++) {
			if(start == -1 && str.charAt(x) == '/') {
				for(start = x-1; start > 0 ; start--) {
					if(!isValidClassChar(str.charAt(start))) {
						start++;
						break;
					}
				}
			}
			
			if(start != -1 && !isValidClassChar(str.charAt(x))) {
//				System.out.println(start +" "+x);
				String r = str.substring(start, x);
				if(r.startsWith("Ljava/") || r.startsWith("Lcom/") || r.startsWith("Lorg/") || r.startsWith("Ljavax/")) {
					r = r.substring(1);
					
				}
				if(!result.contains(r)) {
					result.add(r);
				}
				start = -1;
			}
			
			
		}
		return result;
	}

	
	@SuppressWarnings("unused")
	private static void recurseFile(File file, IWritableDatabase dbInst) {
				
		String fname = file.getName().toLowerCase();
		
		String fpath = file.getPath().toLowerCase();
		
		if(fpath.endsWith(".javapout.txt") ) {
			
			_filesRead++;
			
			if(_filesRead % 10000 == 0) {
				System.out.println("Files read:"+_filesRead);
			}
			
			try {
				FileReader fr = new FileReader(file);
				BufferedReader br = new BufferedReader(fr);
				IWritableDatabase d = dbInst;
				
				List<String> fileContents = new ArrayList<String>();
				String str;
				while( (str = br.readLine()) != null) {
					fileContents.add(str);
				}
				br.close();
				fr.close();

				parseFile(fileContents, file, true, dbInst);
				
								
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	/** Returns true if added, false otherwise*/
	private static boolean addToMapReturnIfAdded(Map<Integer, Boolean> map, int id) {
		Boolean b = map.get((Integer)id);
		if(b == null) {
			map.put((Integer)id, true);
			return true;
		}
		
		return false;
	}
	
	private static void parseFile(List<String> fileContents, File file, boolean ignoreCase, IWritableDatabase dbInst) {
		String str;
		IWritableDatabase d = dbInst;
		
		Map<Integer, Boolean> idAddedMap = new HashMap<Integer, Boolean>();
		
//		System.out.println("file: "+file);
		
		for(int x = 0; x < fileContents.size(); x++) {
			str = fileContents.get(x);
			
//			System.out.println(str);
			
			if((str.contains("// Method") || str.contains("// InterfaceMethod") /*|| str.contains("//Field")*/ || str.contains("// class"))) {
//				System.out.println("-----------------------------------------");
//				System.out.println(str);
				str = str.substring(str.indexOf("//")+2);
				str = str.substring(str.indexOf(" ")+1);
//				System.out.println(str);
				
				List<String> result = null;
				
				if(str.contains("// Method") || str.contains("// InterfaceMethod")) {
					result = extractClassesFromMethodCall(str);
				} else if(str.contains("// class") || str.contains("//class")) { // Added a space between class here
					result = extractClassesFromLine(str);							
				}
				
//				for(String e : result) {
//					System.out.print(e+"  ");
//				}
//				System.out.println();
				if(result != null) {
					for(String e : result) {
						int id = d.createOrGetId(e, ignoreCase);
						if(addToMapReturnIfAdded(idAddedMap, id)) {
							d.addLink(id, file, 1);
						}
					}
				}
//				System.out.println(sub);

			} else if(str.trim().startsWith("#")) {
				// Parse the constant pool entries
				
				if(str.contains("= Class")) {
					String text = str.substring(str.indexOf("= Class")+7);
					text = text.substring(text.indexOf("// ")+3);

					int id = d.createOrGetId(text, ignoreCase);
					if(addToMapReturnIfAdded(idAddedMap, id)) {
						d.addLink(id, file, 1);
					}
					
				} else if(str.contains("= Utf8")) {
					String text = str.substring(str.indexOf("= Utf8")+6);
					List<String> classes = extractClassesFromMethodCall(text);
					if(classes != null) {
						for(String s : classes) {
							int id = d.createOrGetId(s, ignoreCase);
							if(addToMapReturnIfAdded(idAddedMap, id)) {
								d.addLink(id, file, 1);
							}
						}
					}
				}
			} else { 
				if(str.trim().startsWith(";") && (x > 1 && fileContents.get(x-1).trim().length() == 0)
						&& fileContents.get(x+1).trim().contains("flags:")) {
					
					String[] arr = str.split(" ");
					if(arr != null) {
						
						for(int y = 0; y < arr.length; y++) {
							String sub = arr[y];

							int id = d.createOrGetId(sub, ignoreCase);
							if(addToMapReturnIfAdded(idAddedMap, id)) {
								d.addLink(id, file, 2);
							}
						}
					}
					
				}
			}
			
			
			/* else {
			
				String[] arr = str.split(" ");
				if(arr != null) {
					
					for(int x = 0; x < arr.length; x++) {
						String sub = arr[x];
						int count = 0;
						for(int c = 0; c < sub.length(); c++) {
							if(sub.charAt(c) == '.' || sub.charAt(c) == '/') {
								count++;
							}
						}
						if(count >= 2 && !sub.contains("//") && sub.trim().length() >= 4) {
							int id = d.createOrGetId(sub);
							d.addLink(id, file, 2);
							
						}

					}
				}
			} */
		}		
	}	
	
	private static List<String> extractClassesFromMethodCall(String cmd) {
		List<String> result = new ArrayList<String>();
		
		for(int x = 0; x < cmd.length(); x++) {
			
			String str = cmd.substring(x);
			if(str.length() > 2) {
				
				if(str.startsWith("(L") || str.startsWith("[L")) {
					int newx = cmd.indexOf(";", x+1);
					if(newx != -1) {
						String type = cmd.substring(x+2, newx);
						result.add(type);
						x+= 3;
					}
				}

				if(str.startsWith(";L")) {
					int newx = cmd.indexOf(";", x+2);
					if(newx != -1) {
						String type = cmd.substring(x+2, newx);
						result.add(type);
						x+= 3;
					}
				}
				
				if(str.startsWith(")L")) {
					int newx = cmd.indexOf(";", x+1);
					if(newx != -1) {
						String type = cmd.substring(x+2, newx);
						result.add(type);
						x+= 3;
					}
				}
				
			}
			
		}
		
		return result;
		
	}
	
	private static void recurseDirectory(File dir, IWritableDatabase dbInst) {
		
		File[] dirList = dir.listFiles();
		if(dirList == null) {
			return;
		}
		
		for(File f : dirList) {
			if(f.isDirectory()) {
				recurseDirectory(f, dbInst);
			} else if(f.isFile()) {
				recurseFile(f, dbInst);
			}
			
		}
		
	}


}
