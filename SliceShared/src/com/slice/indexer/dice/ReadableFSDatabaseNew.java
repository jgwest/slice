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

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.RandomAccessFile;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.slice.indexer.shared.GenericFileListResult;
import com.slice.indexer.shared.GenericFileListResult2;
import com.slice.indexer.shared.IDatabaseProgress;
import com.slice.indexer.shared.IReadableDatabase2;
import com.slice.indexer.shared.GenericFileListResult.GFLStatus;
import com.slice.indexer.shared.util.BaseConversionUtil;


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
public class ReadableFSDatabaseNew implements /*IReadableDatabase,*/ IReadableDatabase2 {
	 
//	Map<String /* index token */, Integer /* file id */> _stringMap = new HashMap<String, Integer>(100, 0.9f);
	
	File _dirPath;
	
	FSDatabasePathDecompressor _dbPathDecompressor; // Used only during read
	
	
	boolean _isCompressedIdList = FSDatabase.USE_COMPRESSED_IDs;
	
//	boolean _isFlatFileImpl = FSDatabase.IS_FLAT_FILE_ALG;
	List<String> _idList;
	MemIntToIntMap _idToFilePosMap;
	RandomAccessFile _flatIdData;
	
//	List<String> _compressedIdList;
	Map<Integer, String> _compressedIdMap;
	
//	File _idListSizeFile;
	
	
	public void guessAtSizeNew() {
		long size = 0;

		for(Map.Entry<Long, String> e : _dbPathDecompressor._databasePathDecompressor.entrySet()) {
			size += 8; // long
			size += e.getValue().getBytes().length + 4 /* pointer to string */;
		}
		
		for(String str : _idList) {
			size += str.getBytes().length + 4/* pointer to string*/;
		}
		
		size += _idToFilePosMap.getByteArraySize();
		
		System.out.println("Size guestimate: " +NumberFormat.getNumberInstance(Locale.US).format(size));
		
	}
	
	
	public static IReadableDatabase2 generateFSDatabaseForRead(File dirPath) {
		return new ReadableFSDatabaseNew(dirPath);
	}
	
			
	private ReadableFSDatabaseNew(File dirPath) {
		System.out.println("ReadableFSDatabaseNew instantiated.");
		_dirPath = dirPath;
	}
	
	private void readIdList(ObjectInputStream is) throws IOException, ClassNotFoundException {
		
		int numIds = is.readInt();
		
		_idList = new ArrayList<String>(numIds);
		
		for(int x = 0; x < numIds; x++) {
			_idList.add(null);
		}

		for(int x = 0; x < numIds; x++) {
			String str = (String)is.readObject();
			int idPos = is.readInt();
			_idList.set(idPos, str);
		}
		
		
		for(int x = 0; x < numIds; x++) {
			String str = _idList.get(x);
			if(str == null) {
				throw new RuntimeException("Missing strings in id list.");
			}
		}
		
	}
	
	
	@SuppressWarnings("unchecked")
	@Override
	public void readDatabase() {
		
		if(_dirPath != null && _dirPath.exists()) {
			
			ObjectInputStream is;
			try {
				
				File keysFile = new File(_dirPath, "text-to-id.db.new");
				
				if(!keysFile.exists()) {
					System.err.println("Error: New key file not found.");
					return;
				}
				
				is = new ObjectInputStream(new FileInputStream(keysFile));
				Map<Long, String> dbCompressorMap = (Map<Long, String>)is.readObject();
				
				_dbPathDecompressor = new FSDatabasePathDecompressor(dbCompressorMap);
//				_idList = (List<String>)is.readObject();
				
				// Read id list into memory
				readIdList(is);
				is.close();
				
				// Read [id -> file position] map into memory
				_idToFilePosMap = new MemIntToIntMap(new File(_dirPath, "text-to-id.db.new.fposmap"));
				
				_flatIdData = new RandomAccessFile(new File(_dirPath, "text-to-id.db.new.flat"), "r");	

				if(_isCompressedIdList) {
				
					File compressedIdList = new File(_dirPath, "id-list.db.new");
					File idToStrMapFile = new File(_dirPath, "id-to-str-map-file.new");
					
					if(compressedIdList.exists() && compressedIdList.length() > 0 && idToStrMapFile.exists() && idToStrMapFile.length() > 0) {
						
						System.out.println("* Loading compressedIdMap from: "+idToStrMapFile.getPath());
						
						_idList = KeyCompression.readCompressedIdList(compressedIdList);
						is = new ObjectInputStream(new FileInputStream(idToStrMapFile));
						_compressedIdMap = (Map<Integer, String>)is.readObject();
						is.close();
						System.out.println("* Compressed ID list and map are loaded.");
					} else {
						_isCompressedIdList = false;
					}
				}
				
				guessAtSizeNew();
				
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}				
			
		} else {
			System.err.println("Warning: Database not found.");
		}
	}
	
