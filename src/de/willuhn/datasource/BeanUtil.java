/**********************************************************************
 * $Source: /cvsroot/jameica/datasource/src/de/willuhn/datasource/BeanUtil.java,v $
 * $Revision: 1.9 $
 * $Date: 2009/08/19 12:44:54 $
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
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
      Logger.warn(nme.getMessage());
      return null;
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
   * Liefert alle Felder der Bean, die mit Annotations versehen sind - unabhaengig vom Modifier.
   * Eigentlich koennte man hierzu auch {@link Class#getDeclaredAnnotations()}
   * verwenden. Diese Funktion sucht aber nur in der konkreten Klasse und
   * nicht in Super-Klassen.
   * Genau diese Luecke schliesst diese Funktion.
   * Sie liefert also alle Member mit Annotations, die einen public, protected, private oder
   * defaulft-Modifier haben. Aus dieser Klasse und allen Super-Klassen.
   * @param bean Bean, von der die Annotations gesucht werden sollen.
   * @param a optionale Angabe von gesuchten Annotations.
   * Wenn dieser Parameter angegeben ist, werden nur jene Properties zurueckgeliefert,
   * bei denen mind. eine der genannten Annotations deklariert ist.
   * @return Liste der gefundenen Annotations.
   * @throws Exception
   */
  public static List<Field> getAnnotatedFields(Object bean, Class... a) throws Exception
  {
    // Wir packen die gesuchten Annotations in eine List. In der kann man besser suchen
    List onlyThis = null;
    if (a != null && a.length > 0)
      onlyThis = Arrays.asList(a);

    List<Field> found = new ArrayList<Field>();
    
    // Ich mag keine while(true)-Schleifen. Wenn die Abbruchbedingung
    // nicht erfuellt wird, haben wir eine Endlos-Schleife. Und da davon
    // auszugehen ist, dass eine Klasse unmoeglich mehr als 100 Superklassen
    // haben kann, limitieren wir das da.
    // Hier eigentlich nur fuer den Fall, dass es irgend eine Java-Implementierung gibt,
    // die bei Class#getSuperclass() nicht NULL liefert, wenn Class bereits ein
    // "java.lang.Object" ist (wir also schon oben angekommen sind). Ich hab
    // nirgends einen Hinweis gefunden, ob dieser spezifiziert ist. 
    Class current = bean.getClass();
    for (int i=0;i<100;++i)
    {
      Field[] fields = current.getDeclaredFields();
      if (fields != null && fields.length > 0)
      {
        for (Field f:fields)
        {
          Annotation[] al = f.getAnnotations();
          
          // Aufrufer moechte das Member unabhaengig von der Art der Annotation haben
          if (onlyThis == null)
          {
            found.add(f);
            continue;
          }
          
          // Aufrufer moechte nur Members mit bestimmten Annotations haben
          for (Annotation at:al)
          {
            if (onlyThis.contains(at))
              found.add(f); // Jepp, Annotation war gesucht
          }
        }
      }
      
      Class superClass = current.getSuperclass();
      if (superClass == null)
        break; // Oben angekommen
      
      // Ansonsten mit der Super-Klasse weitermachen
      current = superClass;
    }
    
    return found;
  }

}


/**********************************************************************
 * $Log: BeanUtil.java,v $
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
