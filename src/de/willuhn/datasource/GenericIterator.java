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
 * generischer RMI-tauglicher Iterator.
 * @param <T> der konkrete Objekt-Typ.
 */
public interface GenericIterator<T extends GenericObject> extends Remote
{

	/**
	 * Prueft, ob weitere Elemente in diesem Iterator existieren.
	 * @return {@code true}, wenn weitere Elemente vorhanden sind.
	 * @throws RemoteException
	 */
	public boolean hasNext() throws RemoteException;

	/**
	 * Liefert das aktuelle Element der Iteration und blaettert um ein Element weiter.
	 * @return aktuelles Element.
	 * @throws RemoteException
	 */
	public T next() throws RemoteException;

  /**
   * Liefert das aktuelle Element der Iteration und blaetter um ein Element zurueck.
   * @return aktuelles Element.
   * @throws RemoteException
   */
  public T previous() throws RemoteException;

  /**
   * Blaettert den Iterator wieder an den Anfang zurueck. Somit kann er erneut
   * durchlaufen werden.
   * @throws RemoteException
   */
  public void begin() throws RemoteException;

  /**
   * Liefert die Anzahl der Elemente dieses Iterators.
   * @return Anzahl der Elemente in dem Iterator.
   * @throws RemoteException
   */
  public int size() throws RemoteException;

  /**
   * Prueft, ob das uebergebene Objekt in der aktuellen Liste vorhanden ist.
   * @param o das zu pruefende Objekt.
   * @return {@code null} wenn kein Objekt uebereinstimmt, andernfalls das ueberinstimmende Objekt aus dieser Liste.
   * @throws RemoteException
   */
  public T contains(T o) throws RemoteException;

}
