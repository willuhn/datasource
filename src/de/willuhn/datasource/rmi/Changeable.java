/**********************************************************************
 * $Source: /cvsroot/jameica/datasource/src/de/willuhn/datasource/rmi/Changeable.java,v $
 * $Revision: 1.2 $
 * $Date: 2004/08/31 18:13:59 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
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
	 * @return true, wenn es neu ist, andernfalls false.
	 * @throws RemoteException im Fehlerfall.
	 */
	public boolean isNewObject() throws RemoteException;

  /**
   * Ueberschreibt dieses Objekt mit den Eigenschaften des uebergebenen.
   * Dabei werden nur die Werte der Eigenschaften ueberschrieben - nichts anderes.
   * Also auch keine Meta-Daten oder aehnliches.
   * Handelt es sich bei der Quelle um ein Objekt fremden Typs, wird nichts ueberschrieben.
   * @param object das Objekt, welches als Quelle verwendet werden soll.
   * @throws RemoteException im Fehlerfall.
   */
  public void overwrite(DBObject object) throws RemoteException;
 
}

/*********************************************************************
 * $Log: Changeable.java,v $
 * Revision 1.2  2004/08/31 18:13:59  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/08/18 23:14:00  willuhn
 * @D Javadoc
 *
 **********************************************************************/