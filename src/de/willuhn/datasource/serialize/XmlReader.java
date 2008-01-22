/**********************************************************************
 * $Source: /cvsroot/jameica/datasource/src/de/willuhn/datasource/serialize/XmlReader.java,v $
 * $Revision: 1.1 $
 * $Date: 2008/01/22 12:03:09 $
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
import java.io.InputStream;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import de.willuhn.datasource.GenericObject;
import de.willuhn.logging.Logger;

/**
 * Implementierung des Readers im XML-Format.
 */
public class XmlReader extends AbstractXmlIO implements Reader
{
  private InputStream is        = null;
  private ObjectFactory factory = null;
  private Document doc          = null;
  private int pos               = 0;

  /**
   * ct
   * @param is InputStream, von dem gelesen werden soll.
   * @param factory ueber das die Objekt-Instanzen erzeugt werden sollen.
   * @throws Exception
   */
  public XmlReader(InputStream is, ObjectFactory factory) throws Exception
  {
    this.is      = is;
    this.factory = factory;

    this.doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(is);
  }

  /**
   * @see de.willuhn.datasource.serialize.Reader#read()
   */
  public GenericObject read() throws IOException
  {
    NodeList objects  = this.doc.getElementsByTagName("object");
    if (objects == null || objects.getLength() == 0)
      return null; // Keine Objekte in der Datei
    
    Node current = objects.item(pos++);
    
    if (current == null)
      return null; // Am Ende angekommen
    
    NamedNodeMap attributes = current.getAttributes();
    String type = attributes.getNamedItem("type").getNodeValue();
    String id   = attributes.getNamedItem("id").getNodeValue();
    
    HashMap values = new HashMap();
    NodeList list = current.getChildNodes();
    for (int i=0;i<list.getLength();++i)
    {
      Node n = list.item(i);

      if (n.getNodeType() != Node.ELEMENT_NODE)
        continue; // Ist kein Element. Ignorieren wir

      String name  = n.getNodeName();
      String vType = n.getAttributes().getNamedItem("type").getNodeValue();
      String value = null;
      try
      {
        value = n.getLastChild().getNodeValue();
      }
      catch (NullPointerException e)
      {
        // ignore
      }
      if (value == null)
        continue;
      
      Value v = (Value) valueMap.get(vType);
      if (v == null) v = (Value) valueMap.get(null);
      
      values.put(name,v.unserialize(value));
    }
    try
    {
      return factory.create(type,id,values);
    }
    catch (IOException e)
    {
      throw e;
    }
    catch (Exception e2)
    {
      Logger.error("unable to create object " + type + ":" + id,e2);
      throw new IOException("unable to create object " + type + ":" + id);
    }
  }
  
  /**
   * @see de.willuhn.datasource.serialize.IO#close()
   */
  public void close() throws IOException
  {
    this.is.close();
  }
  

}


/*********************************************************************
 * $Log: XmlReader.java,v $
 * Revision 1.1  2008/01/22 12:03:09  willuhn
 * @N Objekt-Serializer/-Deserializer fuer XML-Format
 *
 **********************************************************************/