/**********************************************************************
 * $Source: /cvsroot/jameica/datasource/src/de/willuhn/datasource/db/types/TypeInteger.java,v $
 * $Revision: 1.2 $
 * $Date: 2009/01/29 22:31:29 $
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
 * Typ Integer.
 */
public class TypeInteger extends TypeGeneric
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
      if (value instanceof Integer)
        stmt.setInt(index,((Integer) value).intValue());
      else if (value instanceof Long)
        stmt.setLong(index,((Long) value).longValue());
      else
        stmt.setObject(index,value);
    }
  }
}


/*********************************************************************
 * $Log: TypeInteger.java,v $
 * Revision 1.2  2009/01/29 22:31:29  willuhn
 * @B http://www.onlinebanking-forum.de/phpBB2/viewtopic.php?p=54958#54958
 *
 * Revision 1.1  2008/07/11 09:30:17  willuhn
 * @N Support fuer Byte-Arrays
 * @N SQL-Typen sind jetzt erweiterbar
 *
 **********************************************************************/