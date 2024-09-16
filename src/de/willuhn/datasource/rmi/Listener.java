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
package de.willuhn.datasource.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Ein Listener, der ueber Aenderungen an {@link DBObject}s benachrichtigt wird.
 */
public interface Listener extends Remote
{
	/**
	 * Wird bei Aenderungen des {@link DBObject}s aufgerufen.
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