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

package com.slice.indexer.dice;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.slice.indexer.shared.util.BaseConversionUtil;

/** 
 * This class is used to compress (and decompress) a list of strings, by converting commonly 
 * occurring strings into a single id encoded as a base-62 integer.
 * 
 * Used by (Readable/Writeable)FSDatabaseNew.
 **/
public class KeyCompression {
	
	private static void addToMap(Map<String, KeyEntry> map, String text) {
		KeyEntry ke = map.get(text);
		if(ke == null) {
			ke = new KeyEntry();
			ke.occurs = 0;
			ke.name = text;
			map.put(text, ke);
		}
		
		ke.occurs++;
		
	}
	
	/** Given a map from token id to token string, tokenize the 'inputStr' and reconstruct the original uncompressed string by matching token ids to tokens inside the map. */
	public static String decompressString(Map<Integer /* token id */, String /* token string */> map, String inputStr) {
		final boolean DEBUG = false;
		
		int mode = 0; // expecting id, mode exits with: punctuation, #,
//			mode = 1; // last char was punctuation (not ! or #)
//			mode = 2; // expecting text, mode exits with: punctuation, #
		
		StringBuilder result = new StringBuilder();
		
		StringBuilder currToken = new StringBuilder();
		
		for(int x = 0; x < inputStr.length(); x++) {
			char ch = inputStr.charAt(x);
			
			if(mode == 0) {
				String postFix = "";
				// expecting id chars
				boolean isEnded = false;
				if(ch == '!') {
					mode = 2;
					isEnded = true;
				} else if(ch == '#') {
					mode = 0;
					isEnded = true;
				} else if(	!(Character.isDigit(ch) || Character.isLetter(ch) || ch == ' ')	) {
					postFix = ""+ch; // the token has ended, add the punctuation after the token is added
					mode = 1;
					isEnded = true;
				} else if(Character.isDigit(ch) || Character.isLetter(ch) || ch == ' ') {
					currToken.append(ch);
					isEnded = false;
				} else {
					System.out.println("Error1");
					return null;
				}
				
				if(isEnded) {
					String currTokenStr = currToken.toString(); 
					if(currTokenStr.length() > 0) {
						if(DEBUG) {
							result.append("[id:"+currTokenStr+"]");
						} else {
							long id = BaseConversionUtil.convertFromBase(currTokenStr, 62);
							String tmpToken = map.get((Integer)(int)id);
							if(tmpToken == null) {
								System.out.println("Error 5a: "+id + " "+currTokenStr+ " full line:"+inputStr);
								return null;
							}
							result.append(tmpToken);
						}
					}
					currToken = new StringBuilder();
					
					result.append(postFix);
				}
				
			} else if(mode == 1) {

				if(ch == '!') {
					mode = 2;
				} else if(ch == '#') {
					mode = 0;
				} else if(	!(Character.isDigit(ch) || Character.isLetter(ch) || ch == ' ')	) {
					// char is punctuation
					result.append(ch);
					mode = 1;					
				} else if(Character.isDigit(ch) || Character.isLetter(ch) || ch == ' ') {
					// char is not punctuation
					currToken.append(ch);
					mode = 0;
				} else {
					System.out.println("Error2");
					return null;
				}
				
				
			} else if(mode == 2) {
				

				
				// expecting id chars
				boolean isEnded = false;
				if(ch == '!') {
					currToken.append(ch);
				} else if(ch == '#') {
					mode = 0;
					isEnded = true;
				} else if(	!(Character.isDigit(ch) || Character.isLetter(ch) || ch == ' ')	) {
					currToken.append(ch);
					mode = 1;
					isEnded = true;
				} else if(Character.isDigit(ch) || Character.isLetter(ch) || ch == ' ') {
					currToken.append(ch);
					isEnded = false;
				} else {
					System.out.println("Error3");
					return null;
				}
				
				if(isEnded) {
					String currTokenStr = currToken.toString(); 
					if(currTokenStr.length() > 0) {
						if(DEBUG) {
							result.append("[text:"+currTokenStr+"]");
						} else {
							result.append(currTokenStr);
						}
					}
					currToken = new StringBuilder();
				}
				
				
			}
			
		}
		
		String str = currToken.toString();
		
		if(str.trim().length() > 0) {
			if(mode == 0) {
				// str is an id
				if(DEBUG) {
					result.append("[id:"+str+"]");
				} else {
					long id = BaseConversionUtil.convertFromBase(str, 62);
					String tmpToken = map.get((Integer)(int)id);
					if(tmpToken == null) {
						System.out.println("Error 5b: "+id + " "+str+ " full line:"+inputStr);
						return null;
					}
					result.append(tmpToken);
				}
				
			} else if(mode == 1) {
				System.out.println("Error 4");
				return null;
				
			} else if(mode == 2) {
				// str is text
				if(DEBUG) {
					result.append("[text:"+str+"]");
				} else {
					result.append(str);
				}
				
			}
			
		}
		
		return result.toString();
	}
	
