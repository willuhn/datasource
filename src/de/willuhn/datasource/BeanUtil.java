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

package de.willuhn.datasource;

import java.beans.Expression;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.willuhn.logging.Logger;


/**
 * Hilfsklasse, um auf gemeinsame Weise sowohl {@link GenericObject}
 * als auch regulaere Beans generisch nutzen zu koennen.
 */
public class BeanUtil
{
  /**
   * Liefert die Property-Namen einer Bean.
   * @param bean die Bean.
   * @return die Property-Namen gemaess Bean-Spec. Das sind die Namen der Properties basierend auf
   * den gefundenen public Getter-Methoden.
   * @throws RemoteException
   */
  public static List<String> getProperties(Object bean) throws RemoteException
  {
    List<String> result = new ArrayList<String>();
    if (bean == null)
      return result;
    
    if (bean instanceof GenericObject)
    {
      GenericObject o = (GenericObject) bean;
      result.addAll(Arrays.asList(o.getAttributeNames()));
      return result;
    }
    
    Method[] methods = bean.getClass().getMethods();
    if (methods == null || methods.length == 0)
      return result;
    
    for (Method m:methods)
    {
      String name = m.getName();
      if (name.startsWith("get") && name.length() > 3)
        result.add(toProperty(name));
    }
    return result;
  }
  
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

    for (int i=0;i<10;++i) // Rekursion in die Kind-Beans - maximal aber 10 Stufen. Wenn es mehr sind, ist irgendwas faul ;)
    {
      int dot = attribute.indexOf(".");
      if (dot == -1)
        break;
      
      String s = attribute.substring(0,dot);
      bean = get(bean,s);
      if (bean == null)
        return null; // Hier gehts nicht mehr weiter
      attribute = attribute.substring(dot+1);
    }
    
    if (bean instanceof GenericObject)
      return ((GenericObject)bean).getAttribute(attribute);
    
    try
    {
      return invoke(bean,toGetMethod(attribute),null);
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
        Logger.trace(nme.getMessage());
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
      invoke(bean,toSetMethod(attribute),params);
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
   *
   * <p>Handelt es sich um ein {@link GenericObject}, wird der Wert des Primaer-Attributes zurueckgeliefert.
   *
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
   *
   * <p>Handelt es sich um Objekte des Typs {@link GenericObject}, werden deren equals-Methoden verwendet.
   *
   * @param a Objekt a.
   * @param b Objekt b.
   * @return {@code true}, wenn beide Objekte gleich sind.
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
   * Macht aus einem Attribut-Namen einen Getter.
   * @param attribute Name des Attributes. 
   * @return der erzeugte Methodenname.
   */
  public static String toGetMethod(String attribute)
  {
    return "get" + attribute.substring(0,1).toUpperCase() + attribute.substring(1);
  }

  /**
   * Macht aus einem Attribut-Namen einen Setter.
   * @param attribute Name des Attributes. 
   * @return der erzeugte Methodenname.
   */
  public static String toSetMethod(String attribute)
  {
    return "set" + attribute.substring(0,1).toUpperCase() + attribute.substring(1);
  }
  
  /**
   * Macht aus einem Getter/Setter den Attribut-Namen.
   * @param method der Methoden-Name.
   * @return der Attribut-Name.
   */
  public static String toProperty(String method)
  {
    if (method.length() > 3 && (method.startsWith("get") || method.startsWith("set")))
      return method.substring(3,4).toLowerCase() + method.substring(4);
    return method;
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
    
    // 1. check super class
    Class ct = getActualType(c.getGenericSuperclass());
    if (ct != null)
      return ct;
    
    // 2. check interfaces
    Type[] interfaces = c.getGenericInterfaces();
    if (interfaces == null || interfaces.length == 0)
      return null; // keine Interfaces
    for (Type t:interfaces)
    {
      ct = getActualType(t);
      if (ct != null)
        return ct;
    }
    
    return null; // kein Typ gefunden
  }
  
  /**
   * Liefert die konkrete Typisierung des Typs.
   * @param type der zu pruefende Typ.
   * @return der konkrete Typ oder NULL.
   */
  private static Class getActualType(Type type)
  {
    if (!(type instanceof ParameterizedType))
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
