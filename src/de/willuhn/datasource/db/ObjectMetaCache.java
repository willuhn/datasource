/**********************************************************************
 * $Source: /cvsroot/jameica/datasource/src/de/willuhn/datasource/db/ObjectMetaCache.java,v $
 * $Revision: 1.6 $
 * $Date: 2007/10/05 15:16:27 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.datasource.db;

import java.util.HashMap;

import de.willuhn.logging.Logger;
import de.willuhn.util.Session;

/**
 * Diese Klasse ist (wie der Name schon sagt ;) ein Cache.
 * Und zwar fuer die Meta-Daten der Business-Objekte. Und zwar:
 * AbstractDBObject ist ja die Basisklasse aller Business-Objekte.
 * Und diese ermittelt die Eigenschaften der Objekte "on the fly"
 * aus den Meta-Daten der SQL-Tabelle. Dies ist ein zeitraubender
 * Prozess, der nicht fuer jede Instanziierung eines Objektes neu
 * gemacht werden sollte. Schliesslich kennen wir den Aufbau der
 * SQL-Tabelle ja schon, wenn wir ein Objekt dieses Typs bereits
 * geladen haben. Nunja, dieser Cache macht nichts anderes, als
 * in einer Liste die Metadaten der verwendeten Objekte zu sammeln,
 * damit sie bei der naechsten Erzeugung eines Objektes "recycled"
 * werden koennen.
 * @author willuhn
 */
public class ObjectMetaCache
{

  private static Session metaCache = new Session(1000l * 60 * 60 * 2); // 2 Stunden Timeout
  
  private static long found = 0;
  private static long all = 0;

  /**
   * Liefert die Meta-Daten einer Klasse oder null.
   * @param service Klasse des Service.
   * @param objectType Klasse des Objekt-Typs.
   * @return Die Metadaten.
   */
  public static HashMap getMetaData(Class service, Class objectType)
  {
    if (all == 10000l)
    {
      // Nach 100.000 Aufrufen geben wir die Stats aus.
      Logger.info("[object meta cache stats] requests: " + all + ", matches: " + found + " [" + getStats() + "%]");
      found = 0;
      all = 0;
    }
    ++all;
    HashMap m = (HashMap) metaCache.get(service.getName() + "." + objectType.getName());
    if (m != null) ++found;
    return m;
  }

  /**
   * Fuegt dem Cache die Meta-Daten einer DBObject-Klasse hinzu.
   * @param service Klasse des Service.
   * @param objectType Klasse des Objekt-Typs.
   * @param fields Hashmap mit den Metadaten (key=Feldnamen,value=Datentyp).
   */
  public static void setMetaData(Class service, Class objectType, HashMap fields)
  {
    metaCache.put(service.getName() + "." + objectType.getName(),fields);
  }
  
  /**
   * Liefert den prozentualen Anteil zwischen Cache-Abfragen insgesamt und erfolgreich
   * beantworteten Abfragen.
   * @return Anteil der erfolgreich beantworteten Anfragen in Prozent.
   */
  public static int getStats()
  {
    if (found == 0 || all == 0) return 0;
    int stats = (int) ((100 * found) / all);
    return stats;
  }
}

/*********************************************************************
 * $Log: ObjectMetaCache.java,v $
 * Revision 1.6  2007/10/05 15:16:27  willuhn
 * @N Objekt-Metadaten pro Service speichern
 *
 * Revision 1.5  2007/01/12 14:31:39  willuhn
 * @N made metadata methods public
 *
 * Revision 1.4  2005/04/28 21:28:48  web0
 * *** empty log message ***
 *
 * Revision 1.3  2005/04/28 15:44:09  web0
 * *** empty log message ***
 *
 * Revision 1.2  2005/03/09 01:07:51  web0
 * @D javadoc fixes
 *
 * Revision 1.1  2004/01/10 14:52:19  willuhn
 * @C package removings
 *
 * Revision 1.1  2004/01/08 20:46:43  willuhn
 * @N database stuff separated from jameica
 *
 * Revision 1.5  2003/12/18 21:47:12  willuhn
 * @N AbstractDBObjectNode
 *
 * Revision 1.4  2003/12/15 19:08:01  willuhn
 * *** empty log message ***
 *
 * Revision 1.3  2003/12/13 20:05:21  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2003/12/12 21:11:28  willuhn
 * @N ObjectMetaCache
 *
 * Revision 1.1  2003/12/12 01:28:05  willuhn
 * *** empty log message ***
 *
 **********************************************************************/