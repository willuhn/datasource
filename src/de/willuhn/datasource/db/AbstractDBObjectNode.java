/**********************************************************************
 * $Source: /cvsroot/jameica/datasource/src/de/willuhn/datasource/db/AbstractDBObjectNode.java,v $
 * $Revision: 1.8 $
 * $Date: 2004/08/02 11:53:59 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.datasource.db;

import java.rmi.RemoteException;
import java.util.ArrayList;

import de.willuhn.datasource.GenericIterator;
import de.willuhn.datasource.GenericObjectNode;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.util.ApplicationException;

/**
 * Diese Klasse ist die ideale Basis-Klasse, wenn es gilt, Baum-Strukturen
 * in einer Datenbank abzubilden. Man nehme eine SQL-Tabelle und erweitere
 * sie um eine Spalte fuer das Eltern-Objekt. Diese heisst z.Bsp. "parent_id".
 * Dieser Fremd-Schluessel zeigt auf die selbe Tabelle und dort auf das
 * uebergeordnete Objekt. Ein solches Objekt laesst sich dann prima mit
 * der GUI-Komponente "Tree" darstellen.
 * Hinweis: Objekte, die sich bereits auf der obersten Ebene des Baumes
 * befinden, muessen den Wert "0" im Feld fuer das Eltern-Objekt besitzen.
 * @author willuhn
 */
public abstract class AbstractDBObjectNode extends AbstractDBObject implements GenericObjectNode
{

  /**
   * @throws RemoteException
   */
  public AbstractDBObjectNode() throws RemoteException
  {
    super();
  }

  /**
   * Liefert den Namen der Spalte, in dem sich die ID des
   * übergeordneten Objektes befindet.
   * @return Spalten-Name mit der ID des uebergeordneten Objektes.
   */
  protected String getNodeField()
  {
    return "parent_id";
  }

  /**
   * @see de.willuhn.datasource.rmi.GenericObjectNode#getChilds()
   */
  public GenericIterator getChilds() throws RemoteException
  {
    DBIterator list = getList();
    list.addFilter(getNodeField() + " = " + this.getID());
    return list;
  }

  /**
   * @see de.willuhn.datasource.rmi.GenericObjectNode#getTopLevelList()
   */
  public GenericIterator getTopLevelList() throws RemoteException
  {
    DBIterator list = getList();
    list.addFilter(getNodeField() + " = 0");
    return list;
  }

  /**
   * @see de.willuhn.datasource.rmi.GenericObjectNode#hasChild(de.willuhn.datasource.rmi.GenericObjectNode)
   */
  public boolean hasChild(GenericObjectNode object) throws RemoteException
  {
    if (object == null)
      return false;

		GenericIterator childs = this.getChilds();
    int count = 1;
		GenericObjectNode child = null;
    while (childs.hasNext())
    {
      count++;
      if (count > 100) return false; // limit recursion
      child = (GenericObjectNode) childs.next();
      if (child.hasChild(object))
        return true;
    }
    return false;
  }

  /**
   * @see de.willuhn.datasource.rmi.GenericObjectNode#getParent()
   */
  public GenericObjectNode getParent() throws RemoteException
  {
    DBIterator list = getList();
    list.addFilter(getIDField() + "=" + this.getAttribute(this.getNodeField()));
    if (!list.hasNext())
      return null;
    return (GenericObjectNode) list.next();
  }



  /**
   * @see de.willuhn.datasource.rmi.GenericObjectNode#getPossibleParents()
   */
  public GenericIterator getPossibleParents() throws RemoteException
  {
    DBIterator list = this.getList();
    list.addFilter(getIDField() + " != "+this.getID()); // Objekt darf nicht sich selbst als Eltern-Objekt haben
    ArrayList array = new ArrayList();

		GenericObjectNode element = null;
    while (list.hasNext())
    {
      element = (GenericObjectNode) list.next();

      if (!this.hasChild(element)) {
        // Kinder duerfen keine Eltern sein
        array.add(element.getID());
      }
    }
    return new DBIteratorImpl(this,array,getService());
  }

