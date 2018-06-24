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

package com.slice.indexer.shared.util;

import java.util.List;

import com.slice.indexer.constants.ISearchFileType;

/** Java representation of an active search query */
public final class JavaSrcSearchQuery {

	private List<String> _searchTerm;
	
	private boolean _wholeWorldOnly;
	
	private boolean _caseSensitive;
	
	private String[] _pathExcludeFilterPatterns;
	
	private String[] _pathIncludeOnlyFilterPatterns;

	private String[] _pathIncludeComponentFilterPatterns;
		
	private List<ISearchFileType> searchFileTypes;

	// -----
	
	private String _keysForDisplayOnly;
	
	private String _searchTermUrl;
	
	int _queryId = -1;
		
	// --------------
	
	public JavaSrcSearchQuery() {
	}
	
	public String getSearchTermUrl() {
		return _searchTermUrl;
	}
	
	public void setSearchTermUrl(String searchTermUrl) {
		this._searchTermUrl = searchTermUrl;
	}
	
	public int getQueryId() {
		return _queryId;
	}
	
	public void setQueryId(int queryId) {
		this._queryId = queryId;
	}
	
	public String[] getPathIncludeOnlyFilterPatterns() {
		return _pathIncludeOnlyFilterPatterns;
	}
	
	public void setPathIncludeOnlyFilterPatterns(String[] pathIncludeOnlyFilterPatterns) {
		_pathIncludeOnlyFilterPatterns = pathIncludeOnlyFilterPatterns;
	}
	
	
	public String[] getPathExcludeFilterPatterns() {
		return _pathExcludeFilterPatterns;
	}

	public void setPathExcludeFilterPatterns(String[] pathFilterPatterns) {
		this._pathExcludeFilterPatterns = pathFilterPatterns;
	}

	public List<String> getSearchTerm() {
		return _searchTerm;
	}

	public void setSearchTerm(List<String> searchTerm) {
		this._searchTerm = searchTerm;
	}

	public boolean isWholeWorldOnly() {
		return _wholeWorldOnly;
	}

	public void setWholeWorldOnly(boolean wholeWorldOnly) {
		this._wholeWorldOnly = wholeWorldOnly;
	}

	public boolean isCaseSensitive() {
		return _caseSensitive;
	}

	public void setCaseSensitive(boolean caseSensitive) {
		this._caseSensitive = caseSensitive;
	}

	public void setPathIncludeComponentFilterPatterns(String[] pathIncludeComponentFilterPatterns) {
		this._pathIncludeComponentFilterPatterns = pathIncludeComponentFilterPatterns;
	}
	
	public String[] getPathIncludeComponentFilterPatterns() {
		return _pathIncludeComponentFilterPatterns;
	}
	
	public String getKeysForDisplayOnly() {
		return _keysForDisplayOnly;
	}
	
	public void setKeysForDisplayOnly(String keysForDisplayOnly) {
		this._keysForDisplayOnly = keysForDisplayOnly;
	}
	
	public void setSearchFileTypes(List<ISearchFileType> searchFileTypes) {
		this.searchFileTypes = searchFileTypes;
	}
	
	public List<ISearchFileType> getSearchFileTypes() {
		return searchFileTypes;
	}

}
