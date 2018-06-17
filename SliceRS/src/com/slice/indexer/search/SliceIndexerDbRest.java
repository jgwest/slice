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

import java.util.List;

import com.slice.indexer.rest.IQueryDatabase;
import com.slice.indexer.rest.IQueryDatabaseResult;
import com.slice.indexer.rest.IQueryDatabaseResult.QDRStatus;
import com.slice.indexer.shared.util.JavaSrcSearchQuery;
import com.slice.indexer.ui.FileResultListEntry;
import com.slice.indexer.ui.IDatabaseResultProgess;
import com.slice.indexer.ui.ProductUI;

/** Query the database using a previously generated slice index, using a given query. This class is a work-in-progress. s*/
public class SliceIndexerDbRest implements IQueryDatabase {

	ProductUI _product;
	
	public SliceIndexerDbRest(ProductUI product) {
		_product = product;
	}
	
	@Override
	public IQueryDatabaseResult search(JavaSrcSearchQuery query) {
		DefStringQueryDatabaseResultImpl result = new DefStringQueryDatabaseResultImpl();
		result.setQuery(query);
		
		SliceIndexDbRsSearchTask searchTask = new SliceIndexDbRsSearchTask(query, result, _product);
		searchTask.start();
		
		return result;
		
	}

}

/** A separate thread which acquires the database lock, queries the index using the query, and returns the result. 
 * Work-in-progress. */
class SliceIndexDbRsSearchTask extends Thread implements IDatabaseResultProgess {

	DefStringQueryDatabaseResultImpl _result;
	JavaSrcSearchQuery _query;
	ProductUI _product;
	
	public SliceIndexDbRsSearchTask(JavaSrcSearchQuery query, DefStringQueryDatabaseResultImpl result, ProductUI product) {
		_result = result;
		_query = query;
		_product =product;
	}
	
	@Override
	public void run() {
		
		
//		DatabaseLock dbLock = _product.getDatabase();
//		IConfigConstants constants = _product.getProduct().getConstants();
//		
//		dbLock.acquireLock();
//
//		long startTime = -1;
//		GenericFileListResult<IndexerResultEntry> result = null;
//		try {
//			
//			long startDatabaseLoadTime = System.currentTimeMillis();			
//			dbLock.loadDBIfNeeded();
//			if(System.currentTimeMillis() - startDatabaseLoadTime > 5 * 1000) {
//				addUserText("One-time indexed database load time: "+((System.currentTimeMillis() - startDatabaseLoadTime)/1000d)+" seconds.<br/><br/>\n");
//			}
//
//			startTime = System.currentTimeMillis();
//			setStartTime(startTime);
//			
//			result = JavaSrcSearcherLuceneProgressive.search(dbLock.getLuceneDb(), _query, this, constants);
//			
//			setFinishTime(System.currentTimeMillis());
//			
//		} catch (Throwable e) {
//			e.printStackTrace();
//		} finally {
//			dbLock.releaseLock();
//		}
//
//		if(result != null) {
//
//			try {
//				String reqUrl = "";
//				
//				FileListProcessor.process(result, this, reqUrl, _query, startTime, constants, _product.getProduct().getProductId() );
//				
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//			
//		}
				
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
