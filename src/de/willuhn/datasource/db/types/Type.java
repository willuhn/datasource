/**********************************************************************
 * $Source: /cvsroot/jameica/datasource/src/de/willuhn/datasource/db/types/Type.java,v $
 * $Revision: 1.1 $
 * $Date: 2008/07/11 09:30:17 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.datasource.db.types;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Basis-Interface fuer alle Typen.
 */
public interface Type
{
  /**
   * Liest den Wert zum angegebenen Feld aus dem Resultset.
   * @param rs Resultset
   * @param name Name des Feldes.
   * @return gelesener Wert.
   * @throws SQLException
   */
  public Object get(ResultSet rs, String name) throws SQLException;
  
  /**
   * Speichert den uebergebenen Wert im Statement.
   * @param stmt das Statement.
   * @param index Index.
   * @param value der Wert.
   * @throws SQLException
   */
  public void set(PreparedStatement stmt, int index, Object value) throws SQLException;
}


/*********************************************************************
 * $Log: Type.java,v $
 * Revision 1.1  2008/07/11 09:30:17  willuhn
 * @N Support fuer Byte-Arrays
 * @N SQL-Typen sind jetzt erweiterbar
 *
 **********************************************************************/