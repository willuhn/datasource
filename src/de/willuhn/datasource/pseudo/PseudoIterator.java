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
package de.willuhn.datasource.pseudo;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.willuhn.datasource.GenericIterator;
import de.willuhn.datasource.GenericObject;

/**
 * Ein Pseudo-Iterator, der zwar das GenericIterator-Interface
 * implementiert, jedoch kein Datenbank-Backend benutzt sondern
 * Listen/Maps aus java.util. 
 */
public class PseudoIterator extends UnicastRemoteObject implements GenericIterator
{

  private static final long serialVersionUID = 1L;

  private List list = null;
	private int index = 0;

	/**
   * Der Konstruktor ist private, damit Instanzen nur
   * ueber die statischen Methoden fromFoo erzeugt werden.
	 * @throws RemoteException
   */
  private PseudoIterator() throws RemoteException
	{
	}

  /**
	 * Erzeugt einen GenericIterator aus einem Array von GenericObjects.
   * @param objects das Array, aus dem der Iterator aufgebaut werden soll.
   * @return der generierte Iterator.
   * @throws RemoteException
   */
  public static GenericIterator fromArray(GenericObject[] objects) throws RemoteException
	{
		PseudoIterator i = new PseudoIterator();
		i.list = Arrays.asList(objects);
		return i;
	}
  
  /**
   * Erzeugt eine Liste aus einem GenericIterator.
   * @param iterator zu konvertierender Iterator.
   * @return Liste.
   * @throws RemoteException
   */
  public static List asList(GenericIterator iterator) throws RemoteException
  {
    ArrayList list = new ArrayList();
    while (iterator.hasNext())
      list.add(iterator.next());
    iterator.begin();
    return list;
  }

  @Override
  public boolean hasNext() throws RemoteException
  {
    return (list.size() > index && list.size() > 0);
  }

  @Override
  public GenericObject next() throws RemoteException
  {
    return (GenericObject) list.get(index++);
  }

  @Override
  public GenericObject previous() throws RemoteException
  {
    return (GenericObject) list.get(index--);
  }

  @Override
  public void begin() throws RemoteException
  {
		index = 0;
  }

  @Override
  public int size() throws RemoteException
  {
    return list.size();
  }

  @Override
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
 * Revision 1.8  2007/04/25 13:30:05  willuhn
 * @B call "begin()" after asList()
 *
 * Revision 1.7  2007/04/02 23:00:42  willuhn
 * @B falscher Parameter in BeanUtil#get
 * @N PseudoIterator#asList
 *
 * Revision 1.6  2005/03/09 01:07:51  web0
 * @D javadoc fixes
 *
 * Revision 1.5  2004/11/05 19:48:24  willuhn
 * *** empty log message ***
 *
 * Revision 1.4  2004/08/30 15:02:47  willuhn
 * *** empty log message ***
 *
 * Revision 1.3  2004/08/18 23:14:00  willuhn
 * @D Javadoc
 *
 * Revision 1.2  2004/07/21 23:53:56  willuhn
 * @C massive Refactoring ;)
 *
 * Revision 1.1  2004/06/17 22:06:29  willuhn
 * @N PseudoIterator
 *
 **********************************************************************/