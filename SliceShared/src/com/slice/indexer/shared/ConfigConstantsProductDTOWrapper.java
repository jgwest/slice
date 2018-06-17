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

import java.util.ArrayList;
import java.util.List;

import com.slice.datatypes.fileconfiguration.ComponentsDTO;
import com.slice.datatypes.fileconfiguration.ComponentsDTO.Component;
import com.slice.datatypes.fileconfiguration.MessagesDTO;
import com.slice.datatypes.fileconfiguration.ProductDTO;
import com.slice.datatypes.fileconfiguration.ProductDTO.AllowList;
import com.slice.datatypes.fileconfiguration.ProductDTO.SearchIndex;
import com.slice.datatypes.fileconfiguration.ProductDTO.SearchIndex.JavaSrc;
import com.slice.datatypes.fileconfiguration.ProductDTO.SearchIndex.Lucene;
import com.slice.indexer.constants.IAllowRule;
import com.slice.indexer.constants.IComponentPair;
import com.slice.indexer.constants.IConfigConstants;
import com.slice.indexer.constants.IJavaSrcIndex;
import com.slice.indexer.constants.ILuceneIndex;
import com.slice.indexer.constants.IProductMessages;
import com.slice.indexer.constants.ISearchFileType;
import com.slice.indexer.constants.ISearchIndex;
import com.slice.indexer.constants.IAllowRule.AllowRuleType;
import com.slice.indexer.shared.util.SearchIndexerUtil;
import com.slice.datatypes.fileconfiguration.SearchFileTypeDTO;

/** Wrap the Product XML representation and use it to provide config constants for a product */
public class ConfigConstantsProductDTOWrapper implements IConfigConstants {

	private final ProductDTO _productDTO;
	private final List<ISearchFileType> _searchFileTypes;
	private final List<String> _indexerIgnoreList;
	
	private final String _productId;
	private final String _cookieName;
	
	private final String _dbPresharedKey;
	
	private final List<IAllowRule> _allowList;
	
	private final List<IComponentPair> _components;
	
	
	public ConfigConstantsProductDTOWrapper(ProductDTO configDTO) {
		_productDTO = configDTO;

		/** Perform one-time calculations */
		
		// Create search file types
		List<ISearchFileType> searchFileTypes = new ArrayList<ISearchFileType>();
				
		int x = 0; 
		for(SearchFileTypeDTO d : _productDTO.getSearchFileTypes().getSearchFileType()) {
			
			searchFileTypes.add(new SearchFileTypeImpl(d, x));
			x++;
		}
		_searchFileTypes = searchFileTypes;
	
		// Create indexer ignore list
		_indexerIgnoreList = new ArrayList<String>();
		if(_productDTO.getIndexerIgnoreList() != null && _productDTO.getIndexerIgnoreList().getIgnore() != null) {
			for(String str : _productDTO.getIndexerIgnoreList().getIgnore()){
				str = SearchIndexerUtil.replaceEnvVar(str);
				_indexerIgnoreList.add(str.trim().toLowerCase());
			}
		}

		_productId = SearchIndexerUtil.replaceEnvVar(_productDTO.getId());
		_cookieName = SearchIndexerUtil.replaceEnvVar(_productDTO.getCookieName());
				
		// Allow List
		AllowList list = _productDTO.getAllowList();
		
		_allowList = new ArrayList<IAllowRule>();
		
		if(list != null) { 		
			for(String str : list.getAllow()) {
				_allowList.add(new AllowRuleImpl(AllowRuleType.ALLOW, str));
			}
			
		}

		// Components
		
		_components = new ArrayList<IComponentPair>();
		
		ComponentsDTO components = _productDTO.getComponents();
		if(components != null) {
			List<Component> innerList = components.getComponent();
			
			for(Component c : innerList) {
				_components.add(new ComponentPairImpl(c));
			}
		}
	
		_dbPresharedKey = SearchIndexerUtil.replaceEnvVar(_productDTO.getDatabasePresharedKey());
	}
	
	@Override
	public String getJarMemDatabaseFilePath() {
		return null;
	}

	@Override
	public String getJarFsDatabaseOutputPath() {
		return null;
	}

	@Override
	public String getJarPathToPluginsDir() {
		return null;
	}


	@Override
	public String getPathToSourceDir() {
		return SearchIndexerUtil.replaceEnvVar(_productDTO.getPathToSourceDir());
	}

	@Override
	public String getSearchComponentMapFile() {
		return SearchIndexerUtil.replaceEnvVar(_productDTO.getSearchComponentMapFile());
	}

	@Override
	public String getLogFileOutPath() {
		return SearchIndexerUtil.replaceEnvVar(_productDTO.getLogFileOutPath());
	}

	@Override
	public String getJarSearchWhoaCowboyFilesNumberText() {
		return null;
	}

