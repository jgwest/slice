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

import java.io.File;
import java.util.List;

/**
 * Classes that implement this interface may be queried to determine which files contain which strings. 
 * 
 * To use an instance of this interface:
 * o Where TERM is a search term you are looking for:
 * - call readDatabase() to load the database
 * - call getIdList(), and look for strings in the list that contain TERM; the id of string is the position in the list.
 * 		o You may need to call decompressID() on each string in the id list
 * 		o isCompressedIds() will tell you if this is required.
 * - For each id you found above:
 * 		o Call getFileList to get the list of files that ID appears in 
 * 
 **/
public interface IReadableDatabase2 {

	/** Some databases might have initial setup; this setup will be performed when this method is called. 
	 * Setup is thus deferred until this method is first run. This method MUST be called first before calling
	 * the other methods, and should only be called once.*/
	public void readDatabase();
	
	/** Return list of index strings; position of index string in the list corresponds to the id. Strings may 
	 * need to be decompressed by passing them to decompressId(...)  */
	public List<String> getIdList();
	
	public List<GenericFileListResult2<File>> getFileList(List<Long> idList, long failAfterGivenTime, IDatabaseProgress progress);
	
	/** If needed, decompress the index string */
	public String decompressId(String string);
	
	/** Are the ids compressed? If so, we need to call decompressId before using them. */
	public boolean isCompressedIds();
}