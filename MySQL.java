/*
 * Created on 27.2.2005
 */
package nomi.kvizbot;

import java.sql.*;

/**
 * @author NOMI team
 */
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
	     throws Exception {  
	  
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
