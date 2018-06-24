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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.slice.indexer.constants.IConfigConstants;
import com.slice.indexer.constants.ILuceneIndex;
import com.slice.indexer.generation.FSDatabasePathCompressorPrefetch.SupportedFiles;
import com.slice.indexer.generation.lucene.LuceneIndexer;
import com.slice.indexer.shared.IWritableDatabase2;
import com.slice.indexer.shared.Product;
import com.slice.indexer.shared.util.Logger;
import com.slice.indexer.shared.util.SearchIndexerUtil;

/** This is a standalone Java class that reads the product configuration XML file, and performs the required indexing 
 * based on the contents of that file. 
 * 
 * For example, if a product in the configuration file says that the Slice indexer or Lucene indexer should be used to index, 
 * then the appropriate indexer is automatically invoked on the appropriate product directory.
 * 
 * Likewise if the product in the configuration file indicates that the file/directory structure should be indexed, 
 * this class will invoke that indexer as well. */
public class StandaloneFullIndexerNew {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		Logger.getInstance().open();

		if(args.length != 1) {
			System.out.println("args:");
			System.out.println("- path to product config xml");
			return;
		}
		
		InputStream xsdIs = SearchIndexerUtil.class.getResourceAsStream("/META-INF/FileConfiguration.xsd");
				
