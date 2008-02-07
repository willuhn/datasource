/**********************************************************************
 * $Source: /cvsroot/jameica/datasource/src/de/willuhn/datasource/db/DBServiceImpl.java,v $
 * $Revision: 1.39 $
 * $Date: 2008/02/07 23:56:39 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.datasource.db;

import java.lang.reflect.Constructor;
import java.rmi.RemoteException;
import java.rmi.server.ServerNotActiveException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.datasource.rmi.DBObject;
import de.willuhn.datasource.rmi.DBService;
import de.willuhn.datasource.rmi.ResultSetExtractor;
import de.willuhn.logging.Logger;
import de.willuhn.util.ClassFinder;

/**
 * Diese Klasse implementiert eine ueber RMI erreichbaren Datenbank. 
 * @author willuhn
 */
public class DBServiceImpl extends UnicastRemoteObject implements DBService
{
  private static final long serialVersionUID = 1L;
  
  private String jdbcDriver   = null;
  private String jdbcUrl      = null;
  private String jdbcUsername = null;
  private String jdbcPassword = null;
  
  private Map connections     = null;

  private boolean started		  = false;
  private boolean startable		= true;
  
  private ClassFinder finder  = null;
  private ClassLoader loader  = null;

  /**
   * Erzeugt eine neue Instanz.
   * @throws RemoteException
   */
  public DBServiceImpl() throws RemoteException
  {
    this(0,null,null,null,null);
  }

  /**
   * Erzeugt eine neue Instanz.
   * @param jdbcDriver JDBC-Treiber-Klasse.
   * @param jdbcURL JDBC-URL.
   * @throws RemoteException
   */
	public DBServiceImpl(String jdbcDriver, String jdbcURL) throws RemoteException
	{
    this(0,jdbcDriver,jdbcURL,null,null);
	}
  
  /**
   * Erzeugt eine neue Instanz.
   * @param jdbcDriver JDBC-Treiber-Klasse.
   * @param jdbcURL JDBC-URL.
   * @param jdbcUsername Username.
   * @param jdbcPassword Passwort.
   * @throws RemoteException
   */
  public DBServiceImpl(String jdbcDriver, String jdbcURL, String jdbcUsername, String jdbcPassword) throws RemoteException
  {
    this(0,jdbcDriver,jdbcURL,jdbcUsername,jdbcPassword);
  }
  /**
   * Erzeugt eine neue Instanz.
   * @param rmiPort
   * @param jdbcDriver JDBC-Treiber-Klasse.
   * @param jdbcURL JDBC-URL.
   * @param jdbcUsername Username.
   * @param jdbcPassword Passwort.
   * @throws RemoteException
   */
  public DBServiceImpl(int rmiPort, String jdbcDriver, String jdbcURL, String jdbcUsername, String jdbcPassword) throws RemoteException
  {
    super(rmiPort);
    
		Logger.debug("using jdbc driver  : " + jdbcDriver);
		Logger.debug("using jdbc url     : " + jdbcURL);
		Logger.debug("using jdbc username: " + jdbcUsername);
    this.jdbcUrl      = jdbcURL;
    this.jdbcDriver   = jdbcDriver;
    this.jdbcUsername = jdbcUsername;
    this.jdbcPassword = jdbcPassword;

    this.connections = Collections.synchronizedMap(new HashMap());
  }

	/**
	 * Liefert die Connection, die dieser Service gerade verwendet.
   * @return Connection.
   * @throws RemoteException
   */
	protected Connection getConnection() throws RemoteException
	{
    String key = getClientIdentifier();

    Connection conn = (Connection) this.connections.get(key);

    if (conn != null)
    {
      try
      {
        checkConnection(conn);
      }
      catch (SQLException e)
      {
        Logger.info("connection check failed, creating new connection. message: " + e.getMessage());
        conn = null;
      }
    }
    
    if (conn == null)
    {
      conn = createConnection();
      try
      {
        conn.setAutoCommit(getAutoCommit());
        int trLevel = getTransactionIsolationLevel();
        if (trLevel > 0)
        {
          Logger.info("transaction isolation level: " + trLevel);
          conn.setTransactionIsolation(trLevel);
        }
      }
      catch (SQLException e)
      {
        throw new RemoteException("autocommit=false failed or transaction isolation level not supported",e);
      }
      Logger.info("created new connection for " + (key == null ? "<local>" : key));
      this.connections.put(key,conn);
    }

    return conn;
	}
  
