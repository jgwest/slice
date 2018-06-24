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

package com.slice.indexer.dice;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.slice.indexer.shared.GenericFileListResult;
import com.slice.indexer.shared.util.Logger;

/** This class is not currently used, but is an in-memory of search index results, which can be read from and written to. */
public class InMemoryDatabase { 

	private File _dbFile;  
	
	public InMemoryDatabase(File f) {
		_dbFile = f;
	}
	
	Map<String /* text */, Integer /* id */> _stringMap = new TreeMap<String, Integer>();
//	Map<Integer, String> _idMap = new TreeMap<Integer, String>();
	
	Map<Integer /* id */, List<File> /* list */> _idToFileMap = new TreeMap<Integer, List<File>>();
	
	int _nextId = 0;
	
	private long _cacheHit = 0;
	private long _cacheTotal = 0;
	private long _totalBytes = 0;
	
	public int createOrGetId(String str, boolean ignoreCase) {
		if(!ignoreCase) {
			throw new UnsupportedOperationException("JAR search is case-insensitive only.");
		}
		
		Integer i = _stringMap.get(str.toLowerCase().trim());
		
		_cacheTotal++;
		
		if(i == null) {
			int newId = _nextId;
			_nextId++;
			
			_stringMap.put(str.toLowerCase().trim(), newId);
			
			Logger.getInstance().newId(newId, str);
			
			_totalBytes += str.length();
			
			if(newId % 10000 == 0) {
				System.out.println(newId + " ("+(float)_cacheHit/(float)_cacheTotal+")  ["+_totalBytes+"]");
			}
			return newId;
		} else {
			_cacheHit++;
			return i;
		}
	}

	public void addLink(int id, File f, int debug) {

		Logger.getInstance().inFile(id, f, debug);
		
		List<File> list = _idToFileMap.get(id);
		if(list == null) {
			list = new ArrayList<File>();
			_idToFileMap.put(id, list);
		}
		list.add(f);
		
	}

	@SuppressWarnings("unchecked")
	public void readDatabase() {
		ObjectInputStream is = null;
		try {
			if(!_dbFile.exists()) return;
			
			is = new ObjectInputStream(new FileInputStream(_dbFile));
			_stringMap = (Map<String, Integer>)is.readObject();
			
			_idToFileMap = (Map<Integer, List<File>>)is.readObject();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} finally {
			try { is.close(); } catch (Exception e) { /* ignore */}
		}
		
	}
	
	public void writeDatabase() {
		try {
			ObjectOutputStream os = new ObjectOutputStream(new FileOutputStream(_dbFile));
			os.writeObject(_stringMap);
			os.writeObject(_idToFileMap);
			os.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	public List<File> getFileList(int id) {
		return _idToFileMap.get(id);
	}
	
	public GenericFileListResult<File> getFileList(int id, long failAfterGivenTime) {
		GenericFileListResult<File> result = new GenericFileListResult<File>();
		result.setFileList(getFileList(id));
		
		return result;
	}

	public List<String> getIdList() {
		// Not implemented
		throw new UnsupportedOperationException();
	}

	public String decompressId(String string) {
		throw new UnsupportedOperationException();
	}

}
