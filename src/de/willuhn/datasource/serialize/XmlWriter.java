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

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;

import de.willuhn.datasource.GenericObject;
import de.willuhn.logging.Logger;

/**
 * Implementierung des Serializers mittels XML.
 */
public class XmlWriter extends AbstractXmlIO implements Writer
{
  private OutputStream os       = null;
  private boolean headerWritten = false;

  private final static Map XML_MAP     = new HashMap();
  
  static
  {
    XML_MAP.put(new Character('\u0026'),"&amp;");
    XML_MAP.put(new Character('\''),"&apos;");
    XML_MAP.put(new Character('\u0022'),"&quot;");
    XML_MAP.put(new Character('\u003C'),"&lt;");
    XML_MAP.put(new Character('\u003E'),"&gt;");
  }
  
  /**
   * ct
   * @param os
   */
  public XmlWriter(OutputStream os)
  {
    this.os = new FilterOutputStream(os)
    {
      /**
       * Ueberschrieben, um das Schluss-Tag zu schreiben.
       * @see java.io.FilterOutputStream#close()
       */
      public void close() throws IOException
      {
        try
        {
          Logger.debug("closing xml file");
          write("</objects>".getBytes(ENCODING));
        }
        finally
        {
          super.close();
        }
      }
      
    };
  }
  
  /**
   * @see de.willuhn.datasource.serialize.IO#close()
   */
  public void close() throws IOException
  {
    this.os.close();
  }
  
  /**
   * Liefert die Namen der zu serialisierenden Attributes des Objektes.
   * Kann bei Bedarf ueberschrieben werden.
   * Die Default-Implementierung ruft die Methode "getAttributeNames()"
   * von GenericObject auf.
   * @param object das zu serialisierende Objekt.
   * @return die zu serialisierenden Attributes.
   * @throws RemoteException
   */
  public String[] getAttributeNames(GenericObject object) throws RemoteException
  {
    return object.getAttributeNames();
  }

  /**
   * @see de.willuhn.datasource.serialize.Writer#write(de.willuhn.datasource.GenericObject)
   */
  public synchronized void write(GenericObject object) throws IOException
  {
    if (!headerWritten)
    {
      Logger.debug("writing xml header");
      os.write(("<?xml version=\"1.0\" encoding=\"" + ENCODING + "\"?>\n<objects>\n").getBytes(ENCODING));
      headerWritten = true;
    }
    Logger.debug("serializing object: " + object.getClass().getName() + ":" + object.getID());
    StringBuffer sb = new StringBuffer();
    sb.append("  <object type=\"");
    sb.append(object.getClass().getName());
    sb.append("\" id=\"");
    sb.append(object.getID());
    sb.append("\">\n");
    String[] names = getAttributeNames(object);
    for (int i=0;i<names.length;++i)
    {
      Object o = object.getAttribute(names[i]);
      if (o == null)
        continue;

      String type = o.getClass().getName();
      if (o instanceof GenericObject)
      {
        type = Integer.class.getName();
        o = ((GenericObject)o).getID();
      }

      sb.append("    <");
      sb.append(names[i]);
      sb.append(" type=\"");
      sb.append(type);
      sb.append("\">");
      
      Value v = (Value) valueMap.get(type);
      if (v == null) v = (Value) valueMap.get(null);
      
      sb.append(encode(v.serialize(o)));
      sb.append("</");
      sb.append(names[i]);
      sb.append(">\n");
    }
    sb.append("  </object>\n");
    os.write(sb.toString().getBytes(ENCODING));
  }
  
  /**
   * Fuehrt XML-Escapings durch.
   * @param s zu escapender String.
   * @return der escapte String.
   */
  private String encode(String s)
  {
    char[] chars = s.toCharArray();
    StringBuffer sb = new StringBuffer();
    for (int i=0;i<chars.length;++i)
    {
      String replacement = (String) XML_MAP.get(new Character(chars[i]));
      if (replacement == null)
      {
        sb.append(chars[i]);
        continue;
      }
      sb.append(replacement);
    }
    return sb.toString();
  }

}


/*********************************************************************
 * $Log: XmlWriter.java,v $
 * Revision 1.2  2010/10/24 22:05:34  willuhn
 * @N Alternative Loesung. Im Exporter kann die Liste der zu serialisierenden Attribute ueberschrieben werden.
 *
 * Revision 1.1  2008/01/22 12:03:09  willuhn
 * @N Objekt-Serializer/-Deserializer fuer XML-Format
 *
 **********************************************************************/