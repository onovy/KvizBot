/*
 * Created on 26.2.2005
 */
package nomi.kvizbot;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.InvalidPropertiesFormatException;
import java.util.Properties;

import org.jibble.pircbot.IrcException;
import org.jibble.pircbot.NickAlreadyInUseException;

/**
 * @author NOMI team
 */
public class KvizBot {

	public static void main(String[] args) {
		String db_host = "";
		String db_name = "";
		String db_user = "";
		String db_pass = "";
		String irc_channel = "";
		String irc_server = "";
		int irc_port = 0;
		String irc_pass = "";
		
		// nacteni konfigurace
		Properties prop = new Properties();
		try {
			prop.loadFromXML( new FileInputStream(new File("kviz.cfg")));
			db_host = prop.getProperty("db_host");
			db_name = prop.getProperty("db_name");
			db_user = prop.getProperty("db_user");
			db_pass = prop.getProperty("db_pass");
			
			irc_server = prop.getProperty("irc_server");
			irc_port = Integer.valueOf(prop.getProperty("irc_port"));
			irc_pass = prop.getProperty("irc_pass");
			irc_channel = prop.getProperty("irc_channel");
		} catch (NumberFormatException e2) {
			System.err.println("Chybný port");
			System.exit(-1);
		} catch (InvalidPropertiesFormatException e2) {
			System.err.println("Špatný formát konf. souboru");
			System.exit(-1);
		} catch (FileNotFoundException e2) {
			System.err.println("Konfigurační soubor kviz.cfg nenalezen");
			System.exit(-1);
		} catch (IOException e2) {
			System.err.println("Došlo k IO chybě při načítání konfiguračního souboru");
			System.exit(-1);
		}		
		
		// pripojeni k databazi
		MySQL mysql = null;
		try {
			mysql = new MySQL(db_host,db_name,db_user,db_pass);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}

		// pripojeni k IRC
		IRC irc = new IRC(mysql,prop);
		try {
			irc.connect(irc_server,irc_port,irc_pass);
		} catch (NickAlreadyInUseException e) {
			e.printStackTrace();
			System.exit(-1);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(-1);
		} catch (IrcException e) {
			e.printStackTrace();
			System.exit(-1);
		}

		irc.joinChannel(irc_channel);
		
		irc.newOtazka();
		System.out.println("Kvíz spuštěn");
	}
}
