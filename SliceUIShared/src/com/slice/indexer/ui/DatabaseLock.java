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

import java.io.File;
import java.io.IOException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.slice.indexer.constants.IConfigConstants;
import com.slice.indexer.constants.IJavaSrcIndex;
import com.slice.indexer.constants.ILuceneIndex;
import com.slice.indexer.constants.ISearchIndex.Type;
import com.slice.indexer.dice.ReadableFSDatabaseNew;
import com.slice.indexer.lucene.LuceneDb;
import com.slice.indexer.shared.IReadableDatabase2;

/** 
 * To acquire an instance of the database, first call acquireLock (ensure you releaseLock -- use a try/finally),
 * then acquire a reference to either the Lucene, component map, or search indexer db.  
 * 
 * 
 * Access to the database is mutually exclusive: only a single thread/request should access the database
 * at a time. 
 * 
 * This is especially necessary when the backing storage of the database is not an SSD, but rather a 
 * mechanical HD(and thus does not degrade gracefully under the standard random-read pattern of this workload. 
 *
 **/
public class DatabaseLock {
	private IReadableDatabase2 _dbInst = null; // protected by newLock
	
	private LuceneDb _luceneDb = null;
	
	private ComponentMap _componentMap = null;

	private Lock _lock = new ReentrantLock();
	
	private IConfigConstants _configConstants;

	public DatabaseLock(IConfigConstants configConstants) {
		_configConstants = configConstants;
	}
	
	/** Only call this method when the newLock is owned. (RequestDBInitServlet is current exception) 
	 * @throws IOException */
	public void loadDBIfNeeded() throws IOException {
		
		if(_configConstants.getSearchIndex().getType() == Type.JAVA_SRC) {
			if(_dbInst == null) {
				
				IJavaSrcIndex javaSrcIndex =_configConstants.getSearchIndex().getJavaSrcIndex(); 
				IReadableDatabase2 db = ReadableFSDatabaseNew.generateFSDatabaseForRead(new File(javaSrcIndex.getJavaSrcFsDatabasePath()));
//				IReadableDatabase2 db = new ReadableRestDbDatabase(_configConstants);
				
				System.out.println("Loading Java src database.");
				db.readDatabase();
				System.out.println("Database Java src loaded.");
				_dbInst = db;
			}		
			
		} else if(_configConstants.getSearchIndex().getType() == Type.LUCENE) { 
			if(_luceneDb == null) {
				
				ILuceneIndex luceneIndex = _configConstants.getSearchIndex().getLuceneIndex();
				
				try {
					System.out.println("Loading Lucene Db.");
					_luceneDb = new LuceneDb(new File(luceneIndex.getLuceneDatabasePath()));
					System.out.println("Lucene Db loaded.");
					
				} catch (IOException e) {
					e.printStackTrace();
				}
			} 
			
		}
		
		if(_componentMap == null && _configConstants.getSearchComponentMapFile() != null) {
			_componentMap = new ComponentMap(new File(_configConstants.getSearchComponentMapFile()));
		}
		
	}

	
	public void acquireLock() {
		_lock.lock();
	}
	
	
	public void releaseLock() {
		_lock.unlock();
	}
	
	public IReadableDatabase2 getDbInst() {
		return _dbInst;
	}
	
	public LuceneDb getLuceneDb() {
		return _luceneDb;
	}
	
	public ComponentMap getComponentMap() {
		return _componentMap;
	}
	
}
