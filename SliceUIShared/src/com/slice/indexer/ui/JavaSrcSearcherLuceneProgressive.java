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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.slice.indexer.constants.IConfigConstants;
import com.slice.indexer.lucene.LuceneDb;
import com.slice.indexer.lucene.LuceneSearchUtil;
import com.slice.indexer.shared.GenericFileListResult;
import com.slice.indexer.shared.util.DebugPerformanceTimer;
import com.slice.indexer.shared.util.JavaSrcSearchQuery;
import com.slice.indexer.ui.SearchIndexUIUtil.AddFileResultUtil;


/** Perform a search on the Lucene database. */
public class JavaSrcSearcherLuceneProgressive {
	
	public final static boolean REMOVE_FAT_SEARCH_TERMS = true;
	
	
	@SuppressWarnings("unused")
	public static GenericFileListResult<IndexerResultEntry> search(LuceneDb luceneDb, JavaSrcSearchQuery query, IDatabaseResultProgess progress, IConfigConstants configConstants) throws IOException {
		
		
		GenericFileListResult<IndexerResultEntry> result = new GenericFileListResult<IndexerResultEntry>();
		
		List<IndexerResultEntry> listResult = null;

		DebugPerformanceTimer.startTimer(JavaSrcSearcherLuceneProgressive.class, "search");
		
		List<String> removedSearchTerms = new LinkedList<String>();
		
		
		for(String s : query.getSearchTerm()) {
			
			GenericFileListResult<IndexerResultEntry> t = searchInnerLucene(s, query, luceneDb, progress, configConstants);
			
			// SearchInner returned an error -- handle it
			if(t.getStatus() != GenericFileListResult.GFLStatus.OK) {
				
				// With 2 or more search terms, remove the offending search term, and just return results for the other
				if(query.getSearchTerm().size() > 1) { 
					// t.setFileList(null);
					removedSearchTerms.add(s);
					continue;
					
				} else {
					return t;
				}
				
			}
			
			// Add results to list -- merge with existing list results
			if(listResult != null) {
				List<IndexerResultEntry> newResult = new ArrayList<IndexerResultEntry>();
				
				HashMap<String, Boolean> containedInResult = new HashMap<String, Boolean>();
				for(IndexerResultEntry e : listResult) {
					containedInResult.put(e.getFile(), true);
				}
				
				for(IndexerResultEntry e : t.getFileList()) {
					
					Boolean cir = containedInResult.get(e.getFile()); 
					
					if(cir != null && cir == true) {
						newResult.add(e);
					}
				}
				
				listResult = newResult;
				containedInResult = null; newResult = null;
				
			} else {
				
				// Initial result
				listResult = t.getFileList();				
			}
		}
		
		if(removedSearchTerms.size() > 0 && !REMOVE_FAT_SEARCH_TERMS) {
			for(String s : removedSearchTerms) {
				for(Iterator<String> it = query.getSearchTerm().iterator(); it.hasNext();) {
					String st = it.next();
					
					if(st.equalsIgnoreCase(s)) {

						progress.addErrorText("<br/>Whoa there cowboy, search term '"+st+"' was removed as it has generated WAY too many results (>"+configConstants.getSearchWhoaCowboyFilesNumberText()+" files). Try using a tighter search to keep this from triggering (ex: use case sensitive, whole world only, tighter search terms, etc).<br/>\r\n");
						it.remove();
					}
				}
			}
		}
		
		result.setStatus(GenericFileListResult.GFLStatus.OK);
		result.setFileList(listResult);
		
		DebugPerformanceTimer.stopTimer(JavaSrcSearcherLuceneProgressive.class, "search");
		
		return result;
		
	}
	
	/** Search for a single one of the search terms. */
	private static GenericFileListResult<IndexerResultEntry> searchInnerLucene(String keyToSearch, JavaSrcSearchQuery query, LuceneDb luceneDb, IDatabaseResultProgess progress, IConfigConstants configConstants) throws IOException {
		
		GenericFileListResult<IndexerResultEntry> result;
		
		List<String> pathResults = null;
		
		List<IndexerResultEntry> l = new ArrayList<IndexerResultEntry>();
		
		if(query.isWholeWorldOnly()) {
			
			// whole word
			
			if(query.isCaseSensitive()) {
				pathResults = LuceneSearchUtil.doPagingSearchNew(luceneDb, keyToSearch);
			} else {
				pathResults = LuceneSearchUtil.searchIndexWithWildcardQuery(luceneDb, keyToSearch);
			}
			
		} else {

			// not whole word
			if(query.isCaseSensitive()) {
				pathResults = LuceneSearchUtil.searchIndexWithWildcardQuery(luceneDb, "*"+keyToSearch+"*");
			} else {
				pathResults = LuceneSearchUtil.searchIndexWithWildcardQuery(luceneDb, "*"+keyToSearch+"*");
			}
			
		}
		
		Map<File, Boolean> fileListContains = new HashMap<File, Boolean>();
		
		int filesContainedIn = 0;
		for(String s : pathResults) {

			File f = new File(s);
			
			// Add files to the results (assuming it is not filtered out)
			AddFileResultUtil r = SearchIndexUIUtil.addFileToResults(f, query, fileListContains, l, filesContainedIn, configConstants);
			
			filesContainedIn = r._filesContainedIn;
			if(r._result != null) {
				// error occurred
				return r._result;
			}

		}
			

		Collections.sort(l);
		
		result = new GenericFileListResult<IndexerResultEntry>();
		result.setStatus(GenericFileListResult.GFLStatus.OK);
		result.setFileList(l);
		
		return result;
	}
	
}

