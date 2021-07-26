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
package de.willuhn.datasource.rmi;

import java.rmi.RemoteException;

import de.willuhn.datasource.GenericIterator;
import de.willuhn.datasource.GenericObjectNode;

/**
 * Diese Klasse ist die ideale Basis-Klasse, wenn es gilt, Baum-Strukturen abzubilden.
 * In einer Datenbank wuerde das wie folgt gehen: Man nehme eine SQL-Tabelle und erweitere
 * sie um eine Spalte fuer das Eltern-Objekt. Diese heisst z.Bsp. "parent_id".
 * Dieser Fremd-Schluessel zeigt auf die selbe Tabelle und dort auf das
 * uebergeordnete Objekt. Ein solches Objekt laesst sich dann prima mit
 * der GUI-Komponente "Tree" darstellen.
 * Hinweis: Objekte, die sich bereits auf der obersten Ebene des Baumes
 * befinden, muessen den Wert "0" im Feld fuer das Eltern-Objekt besitzen.
 */
public interface DBObjectNode extends DBObject, GenericObjectNode
{
	/**
	 * Liefert einen Iterator mit allen Root-Objekten.
	 * Das sind all die, welche sich auf oberster Ebene befinden.
	 * @return Iterator mit den Root-Objekten.
	 * @throws RemoteException
	 */
	public GenericIterator getTopLevelList() throws RemoteException;
	
	/**
	 * Speichert das Eltern-Element.
	 * @param parent Eltern-Element.
	 * @throws RemoteException
	 */
	public void setParent(DBObjectNode parent) throws RemoteException;
}

/*********************************************************************
 * $Log: DBObjectNode.java,v $
 * Revision 1.4  2009/02/23 22:13:49  willuhn
 * @N setParent(DBObjectNode) in DBObjectNode
 *
 * Revision 1.3  2004/08/11 23:36:34  willuhn
 * @N Node Objekte in GenericObjectNode und DBObjectNode aufgeteilt
 *
 **********************************************************************/