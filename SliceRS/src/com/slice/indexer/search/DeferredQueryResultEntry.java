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

import com.slice.indexer.shared.util.DeferredStringFactoryFull.IDeferredStringFull;

/** 
 * Contains the contains of a FileResultListEntry, but rather than using standard Strings like a FRE does,
 * instead the contents of the String may instead be (transparently) stored on disk by the deferred string factory.
 * 
 * This helps reduce the amount of heap memory that is required for large queries, at the (slight) expense of disk IO.
 **/
public class DeferredQueryResultEntry {

	/** Full path to file */
	IDeferredStringFull _path;

	/** List of snippets of text from matching files (in HTML), converted to a single string. */
	IDeferredStringFull _content;
	
	/** 
	 * Relative URL to the file view, for example: /SliceRS/fileview.jsp?file=(path)&queryId=(query id)&resourceId=(productId)
	 **/
	IDeferredStringFull _fileUrl;

	public DeferredQueryResultEntry(IDeferredStringFull path, IDeferredStringFull content, IDeferredStringFull fileUrl) {
		this._path = path;
		this._content = content;
		this._fileUrl = fileUrl;
	}

	public IDeferredStringFull getPath() {
		return _path;
	}

	public IDeferredStringFull getContent() {
		return _content;
	}
	
	public IDeferredStringFull getFileUrl() {
		return _fileUrl;
	}
	
}
