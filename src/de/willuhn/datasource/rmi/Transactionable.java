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
 * Revision 1.2  2004/08/31 18:13:59  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/08/18 23:14:00  willuhn
 * @D Javadoc
 *
 **********************************************************************/