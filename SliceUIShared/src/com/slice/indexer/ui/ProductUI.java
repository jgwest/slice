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

import com.slice.indexer.shared.Product;

/** This object contains a reference to a Product, but also includes fields which are exclusive to the UI project.
 * 
 *  The product's database can be acquired (through the database lock) using the getDatabase() method. */
public class ProductUI {
	
	private Product _product;
	
	private DatabaseLock _database;

	public ProductUI(Product product) {
		this._product = product;
	}
	
	public DatabaseLock getDatabase() {
		return _database;
	}

	public void setDatabase(DatabaseLock database) {
		this._database = database;
	}

	public Product getProduct() {
		return _product;
	}
	
	
	@Override
	public int hashCode() {
		return _product.hashCode();
	}
	
	@Override
	public boolean equals(Object o) {
		return _product.equals( ((ProductUI)o)._product);
	}
}
