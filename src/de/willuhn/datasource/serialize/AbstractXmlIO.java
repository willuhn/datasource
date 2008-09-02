/**********************************************************************
 * $Source: /cvsroot/jameica/datasource/src/de/willuhn/datasource/serialize/AbstractXmlIO.java,v $
 * $Revision: 1.2 $
 * $Date: 2008/09/02 17:59:10 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
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
    valueMap.put("java.util.Date",       new DateValue());
    valueMap.put("java.sql.Date",        new SqlDateValue());
    valueMap.put("java.sql.Timestamp",   new TimestampValue());
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
      return o.toString();
    }
  }
  
  /**
   * Implementierung fuer Strings.
   */
  protected static class StringValue extends AbstractValue
  {
    /**
     * @see de.willuhn.datasource.serialize.AbstractXmlIO.Value#unserialize(java.lang.String)
     */
    public Object unserialize(String s) throws IOException
    {
      return new String(s);
    }
  }

  /**
   * Implementierung fuer Double.
   */
  protected static class DoubleValue extends AbstractValue
  {
    /**
     * @see de.willuhn.datasource.serialize.AbstractXmlIO.Value#unserialize(java.lang.String)
     */
    public Object unserialize(String s) throws IOException
    {
      return new Double(s);
    }
  }
  
  /**
   * Implementierung fuer BigDecimal.
   */
  protected static class BigDecimalValue extends AbstractValue
  {
    /**
     * @see de.willuhn.datasource.serialize.AbstractXmlIO.Value#unserialize(java.lang.String)
     */
    public Object unserialize(String s) throws IOException
    {
      return new BigDecimal(s);
    }
  }

  /**
   * Implementierung fuer Integer.
   */
  protected static class IntegerValue extends AbstractValue
  {
    /**
     * @see de.willuhn.datasource.serialize.AbstractXmlIO.Value#unserialize(java.lang.String)
     */
    public Object unserialize(String s) throws IOException
    {
      return new Integer(s);
    }
  }

  /**
   * Implementierung fuer Long.
   */
  protected static class LongValue extends AbstractValue
  {
    /**
     * @see de.willuhn.datasource.serialize.AbstractXmlIO.Value#unserialize(java.lang.String)
     */
    public Object unserialize(String s) throws IOException
    {
      return new Long(s);
    }
  }
  
  /**
   * Implementierung fuer java.util.Date.
   */
  protected static class DateValue implements Value
  {
    private final static DateFormat format = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");

    /**
     * @see de.willuhn.datasource.serialize.AbstractXmlIO.Value#unserialize(java.lang.String)
     */
    public Object unserialize(String s) throws IOException
    {
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
      return format.format((Date)o);
    }
  }
  
  /**
   * Implementierung fuer java.sql.Date.
   */
  protected static class SqlDateValue extends DateValue
  {
    /**
     * @see de.willuhn.datasource.serialize.AbstractXmlIO.Value#unserialize(java.lang.String)
     */
    public Object unserialize(String s) throws IOException
    {
      Date date = (Date) super.unserialize(s);
      return new java.sql.Date(date.getTime());
    }
  }

  /**
   * Implementierung fuer java.sql.Timestamp.
   */
  protected static class TimestampValue extends DateValue
  {
    /**
     * @see de.willuhn.datasource.serialize.AbstractXmlIO.Value#unserialize(java.lang.String)
     */
    public Object unserialize(String s) throws IOException
    {
      Date date = (Date) super.unserialize(s);
      return new java.sql.Timestamp(date.getTime());
    }
  }
}


/*********************************************************************
 * $Log: AbstractXmlIO.java,v $
 * Revision 1.2  2008/09/02 17:59:10  willuhn
 * @N Support fuer BigDecimal im XML-Export
 *
 * Revision 1.1  2008/01/22 12:03:09  willuhn
 * @N Objekt-Serializer/-Deserializer fuer XML-Format
 *
 **********************************************************************/