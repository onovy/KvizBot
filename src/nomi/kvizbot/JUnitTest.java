/*
    This file is part of KvizBot.

    KvizBot is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    KvizBot is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with KvizBot. If not, see <http://www.gnu.org/licenses/>.
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
