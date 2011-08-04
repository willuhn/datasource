/**********************************************************************
 * $Source: /cvsroot/jameica/datasource/src/de/willuhn/datasource/db/types/TypeRegistry.java,v $
 * $Revision: 1.3.2.1 $
 * $Date: 2011/08/04 08:54:44 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.datasource.db.types;

import java.util.HashMap;
import java.util.Map;

import de.willuhn.logging.Logger;

/**
 * Registry, in der die unterstuetzten Feld-Typen gehalten werden.
 */
public class TypeRegistry
{
  private static Map types = new HashMap();
  
  /**
   * Generischer Typ, der Verwendung findet, wenn kein passender Typ gefunden wurde.
   */
  public final static Type TYPE_DEFAULT = new TypeGeneric();
  
  static
  {
    types.put(null,        TYPE_DEFAULT);

    types.put("varchar",     new TypeString());
    types.put("longvarchar", new TypeLongString());
    types.put("text",        new TypeLongString());
    types.put("longtext",    new TypeLongString());

    types.put("date",        new TypeDate());
    types.put("datetime",    new TypeTimestamp());
    types.put("timestamp",   new TypeTimestamp());
    
    types.put("int",         new TypeInteger());
    types.put("double",      new TypeDouble());
    types.put("decimal",     new TypeDouble());
    
    types.put("blob",        new TypeByteArray());
    types.put("longblob",    new TypeByteArray());
    
    // Daemliches PostgreSQL - muessen die denn ALLES anders machen? :(
    types.put("bytea",       new TypeByteArray());
    types.put("timestamptz", new TypeTimestamp());
  }
  
  /**
   * Liefert die Typ-Implementierung fuer den angegebenen Typ.
   * Die Funktion beruecksichtigt KEINE Gross-Kleinschreibung.
   * @param name Name des Feld-Typs.
   * @return Implementierung des Typs. Die Funktion liefert nie
   * <code>null</code> sondern hoechstens TYPE_DEFAULT.
   */
  public static Type getType(String name)
  {
    if (name == null)
      return TYPE_DEFAULT;
    
    Type t = (Type) types.get(name.toLowerCase());
    return t == null ? TYPE_DEFAULT : t;
  }
  
  /**
   * Registriert einen benutzerdefinierten SQL-Typ.
   * @param name Name des Feld-Typs.
   * @param type Implementierung.
   */
  public static void register(String name,Type type)
  {
    if (name == null || type == null)
    {
      Logger.error("name or type cannot be null");
      return;
    }
    types.put(name.toLowerCase(),type);
  }
}


/*********************************************************************
 * $Log: TypeRegistry.java,v $
 * Revision 1.3.2.1  2011/08/04 08:54:44  willuhn
 * @N Backports aus HEAD
 *
 * Revision 1.3  2008-07-15 11:02:31  willuhn
 * @N Neuer Typ "TypeLongString", der aus den Feldern "TEXT", "LONGTEXT" und "LONGVARCHAR" bei Bedarf aus einem Reader liest (ist abhaengig vom JDBC-Treiber)
 *
 * Revision 1.2  2008/07/14 08:55:28  willuhn
 * @N "longvarchar" added
 *
 * Revision 1.1  2008/07/11 09:30:17  willuhn
 * @N Support fuer Byte-Arrays
 * @N SQL-Typen sind jetzt erweiterbar
 *
 **********************************************************************/