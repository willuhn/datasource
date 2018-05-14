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

import de.willuhn.datasource.Service;

/**
 * Basisklasse fuer alle DBServices.
 */
public interface DBService extends Service
{

	/**
   * Liefert eine Liste aller in der Datenbank vorhandenen Objekte des angegebenen Typs,
   * @param clazz Name der Klasse von der eine Liste geholt werden soll.
   * @return Eine Liste mit den gefundenen Objekten.
   * @throws RemoteException
   */
  public <T extends DBObject> DBIterator<T> createList(Class<? extends DBObject> clazz) throws RemoteException;

	/**
	 * Erzeugt ein neues Objekt des angegebenen Typs.
	 * @param clazz Name der Klasse des zu erzeugenden Objektes.
	 * @param identifier der eindeutige Identifier des Objektes.
	 * Kann null sein, wenn ein neues Objekt erzeugt werden soll.
	 * Andernfalls wird das mit dem genannten Identifier geladen.
	 * @return Das erzeugte Objekt
	 * @throws RemoteException
	 */
	public <T extends DBObject> T createObject(Class<? extends DBObject> clazz, String identifier) throws RemoteException;

  /**
   * Fuehrt ein SQL-Statement aus und uebergibt das Resultset an den Extractor.
   * @param sql das Statement.
   * @param params die Parameter zur Erzeugung des PreparedStatements.
   * @param extractor der Extractor.
   * @return die vom ResultSetExtractor zurueckgelieferten Daten.
   * @throws RemoteException
   */
  public Object execute(String sql, Object[] params, ResultSetExtractor extractor) throws RemoteException;

}
