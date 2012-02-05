/**********************************************************************
 * $Source: /cvsroot/jameica/datasource/src/de/willuhn/datasource/BeanUtil.java,v $
 * $Revision: 1.15 $
 * $Date: 2012/02/05 23:20:27 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.datasource;

import java.beans.Expression;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.rmi.RemoteException;

import de.willuhn.logging.Logger;


/**
 * Hilfsklasse, um auf gemeinsame Weise sowhl GenericObjects als auch regulaere Beans generisch nutzen zu koennen.
 */
public class BeanUtil
{

  /**
   * Fuehrt auf der uebergebenen Bean die zugehoerige Getter-Methode zum genannten Attibut aus.
   * @param bean die Bean.
   * @param attribute Name des Attributes. Ein "get" wird automatisch vorangestellt.
   * @return der Rueckgabe-Wert der Methode.
   * @throws RemoteException
   */
  public static Object get(Object bean, String attribute) throws RemoteException
  {
    if (bean == null)
      return null;

    if (attribute == null)
      return toString(bean);
    
    if (bean instanceof GenericObject)
      return ((GenericObject)bean).getAttribute(attribute);
    
    try
    {
      return invoke(bean,toMethod("get",attribute),null);
    }
    catch (RemoteException re)
    {
      throw re;
    }
    catch (NoSuchMethodException nme)
    {
      // Fallback:
      try
      {
        return invoke(bean,attribute,null);
      }
      catch (RemoteException re)
      {
        throw re;
      }
      catch (NoSuchMethodException nme2)
      {
        Logger.warn(nme.getMessage());
        return null;
      }
      catch (Exception e)
      {
        throw new RemoteException("unable to get attribute " + attribute,e);
      }
    }
    catch (Exception e)
    {
      throw new RemoteException("unable to get attribute " + attribute,e);
    }
  }
  
  /**
   * Fuehrt auf der uebergebenen Bean die zugehoerige Setter-Methode zum genannten Attibut aus.
   * @param bean die Bean.
   * @param attribute Name des Attributes. Ein "set" wird automatisch vorangestellt.
   * @param param der zu uebergebende Parameter.
   * @throws RemoteException
   */
  public static void set(Object bean, String attribute, Object param) throws RemoteException
  {
    set(bean,attribute, new Object[]{param});
  }
  
  /**
   * Fuehrt auf der uebergebenen Bean die zugehoerige Setter-Methode zum genannten Attibut aus.
   * @param bean die Bean.
   * @param attribute Name des Attributes. Ein "set" wird automatisch vorangestellt.
   * @param params die zu uebergebenden Parameter.
   * @throws RemoteException
   */
  public static void set(Object bean, String attribute, Object params[]) throws RemoteException
  {
    try
    {
      invoke(bean,toMethod("set",attribute),params);
    }
    catch (RemoteException re)
    {
      throw re;
    }
    catch (Exception e)
    {
      throw new RemoteException("unable to set attribute " + attribute,e);
    }
  }

  /**
   * Liefert eine toString-Repraesentation des Objektes.
   * Handelt es sich um ein GenericObject, wird der Wert des Primaer-Attributes zurueckgeliefert.
   * @param bean die Bean.
   * @return die String-Repraesentation.
   * @throws RemoteException
   */
  public static String toString(Object bean) throws RemoteException
  {
    if (bean == null)
      return null;
    
    if (bean instanceof GenericObject)
    {
      GenericObject gb = (GenericObject) bean;
      Object value = gb.getAttribute(gb.getPrimaryAttribute());
      return value == null ? null : value.toString();
    }
    return bean.toString();
  }
  
  /**
   * Vergleicht zwei Objekte.
   * Handelt es sich um Objekte des Typs GenericObject, werden deren equals-Methoden verwendet.
   * @param a Objekt a.
   * @param b Objekt b.
   * @return True, wenn beide Objekte gleich sind.
   * @throws RemoteException
   */
  public static boolean equals(Object a, Object b) throws RemoteException
  {
    if (a == b)
      return true;
    if (a == null || b == null)
      return false;
    
    if ((a instanceof GenericObject) && (b instanceof GenericObject))
      return ((GenericObject)a).equals((GenericObject)b);
    
    return a.equals(b);
  }
  
