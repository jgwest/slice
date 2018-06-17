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

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Not currently shared w/ readable db, due readable db's use of compressed IDs */
public class SharedKeyAndIdListDiceSingleton {

	private static final SharedKeyAndIdListDiceSingleton _instance = new SharedKeyAndIdListDiceSingleton();
	
	private SharedKeyAndIdListDiceSingleton() {	
	}
	
	public static SharedKeyAndIdListDiceSingleton getInstance() {
		return _instance;
	}
	
	// ---------
	
	private final Object lock = new Object();
	
	/** locked on 'lock' object*/
	private final Map</* product id*/ String, List<String> /* key list (id is the position in the list) */ > productIdToListMap = new HashMap<String, List<String>>();

	
	
	public void putUnmodifiableKeyList(String productId, List<String> keyList) {
		synchronized(lock) {
			productIdToListMap.put(productId.toLowerCase(), Collections.unmodifiableList(keyList));			
		}
	}
	
	public List<String> getUnmodifiableKeyList(String productId) {
		synchronized(lock) {
			return productIdToListMap.get(productId.toLowerCase());
		}
	}
	
	
	
	
}
