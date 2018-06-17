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

package com.slice.searchindexdb.innerdb;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.slice.searchindexdb.NewQueryIdListDTO;
import com.slice.searchindexdb.ObjectFactory;
import com.slice.searchindexdb.QueryIDResultDTO;
import com.slice.searchindexdb.QueryIDResultDTO.QueryIdResultResultDTO;
import com.slice.searchindexdb.QueryIdStatusDTO;
import com.slice.searchindexdb.QueryIdStatusDTO.FileIdStatusDTO;
import com.slice.searchindexdb.innerdb.DbQuery.IdResult;
import com.slice.searchindexdb.innerdb.DbQuery.QueryIdStatus;
import com.slice.indexer.shared.GenericFileListResult2;
import com.slice.indexer.shared.GenericFileListResult2.GFLStatus;
import com.slice.indexer.shared.IDatabaseProgress;
import com.slice.indexer.shared.IReadableDatabase2;

/**
 * Manages a list of active queries, which are created by calling createQuery(...).
 * When a query is created, a new thread is started. The job of this thread is to query 
 * the corresponding database for the results of the query, and to return when the query 
 * has completed.
 * 
 * Thread safe. */
public class DbQuerySingleton {

	private static final DbQuerySingleton _instance = new DbQuerySingleton();
	
	private DbQuerySingleton() {
		
	}
	
	public static DbQuerySingleton getInstance() {
		return _instance;
	}
	
	// ---------------------
	
	private final Object _lock = new Object();
	
	private long _nextQueryId = 0; // locked by 'lock'
	
	/** locked by 'lock', individual Queries are locked on themselves */
	private Map<Long, DbQuery> _queryMap = new HashMap<Long, DbQuery>(); 
	
	
	public long createQuery(NewQueryIdListDTO queryReq, String productId) {
		
		DbProductUI product  = DbProductSingleton.getInstance().getProduct(productId);
		if(product == null) {
			return -1;
		}
		
		long id;
		
		synchronized(_lock) {
			
			id = _nextQueryId++;
		}
		
		DbQuery newQuery = new DbQuery(queryReq.getId(), product);
		
		Map<Long, IdResult> queryMap = newQuery.getMap();
		for(long l : queryReq.getId()) {
			
			IdResult curr = new IdResult();
			curr.result = new LinkedList<File>();
			curr.status = QueryIdStatus.UNFINISHED;
			
			queryMap.put(l, curr);
		}
		
		
		synchronized(_lock) {
			
			_queryMap.put(id,  newQuery);

			

		}
		QueryWorker qw = new QueryWorker(newQuery, queryReq.getFailAfterGivenTimeInMsecs());
		qw.start();
		
		return id;
		
	}
	
	
	public QueryIdStatusDTO createStatusDTO(long id, ObjectFactory of) {
		
		DbQuery query = null;
		synchronized(_lock) {
			
			query = _queryMap.get(id);
			if(query == null) {
				return null;
			}
		}
		
		
		QueryIdStatusDTO result = of.createQueryIdStatusDTO();
		List<QueryIdStatusDTO.FileIdStatusDTO> inner = result.getFileIdStatus();
		
		synchronized(query) {
			
			result.setErrorText(query.getErrorText());
			
			result.setUserText(query.getUserText());
			
			Map<Long, IdResult> innerMap = query.getMap();
			
			
			for(Map.Entry<Long, IdResult> e : innerMap.entrySet()) {
				
				FileIdStatusDTO currEntry = of.createQueryIdStatusDTOFileIdStatusDTO();
				currEntry.setId( e.getKey());
				currEntry.setStatus(e.getValue().status.name());
				inner.add(currEntry);
			}
		}
		
		return result;
	}
	
	
	public QueryIDResultDTO getResult(long queryId, List<Long> fileIds, ObjectFactory of) {
		
		QueryIDResultDTO result = of.createQueryIDResultDTO();
		
		DbQuery q;
		synchronized(_lock) {
			
			q = _queryMap.get(queryId);			
		}
		
		synchronized(q) {
			
			Map<Long, IdResult> resultMap = q.getMap();
			
			if(fileIds != null && fileIds.size() > 0) {
				for(long fileId : fileIds) {
					
					IdResult idResult = resultMap.get(fileId);
					if(idResult != null && idResult.status == QueryIdStatus.FINISHED_OK && idResult.result != null) {
												
						result.getResult().add(createFileResult(fileId, idResult.result, of));
					}
					
				}
			} else {
				
				for(Map.Entry<Long, IdResult> e : resultMap.entrySet()) {
					IdResult idResult = e.getValue();
					if(idResult.status == QueryIdStatus.FINISHED_OK && idResult.result != null) {
						
						result.getResult().add(createFileResult(e.getKey(), idResult.result, of));
					}
					
				}
			}
		} // end query synchronize
		
		return result;
		
	}
	