  /**
   * Macht aus einem Attribut-Namen einen Getter oder Setter.
   * @param getSet String "get" oder "set".
   * @param attribute Name des Attributes. 
   * @return der erzeugte Methodenname.
   */
  private static String toMethod(String getSet, String attribute)
  {
    return getSet + attribute.substring(0,1).toUpperCase() + attribute.substring(1);
  }

  /**
   * Fuehrt auf der uebergebenen Bean genannte Methode aus.
   * @param bean die Bean.
   * @param method der Methodenname.
   * @param params die zu uebergebenden Parameter.
   * @return der Rueckgabe-Wert der Methode.
   * @throws Exception
   */
  public static Object invoke(Object bean, String method, Object params[]) throws Exception
  {
    Expression ex = new Expression(bean,method,params);
    return ex.getValue();
  }
  
  /**
   * Liefert die Typisierung einer Klasse.
   * @param c die Klasse, deren Typisierung ermittelt werden soll.
   * @return der konkrete Typ der Klasse oder NULL, wenn sie nicht typisiert ist.
   */
  public static Class getType(Class c)
  {
    if (c == null)
    {
      Logger.warn("no (typed) class given");
      return null;
    }
    
    // Gefunden in http://www.nautsch.net/2008/10/29/class-von-type-parameter-java-generics-gepimpt/
    // Generics-Voodoo ;)
    Type type = c.getGenericSuperclass();
    if (type == null || !(type instanceof ParameterizedType))
      return null;

    ParameterizedType pType = (ParameterizedType) type;
    Type[] types = pType.getActualTypeArguments();
    if (types == null || types.length == 0)
      return null;
    
    if (!(types[0] instanceof Class))
      return null;
    
    return (Class) types[0];
  }
}


/**********************************************************************
 * $Log: BeanUtil.java,v $
 * Revision 1.15  2012/02/05 23:20:27  willuhn
 * @N null check
 *
 * Revision 1.14  2012/02/05 22:36:58  willuhn
 * @N null check
 *
 * Revision 1.13  2012/02/05 22:35:08  willuhn
 * @N getType()
 *
 * Revision 1.12  2011-07-11 16:00:20  willuhn
 * @N diverse Fallbacks
 *
 * Revision 1.11  2011-03-30 11:51:49  willuhn
 * @C Code verschoben in neuen Injector in de.willuhn.util
 *
 * Revision 1.10  2009/08/19 12:55:13  willuhn
 * @B equals-Vergleich geaendert
 *
 * Revision 1.9  2009/08/19 12:44:54  willuhn
 * *** empty log message ***
 *
 * Revision 1.8  2009/08/19 12:43:09  willuhn
 * @B Annotation-Parameter falsch
 *
 * Revision 1.7  2009/08/19 12:23:33  willuhn
 * @N Neue Methode zum Ermitteln von Field-Annotations in Beans, die auch in Superklassen sucht
 *
 * Revision 1.6  2009/01/13 16:36:43  willuhn
 * @C Wenn benannter Getter einer Bean nicht existiert, dann keine NoSuchMethodException werfen sondern nur warning loggen
 *
 * Revision 1.5  2008/06/16 10:56:26  willuhn
 * @C urspruengliche Exception des invoke nicht fangen
 *
 * Revision 1.4  2008/06/16 10:40:07  willuhn
 * @C BeanUtil#invoke ist jetzt public
 *
 * Revision 1.3  2007/04/10 22:44:48  willuhn
 * @N Additional equals method to honor GenericObjects
 *
 * Revision 1.2  2007/04/02 23:00:42  willuhn
 * @B falscher Parameter in BeanUtil#get
 * @N PseudoIterator#asList
 *
 * Revision 1.1  2007/03/29 23:06:04  willuhn
 * @N helper class for generic bean access
 *
 **********************************************************************/
