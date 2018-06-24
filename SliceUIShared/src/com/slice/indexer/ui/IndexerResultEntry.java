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

import com.slice.indexer.constants.IConfigConstants;

/** Contains a list of files that matched a search query.
 * 
 * This class will naturally sort according to:
 * - First, stream order
 * - The file path (case insensitive) 
 * 
 **/
public class IndexerResultEntry implements Comparable<IndexerResultEntry> {
	private String _file;

	/** Stream refers to the component under which a file is located */
	private String _stream;
	
	private IConfigConstants _constants;
	
	public IndexerResultEntry(String file, String stream, IConfigConstants constants)  {
		_file = file;
		_stream = stream;
		_constants = constants;
	}

	public String getFile() {
		return _file;
	}
	
	public void setFile(String _file) {
		this._file = _file;
	}

	@Override
	public int compareTo(IndexerResultEntry o) {

		int result;
		
		String stream1 = o._stream;
		String stream2 = _stream;
		
		if(stream1 ==  null || stream2 == null) {
			
			if(stream1 == stream2) { return 0; }
			if(stream1 != null) { return 1; }
			if(stream2 != null) { return -1; }
			
		}
		
		result = StreamUtil.compareStreamOrder(_stream, o._stream, _constants);
		
		if(result == 0) {
			result = _file.toLowerCase().compareTo(o._file.toLowerCase()); 
		}
		
		
		return result;
		
	}
	
}
