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

package com.slice.indexer.ui;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import com.slice.indexer.constants.IConfigConstants;
import com.slice.indexer.constants.ISearchFileType;
import com.slice.indexer.shared.GenericFileListResult;
import com.slice.indexer.shared.GenericFileListResult2;
import com.slice.indexer.shared.Product;
import com.slice.indexer.shared.util.JavaSrcSearchQuery;
import com.slice.indexer.shared.util.SearchIndexerUtil;

/** Contains various utility methods used by the search index UI. */
public class SearchIndexUIUtil {

	public static boolean checkCookieInRequest(Product product, HttpServletRequest request, StringBuilder errorText ) {
		
		if(!isCookieValid(product, request)) {

			
			if(errorText != null) {
				String email = product.getConstants().getSearchAdminEmail();
				
				errorText.append(("<div class=\"indexbox\">\n"));
				errorText.append(("Sorry, in order to see search results you need to log-in with your username/password (contact your admin for more information: <a href=\"mailto:"+email+"\">"+email+"</a>), and you must also be authorized to view these results.\n"));
				errorText.append(("<br/><br/>\n"));
				errorText.append("<a href=\"/SliceWeb/auth/?resourceId="+product.getProductId()+"\" target=\"_blank\">Click here to log-in here.</a>\n"); 
				errorText.append(("</div>\n"));
			}
			return false;
		}
		
		return true;
    	
    }

    public static boolean isCookieValid(Product product, HttpServletRequest request) {

    	if(SearchIndexerUtil.isDebugTestModeEnabled()) {
    		return true;
    	}
    	
		String cookieName = product.getConstants().getCookieName();
		if(cookieName == null) {
			return true;
		}
		Cookie cookies[] = request.getCookies();
		
		boolean cookieSet = false;
		if (cookies != null) {
			for (int x = 0; x < cookies.length; x++) {
				
				if (cookies[x].getName().equals(cookieName) && cookies[x].getValue().equalsIgnoreCase("true")) {
					cookieSet = true;
					break;
				}
			}
		}
		
		return cookieSet;
		    	
    }
    
    /** Caller must own a lock on DatabaseLock */
	public static String getComponentOfPath(File path, DatabaseLock database) {
		
		final String[] pluginPrefixes = new String[] {"com.", "org.", "net."};
		
		String str = path.getName();
		while(path != null) {

			
			
			if(str.length() > 4 && str.contains(".")) {
				boolean matchFound = false;
				for(String pref : pluginPrefixes) {
					if(str.toLowerCase().startsWith(pref.toLowerCase())) {
						matchFound = true;
						break;
					}
				}
				
				if(matchFound && database.getComponentMap() != null) {
					return database.getComponentMap().getComponent(str);
				}
			}
			
			
			path = path.getParentFile();
			if(path != null) {
				str = path.getName();
			}
		}
		

		return null;
	}
    