  /**
   * Liefert den Client-Host oder <code>null</code>.
   * @return ein Client-Identifier.
   */
  private String getClientIdentifier()
  {
    try
    {
      return UnicastRemoteObject.getClientHost();
    }
    catch (Throwable t)
    {
      // ignore
    }
    return null;
  }
  /**
   * Erstellt eine neue Connection.
   * @return die neu erstellte Connection.
   * @throws RemoteException
   */
  private Connection createConnection() throws RemoteException
  {
    Logger.info("creating new connection");
    String url = getJdbcUrl();
    try {
      String username = getJdbcUsername();
      String password = getJdbcPassword();
      if (username != null && username.length() > 0 && password != null)
        return DriverManager.getConnection(url,username,password);

      return DriverManager.getConnection(url);
    }
    catch (SQLException e2)
    {
      throw new RemoteException("connection to database." + url + " failed",e2);
    }
  }
  
  /**
   * Schliesst die uebergebene Connection.
   * @param conn
   */
  private void closeConnection(Connection conn)
  {
    if (conn == null)
      return;
    try
    {
      Logger.info("commit connection");
      try
      {
        conn.commit();
      }
      catch (Exception e)
      {
        Logger.warn("commit failed");
      }
      Logger.info("closing connection");
      conn.close();
      Logger.info("connection closed");
    }
    catch (Throwable t)
    {
      Logger.error("error while closing connection. message: " + t.getMessage());
    }
  }
  
  /**
   * Kann von abgeleiteten Klassen ueberschrieben werden, um die Connection
   * zu testen.
   * @param conn die zu testende Connection. Ist nie <code>null</code>.
   * @throws SQLException
   */
  protected void checkConnection(Connection conn) throws SQLException
  {
  }

  /**
   * Definiert einen optionalen Classfinder, der von dem Service
   * zum Laden von Objekten genommen werden soll.
   * Konkret wird er in <code>creatObject</code> und  <code>createList</code>
   * verwendet, um zum uebergebenen Interface eine passende Implementierung
   * zu finden. Dabei wird die Funktion <code>findImplementor()</code> im
   * ClassFinder befragt.<br>
   * Wurde kein ClassFinder angegeben, versucht der Service direkt die
   * uebergebene Klasse zu instanziieren. Ist dies der Fall, koennen den
   * beiden create-Methoden natuerliche keine Interfaces-Klassen uebergeben werden.
   * @param finder zu verwendender ClassFinder.
   */
  protected void setClassFinder(ClassFinder finder)
  {
    this.finder = finder;
  }

	/**
	 * Definiert einen optionalen benutzerdefinierten Classloader.
	 * Wird er nicht gesetzt, wird <code>Class.forName()</code> benutzt.
   * @param loader Benutzerdefinierter Classloader.
   */
  protected void setClassloader(ClassLoader loader)
	{
		this.loader = loader;
	}

	/**
   * @see de.willuhn.datasource.Service#isStartable()
   */
  public synchronized boolean isStartable() throws RemoteException
	{
		return startable;
	}

  /**
   * @see de.willuhn.datasource.Service#start()
   */
  public synchronized void start() throws RemoteException
  {
		if (isStarted()) return;
		
		if (!isStartable())
			throw new RemoteException("service restart not allowed");

		Logger.info("starting db service");
    try {
			Logger.info("request from host: " + UnicastRemoteObject.getClientHost());
    }
    catch (ServerNotActiveException soe) {}

    String driver = getJdbcDriver();
		try {
			if (loader != null)
			{
				try
				{
					DriverManager.registerDriver(new MyDriver(driver,loader));
				}
				catch (Throwable t)
				{
					throw new RemoteException("unable to load jdbc driver",t);
				}
			}
			else
			{
				Class.forName(driver);
			}
		}
		catch (ClassNotFoundException e2)
		{
			Logger.error("unable to load jdbc driver " + driver,e2);
			throw new RemoteException("unable to load jdbc driver " + driver,e2);
		}

    started = true;
  }


