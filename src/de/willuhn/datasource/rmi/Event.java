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