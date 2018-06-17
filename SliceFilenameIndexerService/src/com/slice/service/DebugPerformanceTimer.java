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
package com.slice.service;

import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/** Utility class that can be used to determine the amount of time spent in a specific method.
 *  Useful for performance testing.  */
public class DebugPerformanceTimer {
	
	private static final boolean ENABLED = false;

	private final static HashMap<String, Long> totalTimes = new HashMap<String, Long>();
	private final static HashMap<String, Long> currentTimes = new HashMap<String, Long>();
	
	public static void outputAllTotalTimes() {
		if(!ENABLED) return;
		
		NumberFormat nf = NumberFormat.getNumberInstance(Locale.US);
		
		System.out.println("-----------------------------");
		System.out.println("DebugPerformanceTimer");
		
		
		int maxLengthKey = 0;
		int maxLengthValue = 0;
		for(Map.Entry<String, Long> e : totalTimes.entrySet()) {
			
			if(e.getKey().length() > maxLengthKey) {
				maxLengthKey = e.getKey().length();
			}
			
			String val = nf.format(e.getValue());
			if(val.length() > maxLengthValue) {
				maxLengthValue = val.length();
			}
		}

		
		
		for(Map.Entry<String, Long> e : totalTimes.entrySet()) {
			int keyPadding = maxLengthKey - e.getKey().length();
			
			String val = nf.format(e.getValue());
			int valuePadding = maxLengthValue - val.length();
			
			String out = e.getKey();
			for(int x = 0; x < keyPadding; x++)  { out += " "; }
			out  += " - ";
			
			for(int x = 0; x < valuePadding; x++)  { out += " "; }
			out += val;
			System.out.println(out);
			
		}
		System.out.println("-----------------------------");
		
	}
	
	@SuppressWarnings("rawtypes")
	public static void startTimer(Class c) {
		if(!ENABLED) return;
		startTimer(c.getName());
	}

	
	@SuppressWarnings("rawtypes")
	public static void startTimer(Class c, String m) {
		if(!ENABLED) return;
		startTimer(c.getName()+"."+m);
	}
	
	public static void startTimer(String str) {
		if(!ENABLED) return;
		
		synchronized(currentTimes) {
			currentTimes.put(str, System.nanoTime());
		}
	}

	
	
	@SuppressWarnings("rawtypes")
	public static void stopTimer(Class c, String m) {
		if(!ENABLED) return;
		stopTimer(c.getName()+"."+m);
	}
	
	@SuppressWarnings("rawtypes")
	public static void stopTimer(Class c) {
		if(!ENABLED) return;
		stopTimer(c.getName());
	}
	

	
	public static void stopTimer(String str) {
		if(!ENABLED) return;
		
		long l = 0;
		Long start = null;
		synchronized(currentTimes) {
			l = System.nanoTime();
			start = currentTimes.remove(str);
		}
		
		synchronized(totalTimes) {
			Long total = totalTimes.get(str);
			if(total == null) total = 0l;
			if(start != null) {
				totalTimes.put(str, total + (l - start));				
			} else {
				System.err.println("Warning, no start found for stopTimer.");
				
			}
			
		}
		
	}

	public static long resetTotalTime(String str) {
		if(!ENABLED) return -1;
		return totalTimes.remove(str);
		
	}
	
	public static void resetAll() {
		if(!ENABLED) return;
		
		totalTimes.clear();
		currentTimes.clear();
	}

	
	public static long getTotalTime(String str) {
		if(!ENABLED) return -1;
		return totalTimes.get(str);
		
	}
	
}
