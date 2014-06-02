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

import java.io.*;
import java.util.regex.*;

public class StripHC {
	 private static String asc = "aacdeeeillnorstuuuyzAACDEEILLNORSTUUYZ";
	 private static String iso = "áäčďéěëíĺľňóřšťúůüýžÁÄČĎÉĚÍĹĽŇÓŘŠŤÚŮÝŽ";

	 /**
	  * prevadi string s ceskymi diakritickymi znaky na znaky bez diakritiky
	  * 
	  * @param input
	  * @return output
	  * @throws IOException
	  */
	 
	 public static String convertChars( String input) throws IOException {
	    StringBuffer sb = new StringBuffer();
	     
	     Pattern p = Pattern.compile("(["+iso+"])");
	     
	     Matcher m = p.matcher( input);
	     String repl_str;
	     while ( m.find() )
	      {
	        repl_str = m.group();
	        repl_str = "" + asc.charAt( iso.indexOf(repl_str) );
	        m.appendReplacement(sb, repl_str);
	      }
	     m.appendTail(sb);	
	     
	     String output = sb.toString(); 
	     p = null;
	     sb = null;
	     m = null;
	     return output;
	    }
}
