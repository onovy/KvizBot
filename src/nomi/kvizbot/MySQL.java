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

import java.sql.*;

public class MySQL {
	
	private Connection con=null;
	
	/**
	   * Returns a connection to the MySQL database
	   *
	   */
	public MySQL ( String host, String database, String user, String pass) throws Exception {
		try {
			testDriver();
		} catch (Exception e) {
			throw(e);
		}
		String url = "";
		try {
			url = "jdbc:mysql://" + host + "/" + database + "?characterEncoding=iso8859_2&useUnicode=true";
			con = DriverManager.getConnection(url,user,pass);
	    } catch ( java.sql.SQLException e ) {
	    System.out.println("Connection couldn't be established to " + url);
	    throw ( e );
	    }
	  }
	
	 
	  /**
	   * Checks whether the MySQL JDBC Driver is installed
	   */
	  protected void testDriver () throws Exception {
	    try {
	      Class.forName ( "org.gjt.mm.mysql.Driver" );
	    } catch ( java.lang.ClassNotFoundException e ) {
	      System.out.println("MySQL JDBC Driver not found ... ");
	      throw ( e );
	    }
	  }
	  
	  /**
	   * This method executes an SQL query
	   * @param query query to execute
	   */
	  protected void query ( String query )
	     throws SQLException {  
	  
	    try {
	      PreparedStatement s = con.prepareStatement(query);
	      s.execute();
	      s.close();
	    } catch ( SQLException e ) {
	      System.out.println("Error executing SQL query "+query);
	      throw (e);
	    }
	  }
	  
	  public Connection getConn() {
	  	return con;
	  }
}
