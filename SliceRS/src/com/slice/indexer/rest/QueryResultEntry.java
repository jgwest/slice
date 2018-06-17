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

/** 
 * This class contains the result of querying a database, including:
 * - A path representing a file that matches a query
 * - HTML representation of snippets of text from the file that matches the query
 * - The full URL to view the contents of the file using file view.
 * 
 * Returned by a method of IQueryDatabaseResult. 
 **/
public class QueryResultEntry {

	/** Internal path of the matching file*/
	private final String _path;
	
	/** Matching file snippet containing the match */
	private final String _content;
	
	/** URL where the full file contents can be retrieved from. */
	private final String _fileUrl;

	public QueryResultEntry(String path, String content, String fileUrl) {
		this._path = path;
		this._content = content;
		this._fileUrl = fileUrl;
	}

	public String getPath() {
		return _path;
	}

	public String getContent() {
		return _content;
	}
	
	public String getFileUrl() {
		return _fileUrl;
	}
	
}