  /**
   * @see de.willuhn.datasource.Service#stop(boolean)
   */
  public synchronized void stop(boolean restartAllowed) throws RemoteException
  {
    if (!started)
    {
    	Logger.info("service allready stopped");
			return;
    }

    String key = getClientIdentifier();
    if (key != null)
    {
      // Das ist eine Remote-Client, der sich disconnected.
      // Wir stoppen nicht den Service sondern melden nur
      // den Client ab
      Logger.info("disconnect client " + key);
      closeConnection((Connection) this.connections.remove(key));
      return;
    }
    try
    {
      startable = restartAllowed;

      Logger.info("stopping db service");
      try {
        Logger.info("stop request from host: " + getClientHost());
      }
      catch (ServerNotActiveException soe) {}

      Logger.debug("db service: object cache matches: " + ObjectMetaCache.getStats() + " %");

      int count = 0;
      synchronized(this.connections)
      {
        Iterator i = this.connections.keySet().iterator();
        while (i.hasNext())
        {
          key = (String) i.next();
          closeConnection((Connection) this.connections.remove(key));
          count++;
        }
      }
      Logger.info("db service stopped [" + count + " connection(s) closed]");
    }
    finally
    {
      started = false;
    }
  }
  

  /**
   * Erzeugt ein neues Objekt aus der angegeben Klasse.
   * @param c Klasse des zu erstellenden Objekts.
   * @return das erzeugte Objekt.
   * @throws Exception wenn beim Erzeugen des Objektes ein Fehler auftrat.
   */
  private DBObject create(Class c) throws Exception
  {
    Class clazz = c;
    if (this.finder != null)
    {
      Class[] found = finder.findImplementors(c);
      clazz = found[found.length-1]; // wir nehmen das letzte Element. Das ist am naehesten dran.
    }
    if (clazz.isInterface())
      throw new Exception("no classfinder defined: unable to find implementor for interface " + c.getName());
    Constructor ct = clazz.getConstructor(new Class[]{});
    ct.setAccessible(true);

    AbstractDBObject o = (AbstractDBObject) ct.newInstance(new Object[] {});
    o.setService(this);
    o.init();
    return o;
  }

  /**
   * @see de.willuhn.datasource.rmi.DBService#createObject(java.lang.Class, java.lang.String)
   */
  public DBObject createObject(Class c, String identifier) throws RemoteException
  {
		checkStarted();
    try {
			Logger.debug("try to create new DBObject. request from host: " + getClientHost());
    }
    catch (ServerNotActiveException soe) {}

    try {
      DBObject o = create(c);
      o.load(identifier);
      return o;
    }
    catch (RemoteException re)
    {
    	throw re;
    }
    catch (Exception e)
    {
			Logger.error("unable to create object " + (c == null ? "unknown" : c.getName()),e);
      throw new RemoteException("unable to create object " + (c == null ? "unknown" : c.getName()),e);
    }
  }

  /**
   * @see de.willuhn.datasource.rmi.DBService#createList(java.lang.Class)
   */
  public DBIterator createList(Class c) throws RemoteException
	{
		checkStarted();
    try {
			Logger.debug("try to create new DBIterator. request from host: " + getClientHost());
    }
    catch (ServerNotActiveException soe) {}

		try {
      DBObject o = create(c);
			return new DBIteratorImpl((AbstractDBObject)o,this);
		}
		catch (RemoteException re)
		{
			throw re;
		}
		catch (Exception e)
		{
			Logger.error("unable to create list for object " + c.getName(),e);
			throw new RemoteException("unable to create list for object " + c.getName(),e);
		}
	}

