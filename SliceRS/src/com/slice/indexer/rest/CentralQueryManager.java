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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.slice.indexer.constants.ISearchIndex;
import com.slice.indexer.rest.IQueryDatabaseResult.QDRStatus;
import com.slice.indexer.search.SliceIndexerDatabase;
import com.slice.indexer.search.LuceneIndexerDatabase;
import com.slice.indexer.shared.util.JavaSrcSearchQuery;
import com.slice.indexer.ui.ProductUI;

/** 
 * One CentralQueryManager (CQM) exists for each Product. CQM manages the list of active/completed queries that have been issued by the user, for that product. 
 * Queries and query results will be removed from CQM after a fixed amount of time. 
 **/
public class CentralQueryManager {
	
	private static final long QUERY_RESULTS_EXPIRE_TIME_IN_MSECS = 10* 60 * 1000; // all query results are removed after 10 minutes 
	private static final long QUERY_EXPIRE_TIME_IN_MSECS = 24 * 60 * 60 * 1000; // all queries are removed after a day
			
	private static HashMap<ProductUI, CentralQueryManager> _productMap = new HashMap<ProductUI, CentralQueryManager>();
	
	// Instance variables
	
	private final Object _lock = new Object();
	
	private int _nextQueryId = 0;
	
	private final Map<Integer, IQueryDatabaseResult> _queryDb = new HashMap<Integer, IQueryDatabaseResult>(); 

	private IQueryDatabase _activeDatabase;
	
	private final CentralQueryManagerThread _innerThread;
	
	public static CentralQueryManager getInstance(ProductUI p) {
		synchronized(_productMap) {
			CentralQueryManager result = _productMap.get(p);
			if(result == null) {
				result = new CentralQueryManager(p);
				_productMap.put(p, result);
			}
			
			return result;
		}
	}
	
	private CentralQueryManager(ProductUI p) {
		
		ISearchIndex searchIndex = p.getProduct().getConstants().getSearchIndex(); 
		
		if(searchIndex.getType() == ISearchIndex.Type.JAVA_SRC) {
			_activeDatabase = new SliceIndexerDatabase(p);	
		} else if(searchIndex.getType() == ISearchIndex.Type.LUCENE) {
			_activeDatabase = new LuceneIndexerDatabase(p);	
		}
		
		_innerThread = new CentralQueryManagerThread(this);
		_innerThread.start();
	}
	
	public void setActiveDatabase(IQueryDatabase activeDatabase) {
		this._activeDatabase = activeDatabase;
	}
	
	public int newQuery(JavaSrcSearchQuery query) {
		IQueryDatabaseResult result = _activeDatabase.search(query);
		int queryId;
		
		synchronized(_lock) {
			queryId = _nextQueryId;
			_queryDb.put(queryId, result);
			_nextQueryId++;
		}
		
		
		return queryId;
	}
	
	IQueryDatabaseResult getQueryDatabaseResult(int queryId) {
		synchronized(_lock) {
			return _queryDb.get(queryId);
		}
	}
	
	
	/** Periodically remove expired entries */
	protected void cleanseOldEntries() {
				
		synchronized(_lock) {
			
			Set<Entry<Integer, IQueryDatabaseResult>> s = _queryDb.entrySet();
			
			long currTime = System.currentTimeMillis();
			
			for(Iterator<Entry<Integer, IQueryDatabaseResult>> it = s.iterator(); it.hasNext();) {
				Entry<Integer, IQueryDatabaseResult> e = it.next();
				IQueryDatabaseResult result = e.getValue();
				
				// If the entry has completed, and is more than X minutes old
				if(	(result.getStatus() == QDRStatus.COMPLETE_ERROR || result.getStatus() == QDRStatus.COMPLETE_SUCCESS)) {
					
					// Query results expire more quickly than the query itself, because query results 
					// are much more memory intensive.
					
					if(currTime > result.getFinishedTime()+QUERY_RESULTS_EXPIRE_TIME_IN_MSECS) {
						result.disposeResults();
					}
					
					if(currTime > result.getFinishedTime()+QUERY_EXPIRE_TIME_IN_MSECS) {
						result.dispose();
						it.remove();
						
					}
					
				}
				
			}
			
		}
		
	}
	
}

/** Every 30 seconds, inform the parent that it should consider cleansing old entries from the list.*/
class CentralQueryManagerThread extends Thread {

	CentralQueryManager _parent;
	boolean _isRunning = true;
	
	public CentralQueryManagerThread(CentralQueryManager parent) {
		super(CentralQueryManagerThread.class.getName());
		setDaemon(true);
		_parent = parent;
	}
	
	@Override
	public void run() {
		
		while(_isRunning) {
			
			_parent.cleanseOldEntries();
			
			if(_isRunning) {
				try { Thread.sleep(30 * 1000); } catch (InterruptedException e) { _isRunning = false; }
			}
		}
	}
}