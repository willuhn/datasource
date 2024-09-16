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

import de.willuhn.datasource.GenericObject;

/**
 * Erweiterung von {@link GenericObject} um Datenbank-Funktionalitaet.
 */
public interface DBObject extends GenericObject, Transactionable, Changeable
{
  
  /**
   * Laedt die Eigenschaften des Datensatzes mit der angegebenen
   * ID aus der Datenbank.
   * @param id ID des zu ladenden Objektes.
   * @throws RemoteException im Fehlerfall.
   */
  public void load(String id) throws RemoteException;

  /**
   * Liefert den Wert des angegebenen Attributes.
   *
   * <p>Aber die Funktion ist richtig schlau ;)
   * Sie checkt naemlich den Typ des Feldes in der Datenbank und liefert nicht nur einen String sondern den
   * korrespondierenden Java-Typ.
   * Insofern die Businessklasse die Funktion {@link de.willuhn.datasource.db.AbstractDBObject#getForeignObject(String)}
   * sinnvoll uberschrieben hat, liefert die Funktion bei Fremdschluesseln sogar gleich das entsprechende Objekt aus der
   * Verknuepfungstabelle.
   *
   * @param name Name des Feldes.
   * @return Wert des Feldes.
   * @throws RemoteException im Fehlerfall.
   * @see de.willuhn.datasource.GenericObject#getAttribute(java.lang.String)
   */
  public Object getAttribute(String name) throws RemoteException;

  /**
   * Liefert den Attributtyp des uebergebenen Feldes.
   * @param attributeName Name des Attributes.
   * @return Konstante fuer den Attributtyp.
   * @throws RemoteException im Fehlerfall.
   * @see de.willuhn.datasource.db.AbstractDBObject#setAttribute(String, Object)
   */
  public String getAttributeType(String attributeName) throws RemoteException;

  /**
   * @see de.willuhn.datasource.GenericObject#getPrimaryAttribute()
   */
  public String getPrimaryAttribute() throws RemoteException;

  /**
   * Liefert eine Liste aller Objekte des aktuellen Types.
   * @return Liste mit allen Objekten dieser Tabelle.
   * @throws RemoteException
   */
  public DBIterator getList() throws RemoteException;

  /**
   * Vergleicht dieses Objekt mit dem uebergebenen.
   * Hinweis: Es wird nicht der Inhalt verglichen sondern nur die ID und der Typ.
   * @param other das zu vergleichende Objekt.
   * @return true, wenn sie vom gleichen Typ sind und die selbe ID haben.
   * @throws RemoteException
   * @see de.willuhn.datasource.GenericObject#equals(de.willuhn.datasource.GenericObject)
   */
  public boolean equals(GenericObject other) throws RemoteException;

	/**
	 * Fuegt dem Objekt einen Listener hinzu, der ausgeloest wird, wenn
	 * das Objekt gerade geloescht wurde.
	 * Hinweis: Das Event wird unmittelbar <b>nach</b> dem Loeschen
	 * ausgeloest, jedoch noch bevor die ID auf null gesetzt wurde.
	 * @param l der Listener.
	 * @throws RemoteException
	 */
	public void addDeleteListener(Listener l) throws RemoteException; 
	
	/**
	 * Fuegt dem Objekt einen Listener hinzu, der ausgeloest wird, wenn
	 * das Objekt gespeichert wurde.
	 * Hinweis: Das Event wird <b>nach</b> dem Speichern ausgeloest.
   * @param l der Listener.
   * @throws RemoteException
   */
  public void addStoreListener(Listener l) throws RemoteException;

  /**
   * Entfernt den Listener wieder.
   * @param l der Listener.
   * @throws RemoteException
   */
  public void removeDeleteListener(Listener l) throws RemoteException; 
  
  /**
   * Entfernt den Listener wieder.
   * @param l der Listener.
   * @throws RemoteException
   */
  public void removeStoreListener(Listener l) throws RemoteException;

}

/*********************************************************************
 * $Log: DBObject.java,v $
 * Revision 1.12  2006/10/18 17:07:20  willuhn
 * @N DBIterator registriert einen DeleteListener, um sich selbst zu bereinigen, wenn Objekte aus ihr geloescht werden
 * @N deleteListener in AbstractDBObject
 *
 * Revision 1.11  2006/01/04 17:04:56  web0
 * @C DeleteListener wird nun unmittelbar nach dem Loeschen jedoch vor this.id=null aufgerufen
 *
 * Revision 1.10  2004/10/25 17:58:37  willuhn
 * @N Delete/Store-Listeners
 *
 * Revision 1.9  2004/08/31 18:13:59  willuhn
 * *** empty log message ***
 *
 * Revision 1.8  2004/08/18 23:21:38  willuhn
 * *** empty log message ***
 *
 * Revision 1.7  2004/08/18 23:14:00  willuhn
 * @D Javadoc
 *
 * Revision 1.6  2004/07/21 23:53:56  willuhn
 * @C massive Refactoring ;)
 *
 * Revision 1.5  2004/07/13 22:19:30  willuhn
 * @C paar Funktionsnamen umbenannt
 *
 * Revision 1.4  2004/06/17 00:05:50  willuhn
 * @N GenericObject, GenericIterator
 *
 * Revision 1.3  2004/04/05 23:28:30  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2004/03/06 18:24:34  willuhn
 * @D javadoc
 *
 * Revision 1.1  2004/01/10 14:52:19  willuhn
 * @C package removings
 *
 * Revision 1.1  2004/01/08 20:46:44  willuhn
 * @N database stuff separated from jameica
 *
 * Revision 1.15  2003/12/29 22:07:40  willuhn
 * *** empty log message ***
 *
 * Revision 1.14  2003/12/29 16:29:47  willuhn
 * @N javadoc
 *
 * Revision 1.13  2003/12/28 22:58:27  willuhn
 * @N synchronize mode
 *
 * Revision 1.12  2003/12/27 21:23:33  willuhn
 * @N object serialization
 *
 * Revision 1.11  2003/12/26 21:43:30  willuhn
 * @N customers changable
 *
 * Revision 1.10  2003/12/19 01:43:27  willuhn
 * @N added Tree
 *
 * Revision 1.9  2003/12/15 19:08:01  willuhn
 * *** empty log message ***
 *
 * Revision 1.8  2003/11/27 00:22:18  willuhn
 * @B paar Bugfixes aus Kombination RMI + Reflection
 * @N insertCheck(), deleteCheck(), updateCheck()
 * @R AbstractDBObject#toString() da in RemoteObject ueberschrieben (RMI-Konflikt)
 *
 * Revision 1.7  2003/11/24 23:01:58  willuhn
 * @N added settings
 *
 * Revision 1.6  2003/11/24 16:25:53  willuhn
 * @N AbstractDBObject is now able to resolve foreign keys
 *
 * Revision 1.5  2003/11/24 14:21:53  willuhn
 * *** empty log message ***
 *
 * Revision 1.4  2003/11/22 20:43:05  willuhn
 * *** empty log message ***
 *
 * Revision 1.3  2003/11/21 02:10:21  willuhn
 * @N prepared Statements in AbstractDBObject
 * @N a lot of new SWT parts
 *
 * Revision 1.2  2003/11/20 03:48:42  willuhn
 * @N first dialogues
 *
 * Revision 1.1  2003/11/05 22:46:19  willuhn
 * *** empty log message ***
 **********************************************************************/