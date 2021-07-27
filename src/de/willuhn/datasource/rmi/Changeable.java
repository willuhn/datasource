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

import de.willuhn.util.ApplicationException;

/**
 * Bildet Funktionen zur Aenderung eines Objektes ab.
 */
public interface Changeable
{

	/**
	 * Speichert das Objekt in der Datenbank.
   * Die Funktion prueft selbst, ob es sich um ein neues Objekt handelt
   * und entscheidet, ob ein insert oder update durchgefuehrt werden muss.
	 * @throws RemoteException im Fehlerfall.
   * @throws ApplicationException Wenn das Objekt nicht gespeichert werden darf.
   * Der Grund hierfuer findet sich im Fehlertext der Exception.
	 */
	public void store() throws RemoteException, ApplicationException;

	/**
	 * Loescht das Objekt aus der Datenbank.
	 * @throws RemoteException im Fehlerfall.
   * @throws ApplicationException Wenn das Objekt nicht geloescht werden darf.
   * Der Grund hierfuer findet sich im Fehlertext der Exception.
	 */
	public void delete() throws RemoteException, ApplicationException;

  /**
   * Loescht alle Eigenschaften (incl. ID) aus dem Objekt.
   * Es kann nun erneut befuellt und als neues Objekt in der Datenbank
   * gespeichert werden.
   * @throws RemoteException im Fehlerfall.
   */
  public void clear() throws RemoteException;

	/**
	 * Prueft, ob es sich um ein neues Objekt oder ein bereits in der Datenbank existierendes handelt.
	 * @return {@code true}, wenn es neu ist, andernfalls {@code false}.
	 * @throws RemoteException im Fehlerfall.
	 */
	public boolean isNewObject() throws RemoteException;

  /**
   * Ueberschreibt dieses Objekt mit den Attributen des uebergebenen.
   *
   * <p>Dabei werden nur die Werte der Attribute ueberschrieben - nichts anderes.
   * Also auch keine Meta-Daten oder aehnliches.
   *
   * <p>Handelt es sich bei der Quelle um ein Objekt fremden Typs, wird nichts ueberschrieben.
   *
   * <p>Hinweis: Es werden nur die Attribute ueberschrieben, es wird jedoch
   * noch nicht gespeichert. Sollen die Aenderungen also dauerhaft uebernommen
   * werden, muss anschliessend noch ein {@link #store()} aufgerufen werden.
   *
   * @param object das Objekt, welches als Quelle verwendet werden soll.
   * @throws RemoteException im Fehlerfall.
   */
  public void overwrite(DBObject object) throws RemoteException;

}

/*********************************************************************
 * $Log: Changeable.java,v $
 * Revision 1.3  2004/10/25 17:58:37  willuhn
 * @N Delete/Store-Listeners
 *
 * Revision 1.2  2004/08/31 18:13:59  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/08/18 23:14:00  willuhn
 * @D Javadoc
 *
 **********************************************************************/