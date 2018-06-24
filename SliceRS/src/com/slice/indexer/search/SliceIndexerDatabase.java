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

package com.slice.indexer.search;

import java.io.IOException;
import java.util.List;

import javax.enterprise.concurrent.ManagedThreadFactory;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import com.slice.indexer.constants.IConfigConstants;
import com.slice.indexer.rest.FileListProcessor;
import com.slice.indexer.rest.IQueryDatabase;
import com.slice.indexer.rest.IQueryDatabaseResult;
import com.slice.indexer.rest.IQueryDatabaseResult.QDRStatus;
import com.slice.indexer.shared.GenericFileListResult2;
import com.slice.indexer.shared.IReadableDatabase2;
import com.slice.indexer.shared.util.JavaSrcSearchQuery;
import com.slice.indexer.ui.DatabaseLock;
import com.slice.indexer.ui.FileResultListEntry;
import com.slice.indexer.ui.IDatabaseResultProgess;
import com.slice.indexer.ui.IndexerResultEntry;
import com.slice.indexer.ui.ProductUI;
import com.slice.indexer.ui.dice.JavaSrcSearchProgressive;


/** The primary (non-Lucene) implements of IQueryDatabase. This class starts a new thread to handle
 * querying the appropriate indexer, and returns a non-blocking object that will populate with the
 * query result.  
 * 
 * Only a single instance of this class will exist per product. */
public class SliceIndexerDatabase implements IQueryDatabase {

	ProductUI _product;
	
	public SliceIndexerDatabase(ProductUI product) {
		_product = product;
	}
	
	@Override
	public IQueryDatabaseResult search(JavaSrcSearchQuery query) {
		DefStringQueryDatabaseResultImpl result = new DefStringQueryDatabaseResultImpl();
		result.setQuery(query);
		
		final SliceIndexerSearchTask searchTask = new SliceIndexerSearchTask(query, result, _product);
//		searchTask.start();

		ManagedThreadFactory threadFactory = null;
		try {
			threadFactory = (ManagedThreadFactory) new InitialContext().lookup("java:comp/DefaultManagedThreadFactory");
			
		} catch (NamingException e) {
			throw new RuntimeException(e);
		}
		
		
		Thread t = threadFactory.newThread(new Runnable() {
			@Override
			public void run() {
				searchTask.run();
			}
			
		} );
		
		t.start();
		
		return result;
	}
}


/** A separate thread which acquires the database lock, queries the index using the query, and returns the result. */
class SliceIndexerSearchTask extends Thread implements IDatabaseResultProgess {
	
	private final DefStringQueryDatabaseResultImpl _result;
	private final JavaSrcSearchQuery _query;
	private final ProductUI _product;
	
	public SliceIndexerSearchTask(JavaSrcSearchQuery query, DefStringQueryDatabaseResultImpl result, ProductUI product) {
		_result = result;
		_query = query;
		_product = product;
	}
	
	@Override
	public void run() {
		
		DatabaseLock dbLock = _product.getDatabase();
		IConfigConstants constants = _product.getProduct().getConstants();
		
		dbLock.acquireLock();

		long startTime = -1;
		GenericFileListResult2<IndexerResultEntry> result = null;
		try {
			
			long startDatabaseLoadTime = System.currentTimeMillis();			
			dbLock.loadDBIfNeeded();
			if(System.currentTimeMillis() - startDatabaseLoadTime > 5 * 1000) {
				addUserText("One-time indexed database load time: "+((System.currentTimeMillis() - startDatabaseLoadTime)/1000d)+" seconds.<br/><br/>\n");
			}

			startTime = System.currentTimeMillis();
			setStartTime(startTime);
//			result = JavaSrcSearcherProgressiveNew.search(_query, dbLock.getDbInst(), this, constants);
			result = JavaSrcSearchProgressive.search(_query, (IReadableDatabase2)dbLock.getDbInst(), this, constants);

			setFinishTime(System.currentTimeMillis());
			
		} catch (Throwable e) {
			e.printStackTrace();
		} finally {
			dbLock.releaseLock();
		}

		if(result != null) {

			try {
				String reqUrl = "";
				
				FileListProcessor.processNew(result, this, reqUrl, _query, startTime, constants, _product.getProduct().getProductId() );
				
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
				
	}

	@Override
	public void addErrorText(String str) {
		_result.addErrorText(str);
	}

	@Override
	public void addUserText(String str) {
		_result.addUserText(str);
	}

	@Override
	public void addResultList(List<FileResultListEntry> list) {
		_result.addResults(list);
	}

	@Override
	public void setStatus(DatabaseResultProgressStatus status) {
		
		if(status == DatabaseResultProgressStatus.OK) {
			_result.setCurrStatus(QDRStatus.COMPLETE_SUCCESS);
		} else if(status == DatabaseResultProgressStatus.TIMED_OUT) {
			_result.setCurrStatus(QDRStatus.COMPLETE_ERROR);
		} else if(status == DatabaseResultProgressStatus.TOO_MANY_FILES) {
			_result.setCurrStatus(QDRStatus.COMPLETE_ERROR);
		} else if(status == DatabaseResultProgressStatus.GENERIC_ERROR) {
			_result.setCurrStatus(QDRStatus.COMPLETE_ERROR);
		}
				
	}

	@Override
	public void setStartTime(long startTime) {
		_result.setStartTime(startTime);
		
	}

	@Override
	public void setFinishTime(long finishTime) {
		_result.setFinishTime(finishTime);
	}
	
}