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

package com.slice.indexer.constants;

/** Corresponds to the <SearchIndex> elements of the XML configuration file. Will contain 
 * EITHER the Slice indexer, or a Lucene indexer (not both.) */
public interface ISearchIndex {

	enum Type {LUCENE, JAVA_SRC};
	
	public Type getType();
	
	public IJavaSrcIndex getJavaSrcIndex();
	
	public ILuceneIndex getLuceneIndex();
}
