/**********************************************************************
 * $Source: /cvsroot/jameica/datasource/src/de/willuhn/datasource/db/EmbeddedDatabase.java,v $
 * $Revision: 1.20 $
 * $Date: 2004/11/12 18:21:56 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.datasource.db;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringWriter;
import java.rmi.RemoteException;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import sun.misc.BASE64Encoder;

import com.mckoi.database.TableDataConglomerate;
import com.mckoi.database.TransactionSystem;
import com.mckoi.database.control.DBController;
import com.mckoi.database.control.DBSystem;
import com.mckoi.database.control.DefaultDBConfig;
import com.mckoi.util.UserTerminal;

import de.willuhn.datasource.rmi.DBService;
import de.willuhn.logging.Logger;

/**
 * Embedded Datenbank die man jederzeit gut gebrauchen kann.
 * Einfach eine Instanz mit User, Passwort und Pfad im Konstruktor
 * erzeugen, die Datenbank wird geladen oder (wenn sie noch nicht existiert)
 * automatisch im genannten Verzeichnis angelegt.
 */
public class EmbeddedDatabase
{

	private File path = null;
	private DefaultDBConfig config = null;
	private DBController control = null;
	private DBService db = null;
	
	private String username = null;
	private String password = null;

	private static String defaultConfig =
		"database_path=.\n" +		"log_path=./log\n" +		"root_path=configuration\n" +		"jdbc_server_port=9157\n" +		"ignore_case_for_identifiers=disabled\n" +		"data_cache_size=4194304\n" +		"max_cache_entry_size=8192\n" +		"maximum_worker_threads=4\n" +		"debug_log_file=debug.log\n" +		"debug_level=30\n";

  /**
	 * Erzeugt eine neue Instanz der Datenbank.
	 * Existiert sie noch nicht, wird sie automatisch angelegt.
   * @param path Verzeichnis, in dem sich die Datenbank befindet bzw angelegt werden soll.
   * @param username Username.
   * @param password Passwort.
   * @throws Exception
   */
  public EmbeddedDatabase(String path, String username, String password) throws Exception
	{
		if (username == null || username.length() == 0)
		{
			throw new Exception("please enter a username");
		}

		if (password == null || password.length() == 0)
		{
			throw new Exception("please enter a password");
		}

		if (path == null || path.length() == 0)
		{
			throw new IOException("please enter a path");
		}

		this.path = new File(path);
		this.username = username;
		this.password = password;

		if (!this.path.exists())
		{
			Logger.info("creating directory " + this.path.getAbsolutePath());
			this.path.mkdir();
		}

		if (!this.path.canWrite())
			throw new IOException("write permission failed in " + this.path.getAbsolutePath());


		config = new DefaultDBConfig(this.path);
		config.setDatabasePath(this.path.getAbsolutePath());
		config.setLogPath(this.path.getAbsolutePath() + "/log");

		control = DBController.getDefault();

		if (!exists())
			create();
	}

	/**
	 * Prueft, ob die Datenbank existiert.
   * @return true, wenn sie existiert.
   */
  public synchronized boolean exists()
	{
		return control.databaseExists(config);
	}

  /**
   * Erstellt eine neue Datenbank, falls sie noch nicht existiert.
   * @throws IOException Wenn ein Fehler bei der Erstellung auftrat.
   */
  public synchronized void create() throws IOException
	{
		if (exists())
			return;

		// Config-Datei kopieren
		Logger.info("creating database config file");
		try {
			FileOutputStream fos = new FileOutputStream(this.path.getAbsolutePath() + "/db.conf");
			fos.write(defaultConfig.getBytes());
			fos.close();
		}
		catch (IOException e)
		{
			Logger.error("failed",e);
			throw new IOException(e.getMessage());
		}

		try {

		  DBSystem session = null;

			Logger.info("creating database");
			session = control.createDatabase(config,username,password);
			session.close();
	  }
		catch (Error error)
		{
			Logger.error("error while creating database",error);
			throw new IOException(error.getMessage());
		}
		catch (Exception e)
		{
			Logger.error("error while creating database",e);
			throw new IOException(e.getMessage());
		}
	}

	/**
	 * Loescht die Datenbank gnadenlos.
	 * <b>Diese Funktion bitte MIT BEDACHT verwenden!</b>.
   * @throws IOException
   */
  public synchronized void delete() throws IOException
	{
		Logger.warn("deleting database in " + path.getAbsolutePath());
		if (!exists())
		{
			Logger.warn("database does not exist, skipping");
			return;
		}
		DBSystem system = control.startDatabase(config);
		system.setDeleteOnClose(true);
		system.close();
		Logger.warn("database deleted");
	}

