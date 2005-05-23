/*
 * Created on 23.5.2005
 */
package nomi.kvizbot;

import java.io.IOException;

public class JUnitTest extends junit.framework.TestCase {
	private String string1, string2;
	
	public void setUp() {
		string1="Příliš žluťoučký kůň úpěl ďábelské ódy.";
		string2="Prilis zlutoucky kun upel dabelske ody.";
	}
	
	public void testStripHC() {
		try {
			assertEquals(
					StripHC.convertChars(string1),
					string2
			);
		} catch (IOException e) {
			fail("Došlo k IOException");
			e.printStackTrace();
		}
	}
}
