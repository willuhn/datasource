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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Blob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import de.willuhn.logging.Logger;

/**
 * Typ Byte-Array.
 */
public class TypeByteArray extends TypeGeneric
{
  
  @Override
  public Object get(ResultSet rs, String name) throws SQLException
  {
    Object value = super.get(rs, name);
    if (value == null)
      return null;

    InputStream is = null;
    
    if (value instanceof InputStream)
    {
      is = (InputStream) value;
    }
    else if (value instanceof Blob)
    {
      Blob b = (Blob) value;
      is = b.getBinaryStream();
    }
    else if (value instanceof byte[])
      return value;

    if (is == null)
    {
      Logger.warn("don't know how to handle type " + value.getClass().getName() + " - returning unchanged");
      return value;
    }
    
    // Daten aus dem InputStream kopieren
    try
    {
      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      byte[] buf = new byte[1024];
      int len = 0;
      while ((len = is.read(buf)) != -1)
        bos.write(buf,0,len);
        
      return bos.toByteArray();
    }
    catch (IOException ioe)
    {
      throw new SQLException("unable to read blob: " + ioe.getMessage());
    }
    finally
    {
      try
      {
        is.close();
      }
      catch (IOException e)
      {
        throw new SQLException("unable to close inputstream: " + e.getMessage());
      }
    }
  }

  @Override
  public void set(PreparedStatement stmt, int index, Object value) throws SQLException
  {
    if (value == null)
      stmt.setNull(index,Types.NULL);
    else
      stmt.setBytes(index,(byte[])value);
  }
}


/*********************************************************************
 * $Log: TypeByteArray.java,v $
 * Revision 1.4  2009/07/14 12:00:47  willuhn
 * @C byte[] unveraendert zurueckliefern
 *
 * Revision 1.3  2009/06/28 22:04:09  willuhn
 * @C java.sql.Blob in Byte-Array kopieren (Heiners Patch)
 *
 * Revision 1.2  2008/07/11 16:15:54  willuhn
 * @B rs.getObject() liefert ggf. (abhaengig von der Datenbank) einen InputStream statt byte[]
 *
 * Revision 1.1  2008/07/11 09:30:17  willuhn
 * @N Support fuer Byte-Arrays
 * @N SQL-Typen sind jetzt erweiterbar
 *
 **********************************************************************/