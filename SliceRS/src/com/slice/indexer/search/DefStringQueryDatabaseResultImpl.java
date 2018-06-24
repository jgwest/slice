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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.slice.indexer.rest.IQueryDatabaseResult;
import com.slice.indexer.rest.QueryResultEntry;
import com.slice.indexer.shared.util.DeferredStringFactoryFull;
import com.slice.indexer.shared.util.JavaSrcSearchQuery;
import com.slice.indexer.shared.util.DeferredStringFactoryFull.IDeferredStringFull;
import com.slice.indexer.ui.FileResultListEntry;
import com.slice.indexer.ui.FileResultListEntry.FileMatchesEntry;

/** This class contains the current results of an active database query. Indexers will add results by calling addResults(...),
 * and the UI may call the other getter methods to retrieve the current status.
 * 
 * Strings in this class are all deferred strings, to reduce memory usage.
 * 
 * This class is thread safe.
 **/
class DefStringQueryDatabaseResultImpl implements IQueryDatabaseResult {
	
	private final Object _lock = new Object();
	
	QDRStatus _currStatus = QDRStatus.INCOMPLETE;
	
	String _userText = null;
	
	String _errorText = null;
	
	long _startTime = -1;
	
	long _finishTime = -1;
	
	private final List<DeferredQueryResultEntry> _fullList = new ArrayList<DeferredQueryResultEntry>();

	DeferredStringFactoryFull _stringFactory; 
	
	JavaSrcSearchQuery _query;
	
	public DefStringQueryDatabaseResultImpl() {
		try {
			File tempFile = File.createTempFile("indexer", ".deferred-string");
			_stringFactory = new DeferredStringFactoryFull(tempFile);
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void setFinishTime(long finishTime) {
		this._finishTime = finishTime;
	}
	
	
	public void setQuery(JavaSrcSearchQuery query) {
		synchronized(_lock) {
			this._query = query;
		}
	}
	
	/** Convert FileResultListEntries to a DeferredQueryResultEntry, which is the same but with deferred strings.*/
	public void addResults(List<FileResultListEntry> list) {
		// see below for synchronized lock
		
		synchronized(_lock) {
			List<DeferredQueryResultEntry> listToAdd = new ArrayList<DeferredQueryResultEntry>();
			
			for(FileResultListEntry fre : list) {
				
				try {
					IDeferredStringFull filePath = _stringFactory.createString(fre.getPath());
					IDeferredStringFull fileUrl = _stringFactory.createString(fre.getFileUrl());
					
					IDeferredStringFull contentsStr;
					{
						StringBuilder contents = new StringBuilder();
						List<FileMatchesEntry> fl = fre.getFileResultEntry();
						
						for(FileMatchesEntry fme : fl) {
							contents.append(fme.getContent());
						}
						contentsStr = _stringFactory.createString(contents.toString());
					}

					DeferredQueryResultEntry qre = new DeferredQueryResultEntry(filePath, contentsStr, fileUrl);
					listToAdd.add(qre);
				} catch (IOException e) {
					e.printStackTrace();
				}

			}
			
			_fullList.addAll(listToAdd);
		}
	}

	/**
	 * Extract a subset of results from the result list, and convert them from deferred strings to proper strings. 
	 */
	@Override
	public List<QueryResultEntry> getResults(int start, int end) {
		synchronized(_lock) {
			List<QueryResultEntry> result = new ArrayList<QueryResultEntry>();
			for(int x = start; x < end+1; x++) {
				DeferredQueryResultEntry dqre = _fullList.get(x);
				QueryResultEntry qre;
				
				try {
					// file url is optional
					IDeferredStringFull fileUrl = dqre.getFileUrl();
					String fileUrlValue = fileUrl != null ? fileUrl.getValue() : null;
					
					qre = new QueryResultEntry(dqre.getPath().getValue(), dqre.getContent().getValue(), fileUrlValue);
					result.add(qre);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			return result;
		}
	}
	
	public void setStartTime(long startTime) {
		synchronized(_lock) {
			_startTime = startTime;
		}
	}
	
	public void addUserText(String str) {
		synchronized(_lock) {
			if(_userText == null) {
				_userText = "";
			}
			
			_userText += str;
		}
	}
	
	public void addErrorText(String str) {
		synchronized(_lock) {
			if(_errorText == null) {
				_errorText = "";
			}
			
			_errorText += str;
		}
	}

	
	public void setCurrStatus(QDRStatus currStatus) {
		synchronized(_lock) {
			_currStatus = currStatus;
		}
	}
	

	@Override
	public QDRStatus getStatus() {
		synchronized(_lock) {
			return _currStatus;
		}
	}

	@Override
	public long getStartTime() {
		synchronized(_lock) {
			return _startTime;
		}
	}

	@Override
	public int getNumResultsAvailable() {
		synchronized(_lock) {
			return _fullList.size();
		}
	}


	@Override
	public String getErrorText() {
		synchronized(_lock) {
			return _errorText;
		}
	}

	@Override
	public String getUserText() {
		synchronized(_lock) {
			return _userText;
		}
	}

	@Override
	public JavaSrcSearchQuery getQuery() {
		synchronized(_lock) {
			return _query;
		}
		
	}


	@Override
	public void dispose() {
		
		synchronized(_lock) {
			disposeResults();
		}
	}

	@Override
	public void disposeResults() {
		synchronized(_lock) {
			try {
				if(!_stringFactory.isClosed()) {
					_stringFactory.closeFactory();
				}
				_fullList.clear();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
	}


	@Override
	public long getFinishedTime() {
		synchronized(_lock) {
			return _finishTime;
		}
	}

	
}