/**********************************************************************
 * $Source: /cvsroot/jameica/datasource/src/de/willuhn/datasource/rmi/Event.java,v $
 * $Revision: 1.1 $
 * $Date: 2004/10/25 17:58:37 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.datasource.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Ein Event, dass bei verschiedenen Aktionen ausgeloest werden kann.
 */
public interface Event extends Remote
{
	/**
	 * Das Objekt, fuer das dieses Event ausgeloest wurde.
   * @return das Objekt.
   * @throws RemoteException
   */
  public DBObject getObject() throws RemoteException;
}


/**********************************************************************
 * $Log: Event.java,v $
 * Revision 1.1  2004/10/25 17:58:37  willuhn
 * @N Delete/Store-Listeners
 *
 **********************************************************************/