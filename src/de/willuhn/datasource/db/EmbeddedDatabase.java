/**********************************************************************
 * $Source: /cvsroot/jameica/datasource/src/de/willuhn/datasource/db/EmbeddedDatabase.java,v $
 * $Revision: 1.13 $
 * $Date: 2004/06/30 20:58:07 $
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
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;

import com.mckoi.database.TableDataConglomerate;
import com.mckoi.database.TransactionSystem;
import com.mckoi.database.control.DBController;
import com.mckoi.database.control.DBSystem;
import com.mckoi.database.control.DefaultDBConfig;
import com.mckoi.util.UserTerminal;

import de.willuhn.datasource.rmi.DBService;
import de.willuhn.util.Logger;
import de.willuhn.util.MultipleClassLoader;

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

	private MultipleClassLoader classLoader = null;
	
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

		if (!this.path.canWrite())
			throw new IOException("write permission failed in " + this.path.getAbsolutePath());


		config = new DefaultDBConfig(this.path);
		config.setDatabasePath(this.path.getAbsolutePath());
		config.setLogPath(this.path.getAbsolutePath() + "/log");

		control = DBController.getDefault();

		if (!control.databaseExists(config))
			create();
	}

	/**
	 * Definiert den zu verwendenden ClassLoader.
	 * Die Funktion ist ein Zugestaendnis an die Plugin-Funktionalitaet
	 * von Jameica. Da dort Jars zur Laufzeit geladen und zum Classpath
	 * hinzugefuegt werden und der Service (momentan nur DBService)
	 * die Fach-Klassen kennen muss, fuer die er die Daten aus der
	 * Datenbank lesen soll, braucht er einen Classloader, der auch
	 * die Klassen der Plugins kennt.
	 * @param loader
	 */
  public void setClassLoader(MultipleClassLoader loader)
	{
			this.classLoader = loader;
	}

  /**
   * Erstellt eine neue Datenbank, falls sie noch nicht existiert.
   * @throws IOException Wenn ein Fehler bei der Erstellung auftrat.
   */
  private synchronized void create() throws IOException
	{
		if (control.databaseExists(config))
			return;

		if (!this.path.exists())
		{
			Logger.info("creating directory " + path.getAbsolutePath());
			path.mkdir();
		}

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

		HashMap map = new HashMap();
		map.put("driver","com.mckoi.JDBCDriver");
		map.put("url",":jdbc:mckoi:local://" + path.getAbsolutePath() + "/db.conf?user=" + username + "&password=" + password);
		db = new DBServiceImpl(map);
		db.setClassLoader(classLoader);
		return db;
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
}


/**********************************************************************
 * $Log: EmbeddedDatabase.java,v $
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
