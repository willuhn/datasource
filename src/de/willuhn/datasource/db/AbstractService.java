/**********************************************************************
 * $Source: /cvsroot/jameica/datasource/src/de/willuhn/datasource/db/Attic/AbstractService.java,v $
 * $Revision: 1.1 $
 * $Date: 2004/01/10 14:52:19 $
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

import de.willuhn.datasource.rmi.Service;
import de.willuhn.util.Logger;

/**
 * 
 */
public abstract class AbstractService
  extends UnicastRemoteObject
  implements Service
{

	Logger log = new Logger();

  /**
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