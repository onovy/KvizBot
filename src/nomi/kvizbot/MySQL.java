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
	private String url=null;
	private String user=null;
	private String pass=null;
	
	/**
	   * Returns a connection to the MySQL database
	   *
	   */
	public MySQL ( String host, String database, String user, String pass) throws Exception {
		this.url = "jdbc:mysql://" + host + "/" + database + "?useUnicode=true&useSSL=false&autoReconnect=true";
		this.user = user;
		this.pass = pass;
		this.connect();
	}

	private void connect() throws SQLException {
		try {
			this.con = DriverManager.getConnection(this.url, this.user, this.pass);
		} catch (java.sql.SQLException e) {
			System.out.println("Connection couldn't be established to " + this.url);
			throw(e);
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
	      this.connect();
	      throw (e);
	    }
	  }
	  
	  public Connection getConn() {
	  	return con;
	  }
}
