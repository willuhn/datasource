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