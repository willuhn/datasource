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

import java.rmi.RemoteException;

/**
 * Generisches RMI-faehiges Objekt, welches von genericObject
 * abgeleitet ist, jedoch noch Funktionen zur Abbildung einer Baumstruktur mitbringt.
 */ 
public interface GenericObjectNode extends GenericObject
{

  /**
   * Liefert einen Iterator mit allen direkten Kind-Objekten
   * des aktuellen Objektes. Jedoch keine Kindes-Kinder.
   * @return Iterator mit den direkten Kind-Objekten.
   * @throws RemoteException
   */
  public GenericIterator getChildren() throws RemoteException;


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
 * Revision 1.3  2006/04/20 08:34:13  web0
 * @C s/Childs/Children/
 *
 * Revision 1.2  2004/08/11 23:36:34  willuhn
 * @N Node Objekte in GenericObjectNode und DBObjectNode aufgeteilt
 *
 * Revision 1.1  2004/07/21 23:53:56  willuhn
 * @C massive Refactoring ;)
 *
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