	/**
	 * Fuehrt das uebergebene File mit SQL-Kommandos auf der Datenbank aus.
	 * Die Funktion liefert kein DBIteratorImpl zurueck, weil sie typischerweise
	 * fuer die Erstellung der Tabellen verwendet werden sollte. Wenn das
	 * Plugin also bei der Installation seine SQL-Tabellen erstellen will,
	 * kann es das am besten hier machen.
   * @param file das auszufuehrende SQL-Script.
	 * @throws IOException Wenn beim Lesen des SQL-Scripts Fehler auftraten.
   * @throws SQLException Wenn beim Ausfuehren Fehler auftraten.
   */
  public void executeSQLScript(File file) throws IOException, SQLException
	{
		if (file == null)
			throw new NullPointerException("sql file not given");
		Logger.debug("executing " + file.getAbsolutePath());
		if (!file.canRead() || !file.exists())
			throw new IOException("SQL file does not exist or is not readable");
		

		Connection conn = null;
		Statement stmt = null;
		DBSystem session = null;

		try {

			BufferedReader br = null;
			String thisLine = null;
			StringBuffer all = new StringBuffer();

			try {
				br =  new BufferedReader(new FileReader(file));
				while ((thisLine =  br.readLine()) != null)
				{
					if (!(thisLine.length() > 0))  // Zeile enthaelt nichts
						continue;
					if (thisLine.matches(" *?"))	 // Zeile enthaelt nur Leerzeichen
						continue;
					if (thisLine.startsWith("--")) // Kommentare
						continue;
					if (thisLine.startsWith("\n") || thisLine.startsWith("\r")) // Leerzeile
						continue;
					all.append(thisLine.trim());
				}
			}
			catch (IOException e)
			{
				throw e;
			}
			finally
			{
				try {
					br.close();
				}
				catch (Exception e) {}
			}


			session = control.startDatabase(config);

			conn = session.getConnection(username,password);
			conn.setAutoCommit(false);

			stmt = conn.createStatement();

			Logger.info("executing sql commands from " + file.getAbsolutePath());
			String[] tables = all.toString().split(";");
			for (int i=0;i<tables.length;++i)
			{
				stmt.executeUpdate(tables[i]);
        conn.commit();
			}
		}
		catch (SQLException e)
		{
			try {
				conn.rollback();
			}
			catch (Exception e2) { /* useless */ }

			Logger.error("error while executing sql script",e);
			throw new SQLException("exception while executing sql script: " + e.getMessage());
		}
		finally {
			try {
				stmt.close();
				conn.close();
				session.close();
			}
			catch (Exception e2) { /* useless */ }
		}
		
	}
	
	/**
	 * Liefert den Verzeichnis-Pfad, in dem sich die Datenbank befindet.
   * @return Pfad zur Datenbank.
   */
  public File getPath()
	{
		return this.path;
	}

  /**
	 * Liefert einen DBService zu dieser Datenbank.
   * @return DBService.
   * @throws RemoteException
   */
  public DBService getDBService() throws RemoteException
	{
    if (db != null)
      return db;

		db = new DBServiceImpl("com.mckoi.JDBCDriver",":jdbc:mckoi:local://" + path.getAbsolutePath() + "/db.conf?user=" + username + "&password=" + password);
		return db;
	}

	/**
	 * Liefert eine MD5-Checksumme (BASE64-encoded) der Datenbank-Eigenschaften.
	 * Diese kann gegen eine gespeicherte Version verglichen werden, um zu pruefen,
	 * ob die Datenbank den erwarteten Eigenschaften entspricht. Das ist z.Bsp.
	 * sinnvoll, wenn man pruefen will, ob das Datenbank-Modell zur Software-Version
	 * passt.<br>
	 * Szenario: Eine Anwendung moechte seine embedded Datenbank auf den aktuellen
	 * Stand bringen, weiss jedoch nicht, welche SQL-Statements hierfuer noetig sind,
	 * da das momentane Datenbank-Layout nicht bekannt ist.<br>
	 * Loesung: Die Anwendung haelt fuer alle moeglichen Versionsstaende der Datenbank
	 * je eine MD5-Summe bereit und vergleicht diese mit der aktuellen. Somit kann
	 * sie herausfinden, welche SQL-Befehle noch noetig sind, um die Anwendung
	 * auf den aktuellen Stand zu bringen.<br>
	 * Eine weitere Einsatzmoeglichkeit ist das Detektieren von Datenbank-Manipulationen.
	 * Sprich: Wurde die Datenbank von einem Dritten geaendert, laesst sich dies durch
	 * Pruefen der Checksumme herausfinden.
   * @return MD5-Checksumme.
   * @throws Exception
   */
  public String getMD5Sum() throws Exception
	{
		StringBuffer sum = new StringBuffer();
		ResultSet rs = null;
		try {
			Connection conn = getConnection();
			DatabaseMetaData dmd = conn.getMetaData();
			rs = dmd.getColumns(null,"APP",null,null);
			while (rs.next())
			{
				sum.append(rs.getString("TABLE_NAME") + ":" + rs.getString("COLUMN_NAME") + ":" + rs.getString("TYPE_NAME") + "\n");
			}
			MessageDigest md = MessageDigest.getInstance("MD5");
			byte[] hash = md.digest(sum.toString().getBytes());
			BASE64Encoder encoder = new BASE64Encoder();
			return encoder.encode(hash);
		}
		finally
		{
			try {
				rs.close();
			}
			catch (Exception e) { /*useless*/ }
		}
	}

