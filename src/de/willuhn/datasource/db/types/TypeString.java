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
import java.sql.Types;

/**
 * Typ String.
 */
public class TypeString extends TypeGeneric
{
  
  @Override
  public Object get(ResultSet rs, String name) throws SQLException
  {
    return rs.getString(name);
  }

  @Override
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
 * Revision 1.2  2008/07/14 12:11:53  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2008/07/11 09:30:17  willuhn
 * @N Support fuer Byte-Arrays
 * @N SQL-Typen sind jetzt erweiterbar
 *
 **********************************************************************/