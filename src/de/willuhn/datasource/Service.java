/**********************************************************************
 * $Source: /cvsroot/jameica/datasource/src/de/willuhn/datasource/Service.java,v $
 * $Revision: 1.2 $
 * $Date: 2004/08/31 17:33:11 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
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
   * Initialisiert den Service.
   * @throws RemoteException
   */
  public void init() throws RemoteException;
	
  /**
   * Prueft, ob dieser Service auf dem Server noch verfuegbar ist.
   * @throws RemoteException
   * @return true wenn er verfuegbar ist, sonst false.
   */
  public boolean isAvailable() throws RemoteException;


  /**
   * Faehrt den Service herunter.
   * @param reinitAllowed legt fest, ob der Service bei Bedarf neu initialisiert werden darf.
   * @throws RemoteException
   */
  public void shutDown(boolean reinitAllowed) throws RemoteException;
}

/*********************************************************************
 * $Log: Service.java,v $
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