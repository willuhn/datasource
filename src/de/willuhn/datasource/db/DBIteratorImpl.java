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

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.datasource.rmi.DBService;
import de.willuhn.logging.Logger;

/**
 * Kleiner Hilfsiterator zum Holen von Listen von Objekten aus der Datenbank.
 * @param <T> der konkrete Typ.
 */
public class DBIteratorImpl<T extends AbstractDBObject> extends UnicastRemoteObject implements DBIterator<T> {

	private DBService service       = null;
	private Connection conn         = null;
	private T object                = null;

  private String filter           = "";
  private String order            = "";
  private int limit               = -1;
  private ArrayList params        = new ArrayList();
  private String joins            = "";

  private List<T> list            = new ArrayList<T>();
  private int index               = 0;

  private boolean initialized     = false;

  /**
   * Erzeugt einen neuen Iterator.
   * @param object Objekt, fuer welches die Liste erzeugt werden soll.
   * @param service der Datenbankservice.
   * @throws RemoteException
   */
  DBIteratorImpl(T object, DBServiceImpl service) throws RemoteException
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
  DBIteratorImpl(T object, ArrayList list, DBServiceImpl service) throws RemoteException
  {
		this(object,service);

    if (list == null)
      throw new RemoteException("given list is null");

    try {
      for (int i=0;i<list.size();++i)
      {
        T o = service.createObject(object.getClass(),(String)list.get(i));
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
   * @see de.willuhn.datasource.rmi.DBIterator#setLimit(int)
   */
  public void setLimit(int i) throws RemoteException
  {
    this.limit = i;
  }

  /**
   * @see de.willuhn.datasource.rmi.DBIterator#addFilter(java.lang.String)
   */
  public void addFilter(String filter) throws RemoteException
  {
    this.addFilter(filter,(Object[]) null);
  }

  /**
   * @see de.willuhn.datasource.rmi.DBIterator#addFilter(java.lang.String, java.lang.Object[])
   */
  public void addFilter(String filter, Object... p) throws RemoteException
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
      for (Object o:p)
      {
        this.params.add(o);
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
      sql += order;
    
    if (sql.indexOf(" limit ") == -1 && this.limit > 0)
      sql += " limit " + Integer.toString(this.limit);
    
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
        final T o = service.createObject(object.getClass(),null);
        o.setID(rs.getString(o.getIDField()));
        o.fill(rs);
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
			  if (rs != null) rs.close();
				if (stmt != null) stmt.close();
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
  public T next() throws RemoteException
	{
    init();
    try {
      return list.get(index++);
    }
    catch (Exception e)
    {
      throw new RemoteException(e.getMessage());
    }
	}
  
  /**
   * @see de.willuhn.datasource.GenericIterator#previous()
   */
  public T previous() throws RemoteException
  {
    init();
    try {
      return list.get(index--);
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
  public T contains(T other) throws RemoteException
  {
    init();

    if (other == null)
      return null;

    if (!other.getClass().equals(object.getClass()))
      return null; // wir koennen uns die Iteration sparen.

    T object = null;
    for (int i=0;i<list.size();++i)
    {
      object = list.get(i);
      if (object.equals(other))
        return object;
    }
    
    return null;
    
  }
}
