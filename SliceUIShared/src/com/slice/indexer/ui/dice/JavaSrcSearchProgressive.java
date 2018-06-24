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

package com.slice.indexer.ui.dice;

import java.io.File;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.slice.indexer.constants.IConfigConstants;
import com.slice.indexer.shared.GenericFileListResult2;
import com.slice.indexer.shared.IDatabaseProgress;
import com.slice.indexer.shared.IReadableDatabase2;
import com.slice.indexer.shared.util.JavaSrcSearchQuery;
import com.slice.indexer.shared.util.SearchIndexerUtil;
import com.slice.indexer.ui.IDatabaseResultProgess;
import com.slice.indexer.ui.IndexerResultEntry;
import com.slice.indexer.ui.SearchIndexUIUtil;
import com.slice.indexer.ui.SearchIndexUIUtil.AddFileResultUtil2;
import com.slice.indexer.ui.dice.DiceUIUtil.QueryTerm;

/**
 * Initiate a search of the database using the provided search query, and spit results to IDatabaseResultProgress.  
 */
public class JavaSrcSearchProgressive { 
	
	public static GenericFileListResult2<IndexerResultEntry> search(JavaSrcSearchQuery query, IReadableDatabase2 dbInst, IDatabaseResultProgess progress, IConfigConstants configConstants) throws IOException {
		
		GenericFileListResult2<IndexerResultEntry> result = new GenericFileListResult2<IndexerResultEntry>();
		
		List<QueryTerm> l = DiceUIUtil.sortQueryTermsByEstimatedCompletionTime(query, dbInst.getIdList(), configConstants);
		
		if(l == null) {
			// sortQueryTermsByEstimatedCompletionTime returns a null on timeout
			result.setStatus(GenericFileListResult2.GFLStatus.TIMED_OUT);
			result.setFileList(null);
			return result;
		}
				
		long failAfterTimeInMsecs = -1;
		
		if(configConstants.getSearchWhoaCowboyQueryTimeoutInSecs() > 0) {
			failAfterTimeInMsecs = System.currentTimeMillis()+configConstants.getSearchWhoaCowboyQueryTimeoutInSecs() * 1000;
		}
		
		GenericFileListResult2<IndexerResultEntry> lastPartiallySuccessfulResult = null;
		boolean searchSucceeded = false;
		// Search the query term; stop when one succeeds
		for(int x = 0; x < l.size(); x++) {
			String term = l.get(x).term;
			
			System.out.println("Searching: "+term);
			
			result = searchInnerNew(term, query, dbInst, failAfterTimeInMsecs, progress, configConstants);
			
			if(result.getStatus() != GenericFileListResult2.GFLStatus.OK) {
				if(result.getFileList() != null && result.getFileList().size() > 0) {
					lastPartiallySuccessfulResult = result;
				}
 				continue;
			} else {
				searchSucceeded = true;
				break;
			}
		}
		
		if(!searchSucceeded) {

			if(lastPartiallySuccessfulResult != null) {
				result = lastPartiallySuccessfulResult;
			}
			
		}
		
		
		return result;
		
	}
	
