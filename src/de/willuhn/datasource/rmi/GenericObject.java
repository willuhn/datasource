/**********************************************************************
 * $Source: /cvsroot/jameica/datasource/src/de/willuhn/datasource/rmi/Attic/GenericObject.java,v $
 * $Revision: 1.1 $
 * $Date: 2004/06/17 00:05:50 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.datasource.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Generisches RMI-faehiges Objekt, welches Attribute besitzt.
 * Das also so ziemlich alles sein, vom Kalendereintrag bis
 * zum Datensatz in Datenbank. Entscheidendes Merkmal ist,
 * dass es eine Funktion getAttribute(AliasName) besitzt,
 * mit der die Werte der Attribute ueber Aliasnamen abgefragt
 * werden koennen. 
 */
public interface GenericObject extends Remote {
  

	/**
	 * Liefert den Wert des angegebenen Attributes.
	 * @param name Name des Feldes.
	 * @return Wert des Feldes.
	 * @throws RemoteException im Fehlerfall.
	 */
	public Object getAttribute(String name) throws RemoteException;

	/**
	 * Liefert einen Identifier fuer dieses Objekt.
	 * Dieser muss innerhalb des gesamten Systems/Services fuer diese Objektart eindeutig sein.
   * @return der Identifier des Objektes.
   * @throws RemoteException
   */
  public String getID() throws RemoteException;

	/**
	 * Liefert den Namen des Primaer-Attributes dieses Objektes.
	 * Hintergrund: Wenn man z.Bsp. in einer Select-Box nur einen Wert
	 * anzeigen kann, dann wird dieser genommen.
	 * Achtung: Die Funktion liefert nicht den Wert des Attributes sondern nur dessen Namen.
	 * @return Name des Primaer-Attributes.
	 * @throws RemoteException im Fehlerfall.
	 */
	public String getPrimaryAttribute() throws RemoteException;

	/**
	 * Vergleicht dieses Objekt mit dem uebergebenen.
	 * Achtung: Wir ueberschreiben hier nicht die equals-Funktion von <code>Object</code>
	 * da das via RMI nicht geht.
	 * @param other das zu vergleichende Objekt.
	 * @return true, wenn sie vom gleichen Typ sind und die selbe ID haben.
	 * @throws RemoteException
	 */
	public boolean equals(GenericObject other) throws RemoteException;

}


/**********************************************************************
 * $Log: GenericObject.java,v $
 * Revision 1.1  2004/06/17 00:05:50  willuhn
 * @N GenericObject, GenericIterator
 *
 **********************************************************************/