package testing;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.StreamTokenizer;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import triangle.Triangle;

public class AllTests {

	private static class FileTest extends TestCase {

		protected int inputData[][] = null;

		protected long outputValue = 0;

		protected boolean brokenTest = false;

		public FileTest(String s) {
			super(s);
			try {
				BufferedReader r = new BufferedReader(new FileReader(getName()));
				StreamTokenizer t = new StreamTokenizer(r);
				t.parseNumbers();
				t.nextToken();
				int h = (int) t.nval;
				inputData = new int[h][];
				for (int i = 0; i < h; i++) {
					inputData[i] = new int[i + 1];
					for (int j = 0; j <= i; j++) {
						t.nextToken();
						inputData[i][j] = (int) t.nval;
					}
				}
				t.nextToken();
				outputValue = (int) t.nval;
			} catch (Exception e) {
				e.printStackTrace();
				brokenTest = true;
			}
		}

		public void testMaxPath() {
			assertFalse(brokenTest);
			ExecutorService executor = Executors.newSingleThreadExecutor();
			int answer = -1;
			try {
				Future<Integer> future = executor
						.submit(new Callable<Integer>() {

							@Override
							public Integer call() throws Exception {
								return new Triangle().maxpath(inputData);
							}

						});
				answer = future.get(30, TimeUnit.SECONDS);
			} catch (TimeoutException  te) {
				fail("Test took too long.");
			} catch (ExecutionException e) {
				fail("Test failed with "+e.getMessage());
			}catch (InterruptedException e) {
				fail("Test interrupted.");
			}

			assertTrue("Wrong answer " + answer + ", should be " + outputValue,
					answer == outputValue);
		}

		public void runTest() {
			testMaxPath();
		}
	}

	public static Test suite() {
		String location = System.getProperty("data.location");
		if(location == null || !new File(location).exists()){
			location = "src"+File.separator+"testing"+File.separator+"data";
		}
		TestSuite suite = new TestSuite("Test for Triangle");
		File f = new File(location);
		String s[] = f.list();
		for (int i = 0; i < s.length; i++) {
			String n = s[i];
			if (n.endsWith(".txt")) {
				suite.addTest(new FileTest(location + File.separator + n));
			}
		}
		return suite;
	}

}