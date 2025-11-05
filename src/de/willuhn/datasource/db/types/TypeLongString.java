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

import java.io.IOException;
import java.io.Reader;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

/**
 * Typ fuer "TEXT" und "LONGVARCHAR".
 */
public class TypeLongString extends TypeGeneric
{
  
  @Override
  public Object get(ResultSet rs, String name) throws SQLException
  {
    Object value = rs.getObject(name);
    if (value != null && (value instanceof Reader))
    {
      // Wenn es ein Stream ist, kopieren wir die Daten in ein
      // Byte-Array
      Reader r = (Reader) value;
      try
      {
        StringBuffer sb = new StringBuffer();
        char[] buf = new char[1024];
        int len = 0;
        while ((len = r.read(buf)) != -1)
          sb.append(buf,0,len);
        
        return sb.toString();
      }
      catch (IOException ioe)
      {
        throw new SQLException("unable to read text/longvarchar: " + ioe.getMessage());
      }
      finally
      {
        try
        {
          r.close();
        }
        catch (IOException e)
        {
          throw new SQLException("unable to close inputstream: " + e.getMessage());
        }
      }
    }
    return value;
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
 * $Log: TypeLongString.java,v $
 * Revision 1.1  2008/07/15 11:02:31  willuhn
 * @N Neuer Typ "TypeLongString", der aus den Feldern "TEXT", "LONGTEXT" und "LONGVARCHAR" bei Bedarf aus einem Reader liest (ist abhaengig vom JDBC-Treiber)
 *
 * Revision 1.2  2008/07/14 12:11:53  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2008/07/11 09:30:17  willuhn
 * @N Support fuer Byte-Arrays
 * @N SQL-Typen sind jetzt erweiterbar
 *
 **********************************************************************/