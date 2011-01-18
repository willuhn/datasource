/**********************************************************************
 * $Source: /cvsroot/jameica/datasource/src/de/willuhn/datasource/db/DBIteratorImpl.java,v $
 * $Revision: 1.29 $
 * $Date: 2011/01/18 12:02:56 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.datasource.db;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.ArrayList;

import de.willuhn.datasource.GenericObject;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.datasource.rmi.DBObject;
import de.willuhn.datasource.rmi.DBService;
import de.willuhn.logging.Logger;

/**
 * @author willuhn
 * Kleiner Hilfsiterator zum Holen von Listen von Objekten aus der Datenbank.
 */
public class DBIteratorImpl extends UnicastRemoteObject implements DBIterator {

	private DBService service       = null;
	private Connection conn         = null;
	private AbstractDBObject object = null;

  private String filter           = "";
  private String order            = "";
  private ArrayList params        = new ArrayList();
  private String joins            = "";

  private ArrayList list          = new ArrayList();
  private int index               = 0;

  private boolean initialized     = false;

  /**
   * Erzeugt einen neuen Iterator.
   * @param object Objekt, fuer welches die Liste erzeugt werden soll.
   * @param service der Datenbankservice.
   * @throws RemoteException
   */
  DBIteratorImpl(AbstractDBObject object, DBServiceImpl service) throws RemoteException
	{
		if (object == null)
			throw new RemoteException("given object type is null");

  	this.object  = object;
		this.service = service;
		this.conn    = service.getConnection();

		if (conn == null)
			throw new RemoteException("given connection is null");

  }

  /**
   * Erzeugt einen neuen Iterator mit der uebergebenen Liste von IDs.
   * @param object Objekt, fuer welches die Liste erzeugt werden soll.
   * @param list eine vorgefertigte Liste.
   * @param service der Datenbank-Service.
   * @throws RemoteException
   */
  DBIteratorImpl(AbstractDBObject object, ArrayList list, DBServiceImpl service) throws RemoteException
  {
		this(object,service);

    if (list == null)
      throw new RemoteException("given list is null");

    try {
      for (int i=0;i<list.size();++i)
      {
        DBObject o = (DBObject) service.createObject(object.getClass(),(String)list.get(i));
        this.list.add(o);
      }
    }
    catch (Exception e)
    {
      throw new RemoteException("unable to create list",e);
    }
    this.initialized = true;
  }

  /**
   * @see de.willuhn.datasource.rmi.DBIterator#setOrder(java.lang.String)
   */
  public void setOrder(String order) throws RemoteException {
    if (this.initialized)
      return; // allready initialized

    this.order = " " + order;
  }

  /**
   * @see de.willuhn.datasource.rmi.DBIterator#addFilter(java.lang.String)
   */
  public void addFilter(String filter) throws RemoteException
  {
    this.addFilter(filter,null);
  }

  /**
   * @see de.willuhn.datasource.rmi.DBIterator#addFilter(java.lang.String, java.lang.Object[])
   */
  public void addFilter(String filter, Object[] p) throws RemoteException
  {
    if (this.initialized)
      return; // allready initialized

    if (filter == null)
      return; // no filter given

    if ("".equals(this.filter))
    {
      this.filter = filter;
    }
    else {
      this.filter += " and " + filter;
    }

    if (p != null)
    {
      for (int i=0;i<p.length;++i)
      {
        this.params.add(p[i]);
      }
    }
  }

  /**
   * @see de.willuhn.datasource.rmi.DBIterator#join(java.lang.String)
   */
  public void join(String table) throws RemoteException
  {
    if (this.initialized)
      return; // allready initialized
    
    if (table == null)
      return;

    this.joins += ", " + table;
  }

