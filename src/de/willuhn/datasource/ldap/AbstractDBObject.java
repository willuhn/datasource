/**********************************************************************
 * $Source: /cvsroot/jameica/datasource/src/de/willuhn/datasource/ldap/Attic/AbstractDBObject.java,v $
 * $Revision: 1.1 $
 * $Date: 2004/01/23 00:25:52 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by  bbv AG
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.datasource.ldap;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Enumeration;

import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchResult;

import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.datasource.rmi.DBObject;
import de.willuhn.util.ApplicationException;

class AbstractDBObject extends UnicastRemoteObject implements DBObject
{

	private String dn;
	private SearchResult rs;
	
	public final static String NAME 							= "cn";
	public final static String OBJECT_CLASS				= "objectClass";
	public final static String DISTINGUISHED_NAME = "dn";

	private transient DirContext context = null;

	/**
	 * ct
	 * @throws RemoteException
	 */
	public AbstractDBObject() throws RemoteException
	{
		super(); // Konstruktor von UnicastRemoteObject
	}

	/**
	 * Speichert die Connection.
   * @param conn die Connection.
   * @throws NamingException
   */
  void setContext(DirContext conn) throws NamingException
	{
		if (conn == null)
			throw new NamingException("connection is null");

		this.context = conn;
	}
  
	/**
	 * Liefert die Connection.
   * @return die Connection.
   */
  protected DirContext getContext()
	{
		return this.context;
	}

  /**
   * Initialisiert das Objekt.
   * @throws NamingException
   */
  void init() throws NamingException
	{

		// check, if we were casted to the right class
		String expected = getClass().getName();
					 expected = expected.substring(expected.lastIndexOf(".")+1); // Packagenamen abschneiden 
		String real     = null;
		try {
			real = getObjectClass() + "Impl";
		}
		catch (RemoteException e)
		{
			throw new NamingException("unable to determine object type");
		}

		if (real == null)
			throw new NamingException("no objectClass available for class: " + real);

		if (!real.equalsIgnoreCase(expected))
			throw new NamingException("wrong objectClass: " + real + ", expected: " + expected);

	}

	/**
   * Liefert den Distignuished Name.
   * @return Distignuished Name.
   * @throws RemoteException
   */
  public String getDN() throws RemoteException
	{
		return dn;
	}

	/**
	 * Liefert den Common Name.
   * @return Common Name.
   * @throws RemoteException
   */
  public String getCN() throws RemoteException
	{
		return (String) getField(NAME);
	}

  /**
   * @return
   * @throws RemoteException
   */
  public String getObjectClass() throws RemoteException
	{
		return (String) getField(OBJECT_CLASS);
	}

  /**
   * @see de.willuhn.datasource.rmi.DBObject#getField(java.lang.String)
   */
  public Object getField(String name) throws RemoteException
	{
		return getField(name,0);
	}

  /**
   * @param name
   * @return
   * @throws RemoteException
   */
  protected Enumeration getAll(String name) throws RemoteException
	{
		if (name == null || "".equals(name))
			return null;

		Attribute a = rs.getAttributes().get(name);
		try {
			return a == null ? null : a.getAll();
		}
		catch (NamingException e)
		{
			throw new RemoteException("unable to load values of attribute " + name,e);
		}
	}

  /**
	 * Liefert den Wert des angegebenen Attributes, der sich am uebergebenen Index befindet.
	 * @param name Name des gewuenschten Attributes.
   * @param index Index bei Arrays.
	 * @return Wert des angegebenen Attributes.
   * @throws RemoteException
	 */
	protected Object getField(String name, int index) throws RemoteException
	{
		if (name == null || "".equals(name))
			return null;

		Attribute a = rs.getAttributes().get(name);
		try {
			return a == null ? null : a.get(index);
		}
		catch (Exception e)
		{
			return null;
		}
	}

	/**
   * @param rs
   * @param baseDn
   * @throws NamingException
   */
  void load(SearchResult rs, String baseDn) throws NamingException
	{
		this.rs = rs;
		this.dn = rs.getName() + "," + baseDn;
		init();
	}

  /**
   * @see de.willuhn.datasource.rmi.DBObject#load(java.lang.String)
   */
  public void load(String dn) throws RemoteException
  {
		try {
	  	DBIteratorImpl i = new DBIteratorImpl(this.getClass(),getContext(),LDIFParser.getBaseDN(dn));
	  	QueryFilter f = new QueryFilter(this.getClass());
	  	f.and(NAME,LDIFParser.getCN(dn));
	  	System.out.println(f.toString());
	  	i.addFilter(f.toString());
	  	if (i.hasNext())
				this = (AbstractDBObject) i.next();
  		throw new RemoteException("DN " + dn + " not found");
		}
		catch (Exception e)
		{
			throw new RemoteException("unable to load dn " + dn,e);
		}
  }

  /**
   * @see de.willuhn.datasource.rmi.DBObject#transactionBegin()
   */
  public void transactionBegin() throws RemoteException
  {
  	throw new RemoteException("not implemented");
  }

  /**
   * @see de.willuhn.datasource.rmi.DBObject#transactionCommit()
   */
  public void transactionCommit() throws RemoteException
  {
		throw new RemoteException("not implemented");
  }

  /**
   * @see de.willuhn.datasource.rmi.DBObject#transactionRollback()
   */
  public void transactionRollback() throws RemoteException
  {
		throw new RemoteException("not implemented");
  }

  /**
   * @see de.willuhn.datasource.rmi.DBObject#store()
   */
  public void store() throws RemoteException, ApplicationException
  {
		throw new RemoteException("not implemented");
  }

  /**
   * @see de.willuhn.datasource.rmi.DBObject#delete()
   */
  public void delete() throws RemoteException, ApplicationException
  {
		throw new RemoteException("not implemented");
  }

  /**
   * @see de.willuhn.datasource.rmi.DBObject#clear()
   */
  public void clear() throws RemoteException
  {
		throw new RemoteException("not implemented");
  }

  /**
   * @see de.willuhn.datasource.rmi.DBObject#getID()
   */
  public String getID() throws RemoteException
  {
    return getDN();
  }

  /**
   * @see de.willuhn.datasource.rmi.DBObject#getFieldType(java.lang.String)
   */
  public String getFieldType(String fieldname) throws RemoteException
  {
		throw new RemoteException("not implemented");
  }

  /**
   * @see de.willuhn.datasource.rmi.DBObject#isNewObject()
   */
  public boolean isNewObject() throws RemoteException
  {
		throw new RemoteException("not implemented");
  }

  /**
   * @see de.willuhn.datasource.rmi.DBObject#getPrimaryField()
   */
  public String getPrimaryField() throws RemoteException
  {
		throw new RemoteException("not implemented");
  }

  /**
   * @see de.willuhn.datasource.rmi.DBObject#overwrite(de.willuhn.datasource.rmi.DBObject)
   */
  public void overwrite(DBObject object) throws RemoteException
  {
		throw new RemoteException("not implemented");
  }

  /**
   * @see de.willuhn.datasource.rmi.DBObject#getList()
   */
  public DBIterator getList() throws RemoteException
  {
		throw new RemoteException("not implemented");
  }

  /**
   * @see de.willuhn.datasource.rmi.DBObject#equals(de.willuhn.datasource.rmi.DBObject)
   */
  public boolean equals(DBObject o) throws RemoteException
  {
		throw new RemoteException("not implemented");
  }

}


/*********************************************************************
 * $Log: AbstractDBObject.java,v $
 * Revision 1.1  2004/01/23 00:25:52  willuhn
 * *** empty log message ***
 *
 * Revision 1.4  2003/09/01 09:36:42  willuhn
 * @I changed return type of getPublicKey to X509Certificate
 * @N added GroupOfUniqueNamesTest
 *
 * Revision 1.3  2003/08/29 14:58:00  willuhn
 * @N added LdapObjectFactory
 *
 * Revision 1.2  2003/08/28 16:27:18  willuhn
 * @N inetOrgPerson
 *
 * Revision 1.1  2003/08/22 17:19:17  willuhn
 * @N first test works
 *
 **********************************************************************/