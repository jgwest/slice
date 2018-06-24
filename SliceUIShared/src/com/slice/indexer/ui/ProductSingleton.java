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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.slice.indexer.shared.Product;
import com.slice.indexer.shared.util.SearchIndexerUtil;

/** 
 * Access to all products (that are contained in the configuration file) can be retrieved through via class, using
 * the getProduct(...) and getProductList() methods.   
 *  
 * Only a single instance of this class will ever exist, and the product file will be parsed/loaded by the class on 
 * first access.
 *
 * Since consecutive calls to this class tend to be requesting the same product id as the previous request,
 * results of the getProduct() request are cached to avoid repeated map look-ups. 
 **/
public class ProductSingleton {

	// Static 
	private static final ProductSingleton _instance = new ProductSingleton();
	
	public static ProductSingleton getInstance() {
		synchronized(_instance._lock) {
			
			if(!_instance.loaded) {
				_instance.load();
			}
		}
		return _instance;
	}
	
	// Object
	private final Object _lock = new Object();
	private String _cacheLastProductId;
	private ProductUI _cacheLastProduct;
	private Map<String, ProductUI> _productMap = new HashMap<String, ProductUI>();
	
	private boolean loaded = false;
	
	private ProductSingleton() {

	}
	
	private void readConfigFile(InputStream fileConfigXml, InputStream fileConfigXsd) {
		
		List<Product> products = SearchIndexerUtil.readProductConfigFile(fileConfigXml, fileConfigXsd);
				
		for(Product product : products) {
			
			ProductUI p = new ProductUI(product);
			
			p.setDatabase(new DatabaseLock(product.getConstants()));
			_productMap.put(product.getProductId(), p);
			
			try {
				p.getDatabase().loadDBIfNeeded();
			} catch (IOException e) {
				e.printStackTrace();
				p.setDatabase(null);
			}
	
		}
		
	}
	
	private void load() {
		synchronized(_lock) {
			
			if(!loaded) {

				String xmlConfigPath = SearchIndexerUtil.getConfigXmlPath();
				
				if(xmlConfigPath == null || xmlConfigPath.trim().length() == 0 || !(new File(xmlConfigPath)).exists()  ) {
					
					throw new RuntimeException("The 'slice/config_xml_path' value needs to be set in the server.xml, and it must point to a valid configuration file.");					
					
				} else {
					System.out.println("Loading indexer configuration from '"+xmlConfigPath+"'.");
					
				}
				
				InputStream fileConfigXml;
				try {
					
					fileConfigXml = new FileInputStream(new File(xmlConfigPath));
					InputStream fileConfigXsd = SearchIndexerUtil.class.getResourceAsStream("/META-INF/FileConfiguration.xsd");
					
					readConfigFile(fileConfigXml, fileConfigXsd);
					
					try { fileConfigXml.close(); } catch (IOException e) { e.printStackTrace(); }
					
					try { fileConfigXsd.close(); } catch(IOException e) { e.printStackTrace(); }

				} catch (FileNotFoundException e1) {
					throw(new RuntimeException(e1));
				}// context.getResourceAsStream("META-INF/FileConfiguration.xml");
				
				
				loaded = true;
			}
		}
	}
	
	public void unload() {
		_cacheLastProduct = null;
		_cacheLastProductId = null;
		_productMap =  null;
	}
	
	public ProductUI getProduct(String id) {
		
		synchronized(_lock) {
			
			if(id == _cacheLastProductId) {
				return _cacheLastProduct;
			}
		
			ProductUI result = _productMap.get(id);
			if(result != null) {			
				_cacheLastProduct = result;
				_cacheLastProductId = id;
			}
			
			return result;
		}
		
	}
	
	public List<ProductUI> getProductList() {
		synchronized(_lock) {

			List<ProductUI> result = new ArrayList<ProductUI>();
			
			result.addAll(_productMap.values());
			
			return result;
		}
	}
}