	/** Compress a list of strings, using a token->id map, and then return the result as a String. */
	private static String compressString(Map<String /* uncompressed token*/, Integer /* token id*/> map, List<String> inputStr /* list of strings to process*/) {
		
		String[] result = new String[inputStr.size()];
		
		// id => #id#, for all Strings in inputStr
		for(int x = 0; x < inputStr.size(); x++) {
			String tmpStr = inputStr.get(x);
			Integer i = map.get(tmpStr);
			if(i != null) {
				result[x] = "#"+BaseConversionUtil.convertToBase(i, 62)+"#";
			} else {
				result[x] = tmpStr;
			}
		}
		
		// Add ! and remove trailing chars where appropriate
		for(int x = 0; x < result.length; x++) {
			
			if(x+1 < result.length) {
				
				if(result[x].endsWith("#")) {
					boolean removeTrailingNum = false;
					
					if(result[x+1].startsWith("#")) {
						removeTrailingNum = true;
					} else {
						
						char ch = result[x+1].charAt(0);
						 
						if( ! ( Character.isDigit(ch) || Character.isLetter(ch) ) ) {
							removeTrailingNum = true;
							
						} 
						
						
					}
					
					if(removeTrailingNum) {
						// remove trailing char
						result[x] = result[x].substring(0, result[x].length()-1);						
					}
					
					
				}
			} else {
				if(result[x].endsWith("#")) {
					// remove trailing char
					result[x] = result[x].substring(0, result[x].length()-1);
				}
			}
			
			if(x == 0) {
				if(result[x].startsWith("#")) {
					// remove first char
					result[x] = result[x].substring(1);
				} else {
					result[x] = "!"+result[x];
				}
			}
				
		}
		
		StringBuilder sb = new StringBuilder();
		
		for(int x = 0; x < result.length; x++) {
			String r1 = result[x];
			if(x + 1 < result.length) {
				String r2 = result[x+1];
				if(r1.endsWith("#")) {
					// remove # from end of r1
					result[x] = result[x].substring(0, result[x].length()-1);
					
					// r2 can start with: # (just leave it), punctuation (just leave it), or letter/digit (add ! in front)
					
					if(r2.startsWith("#")) {						
						// no need to handle
					} else {
						char ch = result[x+1].charAt(0);
						if( ! ( Character.isDigit(ch) || Character.isLetter(ch) || ch == ' ' ) ) {
							// no need to handle
						} else {
							// ch is a letter or digit, add a !
							result[x+1] = "!"+result[x+1];
						}
						
					}
				} else {
					
					char ch = result[x].charAt(0);
					if(!(Character.isDigit(ch) || Character.isLetter(ch) || ch == ' ') && ch != '!' && ch != '#') {
						// if result[x] is punctuation...
						
						if(!r2.startsWith("#")) {
							// and result[x+1] is not an id
							result[x+1] = "!"+result[x+1];
						} else {
							// remove leading #
							result[x+1] = result[x+1].substring(1);
						}
						
						
					}
										
				}
				
			} else {
				if(r1.endsWith("#")) {
					// remove trailing char on r1
					result[x] = result[x].substring(0, result[x].length()-1);
				}
				
			}
			sb.append(result[x]);
		}

		return sb.toString();
		
	}
	
	public static List<String> readCompressedIdList(File f) throws IOException {
		FileReader fr = new FileReader(f);
		BufferedReader br = new BufferedReader(fr);
		
		List<String> result = new ArrayList<String>();
		
		String str;
		while(null != (str = br.readLine())) {
			result.add(str);
		}
		
		br.close();
		
		return result;
		
	}
	
