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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** The SearchIndexDB representation of a query to the index database. */
public class DbQuery {

	// When changing these, search other areas of the code that use these exact names.
	public static enum QueryIdStatus { UNFINISHED, FINISHED_OK, FINISHED_TOO_MANY_FILES, FINISHED_TIMED_OUT}

	final private Map<Long /* matching index string id */, IdResult> _map = new HashMap<Long, IdResult>();
	
	final private DbProductUI _product;
	
	final private long _creationTimeInNanos;
	
	private String _userText = null;
	private String _errorText = null;
	
	public DbQuery(List<Long> ids, DbProductUI product) {
		
		_product = product;
		
		for(Long i : ids) {
			
			IdResult s = new IdResult();
			
			_map.put(i, s);
			
		}
		
		_creationTimeInNanos = System.nanoTime();
		
	}
	
	/** The list of files which contain a given index string id. */
	public static class IdResult {
		QueryIdStatus status = QueryIdStatus.UNFINISHED;
		List<File> result = new ArrayList<File>();
	}
	
	public Map<Long, IdResult> getMap() {
		return _map;
	}
	
	public DbProductUI getProduct() {
		return _product;
	}
	
	public long getCreationTimeInNanos() {
		return _creationTimeInNanos;
	}
	
	public String getErrorText() {
		return _errorText;
	}
	
	public void setErrorText(String errorText) {
		this._errorText = errorText;
	}
	
	public String getUserText() {
		return _userText;
	}
	
	public void setUserText(String userText) {
		this._userText = userText;
	}
}
