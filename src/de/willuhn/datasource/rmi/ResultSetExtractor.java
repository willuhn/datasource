/**********************************************************************
 * $Source: /cvsroot/jameica/datasource/src/de/willuhn/datasource/rmi/ResultSetExtractor.java,v $
 * $Revision: 1.1 $
 * $Date: 2006/09/05 20:52:24 $
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
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Ein Interface, ueber das man sich die Daten aus einem Resultset holen kann.
 */
public interface ResultSetExtractor extends Remote
{
  /**
   * Wird vom DBService aufgerufen.
   * @param rs das erzeugte Resultset.
   * @return das extrahierte Objekt.
   * @throws RemoteException
   * @throws SQLException
   */
  public Object extract(ResultSet rs) throws RemoteException, SQLException;
}


/*********************************************************************
 * $Log: ResultSetExtractor.java,v $
 * Revision 1.1  2006/09/05 20:52:24  willuhn
 * @N Added ResultsetExtractor (portiert aus Syntax)
 *
 * Revision 1.1  2006/05/30 23:22:55  willuhn
 * @C Redsign beim Laden der Buchungen. Jahresabschluss nun korrekt
 *
 **********************************************************************/