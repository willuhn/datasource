/***************************************************************************
 * $Source: /cvsroot/jameica/datasource/src/de/willuhn/datasource/ldap/Attic/DBIteratorImpl.java,v $
 * $Revision: 1.1 $
 * $Date: 2004/01/23 00:25:52 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by bbv AG
 * All rights reserved
 ***************************************************************************/

package de.willuhn.datasource.ldap;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.datasource.rmi.DBObject;


/**
 * Iterator fuer LDAP-Objekte.
 */
public class DBIteratorImpl extends UnicastRemoteObject implements DBIterator
{

	private Class type;
	private DirContext context;
	private NamingEnumeration result;
	private String baseDn = null;
	private String query = "";

	private boolean initialized = false;

  /**
   * Erzeugt einen neuen Iterator.
   * @param type Objekt-Typ der Listen-Elemente.
   * @param context Der Such-Context (Connection).
   * @param baseDn Die Base-DN ab der die Suche startet.
   * @throws RemoteException
   */
  DBIteratorImpl(Class type, DirContext context, String baseDn) throws RemoteException
  {
		if (type == null)
			throw new RemoteException("given object type is null");

		if (context == null)
			throw new RemoteException("given context is null");
		
		if (baseDn == null)
			throw new RemoteException("given base DN is null");

		this.type = type;
		this.context = context;
		this.baseDn = baseDn;

		try {
			QueryFilter filter = new QueryFilter(type);
			addFilter(filter.toString());
		}
		catch (NamingException e)
		{
			throw new RemoteException("unable to apply base filter",e);
		}

  }

	/**
	 * Initialisiert die Liste.
   * @throws RemoteException
   */
  private void init() throws RemoteException
	{
		if (initialized)
			return;
		SearchControls sc = new SearchControls();
		sc.setSearchScope(SearchControls.SUBTREE_SCOPE);

		try {
			result = context.search(baseDn,query,sc);
			initialized = true;
		}
		catch (NamingException e)
		{
			throw new RemoteException("unable to init list",e);
		}
	}

  /**
   * @see de.willuhn.datasource.rmi.DBIterator#hasNext()
   */
  public boolean hasNext() throws RemoteException
  {
    init();
		if (result != null && result.hasMoreElements())
		{
			return true;
		}
		else
		{
			try
      {
        result.close();
      }
      catch (Exception e) {}
			initialized = false;
			return false;
		}
	}

  /**
   * @see de.willuhn.datasource.rmi.DBIterator#next()
   */
  public DBObject next() throws RemoteException
	{
		if (!hasNext())
			throw new RemoteException("no more elements in this resultset");

		try {
			AbstractDBObject dbo = (AbstractDBObject) DBServiceImpl.create(context,type,baseDn);
			SearchResult rs = (SearchResult) result.next();
			dbo.load(rs,baseDn);
			return dbo;
		}
		catch (Exception e)
		{
			throw new RemoteException("unable to load object",e);
		}
	}

  /**
   * @see de.willuhn.datasource.rmi.DBIterator#addFilter(java.lang.String)
   */
  public void addFilter(String filter) throws RemoteException
  {
  	if (initialized)
  		return; // allready initialized

  	if (this.query == null)
  		this.query = "";
  	query += filter;
  }

  /**
   * @see de.willuhn.datasource.rmi.DBIterator#begin()
   */
  public void begin() throws RemoteException
  {
    init();
  }

  /**
   * @see de.willuhn.datasource.rmi.DBIterator#contains(de.willuhn.datasource.rmi.DBObject)
   */
  public DBObject contains(DBObject o) throws RemoteException
  {
  	init();
  	while (hasNext())
  	{
  		DBObject test = next();
  		if (test.equals(o))
  			return test;
  	}
  	init();
  	return null;
  }

  /**
   * @see de.willuhn.datasource.rmi.DBIterator#previous()
   */
  public DBObject previous() throws RemoteException
  {
		throw new RemoteException("not implemented");
  }

  /**
   * @see de.willuhn.datasource.rmi.DBIterator#setOrder(java.lang.String)
   */
  public void setOrder(String order) throws RemoteException
  {
  	throw new RemoteException("not implemented");
  }

  /**
   * @see de.willuhn.datasource.rmi.DBIterator#size()
   */
  public int size() throws RemoteException
  {
		throw new RemoteException("not implemented");
  }

}


/***************************************************************************
 * $Log: DBIteratorImpl.java,v $
 * Revision 1.1  2004/01/23 00:25:52  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2003/08/28 16:27:18  willuhn
 * @N inetOrgPerson
 *
 * Revision 1.1  2003/08/22 17:19:17  willuhn
 * @N first test works
 *
 ***************************************************************************/