	public void deleteQuery(long id) {
		synchronized(_queryMap) {
			_queryMap.remove(id);
		}
	}
	
	
	private static QueryIdResultResultDTO createFileResult(long fileId, List<File> files, ObjectFactory of) {
		QueryIdResultResultDTO entry = of.createQueryIDResultDTOQueryIdResultResultDTO();
		entry.setId(fileId);
		List<String> fileStrList = entry.getPath();
		
		for(File file : files) {
			fileStrList.add(file.getPath());
		}
		
		return entry;
		
	}
}


/**
 * The job of this thread is to query the corresponding database for the results of the query, and to return when the query has completed.
 */
class QueryWorker extends Thread {

	/** Always synchronize on this before accessing it */
	private final DbQuery _query;
	
	/** will not be null */
	private final Long _failAfterGivenTimeInMsecs;
	
	public QueryWorker(DbQuery query, Long failAfterGivenTimeInMsecs) {
		this._query = query;
		this._failAfterGivenTimeInMsecs = failAfterGivenTimeInMsecs == null ? -1 : failAfterGivenTimeInMsecs;
	}
	
	@Override
	public void run() {

		LinkedList<Long> ids = new LinkedList<Long>();

		DbProductUI product;
		synchronized(_query)  {
			for(Map.Entry<Long, IdResult> e : _query.getMap().entrySet()) {
				ids.add(e.getKey());
			}
			
			product = _query.getProduct();
		}
		
		DatabaseProgressBridge bridge = new DatabaseProgressBridge(_query);
		
		for(Iterator<Long> it = ids.iterator(); it.hasNext(); ) {
			
			Long curr = it.next();
			
			try {
				product.getDatabase().acquireLock();
				
				IReadableDatabase2 db = product.getDatabase().getDbInst();
								
				List<GenericFileListResult2<File>> resultList = db.getFileList(Arrays.asList(new Long[] { curr}) , _failAfterGivenTimeInMsecs, bridge);

				// There will only be 1, as we are only requesting 1
				GenericFileListResult2<File> result = resultList.get(0);
				
				synchronized(_query) {

					IdResult idResult = _query.getMap().get(curr);
					
					if(result.getStatus() == GFLStatus.OK) {
						idResult.status = QueryIdStatus.FINISHED_OK;
						
						idResult.result.addAll(result.getFileList());
						
					} else if(result.getStatus() == GFLStatus.TIMED_OUT) {
						idResult.status = QueryIdStatus.FINISHED_TIMED_OUT;
						
					} else if(result.getStatus() == GFLStatus.TOO_MANY_FILES) {
						idResult.status = QueryIdStatus.FINISHED_TOO_MANY_FILES;
					}
					
				}
			
			} finally {
				product.getDatabase().releaseLock();
			}
			
		}
		
	}

	/** Receives error/info text and appends it to the DbQuery. */
	private static class DatabaseProgressBridge implements IDatabaseProgress {
		final DbQuery _query;
		
		public DatabaseProgressBridge(DbQuery query) {
			_query = query;
		}

		@Override
		public void addErrorText(String str) {
			synchronized(_query) {
				_query.setErrorText(_query.getErrorText()+str);
			}
		}

		@Override
		public void addUserText(String str) {
			// Ignore user text, we have this already in the caller.
			
//			synchronized(_query) {
//				_query.setUserText(_query.getUserText()+str);
//			}
//			
		} 
		
	}
}