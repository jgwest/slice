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

import java.util.List;

import org.w3c.dom.Element;

import com.slice.indexer.constants.IConfigConstants;

/** A product correspond to a <Product> entry in the XML configuration file, and is a specific grouping of settings, and related files to index.
 * 
 * A product is the atomic unit of organization of source-code-to-index in Slice, and each configured product will be available at a different URL:
 * eg https://(host)/SliceRS/resources/(product id)
 * 
 **/
public class Product {
	
	/** Interface that may be used to retrieve configuration values/settings from the XML file */
	private final IConfigConstants _constants;
	
	/** ID Of the product*/
	private final String _productId;
	
	/** Parsed DOM XML elements for this product, from the XML file; it usually preferable to use the getConstants(..) method, 
	 * rather than to read the XML directly. */
	private final List<Element> _configEntries;
	
	public Product(IConfigConstants constants, String productId, List<Element> configEntries) {
		this._constants = constants;
		this._productId = productId;
		this._configEntries = configEntries;
	}

	public String getProductId() {
		return _productId;
	}
	
	public IConfigConstants getConstants() {
		return _constants;
	}
	
	@Override
	public int hashCode() {
		return _productId.hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		return this == obj;
	}
	
	public List<Element> getConfigEntries() {
		return _configEntries;
	}

}