	@Override
	public int getJarSearchWhoaCowboyFilesNumber() {
		return -1;
	}

	@Override
	public String getSearchWhoaCowboyFilesNumberText() {
		return SearchIndexerUtil.replaceEnvVar(_productDTO.getSearchWhoaCowboyFilesNumberText());
	}

	@Override
	public int getSearchWhoaCowboyFilesNumber() {
		return _productDTO.getSearchWhoaCowboyFilesNumber();
	}

	@Override
	public int getSearchWhoaCowboyQueryTimeoutInSecs() {
		return _productDTO.getSearchWhoaCowboyQueryTimeoutInSecs();
	}

	@Override
	public String[] getJarSearchSupportedExtensions() {
		return null;
	}

//	@Override
//	public String[] getJavaSearchSupportedExtensions() {
//		return _productDTO.getJavaSearchSupportedExtensions().split(" ");
//	}

	@Override
	public List<IComponentPair> getComponents() {
		return _components;
	}

	@Override
	public IProductMessages getProductMessages() {
		return new ProductMessagesImpl(_productDTO.getMessages());
	}

	@Override
	public List<IAllowRule> getAllowRules() {		
		return _allowList;
	}

	@Override
	public ISearchIndex getSearchIndex() {
		SearchIndex si = _productDTO.getSearchIndex();
		
		ISearchIndex result = null;
		
		if(si.getJavaSrc() != null) {
			result = new SearchIndexImpl(new JavaSrcIndexImpl(si.getJavaSrc()) );
			
		} else if(si.getLucene() != null) {
			result = new SearchIndexImpl(new LuceneIndexImpl(si.getLucene()) );
		}
		
		return result;
	}

	@Override
	public String getCookieName() {
		return _cookieName;
	}

	@Override
	public String getSearchAdminEmail() {
		return SearchIndexerUtil.replaceEnvVar(_productDTO.getSearchAdminEmail());
	}

	@Override
	public String getFileIndexerPath() {
		return SearchIndexerUtil.replaceEnvVar(_productDTO.getFileIndexerPath());
	}

	@Override
	public List<ISearchFileType> getSearchFileTypes() {
		return _searchFileTypes;
	}
	
	
	@Override
	public List<String> getIndexerIgnoreList() {
		return _indexerIgnoreList;
	}

	@Override
	public String getDatabasePresharedKey() {
		
		return _dbPresharedKey;
	}

	@Override
	public String getProductId() {
		return _productId;
	}
}

class SearchFileTypeImpl implements ISearchFileType {
	private final SearchFileTypeDTO _searchFileType;
	private final int _searchFileTypeId;
	
	private List<String> _supportedExtensionList;
	
	public SearchFileTypeImpl(SearchFileTypeDTO searchFileType, int searchFileTypeId) {
		_searchFileType = searchFileType;
		_searchFileTypeId = searchFileTypeId;
		
		List<String> result = new ArrayList<String>();
		for(String str : _searchFileType.getSupportedExtensionsList()) {
			String str2 = SearchIndexerUtil.replaceEnvVar(str.trim());
			str2 = (str2.startsWith(".")) ? str2 : "."+str2; // add a . in front
			str2 = str2.toLowerCase(); // to lower
			result.add(str2);
		}
		
		_supportedExtensionList = result;
		
	}
	
	@Override
	public String getSearchNameText() {
		return SearchIndexerUtil.replaceEnvVar(_searchFileType.getSearchNameText());
	}

	@Override
	public List<String> getSupportedExtensionsList() {
		return _supportedExtensionList;
	}

	@Override
	public int getFileTypeId() {
		return _searchFileTypeId;
	}

	@Override
	public boolean isSelectedByDefault() {
		Boolean result = _searchFileType.isSelectedByDefault();
		return result != null ? result.booleanValue() : false;
	}
}


class LuceneIndexImpl implements ILuceneIndex {

//	private final Lucene _lucene;
	
	private final String _luceneDatabasePath;
	
	public LuceneIndexImpl(Lucene lucene) {
		_luceneDatabasePath = SearchIndexerUtil.replaceEnvVar(lucene.getJavaSrcFsLuceneDatabasePath());
	}
	
	@Override
	public String getLuceneDatabasePath() {
		return _luceneDatabasePath;
	}
	
}

class JavaSrcIndexImpl implements IJavaSrcIndex {

	private final JavaSrc _javaSrc;
	
	private final String _javaSrcDbPath;
	
	public JavaSrcIndexImpl(JavaSrc javaSrc) {
		_javaSrc = javaSrc;
		
		_javaSrcDbPath =  SearchIndexerUtil.replaceEnvVar(_javaSrc.getJavaSrcFsDatabasePath());
		
	}
	