  /**
   * @see de.willuhn.datasource.rmi.DBService#execute(java.lang.String, java.lang.Object[], de.willuhn.datasource.rmi.ResultSetExtractor)
   */
  public Object execute(String sql, Object[] params, ResultSetExtractor extractor) throws RemoteException
  {
    checkStarted();
    PreparedStatement ps = null;
    ResultSet rs = null;
    try
    {
      ps = getConnection().prepareStatement(sql);
      if (params != null)
      {
        for (int i=0;i<params.length;++i)
        {
          Object o = params[i];
          if (o == null)
            ps.setNull((i+1), Types.NULL);
          else
            ps.setObject((i+1),params[i]);
        }
      }

      rs = ps.executeQuery();
      return extractor.extract(rs);
    }
    catch (SQLException e)
    {
      Logger.error("error while executing sql statement",e);
      throw new RemoteException("error while executing sql statement: " + e.getMessage(),e);
    }
    finally
    {
      if (rs != null)
      {
        try
        {
          rs.close();
        }
        catch (Throwable t)
        {
          Logger.error("error while closing resultset",t);
        }
      }
      if (ps != null)
      {
        try
        {
          ps.close();
        }
        catch (Throwable t2)
        {
          Logger.error("error while closing statement",t2);
        }
      }
    }
  }

  
	/**
	 * Prueft intern, ob der Service gestartet ist und wirft ggf. eine Exception.
   * @throws RemoteException
   */
  private synchronized void checkStarted() throws RemoteException
	{
		if (!isStarted())
			throw new RemoteException("db service not started");
	}

  /**
   * @see de.willuhn.datasource.Service#isStarted()
   */
  public synchronized boolean isStarted() throws RemoteException
  {
    return started;
  }

  /**
   * @see de.willuhn.datasource.Service#getName()
   */
  public String getName() throws RemoteException
  {
    return "database service";
  }
  
  /**
   * Liefert den JDBC-Treiber.
   * @return der JDBC-Treiber.
   * @throws RemoteException
   */
  protected String getJdbcDriver() throws RemoteException
  {
    return this.jdbcDriver;
  }
  
  /**
   * Liefert die JDBC-URL.
   * @return die JDBC-URL.
   * @throws RemoteException
   */
  protected String getJdbcUrl() throws RemoteException
  {
    return this.jdbcUrl;
  }
  
  /**
   * Liefert den JDBC-Usernamen.
   * @return der Username.
   * @throws RemoteException
   */
  protected String getJdbcUsername() throws RemoteException
  {
    return this.jdbcUsername;
  }
  
  
  /**
   * Liefert das JDBC-Passwort.
   * @return das JDBC-Passwort.
   * @throws RemoteException
   */
  protected String getJdbcPassword() throws RemoteException
  {
    return this.jdbcPassword;
  }

  /**
   * Liefert den Transaction-Isolation-Level.
   * @return transactionIsolationLevel Transaction-Isolation-Level (Default:-1). 
   * @see Connection#TRANSACTION_NONE
   * @see Connection#TRANSACTION_READ_COMMITTED
   * @see Connection#TRANSACTION_READ_UNCOMMITTED
   * @see Connection#TRANSACTION_REPEATABLE_READ
   * @see Connection#TRANSACTION_SERIALIZABLE
   * @throws RemoteException
   */
  protected int getTransactionIsolationLevel() throws RemoteException
  {
    return -1;
  }
  
  /**
   * Liefert true, wenn autocommit aktiv sein soll.
   * Default: false.
   * @return Autocommit.
   * @throws RemoteException
   */
  protected boolean getAutoCommit() throws RemoteException
  {
    return false;
  }
  
  /**
   * Liefert true, wenn der DB-Service bei INSERT-Queries <b>vorher</b> die zu verwendende ID ermitteln soll.
   * MySQL zum besitzt eine auto_increment-Funktion, mit der es nicht notwendig ist, die ID beim
   * Insert mit anzugeben. Falls die Datenbank das jedoch nicht korrekt kann (z.Bsp. McKoi), dann
   * kann die Funktion true liefern. In dem Fall wird vor dem Insert ein "select max(id)+1 from table"
   * ausgefuehrt und diese ID fuer das Insert verwendet.
   * <b>Standard-Wert: TRUE</b>
   * @return true, wenn bei Inserts vorher die ID ermittelt werden soll.
   * @throws RemoteException
   */
  protected boolean getInsertWithID() throws RemoteException
  {
    return true;
  }
}

