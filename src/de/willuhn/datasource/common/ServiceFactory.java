/**********************************************************************
 * $Source: /cvsroot/jameica/datasource/src/de/willuhn/datasource/common/Attic/ServiceFactory.java,v $
 * $Revision: 1.1 $
 * $Date: 2004/07/01 18:58:42 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by  bbv AG
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.datasource.common;

import java.lang.reflect.Constructor;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;

import de.willuhn.datasource.rmi.Service;
import de.willuhn.util.Logger;
import de.willuhn.util.MultipleClassLoader;


/**
 * Diese Klasse stellt Services via RMI zur Verfuegung.
 * Das kann z.Bsp. eine Datenbankverbindung sein.
 * @author willuhn
 */
public class ServiceFactory
{
  
  private final static boolean USE_RMI_FIRST = true;

	private static ServiceFactory factory = null;
		private MultipleClassLoader loader = null;
  	private Hashtable bindings = new Hashtable();
  	private boolean rmiStarted = false;
  	private int port = 1099;


	public static synchronized ServiceFactory getInstance(MultipleClassLoader loader)
	{
		if (factory != null)
			return factory;
		factory = new ServiceFactory();
		factory.loader = loader;
		return factory;
	}
	 
	private ServiceFactory()
	{
		/* private */
	}

  /**
   * Startet die RMI-Registry.
   * @throws RemoteException Wenn ein Fehler beim Starten der Registry auftrat.
   */
  private synchronized void startRegistry() throws RemoteException
  {
    try {
      Logger.info("trying to start new RMI registry");
//      System.setSecurityManager(new NoSecurity());
      LocateRegistry.createRegistry(port);
    }
    catch (RemoteException e)
    {
      Logger.info("failed, trying to use an existing one");
      LocateRegistry.getRegistry(port);
    }
    rmiStarted = true;
    
  }
  /**
   * Gibt einen lokalen Service im Netzwerk frei. 
   * @param service der Datencontainer des Services.
   * @throws Exception wenn das Freigeben fehlschlaegt.
   */
  public void bind(LocalServiceData service) throws Exception
	{
    if (!rmiStarted)
      startRegistry();

    Naming.rebind(service.getUrl(),getLocalServiceInstance(service)); 
		bindings.put(service.getName(),service); 
		Logger.info("added " + service.getUrl());
	}


  /**
   * Sucht explizit lokal nach dem genannten Service.
   * @param service Daten-Container des Services.
   * @return die Instanz des Services.
   * @throws Exception wenn beim Erstellen des Services ein Fehler aufgetreten ist.
   */
  public Service getLocalServiceInstance(LocalServiceData service) throws Exception
  {
  	if (service == null)
  		return null;

		Logger.debug("searching for local service " + service.getName());
		try {
			Class clazz = loader.load(service.getClassName());
			Constructor ct = clazz.getConstructor(new Class[]{HashMap.class});
			ct.setAccessible(true);
			Service s = (Service) ct.newInstance(new Object[] {service.getInitParams()});
			s.setClassLoader(loader);
			return s;
		}
		catch (Exception e)
		{
			Logger.error("service " + service.getName() + " not found");
			throw e;
		}
  }

  /**
   * Sucht explizit im Netzwerk nach dem genannten Service.
   * @param service Remote-Daten-Container des Services. Enthaelt u.a. die URL,
   * unter dem der Service zu finden ist.
   * @return die Instanz des Services.
   * @throws Exception wenn beim Erstellen des Services ein Fehler aufgetreten ist.
   */
  public Service getRemoteServiceInstance(RemoteServiceData service) throws Exception
	{
		if (service == null)
			return null;

		Logger.debug("searching for remote service " + service.getName() + " at " + service.getUrl());
		try
		{
			return (Service) java.rmi.Naming.lookup(service.getUrl());
		}
		catch (Exception e)
		{
			Logger.error("service " + service.getName() + " not found at " + service.getUrl());
			throw e;
		}
		
	}

  /**
   * Faehrt die Services runter.
   */
  public void shutDown()
  {
    Logger.info("shutting down services");

    Enumeration e = bindings.keys();
    String name;
    LocalServiceData serviceData;
    Service service;

    while (e.hasMoreElements())
    {
      name = (String) e.nextElement();
			serviceData = (LocalServiceData) bindings.get(name);

			Logger.info("closing service " + serviceData.getName());

			try {
				service = (Service) Naming.lookup(serviceData.getUrl());
				service.shutDown();
			}
			catch (Exception ex) {
				Logger.error("error while closing service",ex);
      }
    }
  }

//  /**
//   * Dummy-Security-Manager.
//   */
//  private static class NoSecurity extends SecurityManager
//  {
//    /**
//     * @see java.lang.SecurityManager#checkPermission(java.security.Permission)
//     */
//    public void checkPermission(Permission p)
//    {
//      
//    }
//  }

}
/*********************************************************************
 * $Log: ServiceFactory.java,v $
 * Revision 1.1  2004/07/01 18:58:42  willuhn
 * @N added serviceFactory
 *
 **********************************************************************/