		File pathToProductConfigXML = new File(args[0]);
		try {
			List<Product> products = SearchIndexerUtil.readProductConfigFile(new FileInputStream(pathToProductConfigXML), xsdIs);
			for(Product p : products) {
				
				// Index all products with Java indexer config data
				if(p.getConstants().getSearchIndex().getJavaSrcIndex() != null) {
					indexProduct(p.getConstants());
					
				} else if(p.getConstants().getSearchIndex().getLuceneIndex() != null) {
					indexProductLucene(p.getConstants());
					
				} else {
					System.err.println("ERROR: Unrecognized search index.");
				}
		
				// Run file indexer
				if(p.getConstants().getFileIndexerPath() != null) {
					indexFilePath(p.getConstants());
				}
				
			}
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	
	private static void indexFilePath(IConfigConstants constants) throws IOException {
		File directoryToRecurse = new File(constants.getPathToSourceDir()); 

		cleanIndexDirectory(directoryToRecurse);
		// Index file path
		if(constants.getFileIndexerPath() != null) {
			FilenameIndexerNew.createIndex(directoryToRecurse, new File(constants.getFileIndexerPath(), "filename-path-index.zip.idx"));				
		}		
		
	}
	
	private static void indexProductLucene(IConfigConstants constants) {
		
		ILuceneIndex luceneIndex = constants.getSearchIndex().getLuceneIndex();
		
		File dbPath = new File(luceneIndex.getLuceneDatabasePath());
		
		cleanIndexDirectory(dbPath);
		
		LuceneIndexer.index(constants.getPathToSourceDir(), dbPath.getPath(), constants);
		
	}
	
	private static void indexProduct(IConfigConstants constants) {
		try {
			
			File outputDir = new File(constants.getSearchIndex().getJavaSrcIndex().getJavaSrcFsDatabasePath());
			
			File log  = null;
			if(constants.getLogFileOutPath() != null) {
				log = new File(constants.getLogFileOutPath());	
			}
			
			
			cleanIndexDirectory(outputDir);
			
			if(!outputDir.exists()) {
				if(!outputDir.mkdirs()) {
					throw new RuntimeException("Output directory does not exist, and unable to create it");
				}
			}
			
			// Setup optional file logging
			FileOutputStream logFileOS = null;
			FileAndConsoleOutputStream josOut = null;
			FileAndConsoleOutputStream josErr = null;
			
			if(log != null) {
				logFileOS = new FileOutputStream(log);
				josOut = new FileAndConsoleOutputStream(logFileOS, System.out);
				josErr = new FileAndConsoleOutputStream(logFileOS, System.err);

				// Redirect the print stream
				PrintStream err = new PrintStream(josErr);
				PrintStream out = new PrintStream(josOut);
				
				System.setOut(out);
				System.setErr(err);
			}
			
			// Call tools
			
			System.out.println("StandaloneJavaSrcTextIndexer: ");			
			
			/*
			 * This creates in 'outputDir':
			 * text-to-id.db
			 * text-to-id.db.new
			 * id-list.db.new
			 * id-to-str-map-file.new
			 * id-to-files-map.bigstore
			 */
						
			IWritableDatabase2 dbInst = WritableFSDatabaseNew.generateFSDatabaseForWrite(constants, SupportedFiles.JAVA_SRC_SEARCH);
			JavaSrcTextIndexer.indexJavaSrc(constants, dbInst);
			dbInst = null;
			
			File bigStore = new File(outputDir, "id-to-files-map.bigstore");
			
			System.out.println("\r\n\r\nFSDatabasePostProcessCrunch:");
			
			/*
			 * This creates in 'outputDir'
			 * id-to-files-map.bigstore.crunched
			 */
			FSDatabasePostProcessCrunchNew.crunch(bigStore);
			
			File crunchedFile = new File(bigStore.getPath()+".crunched");
			
			String outDirPath = outputDir.getPath();
			
			System.out.println("\r\n\r\nStandaloneFSDatabasePathRandomAccessCompressor:");
			
			File flatFileOut = new File(outDirPath, "text-to-id.db.new.flat");
			File fposMap = new File(outDirPath, "text-to-id.db.new.fposmap");
			
			// Convert the 
			//   id-to-files-map.bigstore.crunched 
			// to
			//   text-to-id-db.new.flat
			StandaloneFSDatabasePathRandomAccessCompressorNew.compressNew(crunchedFile, flatFileOut, fposMap);
			
			crunchedFile.delete();
			bigStore.delete();

			
			if(log != null) {
				logFileOS.flush();
				logFileOS.close();
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
		Logger.getInstance().close();
		
	}


	private static void cleanIndexDirectory(File directory) {
		try {
			final String[] INDEX_FILES = {
					"deferredStrings.bin",
					"id-to-files-map.bigstore",
					"text-to-id.db.new",
					"filename-path-index.zip.idx",
					"text-to-id.db.new.flat",
					"text-to-id.db.new.fposmap"
			};
			
			if(!directory.exists()) {
				directory.mkdirs();
			}
			
			for(File f : directory.listFiles()) {
				String fname = f.getName();
	
				// Delete non-Lucene indices
				for(String indexFile : INDEX_FILES) {
					if(f.exists() && fname.contains(indexFile)) {
						f.delete();
					}
				}			
	
				if(f.exists()) {
					// Delete Lucene indices
					if(fname.startsWith("_") || fname.startsWith("segments")) {
						f.delete();
					}
					
				}
			}
			
		} catch(Exception e) {
			// First, do no harm
			e.printStackTrace();
		}
		
		
	}
	
	@SuppressWarnings("unused")
	@Deprecated
	/** Not currently used -- create a file that contains: for each ID, the number of files it's in */
	private static void createIdSizeList(File bigStore, File outputFile) throws IOException {
		FileReader fr = new FileReader(bigStore);
		BufferedReader br = new BufferedReader(fr);
		
		Map<Integer, Integer> count = new HashMap<Integer, Integer>();
		
		String str = null;
		while(null != (str = br.readLine())) {
			
			int hyphen = str.indexOf("-");
			if(hyphen != -1) {
				int id = Integer.parseInt(str.substring(0, hyphen));
				
				Integer existingCount = count.get(id);
				if(existingCount == null) {
					existingCount = 1;
				} else {
					existingCount++;
					if(existingCount > Integer.MAX_VALUE-4) {
						// Overflowing integer is theoretially possible here; so don't.
						existingCount = Integer.MAX_VALUE-4;
					}
				}
				count.put(id, existingCount);
			}
		}
		
		br.close();
		
		
		ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(outputFile));
		
//		FileWriter fw = new FileWriter(outputFile);
		Set<Entry<Integer, Integer>> s = count.entrySet();
		for(Entry<Integer, Integer> e : s) {
			oos.writeInt(e.getKey());
			oos.writeInt(e.getValue());
//			fw.write(e.getKey()+"-"+e.getValue()+"\n");
		}
		
		oos.close();
	}
}

/** An OutpuStream that will simultaneously log to two output streams, such as System.out and to a file. */
class FileAndConsoleOutputStream extends OutputStream {

	OutputStream _inner;
	OutputStream _consoleOut;
	
	public FileAndConsoleOutputStream(OutputStream inner, OutputStream consoleOut) {
		_inner = inner;
		_consoleOut = consoleOut;
	}
	
	@Override
	public void write(int b) throws IOException {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public void write(byte[] b) throws IOException {
		this.write(b, 0, b.length);
	}
	
	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		_inner.write(b, off, len);
		_consoleOut.write(b, off, len);
	}
	
}