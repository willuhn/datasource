/**********************************************************************
 * $Source: /cvsroot/jameica/datasource/src/de/willuhn/datasource/serialize/ObjectFactory.java,v $
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