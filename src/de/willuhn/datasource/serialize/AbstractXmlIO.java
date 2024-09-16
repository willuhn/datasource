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

package de.willuhn.datasource.serialize;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import de.willuhn.logging.Logger;
import de.willuhn.util.Base64;


/**
 * Abstrakte Basis-Implementierung des XML-Formats.
 */
public abstract class AbstractXmlIO implements IO
{
  protected final static String ENCODING = "UTF-8";
  protected final static Map valueMap = new HashMap();
  static
  {
    valueMap.put(null,                   new StringValue());
    valueMap.put("java.lang.Double",     new DoubleValue());
    valueMap.put("java.math.BigDecimal", new BigDecimalValue());
    valueMap.put("java.lang.Integer",    new IntegerValue());
    valueMap.put("java.lang.Long",       new LongValue());
    valueMap.put("java.lang.Boolean",    new BooleanValue());
    valueMap.put("java.util.Date",       new DateValue());
    valueMap.put("java.sql.Date",        new SqlDateValue());
    valueMap.put("java.sql.Timestamp",   new TimestampValue());
    valueMap.put("[B",                   new ByteArrayValue());
  }

  protected static interface Value
  {
    /**
     * Erzeugt eine String-Repraesentation des Objektes.
     * @param o Objekt.
     * @return String-Repraesentation.
     * @throws IOException
     */
    public String serialize(Object o) throws IOException;
    
    /**
     * Erzeugt ein Object aus dem String.
     * @param s der String.
     * @return das Objekt.
     * @throws IOException
     */
    public Object unserialize(String s) throws IOException;
  }
  
  protected static abstract class AbstractValue implements Value
  {
    /**
     * @see de.willuhn.datasource.serialize.AbstractXmlIO.Value#serialize(java.lang.Object)
     */
    public String serialize(Object o) throws IOException
    {
      return o == null ? "" : o.toString();
    }
  }
  
  /**
   * Implementierung fuer {@link java.lang.String}.
   */
  protected static class StringValue extends AbstractValue
  {
    /**
     * @see de.willuhn.datasource.serialize.AbstractXmlIO.Value#unserialize(java.lang.String)
     */
    public Object unserialize(String s) throws IOException
    {
      return s == null ? "" : s;
    }
  }

  /**
   * Implementierung fuer {@link java.lang.Double}.
   */
  protected static class DoubleValue extends AbstractValue
  {
    /**
     * @see de.willuhn.datasource.serialize.AbstractXmlIO.Value#unserialize(java.lang.String)
     */
    public Object unserialize(String s) throws IOException
    {
      return (s == null || s.length() == 0) ? null : new Double(s);
    }
  }
  
  /**
   * Implementierung fuer {@link java.math.BigDecimal}.
   */
  protected static class BigDecimalValue extends AbstractValue
  {
    /**
     * @see de.willuhn.datasource.serialize.AbstractXmlIO.Value#unserialize(java.lang.String)
     */
    public Object unserialize(String s) throws IOException
    {
      return (s == null || s.length() == 0) ? null : new BigDecimal(s);
    }
  }

  /**
   * Implementierung fuer {@link java.lang.Integer}.
   */
  protected static class IntegerValue extends AbstractValue
  {
    /**
     * @see de.willuhn.datasource.serialize.AbstractXmlIO.Value#unserialize(java.lang.String)
     */
    public Object unserialize(String s) throws IOException
    {
      return (s == null || s.length() == 0) ? null : new Integer(s);
    }
  }

  /**
   * Implementierung fuer {@link java.lang.Long}.
   */
  protected static class LongValue extends AbstractValue
  {
    /**
     * @see de.willuhn.datasource.serialize.AbstractXmlIO.Value#unserialize(java.lang.String)
     */
    public Object unserialize(String s) throws IOException
    {
      return (s == null || s.length() == 0) ? null : new Long(s);
    }
  }
  
