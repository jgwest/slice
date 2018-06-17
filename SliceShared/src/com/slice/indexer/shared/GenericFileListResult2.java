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

/** Contains a list of files of generic type, an ID, and a status. */
public class GenericFileListResult2<T> {
	List<T> fileList;
	
	/** key id*/
	long id = -1;
	
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
	
	public long getId() {
		return id;
	}
	
	public void setId(long id) {
		this.id = id;
	}
	
	
	
	public void copyFrom(long id, GenericFileListResult<T> result) {
		if(result.getFileList() != null) {
			fileList = result.getFileList();
		} else {
			fileList = null;
		}
		
		this.id = id;
		switch(result.getStatus()) {
		case OK:
			this.status = GFLStatus.OK;
			break;
		case TIMED_OUT:
			this.status = GFLStatus.TIMED_OUT;
			break;
		case TOO_MANY_FILES:
			this.status = GFLStatus.TOO_MANY_FILES;
			break;
		}
		
	}
}
