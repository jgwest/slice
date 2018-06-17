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

package com.slice;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Simple utility class that is a wrapper around ProcessBuilder, and that is used for executing processes 
 * using a more developer-friendly API than ProcessBuilder. */
public class ProcessRunner {
	
	public static enum ConsoleStream {ERR, OUT }; 

	private final String[] args;
	
	private OutputStream os;

	private final List<IConsoleFilter> consoleFilters = new ArrayList<>();
	
	private final List<IListener> listeners = new ArrayList<>();
	
	private final StringBuilder received = new StringBuilder();
	
	private final boolean echoOutput;
	
	private Optional<Integer> errorCode = Optional.empty();
	
	private final Map<String, String> envVars = new HashMap<String, String>();
	
	public ProcessRunner(String[] args) {
		this.args = args;
		this.echoOutput = true;
	}

	
	public ProcessRunner(String[] args, boolean echoOutput) {
		this.args = args;
		this.echoOutput = echoOutput;
	}
	
	public int startAndWaitForTermination() throws IOException, InterruptedException {
		
		ProcessBuilder pb = new ProcessBuilder(args);
		
		envVars.entrySet().stream().forEach( e -> {
			pb.environment().put(e.getKey(), e.getValue());
		});
		
		
		Process p = pb.start();
		
		os = p.getOutputStream();
		
		ReadThread input = new ReadThread(p.getInputStream(), echoOutput ? System.out : null, ConsoleStream.OUT);
		input.start();
		
		ReadThread err = new ReadThread(p.getErrorStream(), echoOutput ? System.err : null, ConsoleStream.ERR);
		err.start();

		errorCode = Optional.of(p.waitFor());

		while(!input.isFinished) {
			Thread.sleep(200);
		}
		
		return errorCode.get();
	}
	
	public Map<String, String> getEnvVars() {
		return envVars;
	}

	public String getReceived() {
		return received.toString();
	}

	public void addListener(IListener listener) {
		synchronized (listeners) {
			listeners.add(listener);	
		}
	}
	
	public void addConsoleFilter(IConsoleFilter filter) {
		synchronized (consoleFilters) {
			consoleFilters.add(filter);
		}
	}
	
	public OutputStream getOutput() {
		return os;
	}
	
	public Optional<Integer> getErrorCode() {
		return errorCode;
	}
		

	/** Implement this class to receive text that is output by the process. */
	public static interface IListener {

		void receiveText(String str);
		
	}

	/** Implementors of this interface should return true if they wish to exclude the printing of a specific 
	 * line of text to the screen, or false otherwise. */
	public static interface IConsoleFilter {

		boolean excludeConsoleText(ConsoleStream cs, String line);
		
	}

	/** Read from an input stream, and optionally write that data it to an outputstream or listener. */
	private class ReadThread extends Thread {
		
		final InputStream is;
		final PrintStream ps;
		final ConsoleStream cs;
		private boolean isFinished = false;
		
		public ReadThread(InputStream is, PrintStream ps, ConsoleStream cs) {
			this.is = is;
			this.ps = ps;
			this.cs = cs;
			setDaemon(true);
		}
		
		@Override
		public void run() {
			
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			
			String str;
			try {
				while(null != (str = br.readLine())) {
					final String line = str;
					
					// Do any of the filters say to prevent this text from being printed?
					boolean exclude = false;
					synchronized (consoleFilters) {
						exclude = consoleFilters.stream().anyMatch(e -> e.excludeConsoleText(cs, line) );
					}
					
					if(ps != null && !exclude) {
						ps.append(str+"\n");
					}
					
					synchronized(received) {
						received.append(str+"\n");
					}
					
					
					for(IListener listener : listeners) {
						listener.receiveText(str);
					}
					
				}
				isFinished = true;
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
	}
	
}
