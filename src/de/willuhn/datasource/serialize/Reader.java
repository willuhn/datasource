/**********************************************************************
 * $Source: /cvsroot/jameica/datasource/src/de/willuhn/datasource/serialize/Reader.java,v $
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

import de.willuhn.datasource.GenericObject;

/**
 * Interface zum Lesen von Objekten des Typs GenericObject.
 */
public interface Reader extends IO
{
  /**
   * Liest das naechste Objekt aus dem Reader.
   * @return das naechste verfuegbare Objekt.
   * Wenn die Funktion <code>null</code> liefert,
   * ist der Reader "am Ende angekommen".
   * @throws IOException
   */
  public GenericObject read() throws IOException;
}


/*********************************************************************
 * $Log: Reader.java,v $
 * Revision 1.1  2008/01/22 12:03:09  willuhn
 * @N Objekt-Serializer/-Deserializer fuer XML-Format
 *
 **********************************************************************/