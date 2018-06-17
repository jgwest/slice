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
 * This interface may be used to write search index results to a database.
 * 
 * A link is a relation between one or more strings, and a specific file that contains those strings.
 * - list of: 
 * 		string -> file that contains that string 
 *  
 **/
public interface IWritableDatabase2 {

	/**
	 * Add database links between tokens found a file, and the file path itself.  
	 * 
	 * @param strings A unique list of valid white-space separated tokens contained within a file
	 * @param f The file containing the tokens
	 * @param ignoreCase Whether to ignore the case of the tokens (always false?)	 * 
	 * @param debug Currently unused debug id
	 */
	public void addBulkLinks(List<String> strings, boolean ignoreCase, File f, int debug);		

	/** Once complete, write to the database. */
	public void writeDatabase();
}