  /**
   * Repariert die Datenbank.
   * @param terminal Terminal, welches zur Ausgabe und Interaktion verwendet werden soll.
   * <code>UserTerminal</code> ist ein Interface und muss vom Benutzer implementiert werden.
   */
  public void repair(UserTerminal terminal)
	{
		TransactionSystem system = new TransactionSystem();
		DefaultDBConfig config = new DefaultDBConfig();
		config.setDatabasePath(path.getAbsolutePath());
		config.setLogPath("");
		config.setMinimumDebugLevel(50000);
		// We do not use the NIO API for repairs for safety.
		config.setValue("do_not_use_nio_api", "enabled");
		system.setDebugOutput(new StringWriter());
		system.init(config);
		final TableDataConglomerate conglomerate =
										 new TableDataConglomerate(system, system.storeSystem());
		// Check it.
		conglomerate.fix("DefaultDatabase", terminal);

		// Dispose the transaction system
		system.dispose();
	}
	
  /**
	 * Liefert eine Connection zu dieser Datenbank.
   * @return Connection.
   * @throws Exception
   */
  public Connection getConnection() throws Exception
	{
		Class.forName("com.mckoi.JDBCDriver");
		return DriverManager.getConnection(":jdbc:mckoi:local://" + path.getAbsolutePath() + "/db.conf?user=" + username + "&password=" + password);
	}
}


/**********************************************************************
 * $Log: EmbeddedDatabase.java,v $
 * Revision 1.20  2004/11/12 18:21:56  willuhn
 * *** empty log message ***
 *
 * Revision 1.19  2004/08/18 23:14:00  willuhn
 * @D Javadoc
 *
 * Revision 1.18  2004/07/23 15:51:07  willuhn
 * @C Rest des Refactorings
 *
 * Revision 1.17  2004/07/21 23:53:56  willuhn
 * @C massive Refactoring ;)
 *
 * Revision 1.16  2004/07/09 00:04:19  willuhn
 * @C Redesign
 *
 * Revision 1.15  2004/07/04 17:08:09  willuhn
 * *** empty log message ***
 *
 * Revision 1.14  2004/06/30 21:58:12  willuhn
 * @N md5 check for database
 *
 * Revision 1.13  2004/06/30 20:58:07  willuhn
 * @C some refactoring
 *
 * Revision 1.12  2004/04/22 23:48:04  willuhn
 * *** empty log message ***
 *
 * Revision 1.11  2004/04/13 23:13:09  willuhn
 * *** empty log message ***
 *
 * Revision 1.10  2004/03/18 01:24:17  willuhn
 * @C refactoring
 *
 * Revision 1.9  2004/03/06 18:24:34  willuhn
 * @D javadoc
 *
 * Revision 1.8  2004/02/09 13:04:34  willuhn
 * @C misc
 *
 * Revision 1.7  2004/01/29 00:46:01  willuhn
 * *** empty log message ***
 *
 * Revision 1.6  2004/01/29 00:13:11  willuhn
 * *** empty log message ***
 *
 * Revision 1.5  2004/01/27 23:54:32  willuhn
 * *** empty log message ***
 *
 * Revision 1.4  2004/01/25 18:39:49  willuhn
 * *** empty log message ***
 *
 * Revision 1.3  2004/01/23 00:25:52  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2004/01/10 14:52:19  willuhn
 * @C package removings
 *
 * Revision 1.1  2004/01/08 20:46:44  willuhn
 * @N database stuff separated from jameica
 *
 * Revision 1.5  2004/01/06 20:32:59  willuhn
 * *** empty log message ***
 *
 * Revision 1.4  2004/01/05 19:14:45  willuhn
 * *** empty log message ***
 *
 * Revision 1.3  2004/01/05 18:27:13  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2004/01/04 18:48:36  willuhn
 * @N config store support
 *
 * Revision 1.1  2004/01/03 18:08:05  willuhn
 * @N Exception logging
 * @C replaced bb.util xml parser with nanoxml
 *
 * Revision 1.2  2003/12/30 19:11:29  willuhn
 * @N new splashscreen
 *
 * Revision 1.1  2003/12/30 17:44:41  willuhn
 * @N automatic database create
 *
 **********************************************************************/