	/** Given a list of keys to compress, write to files 'compressedList' and 'idToStrMapFile'.  */
	public static void createCompressedMapFile(File compressedList, File idToStrMapFile, List<String> keys) {
		
		FileWriter fw = null;
		boolean errorOccured = false;
		
		try {
			
			// pre-compression keys: 1,956,479
			// post-compression keys: 445,975
			
			Map<String, KeyEntry> keyMap = new HashMap<String, KeyEntry>();
			
			for(String str : keys) {
				
				List<String> toks = splitByAnyPunct(str);
				for(String str2 : toks) {

					if(!str2.startsWith("#")) {
						List<String> camelCaseList = splitByCamelCase(str2);
						for(String str3 : camelCaseList) {
							addToMap(keyMap, str3);
						}
					} 
					
				}
				
			}
			
			
			List<KeyEntry> keyList = new ArrayList<KeyEntry>(keyMap.values());
			
			Collections.sort(keyList);
			long preBytes = 0;
			long postBytes = 0;
			

			int nextId = 0;
			Map<String /* text */, Integer /* id for text*/> idMap = new HashMap<String, Integer>();
			Map<Integer /* id for text */, String /* text*/> idStrMap = new HashMap<Integer, String>(1024, 0.95f);
			
			for(KeyEntry ke : keyList) {
				
				String nextIdStr = BaseConversionUtil.convertToBase(nextId, 62);
				if(ke.name.length() > nextIdStr.length()) {
					idMap.put(ke.name, nextId);
					idStrMap.put(nextId, ke.name);
					nextId++;
				}
				
			}
			
			System.out.println("id-map size:"+idMap.size());
			FileOutputStream fos1 = new FileOutputStream(idToStrMapFile);
			ObjectOutputStream oos1 = new ObjectOutputStream(fos1);
			oos1.writeObject(idStrMap);
			oos1.close();
//			fos.close();
			
			FileInputStream jgwFis = new FileInputStream(idToStrMapFile);
			ObjectInputStream jgwOis = new ObjectInputStream(jgwFis);
			try {
				@SuppressWarnings({ "unchecked", "unused" })
				Map<Integer /* id for text */, String /* text*/> test = (Map<Integer /* id for text */, String /* text*/>)jgwOis.readObject();
				System.out.println("Successfully read.");
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			} finally {
				try { jgwOis.close(); } catch(Exception e) {}
			}
			
			
			fw = new FileWriter(compressedList);

			for(String str : keys) {
				
				List<String> line = new ArrayList<String>();
				
				List<String> toks = splitByAnyPunct(str);
				for(String str2 : toks) {

					if(!str2.startsWith("#")) {
						List<String> camelCaseList = splitByCamelCase(str2);
						for(String str3 : camelCaseList) {
							line.add(str3);
						}
					} else {
						// Don't add punctuation
						line.add(str2.replace("#", ""));
					}
				}
				
				
				StringBuilder dbgTmp = new StringBuilder();
				
				for(String x : line) {
					preBytes += x.length();
					dbgTmp.append(x);
				}
				
				String result = compressString(idMap, line);
				if(result == null) { 
					errorOccured = true; 
					throw new RuntimeException("Unable to compress string (error 1):"+dbgTmp.toString()); }
				
				String resultBack = decompressString(idStrMap, result);
				if(resultBack == null) {
					errorOccured = true;
					throw new RuntimeException("Unable to compress string (error 2):"+dbgTmp.toString());
//					compressString(idMap, line);
				} 
				
				if(!(resultBack.equals(dbgTmp.toString()))) {
					errorOccured = true;
					throw new RuntimeException("Unable to compress string (error 3):"+dbgTmp.toString());
				}
				
				postBytes += result.length();
				fw.write(result +"\n");
			}
			
			System.out.println(preBytes + " => "+postBytes);
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if(errorOccured) {
				try { compressedList.delete(); } catch(Exception e) {}
				try { idToStrMapFile.delete(); } catch(Exception e) {}
			}
			try { fw.close(); } catch(Exception e) {}
		}
		
	}

	
	
