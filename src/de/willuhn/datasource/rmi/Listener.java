/**********************************************************************
 * $Source: /cvsroot/jameica/datasource/src/de/willuhn/datasource/rmi/Listener.java,v $
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
 * Ein Listener, der ueber Aenderungen an DBObjects benachrichtigt wird.
 */
public interface Listener extends Remote
{
	/**
	 * Wird bei Aenderungen des DBObjects aufgerufen.
   * @param e das ausgeloeste Event.
   * @throws RemoteException
   */
  public void handleEvent(Event e) throws RemoteException;
}


/**********************************************************************
 * $Log: Listener.java,v $
 * Revision 1.1  2004/10/25 17:58:37  willuhn
 * @N Delete/Store-Listeners
 *
 **********************************************************************/