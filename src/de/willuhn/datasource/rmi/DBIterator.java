/**********************************************************************
 * $Source: /cvsroot/jameica/datasource/src/de/willuhn/datasource/rmi/DBIterator.java,v $
 * $Revision: 1.6 $
 * $Date: 2006/08/23 09:31:34 $
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

import de.willuhn.datasource.*;

/**
 * Iterator fuer Datenbanktabellen auf Objekt-Ebene.
 * @author willuhn
 */
public interface DBIterator extends GenericIterator
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
   * <code>addFilter("kontonummer=?",new Object[]{"200"});</code>
   * Die Verwendung des PreparedStatements schuetzt vor SQL-Injections.
   * @see DBIterator#addFilter(String)
   * @param filter ein zusaetzlicher Filter.
   * @param params
   * @throws RemoteException
   */
  public void addFilter(String filter, Object[] params) throws RemoteException;
  
  /**
   * Fuegt dem Iterator eine Sortierung hinzu.
   * @param order
   * @throws RemoteException
   */
  public void setOrder(String order) throws RemoteException;

}


/*********************************************************************
 * $Log: DBIterator.java,v $
 * Revision 1.6  2006/08/23 09:31:34  willuhn
 * @N DBIterator kann nun auch PreparedStatements verwenden
 *
 * Revision 1.5  2004/08/31 18:13:59  willuhn
 * *** empty log message ***
 *
 * Revision 1.4  2004/07/21 23:53:56  willuhn
 * @C massive Refactoring ;)
 *
 * Revision 1.3  2004/06/17 00:05:50  willuhn
 * @N GenericObject, GenericIterator
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
 * Revision 1.8  2003/12/29 16:29:47  willuhn
 * @N javadoc
 *
 * Revision 1.7  2003/12/28 22:58:27  willuhn
 * @N synchronize mode
 *
 * Revision 1.6  2003/12/10 00:47:12  willuhn
 * @N SearchDialog done
 * @N ErrorView
 *
 * Revision 1.5  2003/11/30 16:23:09  willuhn
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
 *
 * Revision 1.3  2003/10/29 20:56:49  willuhn
 * @N added transactionRollback
 *
 * Revision 1.2  2003/10/29 17:33:21  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2003/10/27 11:49:12  willuhn
 * @N added DBIterator
 *
 **********************************************************************/