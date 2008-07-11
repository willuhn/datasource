/**********************************************************************
 * $Source: /cvsroot/jameica/datasource/src/de/willuhn/datasource/db/types/TypeString.java,v $
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
import java.sql.SQLException;
import java.sql.Types;

/**
 * Typ String.
 */
public class TypeString extends TypeGeneric
{
  /**
   * @see de.willuhn.datasource.db.types.TypeGeneric#set(java.sql.PreparedStatement, int, java.lang.Object)
   */
  public void set(PreparedStatement stmt, int index, Object value) throws SQLException
  {
    if (value == null)
      stmt.setNull(index,Types.NULL);
    else
      stmt.setString(index,value.toString());
  }
}


/*********************************************************************
 * $Log: TypeString.java,v $
 * Revision 1.1  2008/07/11 09:30:17  willuhn
 * @N Support fuer Byte-Arrays
 * @N SQL-Typen sind jetzt erweiterbar
 *
 **********************************************************************/