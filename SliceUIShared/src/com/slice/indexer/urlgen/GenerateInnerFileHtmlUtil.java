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

package com.slice.indexer.urlgen;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.slice.indexer.shared.util.JavaSrcSearchQuery;
import com.slice.indexer.shared.util.SearchIndexerUtil;

/** Read the contents of a file from the file system, and apply highlighting of search terms and HTML sanitization. */
public class GenerateInnerFileHtmlUtil {

	/** Returns null if the search term(s) in the file were not found to match any lines (this only occurs with multiple search terms) */
	public static String generateInnerFileHTML(File f, JavaSrcSearchQuery query, boolean showEntireFile) throws IOException {
		StringBuilder sb = new StringBuilder();
		FileReader fr = new FileReader(f);
		BufferedReader br = new BufferedReader(fr);
				
		String str;
		
		List<String> fileContents = new ArrayList<String>();
		
		while(null != (str = br.readLine())) {
			fileContents.add(str);
		}
		
		br.close();
		fr.close();
		
		if(!showEntireFile) { sb.append("<font face=\"courier\" size=\"-1\">"); }
		
		final String MARKED_LINE = "%@#$%^-MARKED-LINE-%@#$%^";
		
		boolean anyLinesMatched = false;
		
		for(int x = 0; x < fileContents.size(); x++) {
			str = fileContents.get(x);
			
			boolean lineMatch = true;
			
			if(query != null && query.getSearchTerm() != null) {
				for(String st : query.getSearchTerm()) {
					
					if(query.isWholeWorldOnly()) {
						if(query.isCaseSensitive()) {
							lineMatch = str.contains(st) && SearchIndexerUtil.containsWholeWordOnly(str, st, query);
						} else {
							lineMatch = str.toLowerCase().contains(st.toLowerCase()) && SearchIndexerUtil.containsWholeWordOnly(str, st, query);
						}
					} else {
						if(query.isCaseSensitive()) {
							lineMatch = str.contains(st);
						} else {
							lineMatch = str.toLowerCase().contains(st.toLowerCase());
						}
					}
					
					if(!lineMatch) {
						break;
					}
					
				}
			} else {
				lineMatch = false;
			}
			
			if(lineMatch) {
				anyLinesMatched = true;
				str = sanitizeHtmlAndXml(str);
				
				int[] matches = new int[str.length()];
				
				for(String st : query.getSearchTerm()) {
					highlighSearchTerms2(str, st, query, matches);
				}
				
				str = applyHighlight2(str, matches, showEntireFile) + MARKED_LINE;
				
				fileContents.set(x, str);
								
			} else {
				str = sanitizeHtmlAndXml(str);
				fileContents.set(x, str);
			}
			
		}
		
		if(anyLinesMatched || showEntireFile) {

			int[] array = new int[fileContents.size()];
			
			// 0 = no matched lines
			// 1 = has matched line
			// 2 = non-matching line, with matching line before or after (will be added to html, but will not be bolded)
			
			for(int x = 0; x < array.length; x++) {
				str = fileContents.get(x);
				
				if(str.contains(MARKED_LINE)) {
					array[x] = 1;
				} else {
					array[x] = 0;
				}
			}
			
			for(int x = 0; x < array.length; x++) {
				if(x + 1 < array.length) {
					
					// if next line matches, and this line doesn't match...
					if(array[x+1] == 1 && array[x] != 1) {
						array[x] = 2;
					}
				}
				
				if(x - 1 >= 0) {
					// if previous line matches, and this line doesn't match..
					if(array[x-1] == 1  && array[x] != 1) {
						array[x] = 2;
					}
				}
				
			}
			
			boolean brAppended = false;
			
			for(int x = 0; x < array.length; x++) {
				str = fileContents.get(x);
				
				if(array[x] == 1) {
					str = str.replace(MARKED_LINE, "");
				}

				// 1 or 2
				if(array[x] > 0 || showEntireFile) {
					if(!showEntireFile) {
						str = str.replace("  ", "&nbsp;&nbsp;");
						str = str.replace("\t", "&nbsp;&nbsp;&nbsp;&nbsp;");
					}
					
					if(!showEntireFile) {
						sb.append("["+(x+1)+"] "); // line no starts at 1, not 0, so add 1
					}
					
					sb.append(str);
					
					if(!showEntireFile) {
						sb.append("<br/>");
					} 
					
					sb.append("\r\n");
					
				}
				
				// 2
				if(array[x] > 1) { 
					
					if(x+1 < array.length) { // is there a line after this one?
						
						if(array[x+1] == 0) { // is the next line a 0
							sb.append("<br/>\r\n");
							brAppended = true;
						} else {
							brAppended = false;
						}
					}
				}
			}
			
			if(!brAppended) {
				sb.append("<br/>\r\n");
			}
	
			if(!showEntireFile) { sb.append("</font>"); }
			
			fileContents.clear();
			
			return sb.toString();
			
		} else {
			// No matches in this file; just return null.
			
			return null;
		}
		
		
		
	}
	
	private static String sanitizeHtmlAndXml(String s) {
		s = s.replace("&", "&amp;");
		s = s.replace("\"", "&quot;");
		s = s.replace("<", "&lt;");
		s = s.replace(">", "&gt;");
		return s;
	}

	private static String applyHighlight2(String str, int[] matches, boolean showEntireFile) {
		final String START;
		if(showEntireFile) {
			START = "<span class=\"boldstyle2\">";
		} else {
			START = "<span class=\"boldstyle\">";
		}
		
		final String END = "</span>";
		
		StringBuilder result = new StringBuilder();
		
		int start = -1;
		
		for(int x = 0; x < matches.length; x++) {
			
			if(matches[x] == 0) {
				// This character is not part of a match
				
				if(start == -1) {
					// This character is not part of a match, just append it normally.
					result.append(str.substring(x, x+1));
				} else {
					// This character is the end of a previous match, so append it
					result.append(START+str.substring(start, x)+END+str.substring(x, x+1));
					start = -1;
				}
				
			} else if(matches[x] == 1) {
				// This character is part of a match
				
				if(start == -1) {
					// This is the first character in the match
					start = x;
				} else {
					// Do nothing; a match is in progress and it will be appended on completion, above
				}
				
			}
			
		}
		
		if(start != -1) {
			result.append(START+str.substring(start, matches.length)+END);
		}
		
		return result.toString();
	}

	private static void highlighSearchTerms2(String source, String searchTerm, JavaSrcSearchQuery query, int[] result) {
				
		for(int x = 0; x < source.length() && source.length()-x-searchTerm.length() >= 0; x++) {
			
			String preCurr = source.substring(0, x);
			String curr = source.substring(x, x+searchTerm.length());
			String postcurr = source.substring(x + searchTerm.length());
			

			// Does the query match 'curr'
			boolean termMatch = (query.isCaseSensitive() && curr.equals(searchTerm)) ||
					(!query.isCaseSensitive() && curr.equalsIgnoreCase(searchTerm));
			
			
			if(termMatch) {
				boolean match = false;
				
				if(query.isWholeWorldOnly()) {
					match = SearchIndexerUtil.arePreAndPostWhitespaceOrEmpty(preCurr, postcurr);
				} else  {
					match = true;
				}
				
				
				if(match) {
					for(int y = x; y < curr.length()+x; y++) {
						result[y] = 1;
					}
				}
			}
			
		}
	}


}
