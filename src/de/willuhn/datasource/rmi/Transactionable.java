/**********************************************************************
 * $Source: /cvsroot/jameica/datasource/src/de/willuhn/datasource/rmi/Transactionable.java,v $
 * $Revision: 1.1 $
 * $Date: 2004/08/18 23:14:00 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by  bbv AG
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.datasource.rmi;

import java.rmi.RemoteException;

/**
 * Bildet Funktionen zur Unterstuetzung von Transaktionen ab.
 */
public interface Transactionable
{

  /**
   * Damit kann man manuell eine Transaktion starten.
   * Normalerweise wir bei store() oder delete() sofort
   * bei Erfolg ein commit gemacht. Wenn man aber von
   * aussen das Transaktionsverhalten beeinflussen will,
   * kann man diese Methode aufrufen. Hat man dies
   * getan, werden store() und delete() erst dann in
   * der Datenbank ausgefuehrt, wenn man anschliessend
   * transactionCommit() aufruft.
   * @throws RemoteException im Fehlerfall.
   */
  public void transactionBegin() throws RemoteException;
  
  /**
   * Beendet eine manuell gestartete Transaktion.
   * Wenn vorher kein <code>transactionBegin()</code> aufgerufen wurde,
   * wird dieser Aufruf ignoriert.
   * @throws RemoteException im Fehlerfall.
   */
  public void transactionCommit() throws RemoteException;

  /**
   * Rollt die angefangene Transaktion manuell zurueck.
   * @throws RemoteException im Fehlerfall.
   */
  public void transactionRollback() throws RemoteException;

}

/*********************************************************************
 * $Log: Transactionable.java,v $
 * Revision 1.1  2004/08/18 23:14:00  willuhn
 * @D Javadoc
 *
 **********************************************************************/