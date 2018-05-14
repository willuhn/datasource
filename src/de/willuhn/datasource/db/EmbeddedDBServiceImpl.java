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
package de.willuhn.datasource.db;

import java.rmi.RemoteException;

/**
 * DB-Service, der gezielt auf die Embedded-DB vorbereitet ist.
 */
public class EmbeddedDBServiceImpl extends DBServiceImpl
{

  /**
   * ct.
   * @param pathToDbConf Path to db.conf.
   * @param username username.
   * @param password password.
   * @throws RemoteException
   */
  public EmbeddedDBServiceImpl(String pathToDbConf, String username, String password)
    throws RemoteException
  {
		super("com.mckoi.JDBCDriver",":jdbc:mckoi:local://" + pathToDbConf,username,password);
  }
}


/**********************************************************************
 * $Log: EmbeddedDBServiceImpl.java,v $
 * Revision 1.1  2004/11/03 18:42:42  willuhn
 * *** empty log message ***
 *
 **********************************************************************/