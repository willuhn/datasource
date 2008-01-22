/**********************************************************************
 * $Source: /cvsroot/jameica/datasource/src/de/willuhn/datasource/serialize/IO.java,v $
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

/**
 * Basis-Interface aller Reader und Writer.
 */
public interface IO
{
  /**
   * Schliesst den Serializer.
   * @throws IOException
   */
  public void close() throws IOException;
}


/*********************************************************************
 * $Log: IO.java,v $
 * Revision 1.1  2008/01/22 12:03:09  willuhn
 * @N Objekt-Serializer/-Deserializer fuer XML-Format
 *
 **********************************************************************/