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
 * This class corresponds to the <AllowList> element in the file configuration XML, and specifies
 * which files in the file system a user may see the full file contents from, using fileview.jsp 
 * (if a file is not allowed, the user may only see small snippets from that file through the search query interface).
 * 
 * Currently implemented by ConfigConstants ProductDTOWrapper.AllowRuleImpl. 
 **/
public interface IAllowRule {

	enum AllowRuleType {ALLOW, DENY}

	public AllowRuleType getType();
	public String getValue();

} 