/**********************************************************************
 * $Source: /cvsroot/jameica/datasource/src/de/willuhn/datasource/pseudo/PseudoIterator.java,v $
 * $Revision: 1.2 $
 * $Date: 2004/07/21 23:53:56 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.datasource.pseudo;

import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.List;

import de.willuhn.datasource.GenericIterator;
import de.willuhn.datasource.GenericObject;

/**
 * Ein Pseudo-Iterator, der zwar das GenericIterator-Interface
 * implementiert, jedoch kein Datenbank-Backend benutzt sondern
 * Listen/Maps aus java.util. 
 */
public class PseudoIterator implements GenericIterator
{

	private List list = null;
	private int index = 0;

	/**
   * Der Konstruktor ist private, damit Instanzen nur
   * ueber die statischen Methoden fromFoo erzeugt werden.
   */
  private PseudoIterator()
	{
	}

	/**
	 * Erzeugt einen GenericIterator aus einem Array von GenericObjects.
   * @param objects das Array, aus dem der Iterator aufgebaut werden soll.
   * @return
   */
  public static GenericIterator fromArray(GenericObject[] objects)
	{
		PseudoIterator i = new PseudoIterator();
		i.list = Arrays.asList(objects);
		return i;
	}

  /**
   * @see de.willuhn.datasource.rmi.GenericIterator#hasNext()
   */
  public boolean hasNext() throws RemoteException
  {
    return (list.size() > index && list.size() > 0);
  }

  /**
   * @see de.willuhn.datasource.rmi.GenericIterator#next()
   */
  public GenericObject next() throws RemoteException
  {
    return (GenericObject) list.get(index++);
  }

  /**
   * @see de.willuhn.datasource.rmi.GenericIterator#previous()
   */
  public GenericObject previous() throws RemoteException
  {
    return (GenericObject) list.get(index--);
  }

  /**
   * @see de.willuhn.datasource.rmi.GenericIterator#begin()
   */
  public void begin() throws RemoteException
  {
		index = 0;
  }

  /**
   * @see de.willuhn.datasource.rmi.GenericIterator#size()
   */
  public int size() throws RemoteException
  {
    return list.size();
  }

  /**
   * @see de.willuhn.datasource.rmi.GenericIterator#contains(de.willuhn.datasource.rmi.GenericObject)
   */
  public GenericObject contains(GenericObject o) throws RemoteException
  {
		if (o == null)
			return null;

		GenericObject object = null;
		for (int i=0;i<list.size();++i)
		{
			object = (GenericObject) list.get(i);
			if (object.equals(o))
				return object;
		}
    
		return null;
  }

}


/**********************************************************************
 * $Log: PseudoIterator.java,v $
 * Revision 1.2  2004/07/21 23:53:56  willuhn
 * @C massive Refactoring ;)
 *
 * Revision 1.1  2004/06/17 22:06:29  willuhn
 * @N PseudoIterator
 *
 **********************************************************************/