  /**
   * Baut das SQL-Statement fuer die Liste zusammen.
   * @return das erzeugte Statement.
   */
  private String prepareSQL()
  {
    String sql = object.getListQuery();

    // mhh, da steht schon eine "where" klausel drin
    if (sql.indexOf(" where ") != -1)
    {
      // also fuegen wir den Filter via "and" hinten dran. Aber nur, wenn auch einer da ist ;)
      if (!"".equals(this.filter))
        sql += " and " + filter;
    }
    else if (filter != null && !"".equals(filter))
    {
      // ansonsten pappen wir den Filter so hinten dran, wie er kommt
      sql += joins + " where " + filter;
    }

    // Statement enthaelt noch kein Order - also koennen wir unseres noch dranschreiben
    if (sql.indexOf(" order ") == -1)
    {
      sql += order;
    }
    return sql;
  }

  /**
   * Initialisiert den Iterator.
   * @throws RemoteException
   */
  private void init() throws RemoteException {
    if (this.initialized)
      return; // allready initialzed

		PreparedStatement stmt = null;
    String sql             = null;
		ResultSet rs           = null;
		try {
      sql = prepareSQL();

      stmt = conn.prepareStatement(sql);
      
      for (int i=0;i<this.params.size();++i)
      {
        Object p = this.params.get(i);
        if (p == null)
          stmt.setNull((i+1),Types.OTHER);
        else
          stmt.setObject((i+1),p);
      }
      
      Logger.debug("executing sql query: " + stmt);

      rs = stmt.executeQuery();
			while (rs.next())
			{
//        final DBObject o = (DBObject) service.createObject(object.getClass(),rs.getString(object.getIDField()));
        final AbstractDBObject o = (AbstractDBObject) service.createObject(object.getClass(),null);
        o.setID(rs.getString(o.getIDField()));
        o.fill(rs);
        
//        o.addDeleteListener(new Listener() {
//          public void handleEvent(Event e) throws RemoteException
//          {
//            int pos = list.indexOf(e.getObject());
//            list.remove(pos);
//
//            // offset ggf. korrigieren
//            if (index > pos)
//              index--;
//          }
//        
//        });
				list.add(o);
			}
      this.initialized = true;
		}
		catch (Exception e)
		{
      String s = stmt == null ? null : stmt.toString();
			throw new RemoteException("unable to init iterator. " + (s != null ? ("statement: " + s) : "") ,e);
		}
		finally {
			try {
				rs.close();
				stmt.close();
			} catch (Exception se) {/*useless*/}
		}
	}

  /**
   * @see de.willuhn.datasource.GenericIterator#hasNext()
   */
  public boolean hasNext() throws RemoteException
	{
    init();
		return (index < list.size() && list.size() > 0);
	}

  /**
   * @see de.willuhn.datasource.GenericIterator#next()
   */
  public GenericObject next() throws RemoteException
	{
    init();
    try {
      return (GenericObject) list.get(index++);
    }
    catch (Exception e)
    {
      throw new RemoteException(e.getMessage());
    }
	}
  
  /**
   * @see de.willuhn.datasource.GenericIterator#previous()
   */
  public GenericObject previous() throws RemoteException
  {
    init();
    try {
      return (GenericObject) list.get(index--);
    }
    catch (Exception e)
    {
      throw new RemoteException(e.getMessage());
    }
  }

  /**
   * @see de.willuhn.datasource.GenericIterator#size()
   */
  public int size() throws RemoteException
  {
    init();
    return list.size();
  }

  /**
   * @see de.willuhn.datasource.GenericIterator#begin()
   */
  public void begin() throws RemoteException
  {
    this.index = 0;
  }

  /**
   * @see de.willuhn.datasource.GenericIterator#contains(de.willuhn.datasource.GenericObject)
   */
  public GenericObject contains(GenericObject other) throws RemoteException
  {
    init();

    if (other == null)
      return null;

    if (!other.getClass().equals(object.getClass()))
      return null; // wir koennen uns die Iteration sparen.

    GenericObject object = null;
    for (int i=0;i<list.size();++i)
    {
      object = (GenericObject) list.get(i);
      if (object.equals(other))
        return object;
    }
    
    return null;
    
  }
}


/*********************************************************************
 * $Log: DBIteratorImpl.java,v $
 * Revision 1.29  2011/01/18 12:02:56  willuhn
 * @R alte Commit-Kommentare entfernt
 *
 * Revision 1.28  2010-05-04 10:38:14  willuhn
 * @N rudimentaere Joins
 **********************************************************************/