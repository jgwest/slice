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

package com.slice.indexer.ui;

import java.util.List;

/** 
 * This class is used by the UI to display search results to the user, specifically, a small snippet of text in the file that
 * matches the terms in the search query.  
 * 
 * The class contains:
 * - a list of strings that match the search query terms (based on the contents of the file)
 * - the full path to the file
 * - The URL to the fileview.jsp, which can be used to see the full file contents.
 * 
 * This class is part of the interface for IDatabaseResultProgress.
 **/
public class FileResultListEntry {

	/** Full path to file */
	private final String _path;
	
	/** 
	 * Relative URL to the file view, for example: /SliceRS/fileview.jsp?file=(path)&queryId=(query id)&resourceId=(productId)
	 **/
	private final String _fileUrl;
	
	/** List of snippets of text from matching files (in HTML) */
	private final List<FileMatchesEntry> _fileResultEntry;
	
	public FileResultListEntry(String path, String fileUrl, List<FileMatchesEntry> fileResultEntry) {
		this._path = path;
		this._fileUrl = fileUrl;
		this._fileResultEntry = fileResultEntry;
	}


	public String getPath() {
		return _path;
	}


	public String getFileUrl() {
		return _fileUrl;
	}


	public List<FileMatchesEntry> getFileResultEntry() {
		return _fileResultEntry;
	}


	/** Contains snippets of text from matching files (in HTML) */
	public static class FileMatchesEntry {
		
		public FileMatchesEntry(String content) {
			_content = content;
		}
		
		private final String _content;
		
		public String getContent() {
			return _content;
		}
	}
}


