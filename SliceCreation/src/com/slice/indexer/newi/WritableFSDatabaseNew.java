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

package com.slice.indexer.newi;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.slice.indexer.constants.IConfigConstants;
import com.slice.indexer.dice.FSDatabase;
import com.slice.indexer.dice.KeyCompression;
import com.slice.indexer.generation.FSDatabasePathCompressor;
import com.slice.indexer.generation.FSDatabasePathCompressorPrefetch;
import com.slice.indexer.generation.FSDatabasePathCompressorPrefetch.SupportedFiles;
import com.slice.indexer.shared.IWritableDatabase2;
import com.slice.indexer.shared.util.Logger;


/**
 * Index generation:
 * StandaloneJavaSrcTextIndexer
 *  - FSDatabase
 *    o FSDatabasePathCompressorPrefetch
 *    o FSDatabasePathCompressor
 * FSDatabasePostProcessCrunch
 * StandaloneFSDatabasePathRandomAccessCompressor
 * 
 */


/*
 * id -> List<File>
 * 
 * Ex: e:\SearchIndexer\00\00\02\00\id-[id].txt which would contain the list of files for that id.
 * 
 * 1. Search the List of known text and find matches; produce a list of IDs for this text. 
 * 2. Using the id, locate the id-[id].txt file at the above path, and make a list of files that contain it.
 */
public class WritableFSDatabaseNew implements /*IWritableDatabase,*/ IWritableDatabase2 {

	private static final boolean USE_DEFERRED_STRING = true;
	
	DeferredStringFactory _defStringFactory;
	
	Map<IDeferredString /* index string token */, Integer /* index token id */> _defStringMap = new HashMap<IDeferredString, Integer>(100, 0.9f);
	
	Map<String /* index token */, Integer /* index token id */> _stringMap = new HashMap<String, Integer>(100, 0.9f);
	
	File _dirPath;
	
	int _nextId = 0; // Used only during write
	
	private long _cacheHit = 0;
	private long _cacheTotal = 0;
	private long _totalBytes = 0;

	FSDatabasePathCompressor _dbPathCompressor; // Used only during write
	
	
	List<String> _idList;
		
	FileWriter _linkWriter;
		
	
	public static WritableFSDatabaseNew generateFSDatabaseForWrite(IConfigConstants constants, SupportedFiles prefetchType) throws IOException {
		return new WritableFSDatabaseNew(constants, prefetchType);
	}
	
	private WritableFSDatabaseNew(IConfigConstants constants, SupportedFiles prefetchType) throws IOException {
		
		File dirPath = new File(constants.getSearchIndex().getJavaSrcIndex().getJavaSrcFsDatabasePath());
		File prefetchDir = new File(constants.getPathToSourceDir());
		
		File defStringFactoryFile = new File(dirPath.getPath()+File.separator+"deferredStrings.bin");	
		defStringFactoryFile.delete();
		_defStringFactory = new DeferredStringFactory(defStringFactoryFile);
		
		_dirPath = dirPath;
		
		Map<String, Long> map = FSDatabasePathCompressorPrefetch.generatePrefetchMap(prefetchDir, prefetchType, constants);
		_dbPathCompressor = new FSDatabasePathCompressor(map);
		
		if(!USE_DEFERRED_STRING) {
			_idList = new ArrayList<String>();
		}
		
		_linkWriter = new FileWriter(new File(dirPath.getPath()+File.separator+"id-to-files-map.bigstore"));
		
	}	

	
	

	private void writeIdList(ObjectOutputStream os) throws IOException {

		// Write the ids out of order to a temporary file

//		File tmpIdListFile   = new File(_dirPath.getPath()+File.separator+"temp-id-list");
//		FileWriter fw = new FileWriter(tmpIdListFile);
//		
		Set<Entry<IDeferredString, Integer>> s = _defStringMap.entrySet();
		
		int setSize = s.size();
		
		os.writeInt(setSize);
		
		long idsProcessed = 0; 
		for(Iterator<Entry<IDeferredString, Integer>> it = s.iterator(); it.hasNext(); ) {
			Entry<IDeferredString, Integer> e = it.next();
			os.writeObject(e.getKey().getValue());
			os.writeInt(e.getValue());
			os.reset();
			os.flush();
			idsProcessed++;
			if(idsProcessed % 100000 == 0) {
				System.out.println("Wrote "+idsProcessed+" ids.");
			}
//			fw.write(e.getKey().getValue()+"\n");
//			fw.write(e.getValue()+"\n");
		}

		s.clear();
		s = null;		
//		
//		// Read the contents of the file back into the correct order
//		_idList = new ArrayList<String>();
//		for(int x = 0; x < setSize; x++) {
//			_idList.add(null);
//		}
//		
//		BufferedReader br = new BufferedReader(new FileReader(tmpIdListFile));
//		String str;
//		while(null != (str = br.readLine())) {
//			long id = Long.parseLong(br.readLine());
//			_idList.set((int)id, str);
//		}
//		br.close();		
//		tmpIdListFile.delete();
	}
	
