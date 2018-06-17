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


/** Processing of the database results if often split between multiple classes. This interface allows classes
 * to append status information to the overall database result process, before the results are returned to the user. 
 * 
 * A major consumer of this API is FileListProcessor. */
public interface IDatabaseResultProgess {
	
	public enum DatabaseResultProgressStatus { OK, TIMED_OUT, TOO_MANY_FILES, GENERIC_ERROR};
	
	// set status needed
	
	public void addErrorText(String str);

	public void addUserText(String str);
	
	public void addResultList(List<FileResultListEntry> list);
	
	public void setStatus(DatabaseResultProgressStatus status);
	
	public void setStartTime(long startTime);
	
	public void setFinishTime(long finishTime);
}
