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
   * @param loader
   * @throws Exception
   */
  public MyDriver(String driverClass, ClassLoader loader) throws Exception
  {
    driver = (Driver) loader.loadClass(driverClass).newInstance();
  }

  @Override
  public int getMajorVersion()
  {
    return driver.getMajorVersion();
  }

  @Override
  public int getMinorVersion()
  {
    return driver.getMinorVersion();
  }

  @Override
  public boolean jdbcCompliant()
  {
    return driver.jdbcCompliant();
  }

  @Override
  public boolean acceptsURL(String url) throws SQLException
  {
    return driver.acceptsURL(url);
  }

  @Override
  public Connection connect(String url, Properties info) throws SQLException
  {
    return driver.connect(url,info);
  }

  @Override
  public DriverPropertyInfo[] getPropertyInfo(String url, Properties info)
    throws SQLException
  {
    return driver.getPropertyInfo(url,info);
  }

  @Override
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