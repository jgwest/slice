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

package com.slice.indexer.rest;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.slice.datatypes.ObjectFactory;
import com.slice.datatypes.QueryDTO;
import com.slice.indexer.constants.IConfigConstants;
import com.slice.indexer.constants.ISearchFileType;
import com.slice.indexer.rest.IQueryDatabaseResult.QDRStatus;
import com.slice.indexer.shared.util.JavaSrcSearchQuery;
import com.slice.indexer.shared.util.SearchIndexerUtil;

/** Utility methods used by the SearchIndexRS project. */
public class RestUtil {

	private static String[] toStringList(List<String> list, boolean allowPathChars) {

		list = SearchIndexerUtil.sanitizeStringList(list, allowPathChars, false);
		
		// Remove empty elements
		for(Iterator<String> it = list.iterator(); it.hasNext();) {
			String str = it.next();
			if(str.trim().length() == 0) {
				it.remove();
			}
		}
		
		return list.toArray(new String[list.size()]);
		
	}
	
	public static QueryDTO convertJavaSrcSearchQuery(JavaSrcSearchQuery query) {
		ObjectFactory of = new ObjectFactory();
		QueryDTO result = of.createQueryDTO();
		result.setCaseSensitive(query.isCaseSensitive());
		
		for(ISearchFileType sfType : query.getSearchFileTypes()) {
			result.getSearchFileTypes().add(Integer.toString(sfType.getFileTypeId()));
		}
		
		result.setWholeWordOnly(query.isWholeWorldOnly());
		
		if(query.getPathExcludeFilterPatterns() != null) {
			
			for(String str : query.getPathExcludeFilterPatterns()) {
				result.getPathExcludeFilterPatterns().add(str);
			}
		}
		
		if(query.getPathIncludeComponentFilterPatterns() != null) {
			for(String str : query.getPathIncludeComponentFilterPatterns()) {
				result.getPathIncludeComponentFilterPatterns().add(str);
			}
			
		}
		
		if(query.getPathIncludeOnlyFilterPatterns() != null) {
			for(String str : query.getPathIncludeOnlyFilterPatterns()) {
				result.getPathIncludeOnlyFilterPatterns().add(str);
			}
			
		}
		
		for(String str : query.getSearchTerm()) {
			result.getSearchTerm().add(str);
		}
		
		return result;
		
	}
	
	public static JavaSrcSearchQuery convertQueryDTO(QueryDTO query, IConfigConstants constants ) {
		JavaSrcSearchQuery result = new JavaSrcSearchQuery();
		
		result.setCaseSensitive(query.isCaseSensitive());
		result.setPathExcludeFilterPatterns(toStringList(query.getPathExcludeFilterPatterns(), true));
		
		// Convert picFP to /picFP/, in an String[]
		List<String> picFP = query.getPathIncludeComponentFilterPatterns();
		String[] picFPArr = new String[picFP.size()];		
		for(int x = 0; x < picFP.size(); x++) {
			picFPArr[x] = File.separator+picFP.get(x)+File.separator;
			picFPArr[x] = SearchIndexerUtil.sanitizeString(picFPArr[x], true, false); 
		}
		result.setPathIncludeComponentFilterPatterns(picFPArr); 
		
		// Remove and split non-alphanumeric search terms
		// eg. [extension-point] => [extension,point]
		List<String> newQueryList = new ArrayList<String>(); 
		for(String str : query.getSearchTerm()){
			String term = str;
			term = SearchIndexerUtil.replaceAllNonJavaIdentifierExceptPeriodsWithSpaces(term);
			
			if(term.contains(" ")) {
				String[] spaceArr = term.split(" ");
				for(String iStr : spaceArr) {
					newQueryList.add(iStr);
				}
			} else {
				newQueryList.add(term);
			}
		}
		
		// Update the query with the up
		query.getSearchTerm().clear();
		query.getSearchTerm().addAll(newQueryList);

		
		result.setPathIncludeOnlyFilterPatterns(toStringList(query.getPathIncludeOnlyFilterPatterns(), true));
		result.setSearchTerm(SearchIndexerUtil.sanitizeStringList(query.getSearchTerm(), false, true));
		
		
		List<ISearchFileType> sfTypeResult = new ArrayList<ISearchFileType>();
		for(String i : query.getSearchFileTypes()) {
			// Find the search file type that corresponds to that id
			for(ISearchFileType sfType : constants.getSearchFileTypes()) {
				if(sfType.getFileTypeId() == Integer.parseInt(i)) {
					sfTypeResult.add(sfType);
					break;
				}
			}
		}
		result.setSearchFileTypes(sfTypeResult);		
		result.setWholeWorldOnly(query.isWholeWordOnly());
		
		return result;
	}
	
	public static String convertStatusToString(IQueryDatabaseResult.QDRStatus status) {
		
		if(status == QDRStatus.COMPLETE_ERROR) {
			return "COMPLETE_ERROR";
		} else if(status == QDRStatus.COMPLETE_SUCCESS) {
			return "COMPLETE_SUCCESS";
		} else if(status == QDRStatus.INCOMPLETE) {
			return "INCOMPLETE";
		}
		
		return null;
	}
}
