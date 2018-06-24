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

package com.slice.indexer.rest;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.slice.indexer.constants.IConfigConstants;
import com.slice.indexer.shared.GenericFileListResult;
import com.slice.indexer.shared.GenericFileListResult2;
import com.slice.indexer.shared.util.JavaSrcSearchQuery;
import com.slice.indexer.shared.util.SearchIndexerUtil;
import com.slice.indexer.ui.FileResultListEntry;
import com.slice.indexer.ui.IDatabaseResultProgess;
import com.slice.indexer.ui.IndexerResultEntry;
import com.slice.indexer.ui.FileResultListEntry.FileMatchesEntry;
import com.slice.indexer.ui.IDatabaseResultProgess.DatabaseResultProgressStatus;
import com.slice.indexer.urlgen.GenerateInnerFileHtmlUtil;

/** 
 * Generate the search results for an in-progress search indexer query.
 * 
 * Input: 
 * - A list of files containing possible results
 * - The search query
 * 
 * Process:
 * - Scan through the list of files looking for files that match the query (skipping those that don't)
 * 
 * Output:
 * - Add results of file scanning to IDatabaseResultProgress
 **/
public class FileListProcessor {

	public static void process(/* in */ GenericFileListResult<IndexerResultEntry> fileList, 
			/* out */ IDatabaseResultProgess progress,
			String reqUrl, JavaSrcSearchQuery query, long startTime, IConfigConstants configConstants, 
			String productId) throws IOException {
		
		String key = "";
		
		for(String str : query.getSearchTerm()) {
			key += str +" ";
		}
		key = key.trim();

		long totalTime = System.currentTimeMillis() - startTime;

		double searchTime = (totalTime / 1000d);

		boolean partialResults = false;

		progress.addUserText("<br/>Search completed in: " + (searchTime) + " seconds.<br/>\n");

		// With the current algorithm, the search will have completed, but it's possible that no results will be returned for a while yet.
		// After some number of seconds, we should print a message indicating that the search is still in progress.
		long timeToWarnAt = System.nanoTime() + TimeUnit.NANOSECONDS.convert(5, TimeUnit.SECONDS);
		
		if (fileList.getFileList() != null) {

			if (fileList.getStatus() == GenericFileListResult.GFLStatus.TOO_MANY_FILES) {
				progress.addErrorText("<br/><br/>Whoa there cowboy... your full search result would return WAY too many files! These are the first "
						+ configConstants.getSearchWhoaCowboyFilesNumberText() + " results:<br/>\n");
				partialResults = true;
			}

			if (fileList.getStatus() == GenericFileListResult.GFLStatus.TIMED_OUT) {
				progress.addErrorText("<br/><br/>Whoa there cowboy... your full search timed out. These are the partial results:<br/>\n");
				partialResults = true;
			}

			if (query.getSearchTerm().size() == 1 && !partialResults) {
				// Only print number of matches with a single search term
				progress.addUserText("Files found: " + fileList.getFileList().size() + "<br/>\n");
				progress.addUserText("<br/>");
			} else {
				progress.addUserText("<br/>");
			}

			// Unlock the servlet lock as we are done using the database
			// _servletLock.unlock();
			// servReqState._servletLockUnlocked = true;

			// Generate results for all the files that match

			String javaSrcPathToSourceDir = configConstants.getPathToSourceDir();
			javaSrcPathToSourceDir = javaSrcPathToSourceDir.toLowerCase();

			javaSrcPathToSourceDir = javaSrcPathToSourceDir.replace("/", File.separator);
			
			int counterFilesScanned = 0;
			int counterFilesResults = 0;
			
			List<FileResultListEntry> resultList = new ArrayList<FileResultListEntry>();
			
			for (IndexerResultEntry e : fileList.getFileList()) {
				String file = e.getFile();
				
				counterFilesScanned++;

				// Generate results for the file; if there are none, then skip to the next file.
				String innerFileHtml = GenerateInnerFileHtmlUtil.generateInnerFileHTML(new File(e.getFile()), query, false);
				if (innerFileHtml == null) {
					if(timeToWarnAt != -1 && counterFilesScanned % 100 == 0 && System.nanoTime() > timeToWarnAt) {
						progress.addUserText("Please wait: The post-search file scanning is taking longer than expected. This occurs when the search space is large, but the search terms themselves occur rarely together.<br/>\n");
						progress.addUserText("<br/>");
						timeToWarnAt = -1;
					}
					continue;
				}

				if (file.toLowerCase().startsWith(javaSrcPathToSourceDir)) {
					file = file.substring(javaSrcPathToSourceDir.length());
				}

				FileMatchesEntry fre = new FileMatchesEntry(innerFileHtml);
				List<FileMatchesEntry> listFre = new ArrayList<FileMatchesEntry>();
				listFre.add(fre);

				FileResultListEntry frl;
				if (SearchIndexerUtil.allowPathAccess(configConstants, new File(file))) {
					String fileUrl = "/SliceRS/fileview.jsp?file="+file+
							(query.getQueryId() != -1 ? "&queryId="+query.getQueryId() : "")+"&resourceId="+productId;
					
					frl = new FileResultListEntry(file, fileUrl, listFre);
				} else {
					frl = new FileResultListEntry(file, null, listFre);
				}
				
				resultList.add(frl);

				counterFilesResults++;
				if (counterFilesResults % 100 == 0) {
					// Flush every 100 files or so.
					
					progress.addResultList(resultList);
					
					resultList = new ArrayList<FileResultListEntry>();
				}

			}
			
			if(resultList.size() > 0) {
				progress.addResultList(resultList);
			}

//			if (filesMatched > 0 && query.getSearchTerm().size() == 1) {
//				progress.setUrlForSearchResult(reqUrl);
//
//			}
			
			progress.setStatus(DatabaseResultProgressStatus.OK);
			

		} else {
			
			if(fileList.getStatus() == GenericFileListResult.GFLStatus.TOO_MANY_FILES) {
				
				progress.addErrorText("<br/><br/>Whoa there cowboy! Your search would return WAY too many results (>"+configConstants.getSearchWhoaCowboyFilesNumberText()+" classes)! <br/>Please try again, but use a tighter search (for instance, qualify classes by package, enable whole word only or enable case-sensitivity, etc).<br/>");
				
				progress.setStatus(DatabaseResultProgressStatus.TOO_MANY_FILES);
				
			} else if(fileList.getStatus() == GenericFileListResult.GFLStatus.TIMED_OUT) { 
				progress.addErrorText("<br/><br/>Whoa there cowboy... your search timed out (>"+configConstants.getSearchWhoaCowboyQueryTimeoutInSecs()+" seconds)!<br/>Please try again, but use a tighter search (for instance, qualify classes by package, enable whole word only or enable case-sensitivity, etc).<br/>");
			
				progress.setStatus(DatabaseResultProgressStatus.TIMED_OUT);				
				
			} else {
				progress.addErrorText("<br/>Generic Error. This shouldn't happen. Tell "+configConstants.getSearchAdminEmail()+".<br/>\n");
				progress.setStatus(DatabaseResultProgressStatus.GENERIC_ERROR);
			}
			
		}
		
	} // end thing

	
	public static void processNew(/* in */ GenericFileListResult2<IndexerResultEntry> fileList, 
			/* out */ IDatabaseResultProgess progress,
			String reqUrl, JavaSrcSearchQuery query, long startTime, IConfigConstants configConstants, 
			String productId) throws IOException {
		
		String key = "";
		
		for(String str : query.getSearchTerm()) {
			key += str +" ";
		}
		key = key.trim();
		

		long totalTime = System.currentTimeMillis() - startTime;

		double searchTime = (totalTime / 1000d);

		boolean partialResults = false;

		progress.addUserText("<br/>Search completed in: " + (searchTime) + " seconds.<br/>\n");

		// With the current algorithm, the search will have completed, but it's possible that no results will be returned for a while yet.
		// After some number of seconds, we should print a message indicating that the search is still in progress.
		long timeToWarnAt = System.nanoTime() + TimeUnit.NANOSECONDS.convert(5, TimeUnit.SECONDS);
		
		if (fileList.getFileList() != null) {

			if (fileList.getStatus() == GenericFileListResult2.GFLStatus.TOO_MANY_FILES) {
				progress.addErrorText("<br/><br/>Whoa there cowboy... your full search result would return WAY too many files! These are the first "
						+ configConstants.getSearchWhoaCowboyFilesNumberText() + " results:<br/>\n");
				partialResults = true;
			}

			if (fileList.getStatus() == GenericFileListResult2.GFLStatus.TIMED_OUT) {
				progress.addErrorText("<br/><br/>Whoa there cowboy... your full search timed out. These are the partial results:<br/>\n");
				partialResults = true;
			}

			if (query.getSearchTerm().size() == 1 && !partialResults) {
				// Only print number of matches with a single search term
				progress.addUserText("Files found: " + fileList.getFileList().size() + "<br/>\n");
				progress.addUserText("<br/>");
			} else {
				progress.addUserText("<br/>");
			}

			// Unlock the servlet lock as we are done using the database
			// _servletLock.unlock();
			// servReqState._servletLockUnlocked = true;

			// Generate results for all the files that match

			String javaSrcPathToSourceDir = configConstants.getPathToSourceDir();
			javaSrcPathToSourceDir = javaSrcPathToSourceDir.toLowerCase();

			javaSrcPathToSourceDir = javaSrcPathToSourceDir.replace("/", File.separator);
			
			int counterFilesScanned = 0;
			int counterFilesResults = 0;
			
			List<FileResultListEntry> resultList = new ArrayList<FileResultListEntry>();
			
			for (IndexerResultEntry e : fileList.getFileList()) {
				String file = e.getFile();
				
				counterFilesScanned++;

				// Generate results for the file; if there are none, then skip to the next file.
				String innerFileHtml = GenerateInnerFileHtmlUtil.generateInnerFileHTML(new File(e.getFile()), query, false);
				if (innerFileHtml == null) {
					if(timeToWarnAt != -1 && counterFilesScanned % 100 == 0 && System.nanoTime() > timeToWarnAt) {
						progress.addUserText("Please wait: The post-search file scanning is taking longer than expected. This occurs when the search space is large, but the search terms themselves occur rarely together.<br/>\n");
						progress.addUserText("<br/>");
						timeToWarnAt = -1;
					}
					continue;
				}

				if (file.toLowerCase().startsWith(javaSrcPathToSourceDir)) {
					file = file.substring(javaSrcPathToSourceDir.length());
				}

				FileMatchesEntry fre = new FileMatchesEntry(innerFileHtml);
				List<FileMatchesEntry> listFre = new ArrayList<FileMatchesEntry>();
				listFre.add(fre);

				FileResultListEntry frl;
				if (SearchIndexerUtil.allowPathAccess(configConstants, new File(file))) {
					String fileUrl = "/SliceRS/fileview.jsp?file="+file+
							(query.getQueryId() != -1 ? "&queryId="+query.getQueryId() : "")+"&resourceId="+productId;
					
					frl = new FileResultListEntry(file, fileUrl, listFre);
				} else {
					frl = new FileResultListEntry(file, null, listFre);
				}
				
				resultList.add(frl);

				counterFilesResults++;
				if (counterFilesResults % 100 == 0) {
					// Flush every 100 files or so.
					
					progress.addResultList(resultList);
					
					resultList = new ArrayList<FileResultListEntry>();
				}

			}
			
			if(resultList.size() > 0) {
				progress.addResultList(resultList);
			}

//			if (filesMatched > 0 && query.getSearchTerm().size() == 1) {
//				progress.setUrlForSearchResult(reqUrl);
//
//			}
			
			progress.setStatus(DatabaseResultProgressStatus.OK);
			

		} else {
			
			if(fileList.getStatus() == GenericFileListResult2.GFLStatus.TOO_MANY_FILES) {
				
				progress.addErrorText("<br/><br/>Whoa there cowboy! Your search would return WAY too many results (>"+configConstants.getSearchWhoaCowboyFilesNumberText()+" classes)! <br/>Please try again, but use a tighter search (for instance, qualify classes by package, enable whole word only or enable case-sensitivity, etc).<br/>");
				
				progress.setStatus(DatabaseResultProgressStatus.TOO_MANY_FILES);
				
			} else if(fileList.getStatus() == GenericFileListResult2.GFLStatus.TIMED_OUT) { 
				progress.addErrorText("<br/><br/>Whoa there cowboy... your search timed out (>"+configConstants.getSearchWhoaCowboyQueryTimeoutInSecs()+" seconds)!<br/>Please try again, but use a tighter search (for instance, qualify classes by package, enable whole word only or enable case-sensitivity, etc).<br/>");
			
				progress.setStatus(DatabaseResultProgressStatus.TIMED_OUT);				
				
			} else {
				progress.addErrorText("<br/>Generic Error. This shouldn't happen. Tell "+configConstants.getSearchAdminEmail()+".<br/>\n");
				progress.setStatus(DatabaseResultProgressStatus.GENERIC_ERROR);
			}
			
		}
		
	} // end thing

}
