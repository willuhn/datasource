/**********************************************************************
 * $Source: /cvsroot/jameica/datasource/src/de/willuhn/datasource/serialize/Writer.java,v $
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
 * Interface zum Schreiben von Objekten des Typs GenericObject.
 */
public interface Writer extends IO
{
  /**
   * Serialisiert das Objekt.
   * @param object das zu serialisierende Objekt.
   * @throws IOException
   */
  public void write(GenericObject object) throws IOException;
}


/*********************************************************************
 * $Log: Writer.java,v $
 * Revision 1.1  2008/01/22 12:03:09  willuhn
 * @N Objekt-Serializer/-Deserializer fuer XML-Format
 *
 **********************************************************************/