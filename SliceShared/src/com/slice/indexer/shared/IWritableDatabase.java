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

/**
 * Indexing a file splits the file into tokens (a single instance of text
 * surround by whitespace):
 * 
 * Example:
 * 
 * <pre>
 * class MyClass {
 * 		String innerVar;
 * }
 * </pre>
 * contains the tokens: [class, MyClass, String, innerVar]
 * 
 * Tokens are assigned a unique integer (id), which are then associated with the
 * file they are contained in (a link).
 * 
 **/
public interface IWritableDatabase {

	public int createOrGetId(String str, boolean ignoreCase);

	public void addLink(int id, File f, int debug);

	public void writeDatabase();

}