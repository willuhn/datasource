/***************************************************************************
 * $Source: /cvsroot/jameica/datasource/src/de/willuhn/datasource/ldap/Attic/DBServiceImpl.java,v $
 * $Revision: 1.2 $
 * $Date: 2004/01/25 18:39:50 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.datasource.ldap;

import java.lang.reflect.Constructor;
import java.rmi.RemoteException;
import java.rmi.server.ServerNotActiveException;
import java.util.HashMap;
import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;

import de.willuhn.datasource.common.AbstractService;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.datasource.rmi.DBObject;
import de.willuhn.datasource.rmi.DBService;
import de.willuhn.util.ClassFinder;

/**
 * Daten-Service fuer LDAP-Verzeichnisse.
 */
public class DBServiceImpl extends AbstractService implements DBService
{

	// Der Directory-Context
  private DirContext context;

	// Die Context-Factory
  private final static String CONTEXT_FACTORY = "com.sun.jndi.ldap.LdapCtxFactory";

	// die Connect-Parameter
	private Hashtable env;
	

	private boolean connected = false;
	private boolean available = true;
	private boolean openInProgress = false;

  /**
   * Erzeugt eine neue Verbindung zum LDAP-Verzeichnis.
   * @param initParams Init-Parameter.
   * @throws RemoteException Bei Verbindungsfehlern.
   */
  public DBServiceImpl(HashMap initParams) throws RemoteException
  {
  	super(initParams);

		String url = (String) initParams.get("url");
		if (url == null || "".equals(url))
			throw new RemoteException("url not set");

		String user = (String) initParams.get("user");
		if (user == null || "".equals(user))
			throw new RemoteException("user dn not set");
			
		String basedn = (String) initParams.get("basedn");
		if (basedn == null || "".equals(basedn))
			throw new RemoteException("base dn not set");
  }

  /**
   * @see de.willuhn.datasource.rmi.Service#close()
   */
  public void close() throws RemoteException
  {
		if (!available)
			return;

		try {
			log.info("closing db connection. request from host: " + getClientHost());
		}
		catch (ServerNotActiveException soe) {}

		try {
			connected = false;
			context.close();
		}
		catch (NullPointerException ne)
		{
			log.info("allready closed");
		}
		catch (NamingException ne)
		{
			log.error("unable to close ldap connection",ne);
			throw new RemoteException("unable to close ldap connection",ne);
		}
    finally {
      context = null;
    }
  }

	/**
	 * Erzeugt ein neues Objekt aus der angegeben Klasse.
	 * @param conn die Connection, die im Objekt gespeichert werden soll.
	 * @param c Klasse des zu erstellenden Objekts.
	 * @return das erzeugte Objekt.
	 * @throws Exception wenn beim Erzeugen des Objektes ein Fehler auftrat.
	 */
	static DBObject create(DirContext conn, Class c, String dn) throws Exception
	{
		Class clazz = ClassFinder.findImplementor(c);

		Constructor ct = clazz.getConstructor(new Class[]{});
		ct.setAccessible(true);

		AbstractDBObject o = (AbstractDBObject) ct.newInstance(new Object[] {});
		o.setContext(conn);
		return o;
	}

  /**
	 * Findet ein AbstractDBObject basierend auf dem uebergebenen DN.
	 * @param clazz Typ des Objektes.
	 * @param dn der Distinguished Name des gewuenschten Objektes.
   * @see de.willuhn.datasource.rmi.DBService#createObject(java.lang.Class, java.lang.String)
	 * @return das gewuenschte Objekt.
	 */
	public DBObject createObject(Class clazz, String dn) throws RemoteException
	{
		try {
			log.debug("try to create new DBObject. request from host: " + getClientHost());
		}
		catch (ServerNotActiveException soe) {}

		open();
		try {
			DBObject o = create(context,clazz,dn);
			o.load(dn);
			return o;
		}
		catch (Exception e)
		{
			log.error("unable to create object " + clazz.getName(),e);
			throw new RemoteException("unable to create object " + clazz.getName(),e);
		}
	}


  /**
   * @see de.willuhn.datasource.rmi.DBService#createList(java.lang.Class)
   */
  public DBIterator createList(Class clazz) throws RemoteException
  {
		try {
			log.debug("try to create new DBIterator. request from host: " + getClientHost());
		}
		catch (ServerNotActiveException soe) {}

		open();
		try {
			return new DBIteratorImpl(clazz,context,(String)initParams.get("basedn"));
		}
		catch (Exception e)
		{
			log.error("unable to create list for object " + clazz.getName(),e);
			throw new RemoteException("unable to create list for object " + clazz.getName(),e);
		}
  }

  /**
   * @see de.willuhn.datasource.rmi.DBService#ping()
   */
  public boolean ping() throws RemoteException
  {
		throw new RemoteException("not implemented");
  }

  /**
   * @see de.willuhn.datasource.rmi.Service#open()
   */
  public void open() throws RemoteException
  {
		if (openInProgress)
			return;

		if (!available)
			throw new RemoteException("server shut down. service no longer available.");

		try {
			openInProgress = true;
	
			env = new Hashtable(11);	
	
			String user = (String) initParams.get("user");
			env.put(Context.INITIAL_CONTEXT_FACTORY, CONTEXT_FACTORY);
			env.put(Context.PROVIDER_URL,            initParams.get("url"));
			env.put(Context.SECURITY_PRINCIPAL,      user);
			env.put(Context.SECURITY_CREDENTIALS,    initParams.get("password"));
	
			String ssl = (String) initParams.get("ssl");
			if ("true".equalsIgnoreCase(ssl) || "yes".equalsIgnoreCase(ssl))
			{
				env.put(Context.SECURITY_PROTOCOL,       "ssl");
				env.put(Context.SECURITY_AUTHENTICATION, "simple");
			}
	
			try
			{
				context = new InitialDirContext(env);
			}
			catch (NamingException e1)
			{
				throw new RemoteException("unable to create initial context",e1);
			}
			
			// verify login
			// TODO: Verify login
//			try {
//				InetOrgPerson iop = (InetOrgPerson) createObject(InetOrgPerson.class,user);
//				if (user.equals(iop.getDN()))
//					return;
//			}
//			catch (Exception e) {
//				this.close();
//				throw new RemoteException("Login with user dn " + user + " failed",e);
//			}
			connected = true;
		}
		finally
		{
			openInProgress = false;    
		}
  }

	/**
	 * @see de.willuhn.jameica.rmi.Service#isAvailable()
	 */
	public boolean isAvailable() throws RemoteException
	{
		return available;
	}


	/**
	 * @see de.willuhn.jameica.rmi.Service#shutDown()
	 */
	public void shutDown() throws RemoteException
	{
		available = false;
		close();
	}

}


/***************************************************************************
 * $Log: DBServiceImpl.java,v $
 * Revision 1.2  2004/01/25 18:39:50  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/01/23 00:25:52  willuhn
 * *** empty log message ***
 *
 * Revision 1.5  2003/11/28 17:53:14  willuhn
 * @C better error handling
 *
 * Revision 1.4  2003/09/01 09:36:42  willuhn
 * @I changed return type of getPublicKey to X509Certificate
 * @N added GroupOfUniqueNamesTest
 *
 * Revision 1.3  2003/08/29 15:48:14  willuhn
 * @C connection verification
 *
 * Revision 1.2  2003/08/28 16:27:18  willuhn
 * @N inetOrgPerson
 *
 * Revision 1.1  2003/08/22 17:19:17  willuhn
 * @N first test works
 *
 ***************************************************************************/