	/**
	 * Add the file to the result list iff:
	 * - it is not filtered out by pattern
	 * - it is not filtered out by component
	 * - it is not only present in the list
	 * - other criteria
	 *  
	 * @param idf File to check
	 * @param query The current query
	 * @param fileListContains Current map of file results already contained in result set
	 * @param listResult result set
	 * @param filesContainedInParam number of files currently contained in result set
	 * @return 
	 */
	public static AddFileResultUtil addFileToResults(File idf, JavaSrcSearchQuery query, Map<File, Boolean> fileListContains, 
			List<IndexerResultEntry> listResult, int filesContainedInParam, IConfigConstants configConstants) {
		
		AddFileResultUtil result = new AddFileResultUtil();
		result._filesContainedIn = filesContainedInParam;
		result._result = null;
		
		// Is the file already in our result list? If not, add it 
		Boolean b = fileListContains.get(idf);
		if(b == null || b == false) {
			
			// Add the file to our result list
			
			// Remove files that match our filter pattern
			boolean matchedFilter = false;
			if(query.getPathExcludeFilterPatterns() != null) {
				for(String s : query.getPathExcludeFilterPatterns()) {
					if(s.trim().length() == 0) continue; // Skip empty
					s = s.trim();
					
					if(idf.getPath().toLowerCase().contains(s.toLowerCase())) {
						matchedFilter = true;
					}
						
				}
			}
			
			
			// Remove files that don't match the request file type
			if(!matchedFilter) {
				boolean fileExtensionMatch = false;

				for(ISearchFileType sfType : query.getSearchFileTypes()) {
					if(SearchIndexerUtil.doesFileExtensionMatchSearchFileType(idf, sfType)) {
						fileExtensionMatch = true;
						break;
					}
				}
				
				matchedFilter = !fileExtensionMatch;
				
			}
			
			// If the file didn't get filtered out by our search engines 
			
			if(!matchedFilter) {
				if(query.getPathIncludeOnlyFilterPatterns() != null) {
					boolean matchedIncludeOnlyFilter = false;
					
					boolean nonEmptyFilterFound = false;
					
					for(String s : query.getPathIncludeOnlyFilterPatterns()) {
						if(s.trim().length() == 0) continue; // Skip empty
						s = s.trim();
						nonEmptyFilterFound = true;

						if(idf.getPath().toLowerCase().contains(s.toLowerCase())) {
							matchedIncludeOnlyFilter = true;
							break;
						}									
					}
					
					if(nonEmptyFilterFound) {
						matchedFilter = !matchedIncludeOnlyFilter;
					}
					
				}
				
			}
			
			// Filter out results by component
			if(!matchedFilter) {
				if(query.getPathIncludeComponentFilterPatterns() != null) {
					boolean matchedIncludeOnlyFilter = false;
					
					boolean nonEmptyFilterFound = false;
					
					for(String s : query.getPathIncludeComponentFilterPatterns()) {
						if(s.trim().length() == 0) continue; // Skip empty
						s = s.trim();
						nonEmptyFilterFound = true;

						if(idf.getPath().toLowerCase().contains(s.toLowerCase())) {
							matchedIncludeOnlyFilter = true;
							break;
						}									
					}
					
					if(nonEmptyFilterFound) {
						matchedFilter = !matchedIncludeOnlyFilter;
					}
					
				}
				
			}
			
			
			// If the file didn't get filtered out by our search options above, then display it
			if(!matchedFilter) {
				
				String stream = StreamUtil.getStreamFromPath(idf, configConstants);
				
				listResult.add(new IndexerResultEntry(idf.getPath(), stream, configConstants));

				result._filesContainedIn ++;
				
				if(result._filesContainedIn > configConstants.getSearchWhoaCowboyFilesNumber()) {
					Collections.sort(listResult);
					result._result = new GenericFileListResult<IndexerResultEntry>();
					result._result.setStatus(GenericFileListResult.GFLStatus.TOO_MANY_FILES);
					result._result.setFileList(listResult);

					return result;

				}								
			}
			
			fileListContains.put(idf, true);
			
		}
		
		return result;
	}

	public static AddFileResultUtil2 addFileToResultsNew(long keyId, File idf, JavaSrcSearchQuery query, Map<File, Boolean> fileListContains, List<IndexerResultEntry> listResult, int filesContainedInParam, IConfigConstants configConstants) {
		
		AddFileResultUtil2 result = new AddFileResultUtil2();
		
		AddFileResultUtil val = addFileToResults(idf, query, fileListContains, listResult, filesContainedInParam, configConstants);
		
		if(val._result != null) {
			
			GenericFileListResult2<IndexerResultEntry> newOne = new GenericFileListResult2<IndexerResultEntry>(); 
			
			newOne.copyFrom(keyId, val._result);
		
			result._result = newOne;
		}
		
		result._filesContainedIn = val._filesContainedIn;
		
		return result;
	}

	/** Return value of addFileToResults(...) */
	public static class AddFileResultUtil {
		public GenericFileListResult<IndexerResultEntry> _result;
		public int _filesContainedIn;
	}

	/** Return value of addFileToResultsNew(...) */
	public static class AddFileResultUtil2 {
		public GenericFileListResult2<IndexerResultEntry> _result;
		public int _filesContainedIn;
	}

}
