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

package com.slice.indexer.constants;

/** 
 * Corresponds to <Component> elements in the configuration XML, and is 
 * implemented by ConfigConstantsProductDTOWrapper.ComponentPairImpl.  
 */
public interface IComponentPair {

	/** User-visible name of the component */
	String getName();
	
	/** A specific substring in the path that will identify a file as being part of this component  */
	String getPath();
	
	String getStrippedName();
	
	String getHelperQueryName();
}
