/**********************************************************************
 * $Source: /cvsroot/jameica/datasource/src/de/willuhn/datasource/Service.java,v $
 * $Revision: 1.1 $
 * $Date: 2004/07/21 23:53:56 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.datasource;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Basis-Interface aller Services, die via RMI genutzt werden koennen.
 * @author willuhn
 */
public interface Service extends Remote 
{

  /**
   * Oeffnet den Service. (Initialisiert ihn).
   * @throws RemoteException
   */
  public void open() throws RemoteException;
	
  /**
   * Schliesst den Service.
   * @throws RemoteException
   */
  public void close() throws RemoteException;

	/**
	 * Erzeugt ein neues Objekt des angegebenen Typs.
	 * @param clazz Name der Klasse des zu erzeugenden Objektes.
	 * @param identifier der eindeutige Identifier des Objektes.
	 * Kann null sein, wenn ein neues Objekt erzeugt werden soll.
	 * Andernfalls wird das mit dem genannten Identifier geladen.
	 * @return Das erzeugte Objekt
	 * @throws RemoteException
	 */
	public GenericObject createObject(Class clazz, String identifier) throws RemoteException;

  /**
   * Prueft, ob dieser Service auf dem Server noch verfuegbar ist.
   * Wenn diese Funktion false liefert, kann der Service auch via open()
   * nicht mehr geoeffnet werden. Heisst: Der Service ist nicht
   * nur geschlossen worden sondern der gesamte Server ist heruntergefahren
   * worden.
   * @throws RemoteException
   * @return true wenn er verfuegbar ist, sonst false.
   */
  public boolean isAvailable() throws RemoteException;


  /**
   * Faehrt den Service herunter. Er kann danach nicht mehr genutzt werden.
   * Diese Funktion wird vom Shutdown-Prozess des Servers aufgerufen.
   * @throws RemoteException
   */
  public void shutDown() throws RemoteException;
}

/*********************************************************************
 * $Log: Service.java,v $
 * Revision 1.1  2004/07/21 23:53:56  willuhn
 * @C massive Refactoring ;)
 *
 * Revision 1.5  2004/06/30 20:58:07  willuhn
 * @C some refactoring
 *
 * Revision 1.4  2004/06/17 00:05:50  willuhn
 * @N GenericObject, GenericIterator
 *
 * Revision 1.3  2004/03/18 01:24:17  willuhn
 * @C refactoring
 *
 * Revision 1.2  2004/03/06 18:24:34  willuhn
 * @D javadoc
 *
 * Revision 1.1  2004/01/10 14:52:19  willuhn
 * @C package removings
 *
 * Revision 1.2  2004/01/08 21:38:47  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/01/08 20:46:44  willuhn
 * @N database stuff separated from jameica
 *
 * Revision 1.3  2003/12/12 21:11:29  willuhn
 * @N ObjectMetaCache
 *
 * Revision 1.2  2003/10/29 00:41:27  willuhn
 * *** empty log message ***
 *
 **********************************************************************/