	@SuppressWarnings("unused")
	private static void createCompressedMapFileOld(File compressedList, File idToStrMapFile, List<String> keys) {
		
		FileWriter fw = null;
		boolean errorOccured = false;
		
		try {
			List<List<String>> lineList = new ArrayList<List<String>>();
			
			// pre-compression keys: 1,956,479
			// post-compression keys: 445,975
			
			Map<String, KeyEntry> keyMap = new HashMap<String, KeyEntry>();
			
			for(String str : keys) {
				
				List<String> line = new ArrayList<String>();
				
				List<String> toks = splitByAnyPunct(str);
				for(String str2 : toks) {

					if(!str2.startsWith("#")) {
						List<String> camelCaseList = splitByCamelCase(str2);
						for(String str3 : camelCaseList) {
							addToMap(keyMap, str3);
							line.add(str3);
						}
					} else {
						// Don't add punctuation
						line.add(str2.replace("#", ""));
					}
				}
				
				lineList.add(line);
			}
			
			
			List<KeyEntry> keyList = new ArrayList<KeyEntry>(keyMap.values());
			
			Collections.sort(keyList);
			long preBytes = 0;
			long postBytes = 0;
			

			int nextId = 0;
			Map<String /* text */, Integer /* id for text*/> idMap = new HashMap<String, Integer>();
			Map<Integer /* id for text */, String /* text*/> idStrMap = new HashMap<Integer, String>(1024, 0.95f);
			
			for(KeyEntry ke : keyList) {
				
				String nextIdStr = BaseConversionUtil.convertToBase(nextId, 62);
				if(ke.name.length() > nextIdStr.length()) {
					idMap.put(ke.name, nextId);
					idStrMap.put(nextId, ke.name);
					nextId++;
				}
				
			}
			
			System.out.println("id-map size:"+idMap.size());
			FileOutputStream fos = new FileOutputStream(idToStrMapFile);
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject(idStrMap);
			oos.close();
			fos.close();
			
			
			
			
			
			fw = new FileWriter(compressedList);

			for(List<String> line : lineList) {
				
				StringBuilder dbgTmp = new StringBuilder();
				
				for(String x : line) {
					preBytes += x.length();
					dbgTmp.append(x);
				}
				
				String result = compressString(idMap, line);
				if(result == null) { 
					errorOccured = true; 
					throw new RuntimeException("Unable to compress string (error 1):"+dbgTmp.toString()); }
				
				String resultBack = decompressString(idStrMap, result);
				if(resultBack == null) {
					errorOccured = true;
					throw new RuntimeException("Unable to compress string (error 2):"+dbgTmp.toString());
//					compressString(idMap, line);
				} 
				
				if(!(resultBack.equals(dbgTmp.toString()))) {
					errorOccured = true;
					throw new RuntimeException("Unable to compress string (error 3):"+dbgTmp.toString());
				}
				
				postBytes += result.length();
				fw.write(result +"\n");
			}
			
			System.out.println(preBytes + " => "+postBytes);
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if(errorOccured) {
				try { compressedList.delete(); } catch(Exception e) {}
				try { idToStrMapFile.delete(); } catch(Exception e) {}
			}
			try { fw.close(); } catch(Exception e) {}
		}
		
	}
	
	private static List<String> splitByCamelCase(String str) {
		
		List<String> result = new ArrayList<String>();

		boolean wasLastCharACapital = false;
		
		StringBuilder currTok = new StringBuilder();
		
		for(int x = 0; x < str.length(); x++) {
			char c = str.charAt(x);
			
			if(Character.isUpperCase(c)) {
				if(wasLastCharACapital) {
					currTok.append(c);
				} else {
					if(currTok.length() > 0) {
						result.add(currTok.toString());
					}
					currTok = new StringBuilder(""+c);
				}
				wasLastCharACapital = true;
			} else {
				currTok.append(c);
				wasLastCharACapital = false;
			}
			
		}
		
		if(currTok.length() > 0) {
			result.add(currTok.toString());
		}
		
		
		return result;
	}
	
	private static List<String> splitByAnyPunct(String str) {
		if(str.contains("#")) {
			return null;
		}
		
		List<String> result = new ArrayList<String>();
		str = str.trim();
		
		StringBuilder currTok = new StringBuilder();
		
		for(int x = 0; x < str.length(); x++) {
			char c = str.charAt(x);
			if(Character.isDigit(c) || Character.isLetter(c)) {
				currTok.append(c);
			} else {
				if(currTok.length() > 0) {
					result.add(currTok.toString());
					result.add("#"+c);
					currTok = new StringBuilder();
				}
				
			}
			
		}
		
		if(currTok.length() > 0) {
			result.add(currTok.toString());
		}
		
		return result;
	}

}

/** Used to keep track of how many times a string occurs, what it's length in chars is, and to provide a natural (descending?) search order for the product of those values. */
class KeyEntry implements Comparable<KeyEntry>{
	String name = null;
	int length = -1;
	int occurs = 0;
	
	public int getLength() {
		if(length == -1) {
			length = name.length();
		}
		return length;
	}
	
	@Override
	public int compareTo(KeyEntry o) {
		return o.getLength()*o.occurs - (getLength()*occurs);
		
	}
	
}