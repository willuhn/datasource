/**********************************************************************
 *
 * Copyright (c) by Olaf Willuhn
 * All rights reserved
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
	 * Liefert true, wenn weitere Elemente in diesem Iterator existieren.
	 * @return true, wenn weitere Elemente vorhanden sind.
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
   * @return null wenn kein Objekt uebereinstimmt, andernfalls das ueberinstimmende Objekt aus dieser Liste.
   * @throws RemoteException
   */
  public T contains(T o) throws RemoteException;

}