  /**
   * @see de.willuhn.datasource.rmi.GenericObjectNode#getPath()
   */
  public GenericIterator getPath() throws RemoteException
  {
    ArrayList objectArray = new ArrayList();
    boolean reached = false;
		GenericObjectNode currentObject = this.getParent();

    if (currentObject == null) {
      // keine Eltern-Objekte. Also liefern wir eine leere Liste.
      return new DBIteratorImpl(this,objectArray,getService());
    }
    objectArray.add(currentObject.getID());

		GenericObjectNode object = null;
    while (!reached) {
      object = currentObject.getParent();
      if (object != null) {
        objectArray.add(object.getID());
        currentObject = object;
      }
      else {
        reached = true;
      }
    }
    return new DBIteratorImpl(this,objectArray,getService());
  }

  /**
   * Da Objekte in einem Baum Abhaengigkeiten untereinander haben,
   * muessen diese vorm Loeschen geprueft werden. Grundsaetzliche
   * Checks koennen wir bereits hier durchfuehren. Zum Beispiel
   * ist es nicht moeglich, ein Objekt zu loeschen, wenn es
   * Kind-Objekte hat.
   * @see de.willuhn.datasource.db.AbstractDBObject#deleteCheck()
   */
  protected void deleteCheck() throws ApplicationException
  {
    try {
			GenericIterator list = getChilds();
      if (list.hasNext())
        throw new ApplicationException("Objekt kann nicht gelöscht werden da Abhängigkeiten existieren.");
    }
    catch (RemoteException e)
    {
      throw new ApplicationException("Fehler beim Prüfen der Abhängigkeiten.");
    }
  }

  /**
   * Prueft, ob das angegebene Eltern-Objekt (insofern vorhanden) erlaubt ist.
   * Sprich: Es wird geprueft, ob es nicht auf sich selbst zurueckzeigt
   * und ob das Eltern-Element nicht gleichzeitig ein Kind-Element ist. 
   * @see de.willuhn.datasource.db.AbstractDBObject#insertCheck()
   */
  protected void insertCheck() throws ApplicationException
  {
    // Wir pruefen, ob das Objekt an gueltiger Stelle eingehaengt wurde.
    try {
      GenericObjectNode parent = getParent();

      if (parent == null)
        return;

      GenericIterator parents = getPossibleParents();
      while (parents.hasNext())
      {
        GenericObjectNode node = (GenericObjectNode) parents.next();
        if (node.equals(parent))
          return; // Angegebenes Eltern-Objekt ist eines der erlaubten Eltern
      }
      throw new ApplicationException("Angegebenes Eltern-Objekt nicht erlaubt");
    }
    catch (RemoteException e)
    {
      throw new ApplicationException("Fehler beim Prüfen der Abhängigkeiten.");
    }
    
  }

  /**
   * @see de.willuhn.datasource.db.AbstractDBObject#updateCheck()
   */
  protected void updateCheck() throws ApplicationException
  {
    insertCheck();
  }

}

/*********************************************************************
 * $Log: AbstractDBObjectNode.java,v $
 * Revision 1.8  2004/08/02 11:53:59  willuhn
 * *** empty log message ***
 *
 * Revision 1.7  2004/08/02 10:31:41  willuhn
 * *** empty log message ***
 *
 * Revision 1.6  2004/07/21 23:53:56  willuhn
 * @C massive Refactoring ;)
 *
 * Revision 1.5  2004/06/17 00:05:50  willuhn
 * @N GenericObject, GenericIterator
 *
 * Revision 1.4  2004/03/29 20:36:23  willuhn
 * *** empty log message ***
 *
 * Revision 1.3  2004/03/18 01:24:16  willuhn
 * @C refactoring
 *
 * Revision 1.2  2004/03/06 18:24:34  willuhn
 * @D javadoc
 *
 * Revision 1.1  2004/01/10 14:52:19  willuhn
 * @C package removings
 *
 * Revision 1.1  2004/01/08 20:46:43  willuhn
 * @N database stuff separated from jameica
 *
 * Revision 1.2  2003/12/19 01:43:26  willuhn
 * @N added Tree
 *
 * Revision 1.1  2003/12/18 21:47:12  willuhn
 * @N AbstractDBObjectNode
 *
 **********************************************************************/