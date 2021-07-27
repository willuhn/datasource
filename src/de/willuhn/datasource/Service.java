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
package de.willuhn.datasource;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Basis-Interface aller Services, die via RMI genutzt werden koennen.
 * @author willuhn
 */
public interface Service extends Remote 
{

  /**
   * Startet den Service.
   * @throws RemoteException
   */
  public void start() throws RemoteException;
	
  /**
   * Prueft, ob dieser Service gestartet ist.
   * @throws RemoteException
   * @return {@code true} wenn er gestartet ist, sonst {@code false}.
   */
  public boolean isStarted() throws RemoteException;

	/**
	 * Prueft, ob der Service gestartet werden darf.
   * @return {@code true}, wenn er gestartet werden darf, sonst {@code false}.
   * @throws RemoteException
   */
  public boolean isStartable() throws RemoteException;

  /**
   * Stoppt den Service.
   * @param restartAllowed legt fest, ob der Service im laufenden Betrieb neu gestartet werden kann.
   * @throws RemoteException
   */
  public void stop(boolean restartAllowed) throws RemoteException;

	/**
	 * Liefert den sprechenden Namen des Services.
   * @return Name des Services.
   * @throws RemoteException
   */
  public String getName() throws RemoteException;
}

/*********************************************************************
 * $Log: Service.java,v $
 * Revision 1.4  2004/09/15 22:31:20  willuhn
 * *** empty log message ***
 *
 * Revision 1.3  2004/09/14 23:27:32  willuhn
 * @C redesign of service handling
 *
 * Revision 1.2  2004/08/31 17:33:11  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/07/21 23:53:56  willuhn
 * @C massive Refactoring ;)
 *
 * Revision 1.5  2004/06/30 20:58:07  willuhn
 * @C some refactoring
 *
 * Revision 1.4  2004/06/17 00:05:50  willuhn
 * @N GenericObject, GenericIterator
 *
 * Revision 1.3  2004/03/18 01:24:17  willuhn
 * @C refactoring
 *
 * Revision 1.2  2004/03/06 18:24:34  willuhn
 * @D javadoc
 *
 * Revision 1.1  2004/01/10 14:52:19  willuhn
 * @C package removings
 *
 * Revision 1.2  2004/01/08 21:38:47  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/01/08 20:46:44  willuhn
 * @N database stuff separated from jameica
 *
 * Revision 1.3  2003/12/12 21:11:29  willuhn
 * @N ObjectMetaCache
 *
 * Revision 1.2  2003/10/29 00:41:27  willuhn
 * *** empty log message ***
 *
 **********************************************************************/