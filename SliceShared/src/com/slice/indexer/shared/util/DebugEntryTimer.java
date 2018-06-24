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

package com.slice.indexer.shared.util;

import java.util.HashMap;
import java.util.Map.Entry;

/** A simple (mostly thread-safe) mechanism to measure the elapsed time, and average time, for the execution of blocks of code.
 * 
 * See also DebugPerformanceTimer.
 **/
public class DebugEntryTimer {
	private static final boolean ENABLED = true;
	
	private final static HashMap<String, Long> totalTimes = new HashMap<String, Long>();
	private final static HashMap<String, Long> currentTimes = new HashMap<String, Long>();

	private final static HashMap<String, Long> count = new HashMap<String, Long>();
	
	
	public static void outputEntriesPerSecond() {
		System.out.println();
		System.out.println("---------------------------");

		for(Entry<String, Long> e : count.entrySet()) {
			long time = totalTimes.get(e.getKey());
			float f = e.getValue()/(time/1000f);
			System.out.println(e.getKey() + " - "+f);
			
		}
		
	}
	
	public static void resetCount(String str) {
		Long l = count.put(str, 0l);
		totalTimes.remove(str);
	}
	
	public static void increaseCount(String str, long addCount) {
		Long l = count.get(str);
		if(l == null) {
			l = 0l;
		}
		
		count.put(str, l+addCount);
		
	}
	
	
	public static void startTimer(String str) {
		if(!ENABLED) return;
		
		synchronized(currentTimes) {
			currentTimes.put(str, System.currentTimeMillis());
		}
	}
	
	
	public static void stopTimer(String str) {
		if(!ENABLED) return;
		
		long l = 0;
		Long start = null;
		synchronized(currentTimes) {
			l = System.currentTimeMillis();
			start = currentTimes.remove(str);
		}
		
		synchronized(totalTimes) {
			Long total = totalTimes.get(str);
			if(total == null) total = 0l;
			totalTimes.put(str, total + (l - start));
		}
		
	}
}
