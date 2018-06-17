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

import java.util.List;

import com.slice.indexer.shared.util.JavaSrcSearchQuery;

/** 
 * This interface corresponds to an active query that is being performed by one the IQueryDatabase classes. The current
 * status of the query may be retrieved from getStatus().
 * 
 * Full results of the query will not be available until the status is COMPLETE_*.
 * 
 * Classes that implement this interface must be thread safe. 
 **/
public interface IQueryDatabaseResult {

	// If adding more of these, find all the references first; there are several
	public enum QDRStatus { INCOMPLETE, COMPLETE_SUCCESS, COMPLETE_ERROR};

	public QDRStatus getStatus();
	
	public String getErrorText();
	
	public String getUserText();

	public List<QueryResultEntry> getResults(int start, int end);
	
	public long getStartTime();
	
	public long getFinishedTime();
	
	public int getNumResultsAvailable();
	
	public JavaSrcSearchQuery getQuery();
	
	public void dispose();
	
	/** Dispose only the results of the query; leave the rest intact (note: this may change the status of the query)*/
	public void disposeResults();
}
