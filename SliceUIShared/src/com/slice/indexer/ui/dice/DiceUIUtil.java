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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.slice.indexer.constants.IConfigConstants;
import com.slice.indexer.shared.util.JavaSrcSearchQuery;

/** 
 * If a search query has multiple terms (eg {"service", "scalable"}), it can be useful to know which of the terms will
 * be more 'expensive' to search for. 
 * 
 * When the user has specified multiple terms, we only need to search for the least expensive term, and then refine
 * the results from that search to exclude files that do not contain the more expensive terms.
 * 
 * In order to estimate how 'expensive' a search term is, we scan through a small subset of the id list (10%) and 
 * identify which of the search terms occurs least frequently. 
 * 
 * Since this task is embarrassingly parallel and CPU-bound, we split this work over all available CPU cores.
 * 
 **/
public class DiceUIUtil {

	/** Returns null on timeout */
	public static List<QueryTerm> sortQueryTermsByEstimatedCompletionTime(JavaSrcSearchQuery query, List<String> idList, IConfigConstants configConstants) {
		System.out.println("-----------------------------------");
				
		int[] searchTermSize = new int[query.getSearchTerm().size()];
		
		for(int x  = 0; x < searchTermSize.length; x++) {
			searchTermSize[x] = 0;
		}

		String[] searchTerms = query.getSearchTerm().toArray(new String[query.getSearchTerm().size()]);
		
		final int NUM_TASKS = 10; // arbitrary number of tasks to break the work into

		final int CHECK_EVERY_NTH_ID = 10; // arbitrary, will check x/100 % of ids, where x is this value 
		
		List<IdCounterTask> tasks = new ArrayList<IdCounterTask>();
		for(int x = 0; x < NUM_TASKS; x++) {
			IdCounterTask idc = new IdCounterTask(searchTerms, idList, x, CHECK_EVERY_NTH_ID*NUM_TASKS, query);
			tasks.add(idc);
		}
	
		ExecutorService es = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
		
		boolean timedOut = false;
		try {
			for(IdCounterTask idc : tasks) {
				es.execute(idc);
			}
		} finally {
			es.shutdown();
			try {
				timedOut = !es.awaitTermination(240, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				timedOut = true;
			}
		}
	

		if(!timedOut) {
			for(IdCounterTask idc : tasks) {
				for(int x = 0 ; x < idc._seachTermHits.length; x++) {
					synchronized(idc._lock) {
						searchTermSize[x] += idc._seachTermHits[x];
					}
				}
			}
			
		} else {
			// Timed out on searching IDs, this really shouldn't happen
			return null;

		}
		
		// Convert the ID results to Query Terms and sort them in ascending order
		List<QueryTerm> l = new ArrayList<QueryTerm>();
		int x = 0;
		for(String str : query.getSearchTerm()) {
			System.out.println(str+" -> "+ searchTermSize[x]);
			l.add(new QueryTerm(str, searchTermSize[x]));
			x++;
		}
		Collections.sort(l);
				
		return l;
	}

	
	/** Contains a representation of the number of ids in the id list (count) that contained a 
	 * specific string (term). */
	public static class QueryTerm implements Comparable<QueryTerm> {
		
		public QueryTerm(String term, int count) {
			this.term = term;
			this.count = count;
		}
		
		String term;
		int count;
		
		@Override
		public int compareTo(QueryTerm o) {
			return this.count - o.count;
		}
	}

	/** Go through a portion of the IDs in the id list (for example, 10%) and count how many matches there are for each query term */
	private static class IdCounterTask implements Runnable {

		/** Lock on access to _searchTermHits */
		private final Object _lock = new Object();
		
		private final String[] _searchTerms;
		
		private int[] _seachTermHits = null;
		
		private final List<String> _idList;
		private final int _skipValue;
		private final int _startValue;
		
		private final JavaSrcSearchQuery _query;
		
		public IdCounterTask(String[] searchTerms, List<String> idList, int startValue, int skipValue, JavaSrcSearchQuery query) {
			
			_searchTerms = searchTerms;
			_idList = idList;
			
			_startValue = startValue;
			_skipValue = skipValue;
			
			_query = query;
			
			if(!query.isCaseSensitive()) {
				for(int x = 0; x <  _searchTerms.length; x++) {
					_searchTerms[x] = _searchTerms[x].toLowerCase();
				}
			}
		}
		
		@Override
		public void run() {

			// Synchronize on lock to ensure cache coherence
			synchronized(_lock) {
				_seachTermHits = new int[_searchTerms.length];
				
				// Go through a portion of the IDs in the id list and count how many matches there are for each query term
				
				for(int x = _startValue; x < _idList.size(); x+= _skipValue) {
					String str = _idList.get(x);
		
					if(!_query.isCaseSensitive()) {
						str = str.toLowerCase();
					}
		
					
					int pos = 0;
					for(String searchTerm : _searchTerms) {
						
						if(str.contains(searchTerm)) {
							_seachTermHits[pos]++;
						}
						
						pos++;
					}
		
					
				}
			}
			
		}
		
	}
}

