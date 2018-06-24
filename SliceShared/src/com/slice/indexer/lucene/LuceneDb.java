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

package com.slice.indexer.lucene;

import java.io.File;
import java.io.IOException;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

/** Contains references to the internal Lucene objects used for reading Lucene results from disk.*/
public class LuceneDb {

	private IndexReader _reader;
	private IndexSearcher _searcher;
	private Analyzer _analyzer;
	
	private QueryParser _parser;
	
	public LuceneDb(File index) throws IOException {
		_reader = DirectoryReader.open(FSDirectory.open(index));
		_searcher = new IndexSearcher(_reader);
		_analyzer = new StandardAnalyzer(Version.LUCENE_45);
		
		_parser = new QueryParser(Version.LUCENE_45, "contents", _analyzer);

	}

	public IndexReader getReader() {
		return _reader;
	}

	public IndexSearcher getSearcher() {
		return _searcher;
	}

	public Analyzer getAnalyzer() {
		return _analyzer;
	}

	public QueryParser getParser() {
		return _parser;
	}
	
}

