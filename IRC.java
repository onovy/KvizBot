/*
 * Created on 27.2.2005
 */
package nomi.kvizbot;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Random;
import java.sql.*;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

import org.nomi.pircbotNg.*;
import nomi.kvizbot.StripHC;
import java.util.Properties;

/**
 * @author NOMI team
 */
public class IRC extends PircBotNg {

	private String otazka="";
	private int otazka_id=0;
	private String odpoved="";
	private String odpoved_shc="";
	private String napoveda="";
	private int napoveda_countch;
	private int napoveda_countoch;
	private int napoveda_num;
	private int napoveda_count;
	private int napoveda_maxcount=3;
	private int napoveda_timelimit=10;
	private int napoveda_auto_delay=30;
	private String bot_name;
	private String welcomeMessage;
	private String welcomeMessage2;
	private int otazka_delay=20;
	private char napoveda_char='#';
	private MySQL mysql;
	private String channel;
	private long napoveda_last;
	private Timer timerNapoveda;
	private Timer timerNewOtazka;
	private static final String[] napoveda_chars=
			{"q","w","e","r","t","y","u","i","o","p",
		     "a","s","d","f","g","h","j","k","l",
			 "z","x","c","v","b","n","n","m",
			 "á","č","ď","é","ě","í","ĺ","ľ",
			 "ň","ó","ř","š","ť","ú","ů","ý","ž",
			 "0","1","2","3","4","5","6","7","8","9"};
	
	public IRC (MySQL mysql, Properties prop) {
		this.mysql=mysql;
		channel = prop.getProperty("irc_channel");
		bot_name = prop.getProperty("irc_nick");
		welcomeMessage = prop.getProperty("welcomeMessage");
		welcomeMessage2 = prop.getProperty("welcomeMessage2");	
		this.setName(bot_name);
		try {
			setEncoding("UTF-8");
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
			System.exit(-1);
		}
		setMessageDelay(0);
		setVerbose(true);
		timerNewOtazka = new Timer();
	}
	
