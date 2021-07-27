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

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Hilfsklasse da {@link java.sql.DriverManager} nur Driver akzeptiert,
 * die vom System-Classloader geladen worden.
 *
 * Sprich: Bringt zum Beispiel ein Jameica-Plugin eigene JDBC-Treiber
 * mit, wuerde {@link java.sql.DriverManager} die nicht haben wollen, weil
 * sie nicht vom System-Classloader kommen. Daher zimmern wir
 * uns einen Wrapper um den eigentlichen Driver. Hauptsache MyDriver
 * ist vom SystemClassloader geladen. Wo der tatsaechliche Treiber
 * herkommt, interessiert den DriverManager nicht ;).
 *
 * @see <a href="http://www.kfu.com/~nsayer/Java/dyn-jdbc.html" target="_top">http://www.kfu.com/~nsayer/Java/dyn-jdbc.html</a>
 */
public class MyDriver implements Driver
{

	private Driver driver = null;

  /**
   * ct.
   * @param driverClass
   * @param loader
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

  /**
   * @since Java 7
   * @see java.sql.Driver#getParentLogger()
   */
  @SuppressWarnings("javadoc")
  public Logger getParentLogger() throws SQLFeatureNotSupportedException
  {
    // FeatureNotSupportedException werfen, damits auch in Java 6 noch compiliert.
    throw new SQLFeatureNotSupportedException("getParentLogger not supported");
  }

}


/**********************************************************************
 * $Log: MyDriver.java,v $
 * Revision 1.2  2005/03/09 01:07:51  web0
 * @D javadoc fixes
 *
 * Revision 1.1  2004/12/07 01:27:58  willuhn
 * @N Dummy Driver
 *
 **********************************************************************/