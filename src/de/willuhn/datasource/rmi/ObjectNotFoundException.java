/**********************************************************************
 * $Source: /cvsroot/jameica/datasource/src/de/willuhn/datasource/rmi/ObjectNotFoundException.java,v $
 * $Revision: 1.1 $
 * $Date: 2004/08/26 23:19:33 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.datasource.rmi;

import java.rmi.RemoteException;

/**
 * Exception, die geworfen wird, wenn ein Objekt nicht in der Datenbank gefunden wurde.
 */
public class ObjectNotFoundException extends RemoteException
{

  /**
   * ct.
   */
  public ObjectNotFoundException()
  {
    super();
  }

  /**
   * ct.
   * @param s message.
   */
  public ObjectNotFoundException(String s)
  {
    super(s);
  }

  /**
   * ct.
   * @param s message
   * @param ex cause.
   */
  public ObjectNotFoundException(String s, Throwable ex)
  {
    super(s, ex);
  }

}


/**********************************************************************
 * $Log: ObjectNotFoundException.java,v $
 * Revision 1.1  2004/08/26 23:19:33  willuhn
 * @N added ObjectNotFoundException
 *
 **********************************************************************/