/**********************************************************************
 * $Source: /cvsroot/jameica/datasource/src/de/willuhn/datasource/db/types/TypeDate.java,v $
 * $Revision: 1.1 $
 * $Date: 2008/07/11 09:30:16 $
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
import java.sql.SQLException;
import java.sql.Types;

/**
 * Typ Date
 */
public class TypeDate extends TypeGeneric
{

  /**
   * @see de.willuhn.datasource.db.types.TypeGeneric#set(java.sql.PreparedStatement, int, java.lang.Object)
   */
  public void set(PreparedStatement stmt, int index, Object value) throws SQLException
  {
    if (value == null)
      stmt.setNull(index,Types.NULL);
    else
      stmt.setDate(index,new java.sql.Date(((java.util.Date) value).getTime()));
  }

}


/*********************************************************************
 * $Log: TypeDate.java,v $
 * Revision 1.1  2008/07/11 09:30:16  willuhn
 * @N Support fuer Byte-Arrays
 * @N SQL-Typen sind jetzt erweiterbar
 *
 **********************************************************************/