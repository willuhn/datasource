/**********************************************************************
 * $Source: /cvsroot/jameica/datasource/src/de/willuhn/datasource/common/Attic/AbstractService.java,v $
 * $Revision: 1.1 $
 * $Date: 2004/01/23 00:25:52 $
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

/**
 * 
 */
public abstract class AbstractService
  extends UnicastRemoteObject
  implements Service
{

	protected Logger log = new Logger();
	protected HashMap initParams = null;

  /**
   * ct.
   * @param initParams Hashmap mit Init-Parametern.
   * @throws RemoteException
   */
  public AbstractService(HashMap initParams) throws RemoteException
  {
    super();
		this.initParams = initParams;
  }
  
  /**
   * ct.
   * From UnicastRemoteObject.
   * @throws RemoteException
   */
  protected AbstractService() throws RemoteException
  {
  	super();
  }

  /**
   * @see de.willuhn.datasource.rmi.Service#setLogger(de.willuhn.util.Logger)
   */
  public void setLogger(Logger l) throws RemoteException
  {
  	if (l != null)
  		log = l;
  }

}


/**********************************************************************
 * $Log: AbstractService.java,v $
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