	private static GenericFileListResult2<IndexerResultEntry> searchInnerNew(String keyToSearch, JavaSrcSearchQuery query, IReadableDatabase2 idbInst, long failAfterTimeInMsecs, IDatabaseResultProgess progress, IConfigConstants configConstants) throws IOException {
//		boolean newAlgorithm = FSDatabase.IS_FLAT_FILE_ALG;
		
		IReadableDatabase2 dbInst = idbInst;
		boolean compressedIds = dbInst.isCompressedIds(); // FSDatabase.USE_COMPRESSED_IDs;
		
		
		
		List<String> idList = dbInst.getIdList();
		
		GenericFileListResult2<IndexerResultEntry> result = new GenericFileListResult2<IndexerResultEntry>();
		
		List<IndexerResultEntry> listResult = new ArrayList<IndexerResultEntry>();
		
		String keyToSearchLower = keyToSearch.toLowerCase();
		
		// -------
				
		Map<File, Boolean> fileListContains = new HashMap<File, Boolean>();
		Map<Long, Boolean> handledIds = new HashMap<Long, Boolean>();
		
		
		DBProgressToDBResultProgress dbProgress = new DBProgressToDBResultProgress(progress);
		
		int filesContainedIn = 0;
		
		long entryNum = 0;
		
		int setSize; 
//			if(compressedIds) {
//				setSize = dbInst.getCompressedIdList().size();
//			} else {
				setSize = idList.size();
//			}

		List<Long> matchingIds = new LinkedList<Long>();
				
		// For each string in the index...
		for(int x = 0; x < setSize; x++) {
			
			String currListKey;
			Long currListKeyId;
			
			if(compressedIds) {
				 currListKey = dbInst.decompressId(idList.get(x));
			} else {
				currListKey = idList.get(x);
			}
			
			currListKeyId = entryNum;
						
			entryNum ++;
			
			boolean idMatch = queryKeyMatchesListKey(keyToSearch, keyToSearchLower, currListKey, query);
			
			
			// new query: list<long matching ids #>, failAfterTime
			
			// Find keys that match our search term
			if(idMatch) {
				
				Boolean handledId = handledIds.get(currListKeyId);
				
				// Have we used this ID before? If so, don't use it again. 
				if(handledId == null || handledId == false) {
					
					handledIds.put(currListKeyId, true);
					matchingIds.add(currListKeyId);				
				}
				
			}
			
		} // end for

		List<GenericFileListResult2<File>> resultList = idbInst.getFileList(matchingIds, failAfterTimeInMsecs, dbProgress);
		
		for(GenericFileListResult2<File> entry : resultList) {
			List<File> idFileList = entry.getFileList();
			
			if(idFileList == null || (failAfterTimeInMsecs > 0 && System.currentTimeMillis() - failAfterTimeInMsecs >= 0)) {
				// Took too long to complete this query.
				Collections.sort(listResult);

				result.setFileList(listResult);
				if(entry.getStatus() != null) {
					result.setStatus(entry.getStatus());
				} else {
					result.setStatus(GenericFileListResult2.GFLStatus.TIMED_OUT);
				}
				
				return result;
			}
			
			for(File idf : idFileList) {
				
				// Add files to the results (assuming it is not filtered out)
				AddFileResultUtil2 r = SearchIndexUIUtil.addFileToResultsNew(entry.getId(), idf, query, fileListContains, listResult, filesContainedIn, configConstants);
				
				filesContainedIn = r._filesContainedIn;
				if(r._result != null) {
					// if r._result is not null, it means we hit the 'too many files' errors and need to return what we have.
					return r._result;
				}

			}
			
		}
		
		if(dbProgress.isDotsPrinted()) {

			// Only report the number of matches found for a search term if it is the only search term (otherwise it can be confusing for user due to improved search algorithm we are using that skips "heavy" terms)
			if(listResult.size() > 500 && query.getSearchTerm().size() == 1) {
				String files = NumberFormat.getNumberInstance(Locale.US).format(listResult.size());
				progress.addUserText("&nbsp;&nbsp;&nbsp;&nbsp;( '"+keyToSearch+"' found in "+files+" files. )");
			}
			progress.addUserText("<br/>");
		}
		
		Collections.sort(listResult);

		result.setStatus(GenericFileListResult2.GFLStatus.OK);
		result.setFileList(listResult);
		
		return result;

	}

	private static boolean queryKeyMatchesListKey(String keyToSearch, String keyToSearchLower, String listKey, JavaSrcSearchQuery query) {
		
		boolean idMatch = false;
		
		if(query.isWholeWorldOnly()) {
			if(query.isCaseSensitive()) {
				if(listKey.contains(keyToSearch)) {
					
					idMatch = SearchIndexerUtil.containsWholeWordOnly(listKey, keyToSearch, query);
				}
			} else {
				
				if(listKey.toLowerCase().contains(keyToSearch.toLowerCase())) {
					
					idMatch = SearchIndexerUtil.containsWholeWordOnly(listKey, keyToSearch, query);
				}										
				
			}
			
		} else {
			if(query.isCaseSensitive()) {
				idMatch = listKey.contains(keyToSearch);
			} else {
				idMatch = listKey.toLowerCase().contains(keyToSearchLower);
			}
		 
		}
		
		return idMatch;
		
	}
	
	/* Deprecated */
	private static class DBProgressToDBResultProgress implements IDatabaseProgress {

		final IDatabaseResultProgess _drp;

		private boolean dotsPrinted = false;
		
		public DBProgressToDBResultProgress(IDatabaseResultProgess drp) {
			this._drp = drp;
		}

		@Override
		public void addErrorText(String str) {
			_drp.addErrorText(str);
			
		}

		@Override
		public void addUserText(String str) {
			_drp.addUserText(str);
			dotsPrinted = true;
		}
		
		public boolean isDotsPrinted() {
			return dotsPrinted;
		}
		
	}
	

}

