/*
 * Created on 28.3.2005
 */
package nomi.kvizbot;

import java.io.*;
import java.util.regex.*;

/**
 * @author NOMI team
 */
public class StripHC {
	 private static String asc = "aacdeeillnorstuuuyzAACDEEILLNORSTUIYZ";
	 private static String iso = "áäčďéěíĺľňóřšťúůüýžÁÄČĎÉĚÍĹĽŇÓŘŠŤÚŮÝŽ";

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
