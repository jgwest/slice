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

package com.slice.indexer.shared;

import java.util.List;

/** Contains a list of files of generic type, and a status. */
public class GenericFileListResult<T> {
	List<T> fileList;
	
	GFLStatus status;
	
	public enum GFLStatus { OK, TIMED_OUT, TOO_MANY_FILES}

	public List<T> getFileList() {
		return fileList;
	}

	public GFLStatus getStatus() {
		return status;
	}

	public void setStatus(GFLStatus status) {
		this.status = status;
	}
	
	
	public void setFileList(List<T> fileList) {
		this.fileList = fileList;
	}
	
}