	@Override
	public String getJavaSrcFsDatabasePath() {
		return _javaSrcDbPath;
	}

	@Override
	public String getJavaSrcFsDatabaseUrl() {
		return _javaSrc.getJavaSrcFsDatabaseUrl();
	}
	
}

class SearchIndexImpl implements ISearchIndex {

	private final Type type;
	private final IJavaSrcIndex javaIndex;
	private final ILuceneIndex luceneIndex;
		
	public SearchIndexImpl(IJavaSrcIndex javaIndex) {
		this.type = ISearchIndex.Type.JAVA_SRC;
		this.javaIndex = javaIndex;
		this.luceneIndex = null;
	}
	
	public SearchIndexImpl(ILuceneIndex luceneIndex) {
		this.type = ISearchIndex.Type.LUCENE;
		this.luceneIndex = luceneIndex;
		this.javaIndex = null;
	}


	@Override
	public Type getType() {
		return type;
	}

	@Override
	public IJavaSrcIndex getJavaSrcIndex() {
		return javaIndex;
	}

	@Override
	public ILuceneIndex getLuceneIndex() {
		return luceneIndex;
	}
	
	
}


/** Corresponds to the <AllowList> element, see IAllowRule. */
class AllowRuleImpl implements IAllowRule {

	private final AllowRuleType _type;
	private final String _value;
	
	public AllowRuleImpl(AllowRuleType type, String value) {
		this._type = type;
		this._value = SearchIndexerUtil.replaceEnvVar(value);
	}

	@Override
	public AllowRuleType getType() {
		return _type;
	}

	@Override
	public String getValue() {
		return _value;
	}
	
}

/** Corresponds to product message in the file configuration XML, see IProductMessages. */
class ProductMessagesImpl implements IProductMessages {
	private final MessagesDTO _messages;
	
	public ProductMessagesImpl(MessagesDTO messages) {
		_messages = messages;
	}
	
	@Override
	public String getPageTitle() {
		return SearchIndexerUtil.replaceEnvVar(_messages.getPageTitle().trim());
	}

	@Override
	public String getTopTitle() {
		return SearchIndexerUtil.replaceEnvVar(_messages.getTopTitle().trim());
	}

	@Override
	public String getContentTitle() {
		return SearchIndexerUtil.replaceEnvVar(_messages.getContentTitle().trim());
	}

	@Override
	public String getContentDescription() {
		return SearchIndexerUtil.replaceEnvVar(_messages.getContentDescription().trim());
	}

	@Override
	public String getTextToSearchFor() {
		return SearchIndexerUtil.replaceEnvVar(_messages.getTextToSearchFor().trim());
	}

	@Override
	public String getSpecifyExclude() {
		return SearchIndexerUtil.replaceEnvVar(_messages.getSpecifyExclude().trim());
	}

	@Override
	public String getSpecifyOnlyInclude() {
		return SearchIndexerUtil.replaceEnvVar(_messages.getSpecifyOnlyInclude().trim());
	}
	
}

/** Corresponds to <Component> elements in the configuration XML, see IComponentPair*/
class ComponentPairImpl implements IComponentPair {

//	Component _component;
	
	private final String _name;
	private final String _path;
	
	private final String _strippedName;
	
	private final String _helperQueryName;
	
	public ComponentPairImpl(Component comp) {
		_name = SearchIndexerUtil.replaceEnvVar(comp.getName());
		_path = SearchIndexerUtil.replaceEnvVar(comp.getPath());
		_strippedName = strip(_name);
		_helperQueryName = calcHelperQueryName(_name);
	}
	
	@Override
	public String getName() {
		return _name;
	}

	@Override
	public String getPath() {
		return _path;
	}

	
	private static String strip(String str) {
		StringBuilder result = new StringBuilder();
		
		str = str.toLowerCase();
		
		for(int x = 0; x < str.length(); x++) {
			char ch = str.charAt(x);
			if(Character.isLetterOrDigit(ch)) {
				result.append(ch);
			} else {
				result.append("-");
			}
		}
		
		return result.toString();
	}
	
	@Override
	public String getStrippedName() {
		return _strippedName;
	}
	
	
	@Override
	public String getHelperQueryName() {
		return _helperQueryName;
	}
	
	private static String calcHelperQueryName(String str) {
		
		StringBuilder result = new StringBuilder();
		
		str = str.toLowerCase();
		
		boolean alphaNext = true;
		
		for(int x = 0; x < str.length(); x++) {
			char ch = str.charAt(x);
			if(alphaNext && Character.isLetter(ch)) {
				ch = Character.toUpperCase(ch);
				alphaNext = false;
			}
			
			if(Character.isLetterOrDigit(ch)) {
				result.append(ch);
			} else {
				alphaNext = true;
			}
		}
		
		return result.toString();

	}
}
