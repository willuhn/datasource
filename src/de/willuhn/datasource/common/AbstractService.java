/**********************************************************************
 * $Source: /cvsroot/jameica/datasource/src/de/willuhn/datasource/common/Attic/AbstractService.java,v $
 * $Revision: 1.3 $
 * $Date: 2004/06/17 00:05:51 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.datasource.common;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;

import de.willuhn.datasource.rmi.Service;
import de.willuhn.util.Logger;
import de.willuhn.util.MultipleClassLoader;

/**
 * 
 */
public abstract class AbstractService
  extends UnicastRemoteObject
  implements Service
{

	private Logger log = null;
	private MultipleClassLoader classLoader = null;
	private HashMap initParams = null;

  /**
   * ct.
   * @param initParams Hashmap mit Init-Parametern.
   * @throws RemoteException
   */
  public AbstractService(HashMap initParams) throws RemoteException
  {
    super();
		this.initParams = initParams;

		// Wir definieren erstmal nen Default-Logger
		this.log = new Logger("Service");
		this.log.addTarget(System.out);
		
		// und nen Default-ClassLoader
		this.classLoader = new MultipleClassLoader();
  }
  
	/**
	 * Liefert den Logger.
   * @return Logger.
   */
  protected Logger getLogger()
	{
		return log;
	}

	/**
	 * Liefert den ClassLoader.
   * @return ClassLoader.
   */
	protected MultipleClassLoader getClassLoder()
	{
		return classLoader;
	}
	
	/**
	 * Liefert die Init-Params.
   * @return Init-Params.
   */
	protected HashMap getInitParams()
	{
		return initParams;
	}

  /**
   * @see de.willuhn.datasource.rmi.Service#setLogger(de.willuhn.util.Logger)
   */
  public void setLogger(Logger l) throws RemoteException
  {
  	if (l != null)
  		log = l;
  }

  /**
   * @see de.willuhn.datasource.rmi.Service#setClassLoader(de.willuhn.util.MultipleClassLoader)
   */
  public void setClassLoader(MultipleClassLoader loader)
    throws RemoteException {
    	this.classLoader = loader;
  }

}


/**********************************************************************
 * $Log: AbstractService.java,v $
 * Revision 1.3  2004/06/17 00:05:51  willuhn
 * @N GenericObject, GenericIterator
 *
 * Revision 1.2  2004/03/18 01:24:17  willuhn
 * @C refactoring
 *
 * Revision 1.1  2004/01/23 00:25:52  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/01/10 14:52:19  willuhn
 * @C package removings
 *
 * Revision 1.2  2004/01/08 21:38:47  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/01/08 20:46:43  willuhn
 * @N database stuff separated from jameica
 *
 **********************************************************************/