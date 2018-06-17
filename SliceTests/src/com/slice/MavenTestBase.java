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

import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.junit.BeforeClass;
import com.slice.ProcessRunner;

/** When running integration tests from Maven, this class is responsible for the setup and teardown of 
 * the test environment.  */
public abstract class MavenTestBase extends TestBase {

	public static boolean testSetupCompleted = false;
	
	
	public MavenTestBase(String productId) {
		super(productId);
	}


	@BeforeClass
	public static void setupTest() throws IOException, InterruptedException {
		if(testSetupCompleted) { return; }
		
		setupMyTest();
		
		testSetupCompleted = true;
	}
	
		
	private static File findMavenSrcRootDir() {
		File userDir = new File(System.getProperty("user.dir"));
		
		do {
			
			if(Arrays.asList(userDir.listFiles())
					.stream().anyMatch(e -> e.getName().equals("SliceTests"))) {
				// Match!
				break;
			}
			
			userDir = userDir.getParentFile();
			
			
		} while(userDir != null);
		

		return userDir;
	}
	
	
	private static File setupMyTest() throws IOException, InterruptedException {
		
		Path p = Files.createTempDirectory("slice-test");
		
		final File f = p.toFile();
		System.out.println("Test dir is: "+f.getPath());
		
//		mkdir $NODE_ROOT
//		mkdir $NODE_ROOT/Test
//		mkdir $NODE_ROOT/Test/JavaSrc
//		mkdir $NODE_ROOT/Test/JavaDb

		if(!f.exists()) { fail("Unable to create directory."); }
		
		File fTest = new File(f, "Test");
		if(!fTest.mkdirs()) { fail("Unable to create directory."); }
		
		File fTestJavaSrc = new File(fTest, "JavaSrc");
		if(!fTestJavaSrc.mkdirs()) { fail("Unable to create directory."); }		

		File fTestJavaDb = new File(fTest, "JavaDb");
		if(!fTestJavaDb.mkdirs()) { fail("Unable to create directory."); }

		File mavenSrcDir = findMavenSrcRootDir();
		

		// cp -R Slice* $NODE_ROOT/Test/JavaSrc
		Arrays.asList(mavenSrcDir.listFiles()).stream().filter( e -> e.getName().startsWith("Slice") ).forEach( e -> {
					try {
						System.out.println("* Copying "+e.getName());
						FileUtils.copyDirectory(e, new File(fTestJavaSrc, e.getName()), indexableFileFilter);
					} catch (IOException e1) {
						throw new RuntimeException(e1);
					}
			});
		
		File outputTestFile = new File(fTest, "Test-FileConfiguration.xml");
		
		// cp artifacts/Test-FileConfiguration.xml  $NODE_ROOT/Test/Test-FileConfiguration.xml
		FileUtils.copyFile(new File(new File(mavenSrcDir, "artifacts"), "Test-FileConfiguration.xml"), 
				outputTestFile);

		// java -jar StandaloneFullIndexerNew.jar $NODE_ROOT/Test/Test-FileConfiguration.xml
		File jarPath = new File(mavenSrcDir, "SliceCreation/target/SliceCreation-1.0.0-SNAPSHOT.jar");
		
		String[] args = new String[] {
			"java", "-jar", jarPath.getPath(), outputTestFile.getPath()
		};
		
		ProcessRunner pr = new ProcessRunner(args, true);
		pr.getEnvVars().put("NODE_ROOT", f.getPath());
		pr.startAndWaitForTermination();
	
		{
			File tmpDirPropFile = new File(System.getProperty("java.io.tmpdir")+"/Slice-Test.properties");
			Properties props = new Properties();
			props.put("test-config-xml", outputTestFile.getPath());
			FileOutputStream fos = new FileOutputStream(tmpDirPropFile);
			props.store(fos, "");
			fos.close();
			System.out.println("* Wrote Slice-Test.properties to temp dir root.");
		}
		
		return null;
		
	}
	
	private static final IndexableFileFilter indexableFileFilter = new IndexableFileFilter();

	/** Whether a specific file is capable of being indexed by one or more of the search indexers.
	 *  
	 * This is used to determine whether or not to copy the file into the target test directory which 
	 * will be indexed at a later step. */
	private static class IndexableFileFilter implements FileFilter {

		private static final String[] ACCEPTED_EXTENSIONS = new String[] { "java" }; 
		
		@Override
		public boolean accept(File pathname) {
			if(pathname.isDirectory()) { return true; }
			
			return Arrays.asList(ACCEPTED_EXTENSIONS).stream()
					.anyMatch( e -> pathname.getName().toLowerCase().endsWith(e));
			
		}
		
	}
	
}
