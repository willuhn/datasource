/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * GNU LESSER GENERAL PUBLIC LICENSE 2.1.
 * Please consult the file "LICENSE" for details. 
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
    this(null,null,null,null);
  }

  /**
   * Erzeugt eine neue Instanz.
   * @param jdbcDriver JDBC-Treiber-Klasse.
   * @param jdbcURL JDBC-URL.
   * @throws RemoteException
   */
	public DBServiceImpl(String jdbcDriver, String jdbcURL) throws RemoteException
	{
    this(jdbcDriver,jdbcURL,null,null);
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
    super();
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

  @Override
  public synchronized boolean isStartable() throws RemoteException
	{
		return startable;
	}

  @Override
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


  @Override
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
  private <T extends DBObject> T create(Class<? extends DBObject> c) throws Exception
  {
    Class<? extends DBObject> clazz = c;
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
    return (T)o;
  }

  @Override
  public <T extends DBObject> T createObject(Class<? extends DBObject> c, String identifier) throws RemoteException
  {
		checkStarted();
    try {
			Logger.debug("try to create new DBObject. request from host: " + getClientHost());
    }
    catch (ServerNotActiveException soe) {}

    try {
      T o = create(c);
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

  @Override
  public <T extends DBObject> DBIterator<T> createList(Class<? extends DBObject> c) throws RemoteException
	{
		checkStarted();
    try {
			Logger.debug("try to create new DBIterator. request from host: " + getClientHost());
    }
    catch (ServerNotActiveException soe) {}

		try {
      T o = create(c);
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

  @Override
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

  @Override
  public synchronized boolean isStarted() throws RemoteException
  {
    return started;
  }

  @Override
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
