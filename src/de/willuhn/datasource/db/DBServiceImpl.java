/**********************************************************************
 * $Source: /cvsroot/jameica/datasource/src/de/willuhn/datasource/db/DBServiceImpl.java,v $
 * $Revision: 1.7 $
 * $Date: 2004/03/18 01:24:17 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by  bbv AG
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.datasource.db;

import java.lang.reflect.Constructor;
import java.rmi.RemoteException;
import java.rmi.server.ServerNotActiveException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;

import de.willuhn.datasource.common.AbstractService;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.datasource.rmi.DBObject;
import de.willuhn.datasource.rmi.DBService;

/**
 * Diese Klasse implementiert eine ueber RMI erreichbaren Datenbank. 
 * @author willuhn
 */
public class DBServiceImpl extends AbstractService implements DBService
{

  private String driverClass = null;

  private String jdbcUrl = null;
  
  private Connection conn;

  private boolean available = true;

  /**
	 * Erzeugt eine neue Instanz.
   * @param initParams HashMap mit Initialisierungsparametern.
   * @throws RemoteException im Fehlerfall.
	 */
	public DBServiceImpl(HashMap initParams) throws RemoteException
	{
		super(initParams);
    
    jdbcUrl = (String) initParams.get("url");
    if (jdbcUrl == null || "".equals(jdbcUrl)) {
      throw new RemoteException("url not set");
    }

    driverClass = (String) initParams.get("driver");
    if (driverClass == null || "".equals(driverClass)) {
      throw new RemoteException("driver not set");
    }
	}
  
	/**
	 * @see de.willuhn.datasource.rmi.DBService#getConnection()
	 */
	public Connection getConnection() throws RemoteException {
		open();
		return conn;
	}

  /**
   * @see de.willuhn.datasource.rmi.Service#open()
   */
  public synchronized void open() throws RemoteException
  {
    if (!available)
      throw new RemoteException("server shut down. service no longer available.");

    try {
      log.info("opening db connection. request from host: " + getClientHost());
    }
    catch (ServerNotActiveException soe) {}
    
		if (ping()) return; // test connection

		// mhh, entweder die Verbindung wurde noch nie geoeffnet
		// oder sie ist im Eimer. Wir oeffnen sie neu

		// Ob es hier Sinn macht, vorher nochmal close() aufzurufen?
		try {
			Class.forName(driverClass);
		}
		catch (ClassNotFoundException e2)
		{
			log.error("unable to load jb driver " + driverClass,e2);
			throw new RemoteException("unable to load jdbc driver " + driverClass,e2);
		}

		try {
			conn = DriverManager.getConnection(jdbcUrl);    
		}
		catch (SQLException e2)
		{
			log.error("connection to database " + jdbcUrl + " failed",e2);
			throw new RemoteException("connection to database." + jdbcUrl + " failed",e2);
		}
  }


  /**
   * @see de.willuhn.datasource.rmi.Service#close()
   */
  public synchronized void close() throws RemoteException
  {
    if (!available)
      return;

    try {
			log.info("closing db connection. request from host: " + getClientHost());
    }
    catch (ServerNotActiveException soe) {}

    try {
      conn.close();
    }
    catch (NullPointerException ne)
		{
			log.info("  allready closed");
		}
    catch (SQLException e)
    {
			log.error("  unable to close database connection",e);
      throw new RemoteException("  unable to close database connection",e);
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
    Class clazz = classLoader.findImplementor(c);
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
  public DBObject createObject(Class c, String id) throws RemoteException
  {
    try {
      log.debug("try to create new DBObject. request from host: " + getClientHost());
    }
    catch (ServerNotActiveException soe) {}

    try {
      DBObject o = create(c);
      o.load(id);
      return o;
    }
    catch (Exception e)
    {
      log.error("unable to create object " + (c == null ? "unknown" : c.getName()),e);
      throw new RemoteException("unable to create object " + (c == null ? "unknown" : c.getName()),e);
    }
  }

  /**
   * @see de.willuhn.datasource.rmi.DBService#createList(java.lang.Class)
   */
  public DBIterator createList(Class c) throws RemoteException
	{
    try {
      log.debug("try to create new DBIterator. request from host: " + getClientHost());
    }
    catch (ServerNotActiveException soe) {}

		try {
      DBObject o = create(c);
			return new DBIteratorImpl((AbstractDBObject)o,this);
		}
		catch (Exception e)
		{
			log.error("unable to create list for object " + c.getName(),e);
			throw new RemoteException("unable to create list for object " + c.getName(),e);
		}
	}


  /**
   * @see de.willuhn.datasource.rmi.Service#isAvailable()
   */
  public boolean isAvailable() throws RemoteException
  {
    return available;
  }


  /**
   * @see de.willuhn.datasource.rmi.Service#shutDown()
   */
  public synchronized void shutDown() throws RemoteException
  {
    available = false;
    close();
    
    // print chache stats
    log.debug("object cache matches: " + ObjectMetaCache.getStats() + " %");
  }


  /**
   * @see de.willuhn.datasource.rmi.DBService#ping()
   */
  public boolean ping() throws RemoteException
  {
    if (!available)
      return false;

		Statement stmt = null;
    try {
      log.debug("sending ping to database");
      stmt = conn.createStatement();
      boolean b = stmt.execute("select 1");
      if (b)
        log.debug("ok");
      else
        log.debug("failed");
      stmt.close();
      return b;
    }
    catch (Exception e) {}
    finally
    {
    	try {
    		stmt.close();
    	}	catch (Exception ee) {/*useless*/}
    }
		return false;
  }
}

/*********************************************************************
 * $Log: DBServiceImpl.java,v $
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