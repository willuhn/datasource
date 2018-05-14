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