	@Override
	public List<String> getIdList() {
		return _idList;
	}

//	@Override
	public List<File> getFileList(int id) {
		return getFileList(id, -1).getFileList();
	}
	
	public GenericFileListResult<File> getFileList(long id, long failAfterGivenTime) {
		return getFileList(id, failAfterGivenTime, _dirPath);
	}
	
	private List<String> getFileContentsNew(long id) throws IOException {
		
		int pos = -1;
		pos = _idToFilePosMap.get( (int) id);
		
		_flatIdData.seek(pos);
		int length = _flatIdData.readInt();
		
		byte[] barr = new byte[length];
		int bytesRead = _flatIdData.read(barr);
		
		if(bytesRead != barr.length) {
			System.err.println("Could not read all data.");
			
		}
		
		List<String> result = new ArrayList<String>();
		String str;
		
		ByteArrayInputStream bais = new ByteArrayInputStream(barr);
		InputStreamReader isr = new InputStreamReader(bais);
		BufferedReader br = new BufferedReader(isr);
		while(null != (str = br.readLine())) {
			result.add(str);
		}
		br.close();
		
		barr = null;
		
		return result;

		
	}
	
	
	private GenericFileListResult<File> getFileList(long id, long failAfterGivenTime, File dirPath) {
		
		GenericFileListResult<File> result = new GenericFileListResult<File>();
		result.setStatus(GFLStatus.OK);
		
		List<File> listResult = new ArrayList<File>();
		
		try {
			List<String> fileContents;
			fileContents = getFileContentsNew(id);


			String str;
			int linesRead = 0;
			String lastLine = null;
			
			for(int pos = 0; pos < fileContents.size(); pos++) {
				
				str = fileContents.get(pos);
				
				String debugPreStr = str;
				
				str = decrunch(str, lastLine); // thread-safe in context only
				lastLine = str;
				
				str = _dbPathDecompressor.decompress(str); // thread-safe in context only
				if(str.contains("null")) {
					System.out.println("id:"+id+" >"+debugPreStr+" str:"+str+ " lastLine:"+lastLine);
				}
				
				File c = new File(str);
				listResult.add(c);
				linesRead++;
				
				if(linesRead % 1000 == 0) {
					if(failAfterGivenTime > 0 && System.currentTimeMillis() - failAfterGivenTime >= 0) {
						result.setStatus(GenericFileListResult.GFLStatus.TIMED_OUT);
						result.setFileList(null);
						
						return result;
					}
				}
				
			}
			
			fileContents = null;
		} catch(IOException e) {
			e.printStackTrace();
		} 
		result.setFileList(listResult);
		
		return result;

	}
	
	
	private static String decrunch(String input, String lastLineFull) {
		
		if(input.startsWith("*")) {
			return input.substring(1);
		}
		
		String firstChar = input.substring(0, 1);
		int l = (int)BaseConversionUtil.convertFromBase(firstChar, 62);
		String result = lastLineFull.substring(0, l+1);
		result += input.substring(1);
		return result;
	}

	
	@Override
	public String decompressId(String id) {
		return KeyCompression.decompressString(_compressedIdMap, id);
	}


	@Override
	public List<GenericFileListResult2<File>> getFileList(List<Long> keyIdList, long failAfterGivenTime, IDatabaseProgress progress) {
		
		
		int lastPercentReported = 0;
		
		List<GenericFileListResult2<File>> result = new ArrayList<GenericFileListResult2<File>>();
		
		int entryNum =0 ;
		for(Long keyId : keyIdList) {
			
			GenericFileListResult2<File> entry = new GenericFileListResult2<>();
			result.add(entry);
			
			entry.setId(keyId);
			
			GenericFileListResult<File> ret = getFileList(keyId, failAfterGivenTime);
			entry.copyFrom(keyId, ret);
			
			if(ret.getStatus() == GFLStatus.TIMED_OUT || System.currentTimeMillis() > failAfterGivenTime) {
				entry.setStatus(com.slice.indexer.shared.GenericFileListResult2.GFLStatus.TIMED_OUT);
				break;				
			} else if(ret.getStatus() == GFLStatus.TOO_MANY_FILES) {
				entry.setStatus(com.slice.indexer.shared.GenericFileListResult2.GFLStatus.TOO_MANY_FILES);
				break;
			}
			
			int currPercentDone = (int)((double)entryNum/(double)keyIdList.size()*100);
			
			
			if(currPercentDone>= lastPercentReported+10) {
				lastPercentReported = currPercentDone;
				if(progress != null) {
					progress.addUserText(".");
				}
			}

			entryNum++;
		}
		
		return result;
	}


	@Override
	public boolean isCompressedIds() {
		return FSDatabase.USE_COMPRESSED_IDs;
	}

}
