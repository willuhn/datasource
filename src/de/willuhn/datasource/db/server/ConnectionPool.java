/**********************************************************************
 * $Source: /cvsroot/jameica/datasource/src/de/willuhn/datasource/db/server/Attic/ConnectionPool.java,v $
 * $Revision: 1.1 $
 * $Date: 2004/01/08 20:46:44 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.datasource.db.server;

import java.sql.Connection;

import de.willuhn.datasource.db.rmi.DBObject;
import de.willuhn.util.Session;

/**
 * Das Teil hat eigentlich nur folgenden Grund: Die Connection darf
 * kein Member eines DBObject sein, da es sich sonst nicht serialisieren
 * laesst. Die Serialisierung ist aber fuer den SynchronizeWatcher notwendig.
 * @author willuhn
 */
public class ConnectionPool
{

  private static Session session = null;

  /**
   * 
   */
  private ConnectionPool()
  {
  }

  public static void setConnection(DBObject o, Connection conn)
  {
    if (session == null)
      session = new Session(ConnectionPool.class);

    session.setAttribute(o,conn);
  }

  public static Connection getConnection(DBObject o)
  {
    if (session == null || o == null)
      return null;
    return (Connection) session.getAttribute(o);
  }
}

/*********************************************************************
 * $Log: ConnectionPool.java,v $
 * Revision 1.1  2004/01/08 20:46:44  willuhn
 * @N database stuff separated from jameica
 *
 * Revision 1.3  2004/01/04 18:48:36  willuhn
 * @N config store support
 *
 * Revision 1.2  2003/12/28 22:58:27  willuhn
 * @N synchronize mode
 *
 * Revision 1.1  2003/12/27 21:23:33  willuhn
 * @N object serialization
 *
 **********************************************************************/