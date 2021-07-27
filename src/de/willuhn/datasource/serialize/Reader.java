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

import de.willuhn.datasource.GenericObject;

/**
 * Interface zum Lesen von Objekten des Typs {@link GenericObject}.
 */
public interface Reader extends IO
{
  /**
   * Liest das naechste Objekt aus dem Reader.
   *
   * @return das naechste verfuegbare Objekt.
   * Wenn die Funktion {@code null} liefert,
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