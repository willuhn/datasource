/**********************************************************************
 * $Source: /cvsroot/jameica/datasource/src/de/willuhn/datasource/rmi/Attic/GenericObjectNode.java,v $
 * $Revision: 1.1 $
 * $Date: 2004/06/17 00:05:50 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.datasource.rmi;

import java.rmi.RemoteException;

/**
 * Diese Klasse ist die ideale Basis-Klasse, wenn es gilt, Baum-Strukturen abzubilden.
 * In einer Datenbank wuerde das wie folgt gehen: Man nehme eine SQL-Tabelle und erweitere
 * sie um eine Spalte fuer das Eltern-Objekt. Diese heisst z.Bsp. "parent_id".
 * Dieser Fremd-Schluessel zeigt auf die selbe Tabelle und dort auf das
 * uebergeordnete Objekt. Ein solches Objekt laesst sich dann prima mit
 * der GUI-Komponente "Tree" darstellen.
 * Hinweis: Objekte, die sich bereits auf der obersten Ebene des Baumes
 * befinden, muessen den Wert "0" im Feld fuer das Eltern-Objekt besitzen.
 * @author willuhn
 */
public interface GenericObjectNode extends DBObject
{

  /**
   * Liefert einen Iterator mit allen direkten Kind-Objekten
   * des aktuellen Objektes. Jedoch keine Kindes-Kinder.
   * @return Iterator mit den direkten Kind-Objekten.
   * @throws RemoteException
   */
  public GenericIterator getChilds() throws RemoteException;

  /**
   * Liefert einen Iterator mit allen Root-Objekten.
   * Das sind all die, welche sich auf oberster Ebene befinden.
   * @return Iterator mit den Root-Objekten.
   * @throws RemoteException
   */
  public GenericIterator getTopLevelList() throws RemoteException;

  /**
   * Prueft, ob das uebergeben Node-Objekt ein Kind des aktuellen
   * ist. Dabei wird der gesamte Baum ab hier rekursiv durchsucht.
   * @param object das zu testende Objekt.
   * @return true wenn es ein Kind ist, sonst false.
   * @throws RemoteException
   */
  public boolean hasChild(GenericObjectNode object) throws RemoteException;


  /**
   * Liefert das Eltern-Element des aktuellen oder null, wenn es sich
   * bereits auf oberster Ebene befindet.
   * @return das Eltern-Objekt oder null.
   * @throws RemoteException
   */
  public GenericObjectNode getParent() throws RemoteException;

  /**
   * Liefert alle moeglichen Eltern-Objekte dieses Objektes.
   * Das sind nicht die tatsaechlichen Eltern (denn jedes Objekt
   * kann ja nur ein Eltern-Objekt haben) sondern eine Liste
   * der Objekte, an die es als Kind gehangen werden werden.
   * Das ist z.Bsp. sinnvoll, wenn man ein Kind-Element im Baum
   * woanders hinhaengenn will. Da das Objekt jedoch nicht an
   * eines seiner eigenen Kinder und auch nicht an sich selbst
   * gehangen werden kann (Rekursion) liefert diese Funktion nur
   * die moeglichen Eltern-Objekte.
   * @return Liste der moeglichen Eltern-Objekte.
   * @throws RemoteException
   */
  public GenericIterator getPossibleParents() throws RemoteException;

  /**
   * Liefert eine Liste mit allen Eltern-Objekten bis hoch zum
   * Root-Objekt. Also sowas wie ein voller Verzeichnisname, jedoch
   * andersrum. Das oberste Element steht am Ende der Liste.
   * @return Liste aller Elternobjekte bis zum Root-Objekt.
   * @throws RemoteException
   */
  public GenericIterator getPath() throws RemoteException;
}

/*********************************************************************
 * $Log: GenericObjectNode.java,v $
 * Revision 1.1  2004/06/17 00:05:50  willuhn
 * @N GenericObject, GenericIterator
 *
 * Revision 1.1  2004/01/10 14:52:19  willuhn
 * @C package removings
 *
 * Revision 1.1  2004/01/08 20:46:44  willuhn
 * @N database stuff separated from jameica
 *
 * Revision 1.1  2003/12/19 01:43:27  willuhn
 * @N added Tree
 *
 **********************************************************************/