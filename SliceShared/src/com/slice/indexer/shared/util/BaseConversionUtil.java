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

/** A simple decimal to base-X conversion utility, where X <= 62 */
public class BaseConversionUtil {
	private static final String convTable = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

	public static String convertToBase(long num, int base) {
		String result = num == 0 ? "0" : "";
		while (num != 0) {
			int mod = (int)(num % base);
			result = convTable.substring(mod, mod + 1) + result;
			num = num / base;
		}
		return result;
	}

	public static long convertFromBase(String num, int base) {
		int result = 0;
		
		int mult = 1;
		
		for (int x = num.length(); x > 0; x--) {
			result += convTable.indexOf(num.substring(x - 1, x)) * mult;
			mult *= base;
		}
		
		return result;
	}

}
