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

package com.slice.indexer.dice;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.slice.searchindexdb.IdListTypeDTO;
import com.slice.searchindexdb.NewQueryIdListDTO;
import com.slice.searchindexdb.ObjectFactory;
import com.slice.searchindexdb.QueryIDResultDTO;
import com.slice.searchindexdb.QueryIDResultDTO.QueryIdResultResultDTO;
import com.slice.searchindexdb.QueryIdStatusDTO;
import com.slice.searchindexdb.QueryIdStatusDTO.FileIdStatusDTO;
import com.slice.indexer.constants.IConfigConstants;
import com.slice.indexer.restdb.KeyListServiceClient;
import com.slice.indexer.restdb.QueryServiceClient;
import com.slice.indexer.shared.GenericFileListResult2;
import com.slice.indexer.shared.IDatabaseProgress;
import com.slice.indexer.shared.IReadableDatabase2;

/** This class may be used to issue queries against a remote (or local) Slice REST Web service.  
 * 
 * This includes:
 * - Getting the id list from the server
 * - Getting a list of files that contain those specific IDs 
 **/
public class ReadableRestDbDatabase implements IReadableDatabase2 {

	private final IConfigConstants _configConstants; 
	
	private List<String> _keyList;

	private static enum QueryStatus {UNFINISHED, FINISHED_OK, FINISHED_TOO_MANY_FILES, FINISHED_TIMED_OUT};
	
	public ReadableRestDbDatabase(IConfigConstants configConstants) {
		_configConstants = configConstants;
		
	}

	@Override
	public void readDatabase() {
		_keyList = SharedKeyAndIdListDiceSingleton.getInstance().getUnmodifiableKeyList(_configConstants.getProductId());
		
		if(_keyList == null) {
			
			_keyList = getIdListFromServer(_configConstants);
						
			SharedKeyAndIdListDiceSingleton.getInstance().putUnmodifiableKeyList(_configConstants.getProductId(), _keyList);
			
		}
		
	}


	
	private static List<String> getIdListFromServer(IConfigConstants configConstants) {
		List<String> result = new ArrayList<String>();

		final long KEYS_TO_ACQUIRE_PER_REQUEST = 1000;

		KeyListServiceClient client = new KeyListServiceClient(configConstants.getSearchIndex().getJavaSrcIndex().getJavaSrcFsDatabaseUrl()+"/SliceDB/jaxrs/");
		long keyListSize = client.getIdListSize(configConstants);

		for (long x = 0; x < keyListSize; x++) {

			long currEnd = Math.min(keyListSize - 1, x + KEYS_TO_ACQUIRE_PER_REQUEST-1);
			IdListTypeDTO list = client.getIdList(configConstants, x, currEnd);

			result.addAll(list.getValue());

			x = currEnd;
		}

		return result;

	}

	@Override
	public List<String> getIdList() {
		return _keyList;
	}

	
	
	@Override
	public List<GenericFileListResult2<File>> getFileList(List<Long> idList, long failAfterGivenTimeInMsecs,
			IDatabaseProgress progress) {
		
		QueryServiceClient client = new QueryServiceClient(_configConstants.getSearchIndex().getJavaSrcIndex().getJavaSrcFsDatabaseUrl()+"/SliceDB/jaxrs/");
		
		ObjectFactory of = new ObjectFactory();
		NewQueryIdListDTO queryRequest = of.createNewQueryIdListDTO();
		
		for(Long i : idList) {
			queryRequest.getId().add((Long)(long)i);
		}
		
		
		queryRequest.setFailAfterGivenTimeInMsecs(failAfterGivenTimeInMsecs);
		
		if(idList.size() == 0) {
			return new ArrayList<>();
		}
		
		long queryId = client.createNewQuery(_configConstants, queryRequest);
		if(queryId == -1) {
			throw new RuntimeException("Query id of -1 returned from createNewQuery");
		}
		try {
			List<GenericFileListResult2<File>> result = monitorQueryAndExtractResult(idList, failAfterGivenTimeInMsecs, progress, queryId, client, _configConstants);
			
			return result;
		} finally {
			client.deleteQuery(_configConstants, queryId);
		}
	}
	
