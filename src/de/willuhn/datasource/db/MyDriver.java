/**********************************************************************
 * $Source: /cvsroot/jameica/datasource/src/de/willuhn/datasource/db/MyDriver.java,v $
 * $Revision: 1.1 $
 * $Date: 2004/12/07 01:27:58 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.datasource.db;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Hilfsklasse da java.sql.DriverManager nur Driver akzeptiert,
 * die vom Systemclassloader geladen worden.
 * Siehe: http://www.kfu.com/~nsayer/Java/dyn-jdbc.html
 * Sprich: Bringt zum Beispiel ein Jameica-Plugin eigene JDBC-Treiber
 * mit, wuerde java.sql.DriverManager die nicht haben wollen, weil
 * sie nicht vom System-Classloader kommen. Daher zimmern wir
 * uns einen Wrapper um den eigentlichen Driver. Hauptsache MyDriver
 * ist vom SystemClassloader geladen. Wo der tatsaechliche Treiber
 * herkommt, interessiert den DriverManager nicht ;).
 */
public class MyDriver implements Driver
{

	private Driver driver = null;

  /**
   * ct.
   * @param driverClass
   * @throws Exception
   */
  public MyDriver(String driverClass, ClassLoader loader) throws Exception
  {
    driver = (Driver) loader.loadClass(driverClass).newInstance();
  }

  /**
   * @see java.sql.Driver#getMajorVersion()
   */
  public int getMajorVersion()
  {
    return driver.getMajorVersion();
  }

  /**
   * @see java.sql.Driver#getMinorVersion()
   */
  public int getMinorVersion()
  {
    return driver.getMinorVersion();
  }

  /**
   * @see java.sql.Driver#jdbcCompliant()
   */
  public boolean jdbcCompliant()
  {
    return driver.jdbcCompliant();
  }

  /**
   * @see java.sql.Driver#acceptsURL(java.lang.String)
   */
  public boolean acceptsURL(String url) throws SQLException
  {
    return driver.acceptsURL(url);
  }

  /**
   * @see java.sql.Driver#connect(java.lang.String, java.util.Properties)
   */
  public Connection connect(String url, Properties info) throws SQLException
  {
    return driver.connect(url,info);
  }

  /**
   * @see java.sql.Driver#getPropertyInfo(java.lang.String, java.util.Properties)
   */
  public DriverPropertyInfo[] getPropertyInfo(String url, Properties info)
    throws SQLException
  {
    return driver.getPropertyInfo(url,info);
  }

}


/**********************************************************************
 * $Log: MyDriver.java,v $
 * Revision 1.1  2004/12/07 01:27:58  willuhn
 * @N Dummy Driver
 *
 **********************************************************************/