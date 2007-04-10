/**********************************************************************
 * $Source: /cvsroot/jameica/datasource/src/de/willuhn/datasource/BeanUtil.java,v $
 * $Revision: 1.3 $
 * $Date: 2007/04/10 22:44:48 $
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
import java.rmi.RemoteException;


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
    if (bean instanceof GenericObject)
      return ((GenericObject)bean).getAttribute(attribute);
    
    return invoke(bean,toMethod("get",attribute),null);
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
    invoke(bean,toMethod("set",attribute),params);
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
   * @throws RemoteException
   */
  private static Object invoke(Object bean, String method, Object params[]) throws RemoteException
  {
    Expression ex = new Expression(bean,method,params);
    try
    {
      return ex.getValue();
    }
    catch (RemoteException re)
    {
      throw re;
    }
    catch (Exception e)
    {
      throw new RemoteException("unable to invoke method " + method,e);
    }
  }

}


/**********************************************************************
 * $Log: BeanUtil.java,v $
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