	@Override
	public void writeDatabase() {
		try {
			
			System.out.println("Writing database.");
			
			// text-to-id.db
			if(!USE_DEFERRED_STRING) {
				File keysFile = new File(_dirPath.getPath()+File.separator+"text-to-id.db");
				
				ObjectOutputStream os = new ObjectOutputStream(new FileOutputStream(keysFile));
				os.writeObject(_dbPathCompressor.getGeneratedDatabasePathDecompressor());
				os.writeObject(_stringMap);
				os.flush();
				os.close();
			}

			
			// text-to-id.db.new
			File keysFileNew = new File(_dirPath.getPath()+File.separator+"text-to-id.db.new");
			
			System.out.println("pre def string");
			ObjectOutputStream os = new ObjectOutputStream(new FileOutputStream(keysFileNew));
			os.writeObject(_dbPathCompressor.getGeneratedDatabasePathDecompressor());
			
			if(USE_DEFERRED_STRING) {
				// Write the id list out of order, so as not to consume any more heap space by sorting them
				writeIdList(os);
				
				// Allow the map to be GC-ed
				_defStringMap.clear();
				_defStringMap = null;
				System.gc();
				
			} else {
				os.writeObject(_idList);				
			}
			
			System.out.println("post def string");
			
			

			os.flush();
			os.close();
			
			_stringMap = null;
			_dbPathCompressor = null;
			
			if(FSDatabase.USE_COMPRESSED_IDs) {
				File compressedIdList = new File(_dirPath.getPath()+File.separator+"id-list.db.new");
				File idToStrMapFile = new File(_dirPath.getPath()+File.separator+"id-to-str-map-file.new");
				try {
					KeyCompression.createCompressedMapFile(compressedIdList, idToStrMapFile, _idList);
				} catch(Exception e) {
					e.printStackTrace();
					// Only any exception, nuke the files to make sure they aren't used.
					try { compressedIdList.delete(); } catch(Exception e1) {}
					try { idToStrMapFile.delete(); } catch(Exception e1) {}
				}
			}
			
			_idList = null;

			_linkWriter.close();
			
			if(USE_DEFERRED_STRING) {
				_defStringFactory.closeFactory();
				_defStringFactory = null;
			}
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
//	@Override
	public int createOrGetId(final String str, boolean ignoreCase) {
		Integer i;
		
		String key;
		
		if(ignoreCase) {
			key = str.toLowerCase().trim();
		} else {
			key = str.trim();
		}
		
		if(USE_DEFERRED_STRING) {
			UnmappedDeferredString ds = _defStringFactory.createUnmappedDeferredString(key);
			
			i = _defStringMap.get(ds);
		} else {
			i = _stringMap.get(key);
		}
		
		
		_cacheTotal++;
		
		if(i == null) {
		 	int newId = _nextId;
			_nextId++;
			
			if(USE_DEFERRED_STRING) {
				IDeferredString ds; 
				try {
					
					ds = _defStringFactory.createString(key);
					
				} catch (IOException e) { throw new RuntimeException(e); }
				
				_defStringMap.put(ds, newId);
				
			} else {
				_stringMap.put(key, newId);
				_idList.add(key);
			}
			
			
			Logger.getInstance().newId(newId, key);
			
			_totalBytes += key.length();
			
			if(newId % 10000 == 0) {
				System.out.println(newId + " ("+(float)_cacheHit/(float)_cacheTotal+")  ["+_totalBytes+"]");
			}
			return newId;
		} else {
			_cacheHit++;
			return i;
		}
	}

//	@Override
	public void addLink(int id, File f) {
		
		try {
			
			String pathToWrite = _dbPathCompressor.compress(f.getPath());

			_linkWriter.write(id+"-"+pathToWrite+"\n");
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

	@Override
	public void addBulkLinks(List<String> strings, boolean ignoreCase, File f, int debug /* unused*/) {
		
		for(String str : strings) {
			int id = createOrGetId(str, ignoreCase);
			addLink(id, f);
		}
		
	} 
	
}

/** Deferred strings are designed to reduce memory consumption at the minor expense of indexing time. Rather than
 * storing strings in memory, all string are stored in a single file; in memory an integer (or long) is stored
 * for each String, to indicate where in the file the String contents can be found.*/
class DeferredStringFactory {
	
	// HACK for memory reduction.
	public static DeferredStringFactory _currInstance;
	
	long _currPos = 0;
	
	File _sharedFilePath;
	RandomAccessFile _sharedFile;
	
	public DeferredStringFactory(File sharedFile) throws FileNotFoundException {
		_sharedFilePath = sharedFile;
		_sharedFile = new RandomAccessFile(sharedFile, "rw");
		_currInstance = this;
	}
	
	public IDeferredString createString(String str) throws IOException {
		if(str.length() <= 4) {
			return createUnmappedDeferredString(str);
		} else {
			return createDeferredString(str);
		}
	}
	
	/** Will not be mapped to a file, and the text will be fully resident */
	public UnmappedDeferredString createUnmappedDeferredString(String str) {
		return new UnmappedDeferredString(str);
	}
	
	public IDeferredString createDeferredString(String str) throws IOException {
		
		long positionInFile = _currPos;
		
		_sharedFile.seek(_currPos);
		
		byte[] strBytes = str.getBytes("UTF-8"); 
		
		_sharedFile.writeInt(strBytes.length); _currPos +=4;
		_sharedFile.write(strBytes); _currPos += strBytes.length;
	
		IDeferredString result;
		if(positionInFile > (Integer.MAX_VALUE-100) /** Above the max of a signed int, plus some leeway*/) {
			result = new DeferredStringLong(positionInFile);
		} else {
			result = new DeferredStringInt((int)positionInFile);
		}
		
		
		return result;
	}
	
	protected String getStringValue(long positionInFile) throws IOException {
		_sharedFile.seek(positionInFile);
		int bytesToRead = _sharedFile.readInt();
		byte[] barr = new byte[bytesToRead];
		
		int c = _sharedFile.read(barr, 0, barr.length);
		if(c != barr.length) {
			throw new IOException("Invalid number of bytes read");
		}
		
		String str = new String(barr, "UTF-8");
		return str;
		
	}
	
	public void closeFactory() throws IOException {
		_sharedFile.close();
		_sharedFilePath.delete();
	}
	
}

/** DeferredStrings of this class will not be written to file (ie are unmapped) */
class UnmappedDeferredString implements IDeferredString {
	
	// Unmapped strings only
	byte[] _strValue;


	public UnmappedDeferredString(String value) {
		try {
			_strValue = value.getBytes("UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public String getValue()  {
		try {
			return new String(_strValue, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		} 
		/*
		catch(ArrayIndexOutOfBoundsException e2) {
			
			
			throw e2;
		}*/
	}

	@Override
	public int hashCode() {
		return getValue().hashCode();
	}
	
	@Override
	public boolean equals(Object o) {
		try {
			return getValue().equals(((IDeferredString)o).getValue() );
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
}
/** 
 * This deferred string implementation points to a specific location in a local file where the actual string contents can be found. 
 * An int is used, so only 2.4GB of strings may be encode by this class (after which case DeferredStringLong should be used). 
 * 
 * Note that this implementation is intentionally NOT thread safe, to save memory. */
class DeferredStringInt implements IDeferredString {	
	
	int _positionInFile;
	
	public DeferredStringInt(int positionInFile) {
		_positionInFile = positionInFile;
	}

	
	@Override
	public boolean equals(Object o) {
		String strVal;
		try {
			strVal = getValue();
			
			String eqVal = ((IDeferredString)o).getValue();
			
			return strVal.equals(eqVal);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		
	}
	
	@Override
	public int hashCode() {
		try {
			return getValue().hashCode();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
		
	@Override
	public String getValue() throws IOException {
		return 	DeferredStringFactory._currInstance.getStringValue(_positionInFile);
	}

}

/** 
 * This deferred string implementation points to a specific location in a local file where the actual string contents can be found. A long
 * is used to allow for string files > 2.4GB. 
 * 
 * Note that this implementation is intentionally NOT thread safe, to save memory. */
class DeferredStringLong implements IDeferredString {	
	
	long _positionInFile;
	
	public DeferredStringLong(long positionInFile) {
		_positionInFile = positionInFile;
	}

	
	@Override
	public boolean equals(Object o) {
		String strVal;
		try {
			strVal = getValue();
			
			String eqVal = ((IDeferredString)o).getValue();
			
			return strVal.equals(eqVal);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		
	}
	
	@Override
	public int hashCode() {
		try {
			return getValue().hashCode();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
		
	@Override
	public String getValue() throws IOException {
		return 	DeferredStringFactory._currInstance.getStringValue(_positionInFile);
	}
}

/** No matter how a deferred string is stored, it will return the full uncompressed string when getValue() is invoked. */
interface IDeferredString {

	public String getValue() throws IOException;

}