	private static List<GenericFileListResult2<File>> monitorQueryAndExtractResult(List<Long> idList, long failAfterGivenTimeInMsecs, IDatabaseProgress progress, long queryId, QueryServiceClient client, IConfigConstants product) {
		// UNFINISHED, FINISHED_OK, FINISHED_TOO_MANY_FILES, FINISHED_TIMED_OUT

		List<GenericFileListResult2<File>> result = new ArrayList<GenericFileListResult2<File>>();
		
		long firstFailId = -1;
		QueryStatus failStatus = null; 
		
//		int lastPercentReported = 0;
		
		int dotsPrinted = 0;
		
		boolean queryComplete = false;
		
		while(!queryComplete && System.currentTimeMillis() < failAfterGivenTimeInMsecs) {
		
			QueryIdStatusDTO queryStatus = client.getQueryStatus(product, queryId);	
			int finishedIds = 0;
			System.out.println("statii: "+queryStatus.getFileIdStatus().get(0).getStatus());
		
			for(FileIdStatusDTO status : queryStatus.getFileIdStatus()) {
				
				if(status.getStatus().equals(QueryStatus.FINISHED_OK.name())) {
					finishedIds++;
				}
				
				if(status.getStatus().equals(QueryStatus.FINISHED_TOO_MANY_FILES.name())) {
					failStatus = QueryStatus.FINISHED_TOO_MANY_FILES;
					firstFailId = status.getId();
				}
				
				if(status.getStatus().equals(QueryStatus.FINISHED_TIMED_OUT.name())) {
					failStatus = QueryStatus.FINISHED_TIMED_OUT;
					firstFailId = status.getId();
				}
				
				if(finishedIds == idList.size() || failStatus != null || System.currentTimeMillis() > failAfterGivenTimeInMsecs) {
					queryComplete = true;
					break;
				}
			}
			
			// Print another . after every 10 percent are complete
			int currPercentDone = (int)((double)finishedIds/(double)idList.size()*100);
			if(currPercentDone>= dotsPrinted*10+10) {
				
				int newDots = currPercentDone /10;
				
//				int dots = (currPercentDone- lastPercentReported) / 10;
				for(int x = dotsPrinted; x <= newDots; x++) {
					progress.addUserText(".");
				}
				dotsPrinted = newDots;
			}
			
			if(!queryComplete) {
				// Wait 100 msecs between queries to keep from overloading the db
				try { TimeUnit.MILLISECONDS.sleep(100); } catch (InterruptedException e) { throw new RuntimeException(e); }
			}
			
		} // end while
		
		

		if(failStatus == null) {
			// Query succeeded (it didn't time out or result in too many files)
			
			// Print the remaining dots
			for(int x = dotsPrinted; x <= 10; x++) {
				progress.addUserText(".");
			}
			
			// Get the final results of the query, and convert it into the format we want
			QueryIDResultDTO resultDto = client.invokeGetResults(product, queryId, null);
			
			for(QueryIdResultResultDTO resultDtoEntry : resultDto.getResult()) {
				GenericFileListResult2<File> entry = new GenericFileListResult2<File>();
				entry.setId((int)resultDtoEntry.getId());
				
				entry.setStatus(com.slice.indexer.shared.GenericFileListResult2.GFLStatus.OK);
				List<File> fileList = new ArrayList<File>();
				for(String filePath : resultDtoEntry.getPath()) {
					fileList.add(new File(filePath));
				}
				
				entry.setFileList(fileList);
				
				result.add(entry);
				
			}
			
			
		} else {
			// Query failed: it timed out or had too many files
			GenericFileListResult2<File> entry = new GenericFileListResult2<File>();
			entry.setId((int)firstFailId);
			result.add(entry);
			
			if(failStatus == QueryStatus.FINISHED_TIMED_OUT || System.currentTimeMillis() > failAfterGivenTimeInMsecs) {
				entry.setStatus(com.slice.indexer.shared.GenericFileListResult2.GFLStatus.TIMED_OUT);
			} else if(failStatus == QueryStatus.FINISHED_TOO_MANY_FILES) {
				entry.setStatus(com.slice.indexer.shared.GenericFileListResult2.GFLStatus.TOO_MANY_FILES);
			}
		}		
		
		
		return result;
		
	}

	@Override
	public String decompressId(String string) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isCompressedIds() {
		return false;
	}

}