/*********************************************************************
 * $Log: DBServiceImpl.java,v $
 * Revision 1.39  2008/02/07 23:56:39  willuhn
 * @N Explizite Angabe des RMI-Ports moeglich
 *
 * Revision 1.38  2008/02/07 23:42:08  willuhn
 * @C kein implizites Exportieren der Objekte mehr
 *
 * Revision 1.37  2007/07/17 12:40:23  willuhn
 * *** empty log message ***
 *
 * Revision 1.36  2007/04/02 23:00:42  willuhn
 * @B falscher Parameter in BeanUtil#get
 * @N PseudoIterator#asList
 *
 * Revision 1.35  2007/03/02 15:25:03  willuhn
 * @N getInsertWithID um festlegen zu koennen, ob INSERTs mit ID erzeugt werden sollen
 * @C last_insert_id() nur aufrufen, wenn nach dem INSERT noch keine ID vorhanden ist
 *
 * Revision 1.34  2007/01/30 23:17:25  willuhn
 * @D typo
 *
 * Revision 1.33  2006/12/12 13:05:08  willuhn
 * @N connection commit on close
 *
 * Revision 1.32  2006/11/20 22:58:41  willuhn
 * @N autocommit and transaction isolation level are now configurable
 * @N rmi clients can now be disconnected
 *
 * Revision 1.31  2006/09/05 20:52:24  willuhn
 * @N Added ResultsetExtractor (portiert aus Syntax)
 *
 * Revision 1.30  2006/06/19 22:22:48  willuhn
 * @N Ueberschreibbare Getter fuer JDBC-Daten
 *
 * Revision 1.29  2006/03/27 16:26:48  web0
 * @C replaced Session with Hashtable
 *
 * Revision 1.28  2006/03/23 10:23:08  web0
 * @N connections are now created per client host
 * @N checkConnection()
 * @N connections now have a 5 minute timeout
 *
 * Revision 1.27  2005/03/09 01:07:51  web0
 * @D javadoc fixes
 *
 * Revision 1.26  2004/12/07 01:27:58  willuhn
 * @N Dummy Driver
 *
 * Revision 1.25  2004/11/12 18:21:56  willuhn
 * *** empty log message ***
 *
 * Revision 1.24  2004/11/04 17:48:04  willuhn
 * *** empty log message ***
 *
 * Revision 1.23  2004/11/04 17:47:09  willuhn
 * *** empty log message ***
 *
 * Revision 1.22  2004/09/15 22:31:20  willuhn
 * *** empty log message ***
 *
 * Revision 1.21  2004/09/14 23:27:32  willuhn
 * @C redesign of service handling
 *
 * Revision 1.20  2004/09/13 23:26:54  willuhn
 * *** empty log message ***
 *
 * Revision 1.19  2004/08/31 18:14:00  willuhn
 * *** empty log message ***
 *
 * Revision 1.18  2004/08/31 17:33:10  willuhn
 * *** empty log message ***
 *
 * Revision 1.17  2004/08/26 23:19:33  willuhn
 * @N added ObjectNotFoundException
 *
 * Revision 1.16  2004/08/18 23:14:00  willuhn
 * @D Javadoc
 *
 * Revision 1.15  2004/08/11 22:23:51  willuhn
 * @N AbstractDBObject.getLoadQuery
 *
 * Revision 1.14  2004/07/23 16:24:05  willuhn
 * *** empty log message ***
 *
 * Revision 1.13  2004/07/23 15:51:07  willuhn
 * @C Rest des Refactorings
 *
 * Revision 1.12  2004/07/21 23:53:56  willuhn
 * @C massive Refactoring ;)
 *
 * Revision 1.11  2004/06/30 20:58:07  willuhn
 * @C some refactoring
 *
 * Revision 1.10  2004/06/17 00:05:50  willuhn
 * @N GenericObject, GenericIterator
 *
 * Revision 1.9  2004/05/04 23:05:25  willuhn
 * *** empty log message ***
 *
 * Revision 1.8  2004/03/19 18:56:47  willuhn
 * @R removed ping() from within open()
 *
 * Revision 1.7  2004/03/18 01:24:17  willuhn
 * @C refactoring
 *
 * Revision 1.6  2004/03/06 18:24:34  willuhn
 * @D javadoc
 *
 * Revision 1.5  2004/02/27 01:09:51  willuhn
 * *** empty log message ***
 *
 * Revision 1.4  2004/01/29 00:13:11  willuhn
 * *** empty log message ***
 *
 * Revision 1.3  2004/01/25 18:39:49  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2004/01/23 00:25:52  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/01/10 14:52:19  willuhn
 * @C package removings
 *
 * Revision 1.1  2004/01/08 20:46:43  willuhn
 * @N database stuff separated from jameica
 *
 * Revision 1.14  2004/01/05 18:04:46  willuhn
 * @N added MultipleClassLoader
 *
 * Revision 1.13  2004/01/03 18:08:05  willuhn
 * @N Exception logging
 * @C replaced bb.util xml parser with nanoxml
 *
 * Revision 1.12  2003/12/30 17:44:54  willuhn
 * *** empty log message ***
 *
 * Revision 1.11  2003/12/29 16:29:47  willuhn
 * @N javadoc
 *
 * Revision 1.10  2003/12/27 21:23:33  willuhn
 * @N object serialization
 *
 * Revision 1.9  2003/12/22 21:00:34  willuhn
 * *** empty log message ***
 *
 * Revision 1.8  2003/12/15 19:08:01  willuhn
 * *** empty log message ***
 *
 * Revision 1.7  2003/12/13 20:05:21  willuhn
 * *** empty log message ***
 *
 * Revision 1.6  2003/12/12 21:11:29  willuhn
 * @N ObjectMetaCache
 *
 * Revision 1.5  2003/12/11 21:00:54  willuhn
 * @C refactoring
 *
 * Revision 1.4  2003/11/27 00:22:17  willuhn
 * @B paar Bugfixes aus Kombination RMI + Reflection
 * @N insertCheck(), deleteCheck(), updateCheck()
 * @R AbstractDBObject#toString() da in RemoteObject ueberschrieben (RMI-Konflikt)
 *
 * Revision 1.3  2003/11/20 03:48:42  willuhn
 * @N first dialogues
 *
 * Revision 1.2  2003/11/12 00:58:54  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2003/11/05 22:46:19  willuhn
 * *** empty log message ***
 *
 * Revision 1.12  2003/10/28 11:43:02  willuhn
 * @N default DBService and PrinterHub
 *
 * Revision 1.11  2003/10/27 23:42:31  willuhn
 * *** empty log message ***
 *
 * Revision 1.10  2003/10/27 23:36:39  willuhn
 * @N debug messages
 *
 * Revision 1.9  2003/10/27 21:08:38  willuhn
 * @N button to change language
 * @N application.changeLanguageTo()
 * @C DBServiceImpl auto reconnect
 *
 * Revision 1.8  2003/10/27 18:50:57  willuhn
 * @N all service have to implement open() and close() now
 *
 * Revision 1.7  2003/10/27 18:21:57  willuhn
 * @B RMI fixes in business objects
 *
 * Revision 1.6  2003/10/27 11:49:12  willuhn
 * @N added DBIterator
 *
 * Revision 1.5  2003/10/26 17:46:30  willuhn
 * @N DBObject
 *
 * Revision 1.4  2003/10/25 19:49:47  willuhn
 * *** empty log message ***
 *
 * Revision 1.3  2003/10/25 19:25:30  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2003/10/25 17:44:36  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2003/10/25 17:17:51  willuhn
 * @N added Empfaenger
 *
 **********************************************************************/