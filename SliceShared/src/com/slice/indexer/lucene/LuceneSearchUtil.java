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

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.document.Document;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;

/** Simple command-line based search demo. */
public class LuceneSearchUtil {
	
	public static List<String> doPagingSearchNew(LuceneDb luceneDb, String query) throws IOException {
		
		QueryParser parser = luceneDb.getParser();
		
		Query jgwQuery;
		
		try {
			
			jgwQuery = parser.parse(query);
		
			IndexSearcher searcher = luceneDb.getSearcher();
			
			int hitsPerPage = Integer.MAX_VALUE;
			
			TopDocs results = searcher.search(jgwQuery, hitsPerPage);
			ScoreDoc[] hits = results.scoreDocs;
	
			int numTotalHits = results.totalHits;
//			System.out.println(numTotalHits + " total matching documents");
			int start = 0;
			int end = Math.min(numTotalHits, hitsPerPage);
	
			end = Math.min(hits.length, start + hitsPerPage);
						
			return getResultsPath(searcher, hits, start, end);
			
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		return null;
		

	}
	
	private static List<String> getResultsPath(IndexSearcher searcher, ScoreDoc[] hits, int start, int end) throws IOException {

		List<String> results = new ArrayList<String>();
		
		for (int i = start; i < end; i++) {

			Document doc = searcher.doc(hits[i].doc);
			String path = doc.get("path");
			if (path != null) {
				results.add(path);
			} 

		}

		return results;
		
	}
		

	public static List<String> searchIndexWithWildcardQuery(LuceneDb luceneDb, String searchString) throws IOException {
		IndexSearcher indexSearcher = luceneDb.getSearcher();
		
		QueryParser parser = luceneDb.getParser();
		
		if(searchString.startsWith("*")) {
			parser.setAllowLeadingWildcard(true);
		}
		
		Query jgwQuery = null;
		try {
			jgwQuery = parser.parse(searchString);
		} catch (ParseException e) {
			e.printStackTrace();
			return null;
		}		
		
//		Term term = new Term("contents", searchString);
		
//		Query query = new WildcardQuery(term);
		
		TopDocs docs = indexSearcher.search(jgwQuery, Integer.MAX_VALUE);
		ScoreDoc[] hits = docs.scoreDocs;
		
//		System.out.println(hits.length + " total matching documents");
		
		return getResultsPath(indexSearcher, hits, 0, docs.totalHits);
		
	}
	
	
}