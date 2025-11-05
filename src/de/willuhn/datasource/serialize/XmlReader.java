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

  @Override
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
    
    Node nid = attributes.getNamedItem("id");
    String id = nid != null ? nid.getNodeValue() : null;
    
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
  
  @Override
  public void close() throws IOException
  {
    this.is.close();
  }
  

}


/*********************************************************************
 * $Log: XmlReader.java,v $
 * Revision 1.2  2008/09/29 14:18:00  willuhn
 * @N Support for NULL-values
 *
 * Revision 1.1  2008/01/22 12:03:09  willuhn
 * @N Objekt-Serializer/-Deserializer fuer XML-Format
 *
 **********************************************************************/