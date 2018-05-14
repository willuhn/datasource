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

import java.util.Map;

import de.willuhn.datasource.GenericObject;

/**
 * Factory, welche die Objekt-Instanzen erzeugt.
 */
public interface ObjectFactory
{
  /**
   * Erzeugt ein neues Objekt des angegebenen Typs.
   * @param type Typ (Klassen-Name).
   * @param identifier ID.
   * @param attributes Map mit den Objekt-Attributen.
   * @return die Instanz des erzeugten Objektes.
   * @throws Exception
   */
  public GenericObject create(String type, String identifier, Map attributes) throws Exception;
}


/*********************************************************************
 * $Log: ObjectFactory.java,v $
 * Revision 1.1  2008/01/22 12:03:09  willuhn
 * @N Objekt-Serializer/-Deserializer fuer XML-Format
 *
 **********************************************************************/