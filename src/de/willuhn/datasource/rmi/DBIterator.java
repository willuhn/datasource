/**********************************************************************
 *
 * Copyright (c) by Olaf Willuhn
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.datasource.rmi;

import java.rmi.RemoteException;

import de.willuhn.datasource.GenericIterator;

/**
 * Iterator fuer Datenbanktabellen auf Objekt-Ebene.
 * @param <T> der konkrete Typ.
 */
public interface DBIterator<T extends DBObject> extends GenericIterator<T>
{


  /**
   * Fuegt dem Iterator einen zusaetzlichen Filter hinzu, der
   * sich auf die Anzahl der Treffer auswirkt. Bsp:
   * addFilter("kontonummer='2020'");
   * Bewirkt, dass eine zusaetzliche Where-Klausel "where kontonummer='2020'"
   * hinzugefuegt wird.
   * @param filter ein zusaetzlicher SQL-Filter.
   * Z.Bsp.: "konto_id = 20".
   * @throws RemoteException
   */
  public void addFilter(String filter) throws RemoteException;
  
  /**
   * Wie {@link DBIterator#addFilter(String)} - allerdings mit dem
   * Unterschied, dass ueber das Objekt-Array zusaetzliche Parameter
   * angegeben werden koennen, mit denen dann ein PreparedStatement
   * gefuellt wird.
   * Mann kann also entweder schreiben:
   * <code>addFilter("kontonummer='200'");</code>
   * oder
   * <code>addFilter("kontonummer=?","200");</code>
   * Die Verwendung des PreparedStatements schuetzt vor SQL-Injections.
   * @see DBIterator#addFilter(String)
   * @param filter ein zusaetzlicher Filter.
   * @param params
   * @throws RemoteException
   */
  public void addFilter(String filter, Object... params) throws RemoteException;
  
  /**
   * Fuegt eine Tabelle via Join hinzu.
   * @param table zu joinende Tabelle.
   * @throws RemoteException
   */
  public void join(String table) throws RemoteException;
  
  /**
   * Fuegt dem Iterator eine Sortierung hinzu.
   * @param order
   * @throws RemoteException
   */
  public void setOrder(String order) throws RemoteException;
  
  /**
   * Fuegt ein "limit {i}" dem Statement hinzu.
   * @param i Hoehe des Limit.
   * @throws RemoteException
   */
  public void setLimit(int i) throws RemoteException;

}
