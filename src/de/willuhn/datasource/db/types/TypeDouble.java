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

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

/**
 * Typ Double.
 */
public class TypeDouble extends TypeGeneric
{

  /**
   * @see de.willuhn.datasource.db.types.TypeGeneric#set(java.sql.PreparedStatement, int, java.lang.Object)
   */
  public void set(PreparedStatement stmt, int index, Object value) throws SQLException
  {
    if (value == null)
      stmt.setNull(index,Types.NULL);
    else
    {
      if (value instanceof Double)
        stmt.setDouble(index,((Number) value).doubleValue());
      else if (value instanceof BigDecimal)
        stmt.setBigDecimal(index,(BigDecimal) value);
      else
        stmt.setObject(index,value);
    }
 }
}


/*********************************************************************
 * $Log: TypeDouble.java,v $
 * Revision 1.3  2008/09/02 18:00:12  willuhn
 * @N BigDecimal akzeptieren
 *
 * Revision 1.1  2008/07/11 09:30:17  willuhn
 * @N Support fuer Byte-Arrays
 * @N SQL-Typen sind jetzt erweiterbar
 *
 **********************************************************************/