  /**
   * Implementierung fuer {@link java.lang.Boolean}.
   */
  protected static class BooleanValue extends AbstractValue
  {
    /**
     * @see de.willuhn.datasource.serialize.AbstractXmlIO.Value#unserialize(java.lang.String)
     */
    public Object unserialize(String s) throws IOException
    {
      return s != null && (s.equalsIgnoreCase("true") || s.equalsIgnoreCase("1"));
    }
  }

  /**
   * Implementierung fuer {@link java.util.Date}.
   */
  protected static class DateValue implements Value
  {
    private final static DateFormat format = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");

    /**
     * @see de.willuhn.datasource.serialize.AbstractXmlIO.Value#unserialize(java.lang.String)
     */
    public Object unserialize(String s) throws IOException
    {
      if (s == null || s.length() == 0)
        return null;
      try
      {
        return format.parse(s);
      }
      catch (ParseException e)
      {
        Logger.error("unable to parse date " + s);
        throw new IOException(e.getMessage());
      } 
    }

    /**
     * @see de.willuhn.datasource.serialize.AbstractXmlIO.Value#serialize(java.lang.Object)
     */
    public String serialize(Object o) throws IOException
    {
      return o == null ? "" : format.format((Date)o);
    }
  }
  
  /**
   * Implementierung fuer {@link java.sql.Date}.
   */
  protected static class SqlDateValue extends DateValue
  {
    /**
     * @see de.willuhn.datasource.serialize.AbstractXmlIO.Value#unserialize(java.lang.String)
     */
    public Object unserialize(String s) throws IOException
    {
      Date date = (Date) super.unserialize(s);
      return date == null ? null : new java.sql.Date(date.getTime());
    }
  }

  /**
   * Implementierung fuer {@link java.sql.Timestamp}.
   */
  protected static class TimestampValue extends DateValue
  {
    /**
     * @see de.willuhn.datasource.serialize.AbstractXmlIO.Value#unserialize(java.lang.String)
     */
    public Object unserialize(String s) throws IOException
    {
      Date date = (Date) super.unserialize(s);
      return date == null ? null : new java.sql.Timestamp(date.getTime());
    }
  }
  
  /**
   * Implementierung fuer {@link java.lang.Byte}-Arrays.
   */
  protected static class ByteArrayValue implements Value
  {

    /**
     * @see de.willuhn.datasource.serialize.AbstractXmlIO.Value#serialize(java.lang.Object)
     */
    public String serialize(Object o) throws IOException {
      if (o == null)
        return "";
      if (!(o instanceof byte[]))
        throw new IOException("unable to serialize " + o);
      return Base64.encode((byte[])o);
    }

    /**
     * @see de.willuhn.datasource.serialize.AbstractXmlIO.Value#unserialize(java.lang.String)
     */
    public Object unserialize(String s) throws IOException {
      if (s == null || s.length() == 0)
        return null;
      return Base64.decode(s);
    }
    
  }
}


/**********************************************************************
 * $Source: /cvsroot/jameica/datasource/src/de/willuhn/datasource/serialize/AbstractXmlIO.java,v $
 * $Revision: 1.5 $
 * $Date: 2011/06/29 19:56:45 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/
/*********************************************************************
 * $Log: AbstractXmlIO.java,v $
 * Revision 1.5  2011/06/29 19:56:45  willuhn
 * @N Support fuer Boolean in XMLReader/XMLWriter
 *
 * Revision 1.4  2008/09/29 14:18:00  willuhn
 * @N Support for NULL-values
 *
 * Revision 1.3  2008/09/28 23:26:53  willuhn
 * @N Support fuer bytearray
 *
 * Revision 1.2  2008/09/02 17:59:10  willuhn
 * @N Support fuer BigDecimal im XML-Export
 *
 * Revision 1.1  2008/01/22 12:03:09  willuhn
 * @N Objekt-Serializer/-Deserializer fuer XML-Format
 *
 **********************************************************************/