	/**
	 * Overeni prav z DB
	 * 
	 * @param nick
	 * @param perm
	 *  e - exit
	 *  n - next
	 *  h - help
	 *  t - showtop
	 *  a - admin
	 *  o - manipulace s otazkama
	 *  s - spravce kvizu
	 * @return true/false
	 */
	public synchronized boolean check_perm(String nick, String perm) {
		/* Global perm */
		if (perm=="t") return true;
		
		try {
			ResultSet rs;
			String query="SELECT perm.id FROM perm LEFT JOIN nicks ON perm.nick=nicks.id WHERE LOWER(nicks.nick)=LOWER(?) AND perm=?";
			PreparedStatement ps=mysql.getConn().prepareStatement(query);
			ps.setString(1,nick);
			ps.setString(2,perm);
			rs = ps.executeQuery();

			try {
				rs.next();
				rs.getInt("id");
				rs.close();
				ps.close();
				return true;
			} catch (SQLException se) {
				rs.close();
				ps.close();
				return false;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	protected void onNotice(String sourceNick,
            String sourceLogin,
            String sourceHostname,
            String target,
            String notice) {
		System.out.println("NOTICE from " +sourceNick + ": " + notice);
	}

	protected void onPrivateMessage(String sender,
	    String login, String hostname, String message) {
		System.out.println("PRIVMSG from " + sender + ": " + message);
	}

	
	public synchronized void onKick(String channel, String kickerNick, String kickerLogin, String kickerHostname, String recipientNick, String reason) {
		if (recipientNick.equalsIgnoreCase(getNick())) {
		    joinChannel(channel);
		}
	}
	
	public synchronized void onConnect() {
		sendRawLine("SET IDLE_INTERVAL 720");		
		sendRawLine("SET RECV_INTERVAL 1");		
	}
	
	public synchronized void onDisconnect() {
		while (!isConnected()) {
		    try {
		        reconnect();
		    }
		    catch (Exception e) {
		    	try {
					Thread.sleep(5000);
				} catch (InterruptedException e1) {
				}
		    }
		}
		joinChannel(channel);
	}
	
	private synchronized void add_online_user(String nick) {
		try {
			PreparedStatement ps;
			String query="INSERT INTO online (nick) VALUES (?)";
			ps=mysql.getConn().prepareStatement(query);
			ps.setString(1,nick);
			ps.executeUpdate();
			ps.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}			
	}
	
	public synchronized void onJoin(String channel, String sender, String login, String hostname) {

			try {
				mysql.query("DELETE FROM online");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			User[] users=getUsers(channel);
			for(int x=0; x < users.length; x++) {
				User user = users[x];
				add_online_user(user.getNick());
			}
			
			if (!sender.equalsIgnoreCase(bot_name)) {
				if (!welcomeMessage.equals("")) {
					sendMessage(sender,welcomeMessage);
				}
				if (!welcomeMessage2.equals("")) {
					sendMessage(sender,welcomeMessage2);
				}
			}
			
	}
	
	public synchronized void onPart(String channel, String sender, String login, String hostname) {
		PreparedStatement ps;
		String query="DELETE FROM online WHERE nick=?";
		try {
			ps=mysql.getConn().prepareStatement(query);
			ps.setString(1,sender);
			ps.executeUpdate();
			ps.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}				
	}
    public synchronized void onMessage(String channel, String sender, String login, String hostname, String message) {
    	if (!odpoved.equals("")) {			
    		if (message.equalsIgnoreCase("!dalsi") ||
        		message.equalsIgnoreCase("!next")) {
        		if (check_perm(sender,"n")) {
        			sendMessage(channel,"*46* Odpověď na otázku číslo "+otazka_id+" je "+odpoved+". Zkusíme další... *46*");
        			newOtazka();
        		}
        	}
        	if (message.equalsIgnoreCase("!napovez") ||
        		message.equalsIgnoreCase("!hint") ||
        		message.equalsIgnoreCase("!h")) {
        		if (check_perm(sender,"h")) {
        			newNapoveda();
        		}
        	}
        	message=message.toLowerCase();

			// oddelani mezer na zacatku
			if (message.length()!=0) { 
				while (message.charAt(0)==' ') {
					message=message.substring(1);
					if (message.length()==0) continue;
				}
			}
				
			// oddelani mezer na konci
			if (message.length()!=0) { 
				while (message.charAt(message.length()-1)==' ') {
					message=message.substring(0,message.length()-1);
					if (message.length()==0) continue;
				}
			}
			
        	if (message.equals(odpoved_shc)) {
			boolean blocked = false;
			try {
			    blocked = getBlockedByNick(sender);
    			} catch (Exception e) {}
			if (!blocked) {
        		    timerNapoveda.cancel();
        		
        		    int bodu=napoveda_count-napoveda_num+1;
        			
        		    sendMessage(channel,"*52* To je ono "+sender+", správná odpověď na otázku číslo "+otazka_id+" je "+odpoved+". Získáváš "+bodu+" bod"+word_ending3(bodu)+" *52*");
        		    addBod(sender,bodu);
        		    showPlayerInfo(sender);
            		    newOtazka();
			}
        	}
    	}
    	
    	if (message.equalsIgnoreCase("!exit")) {
    		if (check_perm(sender,"e")) {
    			System.exit(0);
    		}
    	}
    	if (message.equalsIgnoreCase("!top") ||
       	    message.equalsIgnoreCase("!top10")
			) {
    		if (check_perm(sender,"t")) {
    			showTop();
    		}
    	}
		
    	// spravcovske veci
    	// OP
    	if (message.equalsIgnoreCase("!op")) {
    		if (check_perm(sender,"s")) {
    			op(channel,sender);
    		}
    	}
    		
    	// KICK
    	if (message.startsWith("!kick")) {
    		if (check_perm(sender,"s")) {
    			String[] param = message.split(" ");
    			if (param[1].equalsIgnoreCase(sender)) {
    				sendMessage(sender,"Nemůžeš vyhodit sám sebe");
    			} else {
    				if (param[1].equalsIgnoreCase(bot_name)) {
    					sendMessage(sender,"Nemůžeš vyhodit bota");
    				} else {
    					if (check_perm(param[1],"s")) {
    						sendMessage(sender,"Nemůžeš vyhodit jiného správce");
    					} else {
    						kick(channel,param[1],"Správce Kvízu "+sender+" tě vyhodil z místnosti");
    					}
    				}
    			}
    		}
    	}
    		
    	// KEY
    	if (message.startsWith("!key")) {
    		if (check_perm(sender,"s")) {
    			String[] param = message.split(" ");
    			if (param[1].equalsIgnoreCase(sender)) {
    				sendMessage(sender,"Nemůžeš dát do klíčů sám sebe");
    			} else {
    				if (param[1].equalsIgnoreCase(bot_name)) {
    					sendMessage(sender,"Nemůžeš dát do klíčů bota");
    				} else {
    					if (check_perm(param[1],"s")) {
    						sendMessage(sender,"Nemůžeš dát do klíčů jiného správce");
    					} else {
    	    				sendMessage(channel,"/key "+param[1]);
							sendMessage(channel,"Uživatel "+param[1]+" byl přidán do klíčů");
    					}
    				}
    			}
    		}
    	}

		// UNKEY
    	if (message.startsWith("!unkey")) {
    		if (check_perm(sender,"s")) {
    			String[] param = message.split(" ");
    			if (param[1].equalsIgnoreCase(sender)) {
    				sendMessage(sender,"Nemůžeš odklíčovat sám sebe");
    			} else {
    				if (param[1].equalsIgnoreCase(bot_name)) {
    					sendMessage(sender,"Nemůžeš odklíčovat bota");
    				} else {
    					if (check_perm(param[1],"s")) {
    						sendMessage(sender,"Nemůžeš odklíčovat jiného správce");
    					} else {
    	    				sendMessage(channel,"/unkey "+param[1]);
							sendMessage(channel,"Uživatel "+param[1]+" byl odebrán z klíčů");
    					}
    				}
    			}
    		}
    	}
    }
    
    public synchronized void setOdpoved(String odpoved) {
    	this.odpoved=odpoved;
    }
    
    public synchronized String getOdpoved() {
    	return odpoved;
    }
    
    public synchronized void newOtazka() {
		System.gc();
		otazka="";
		odpoved="";
		if (timerNapoveda!=null) {
			timerNapoveda.cancel();
		}	
        timerNewOtazka.schedule(new TimerNewOtazka(), otazka_delay*1000);
    }
    
    public synchronized void newOtazka_reall() {
		try {
				// Registrace
				String queryr = "SELECT pass_req.id, nicks.nick, hash FROM pass_req JOIN nicks ON (nicks.id = pass_req.nick) WHERE sent = false";
				PreparedStatement psr=mysql.getConn().prepareStatement(queryr);
				ResultSet rsr = psr.executeQuery();		
			    	while (rsr.next()) {
				    sendMessage(rsr.getString("nick"), "Aktivace registrace: http://www.xkviz.net/registrace.htm/" + rsr.getString("hash"));
				    String queryrs = "UPDATE pass_req SET sent = true WHERE id = ?";
				    PreparedStatement psrs=mysql.getConn().prepareStatement(queryrs);
				    psrs.setInt(1, rsr.getInt("id"));
				    psrs.executeUpdate();
				    psrs.close();
				}
				rsr.close();
				psr.close();
				
				String query = "SELECT id,otazka,odpoved FROM otazky WHERE schvaleni=0 AND last IS NULL AND (game & 1) != 0 ORDER BY RAND() LIMIT 1";

				PreparedStatement ps=mysql.getConn().prepareStatement(query);
				ResultSet rs = ps.executeQuery();
			
				rs.next();
				
				otazka_id=rs.getInt("id");
				otazka=rs.getObject("otazka").toString();
				odpoved=rs.getObject("odpoved").toString();
				rs.close();
				ps.close();
				
				// oznaceni otazky
				try {
					query="UPDATE otazky SET last=now() WHERE id=?";
		    		PreparedStatement ps2=mysql.getConn().prepareStatement(query);
		    		ps2.setInt(1,otazka_id);
		    		ps2.executeUpdate();
		    		ps2.close();
				} catch (java.sql.SQLException se) {
					// TODO Auto-generated catch block
					se.printStackTrace();					
				}
				
	    		try {
					odpoved_shc=StripHC.convertChars(odpoved).toLowerCase();
				} catch (IOException e) {
					e.printStackTrace();
					System.out.println("Chyba pri stripovani hacku a carek!");
				}
								
				napoveda=odpoved.toLowerCase();
				for (int a=0 ; a<napoveda_chars.length ; a++) {
					napoveda=napoveda.replace(napoveda_chars[a],""+napoveda_char);
				}
				napoveda_num=0;
		    	napoveda_countch=0;
		    	for (int a=0 ; a<napoveda.length() ; a++) {
		    		if (napoveda.charAt(a)==napoveda_char) {
		    			napoveda_countch++;
		    		}
		    	}
		    	napoveda_countoch=napoveda_countch;
		    	napoveda_count=napoveda_maxcount;
				if (napoveda_countch<napoveda_maxcount+1) napoveda_count=napoveda_countch-1;
				napoveda_last=System.currentTimeMillis();
				
				timerNapoveda = new Timer();
		        timerNapoveda.scheduleAtFixedRate(new TimerNapoveda(), (napoveda_auto_delay+3)*1000, napoveda_auto_delay*1000);
		        
				sendMessage(channel,"*74* "+otazka+" *74*");
				sendMessage(channel,"*29* Nápověda (0/"+napoveda_count+"): "+napoveda+" *29*");
		} catch (Exception e1) {
			e1.printStackTrace();
		}    
    }
    
    public synchronized void newNapoveda() {
    	if (napoveda_num==napoveda_count) {
    		return;
    	}
    	if ((System.currentTimeMillis()-napoveda_last)<napoveda_timelimit*1000) {
    		return;    	
    	}

    	napoveda_last=System.currentTimeMillis();
    	napoveda_num++;
    	Random random = new Random();
    	int count=(int)java.lang.Math.ceil(napoveda_countoch/(napoveda_count+1));
    	for (int c=0 ; c<count ; c++) {
    		int pos = random.nextInt(napoveda_countch-1);
    		int b=0;
    		int a=0;
    		for (a=0 ; a<napoveda.length() ; a++) {
    			if (napoveda.charAt(a)==napoveda_char) {
    				if (b==pos) break;
    				b++;
    			}
    		}
    		napoveda=napoveda.substring(0,a)+odpoved.charAt(a)+napoveda.substring(a+1);
    		napoveda_countch--;
    	}
    	sendMessage(channel,"*29* Nápověda ("+napoveda_num+"/"+napoveda_count+"): "+napoveda+" *29*");
    }
    
    public int getIdByNick(String nick) throws java.sql.SQLException {
		String query="SELECT id FROM nicks WHERE LOWER(nick)=LOWER(?)";
		PreparedStatement ps=mysql.getConn().prepareStatement(query);
		ps.setString(1,nick);
		ResultSet rs=ps.executeQuery();
		rs.next();
		int id = rs.getInt("id");
		rs.close();
		ps.close();
		return id;
    }
    
    public boolean getBlockedByNick(String nick) throws java.sql.SQLException {
		String query="SELECT blocked FROM nicks WHERE LOWER(nick)=LOWER(?)";
		PreparedStatement ps=mysql.getConn().prepareStatement(query);
		ps.setString(1,nick);
		ResultSet rs=ps.executeQuery();
		rs.next();
		boolean blocked = rs.getBoolean("blocked");
		rs.close();
		ps.close();
		return blocked;
    }
    
    public boolean tableExists(String table) {
		String query="SELECT * FROM "+table+" LIMIT 0";
		ResultSet rs;
		PreparedStatement ps;
		try {
			ps=mysql.getConn().prepareStatement(query);
			rs=ps.executeQuery();
			
			ps.close();
			rs.close();
			return true;
		} catch (SQLException e) {
			return false;
		}
    }
    
    public synchronized void addBod(String nick, int bodu) {
    	try {
    		ResultSet rs;
    		PreparedStatement ps;
    		String query;
    		
    		int nick_id;
			try {
				nick_id = getIdByNick(nick);
				mysql.query("UPDATE nicks SET body=body+"+bodu+" WHERE id="+nick_id);				
			} catch (java.sql.SQLException e) {
				query="INSERT INTO nicks (nick,body,added) VALUES (?,?,now())";
	    		ps=mysql.getConn().prepareStatement(query);
	    		ps.setString(1,nick);
	    		ps.setInt(2,bodu);
	    		ps.executeUpdate();
				ps.close();
				
				nick_id=getIdByNick(nick);
			}

			// mesicni bodovani
    		String mesic;
    		Calendar now = Calendar.getInstance();
    		mesic = 
    			Integer.toString(now.get(Calendar.YEAR))
    		    +"_"+
    			Integer.toString(now.get(Calendar.MONTH));
    		
    		if (!tableExists("score_"+mesic)) {
    			mysql.query("CREATE TABLE score_"+mesic+" (id int AUTO_INCREMENT PRIMARY KEY, nick int, body int)");
    		}

			query="SELECT id FROM score_"+mesic+" WHERE nick=?";
    		ps=mysql.getConn().prepareStatement(query);
    		ps.setInt(1,nick_id);
    		rs=ps.executeQuery();
    		rs.next();

    		try {
				int id = rs.getInt("id");
				mysql.query("UPDATE score_"+mesic+" SET body=body+"+bodu+" WHERE id="+id);				
			} catch (java.sql.SQLException se) {
				query="INSERT INTO score_"+mesic+" (nick,body) VALUES (?,?)";
	    		PreparedStatement ps2=mysql.getConn().prepareStatement(query);
	    		ps2.setInt(1,nick_id);
	    		ps2.setInt(2,bodu);
	    		ps2.executeUpdate();
				ps2.close();
			}
			rs.close();
			ps.close();
			
			
    	} catch (Exception e) {
			e.printStackTrace();
		}
    }
    
    public synchronized void showTop() {
		try {
			String query = "SELECT nick,body FROM nicks ORDER BY body DESC LIMIT 10";
			PreparedStatement ps=mysql.getConn().prepareStatement(query);
			ResultSet rs = ps.executeQuery();

    		String msg="";
			int pos=0;
			while (rs.next()) {
				pos++;
				msg+=pos+". "+rs.getString("nick")+" "+rs.getInt("body")+" ; ";
			}
			rs.close();
			ps.close();
			sendMessage(channel,msg);
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}	
    }
    
    public synchronized void showPlayerInfo(String nick) {
    	try {
    		int body;
    		int pozice;
    		String behind;
    		int behind_body;
    		String msg;

    		String query;
    		PreparedStatement ps;
			ResultSet rs;
			
    		query="SELECT body FROM nicks WHERE nick=?";
    		ps=mysql.getConn().prepareStatement(query);
    		ps.setString(1,nick);
    		rs=ps.executeQuery();
    		rs.next();
    		body=rs.getInt("body");
    		rs.close();
    		ps.close();
    		
    		query="SELECT COUNT(*)+1 AS pozice FROM nicks WHERE body>?";
    		ps=mysql.getConn().prepareStatement(query);
			ps.setInt(1,body);
			rs=ps.executeQuery();
    		rs.next();
    		pozice=rs.getInt("pozice");
    		rs.close();
    		ps.close();

    		msg="*28* "+nick+" je na "+pozice+". místě s "+body+" bod"+word_ending7(body);
    		
			query="SELECT nick,body FROM nicks WHERE (body>? OR body=?) AND nick!=? ORDER BY body LIMIT 0,1";
			ps=mysql.getConn().prepareStatement(query);
			ps.setInt(1,body);
			ps.setInt(2,body);
			ps.setString(3,nick);
			rs=ps.executeQuery();
			
			try {
    			rs.next();
        		behind=rs.getString("nick");
        		behind_body=rs.getInt("body");
				if ((behind_body-body)==0) {
					msg+=" zároveň s ";
				} else {
					msg+=" hned za ";
				}
				msg+=behind;
				if ((behind_body-body)!=0) {
					msg+=" s "+behind_body+" bod"+word_ending7(behind_body)+". ";
					msg+="Chybí mu na něj "+(behind_body-body)+" bod"+word_ending3(behind_body-body);
				}
    		} catch (SQLException se) {
    		}
    		rs.close();
    		ps.close();
    		    		    		
    		msg+=" *28*";
    		sendMessage(channel,msg);
    	} catch (SQLException e) {
    		e.printStackTrace();
    	} catch (Exception e) {
    		e.printStackTrace();
    	}	
    }
    
    public synchronized String word_ending7(int i) {
    	if (i==1) return "em";
    		else return "y";
    }

    public synchronized String word_ending3(int i) {
    	if (i>4) return "u";
    	if (i>1) return "y";
    		else return "";
    }
    
    class TimerNapoveda extends TimerTask {
    	public void run() {
        	if (napoveda_num==napoveda_count) {
        		sendMessage(channel,"Nikdo? Odpověď na otázku číslo "+otazka_id+" je "+odpoved+". Zkusíme další...");
        		newOtazka();
        	} else {
        		newNapoveda();
       	}
    	}
    }

    class TimerNewOtazka extends TimerTask {
    	public void run() {
			String[] rooms = getChannels();
			if (rooms.length==0) {
			    joinChannel(channel);
			}
			
			try {
				// overeni jestli jiz neprobehly vsechny otazky
				String query="SELECT COUNT(*) AS pocet FROM otazky WHERE schvaleni=0 AND last IS NULL AND (game & 1) != 0 ";
				PreparedStatement ps=mysql.getConn().prepareStatement(query);
				ResultSet rs=ps.executeQuery();
				rs.next();
				int pocet=rs.getInt("pocet");
				rs.close();
				ps.close();
				if (pocet==0) {
			    	sendMessage(channel,"Došly otázky, jedeme znovu!");
					mysql.query("UPDATE otazky SET last=NULL");
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
			
			newOtazka_reall();
        }
    }

    protected void onVersion(String sourceNick, String sourceLogin, String sourceHostname, String target) {
    }
    
    protected void onPing(String sourceNick, String sourceLogin, String sourceHostname, String target, String pingValue) {
    }

    protected void onTime(String sourceNick, String sourceLogin, String sourceHostname, String target) {
    }
    
    protected void onFinger(String sourceNick, String sourceLogin, String sourceHostname, String target) {
    }
} 
