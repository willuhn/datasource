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
 * Generisches RMI-faehiges Objekt, welches Attribute besitzt.
 *
 * <p>Das kann also so ziemlich alles sein, vom Kalendereintrag bis
 * zum Datensatz in einer Datenbank. Entscheidendes Merkmal ist,
 * dass es eine Funktion {@link #getAttribute(String)} besitzt,
 * mit der die Werte der Attribute ueber Aliasnamen abgefragt
 * werden koennen. 
 */
public interface GenericObject extends Remote {
  

	/**
	 * Liefert den Wert des angegebenen Attributes.
	 * @param name Name des Attributes.
	 * @return Wert des Attributes.
	 * @throws RemoteException im Fehlerfall.
	 */
	public Object getAttribute(String name) throws RemoteException;

	/**
	 * Liefert ein String-Array mit allen verfuegbaren Attribut-Namen.
   * @return Liste aller Attribut-Namen.
   * @throws RemoteException
   */
  public String[] getAttributeNames() throws RemoteException;
	
	/**
	 * Liefert einen Identifier fuer dieses Objekt.
	 * Dieser muss innerhalb des gesamten Systems/Services fuer diese Objektart eindeutig sein.
   * @return der Identifier des Objektes.
   * @throws RemoteException
   */
  public String getID() throws RemoteException;

	/**
	 * Liefert den Namen des Primaer-Attributes dieses Objektes.
	 *
	 * <p>Hintergrund: Wenn man z.Bsp. in einer Select-Box nur einen Wert
	 * anzeigen kann, dann wird dieser genommen.
	 *
	 * <p>Achtung: Die Funktion liefert nicht den Wert des Attributes sondern nur dessen Namen.
	 *
	 * @return Name des Primaer-Attributes.
	 * @throws RemoteException im Fehlerfall.
	 */
	public String getPrimaryAttribute() throws RemoteException;

	/**
	 * Vergleicht dieses Objekt mit dem uebergebenen.
	 *
	 * <p>Achtung: Wir ueberschreiben hier nicht {@link Object#equals(Object)},
	 * da das via RMI nicht geht.
	 * @param other das zu vergleichende Objekt.
	 * @return true, die Objekte gleiche Eigenschaften besitzen.
	 * @throws RemoteException
	 */
	public boolean equals(GenericObject other) throws RemoteException;

}


/**********************************************************************
 * $Log: GenericObject.java,v $
 * Revision 1.2  2004/12/09 23:22:25  willuhn
 * @N getAttributeNames nun Bestandteil der API
 *
 * Revision 1.1  2004/07/21 23:53:56  willuhn
 * @C massive Refactoring ;)
 *
 * Revision 1.2  2004/06/17 00:28:01  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/06/17 00:05:50  willuhn
 * @N GenericObject, GenericIterator
 *
 **********************************************************************/