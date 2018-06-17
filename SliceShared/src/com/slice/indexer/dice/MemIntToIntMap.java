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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

/** 
 * A map specialized to handle only Integer -> Integer relations, and which has the following properties:
 * - Has a fixed size (will not grow)
 * - Can be read/written to file.
 * - Is stored as an array of bytes, rather than as an array of integers.
 **/
public class MemIntToIntMap implements Map<Integer, Integer> {

	byte[] _array;

	
	public MemIntToIntMap(int totalSize) {
		_array = new byte[totalSize*4];
	}
	
	public MemIntToIntMap(File f) throws IOException {
		readFromFile(f);
	}
	
	private void readFromFile(File f) throws IOException {
		FileInputStream fis = new FileInputStream(f);
		
		_array = new byte[(int)(f.length())];
		
		int bytesRead = fis.read(_array, 0, _array.length);
		fis.close();
		
		if(bytesRead != _array.length) {
			
			throw new IOException("Could not read whole file");
		}
		
		
	}
	
	public void writeToFile(File f) throws IOException {
		
		FileOutputStream fos = new FileOutputStream(f);
		
		fos.write(_array, 0, _array.length);
		
		fos.close();
		
	}
	

	private static int getIntAtPos(byte[] array, int pos) {

        int ch1 = array[pos] & 0xFF;
        int ch2 = array[pos + 1] & 0xFF;
        int ch3 = array[pos + 2] & 0xFF;
        int ch4 = array[pos + 3] & 0xFF;
        
        return ((ch4 << 0) + (ch3 << 8) + (ch2 << 16) + (ch1 << 24));

	}
	
	private static void putIntAtPos(byte[] array, int pos, int value) {
		array[pos+0]=(byte)(value>>24 & 0xFF);
		array[pos+1]=(byte)(value>>16 & 0xFF);
		array[pos+2]=(byte)(value>>8  & 0xFF);
		array[pos+3]=(byte)value;

	}
	
	public long getByteArraySize() {
		return _array.length;
	}
		
	@Override
	public void clear() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean containsKey(Object key) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean containsValue(Object value) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Set<java.util.Map.Entry<Integer, Integer>> entrySet() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Integer get(Object key) {
		int pos = (Integer)key;
		return getIntAtPos(_array, pos*4);
		
	}

	@Override
	public boolean isEmpty() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Set<Integer> keySet() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Integer put(Integer key, Integer value) {
		
		putIntAtPos(_array, key*4, value);
		return value;
	}

	@Override
	public void putAll(Map<? extends Integer, ? extends Integer> map) {
		throw new UnsupportedOperationException();		
	}

	@Override
	public Integer remove(Object key) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int size() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Collection<Integer> values() {
		throw new UnsupportedOperationException();
	}


	/*
	public static void main(String[] args) {
		byte[] b = new byte[4];
		for(int x = 0; x < 1600000000; x++) {
			putIntAtPos(b, 0, x);
			
			int extractedValue = getIntAtPos(b, 0);
			
			if(extractedValue != x) {
				System.out.println("Error! "+x);
			}
			
			
		}
		
	}*/
}
