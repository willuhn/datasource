/**********************************************************************
 * $Source: /cvsroot/jameica/datasource/src/de/willuhn/datasource/common/Attic/AbstractServiceData.java,v $
 * $Revision: 1.2 $
 * $Date: 2004/02/11 00:10:55 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 **********************************************************************/
package de.willuhn.datasource.common;

import de.willuhn.datasource.rmi.ServiceData;

/**
 * Basis-Klasse fuer Service-Datencontainer.
 */
public abstract class AbstractServiceData implements ServiceData {

	private String name;
	private String type;
  private String className;
  private String description;

  /**
   * @see de.willuhn.jameica.rmi.ServiceData#getClassName()
   */
  public String getClassName()
  {
    return className;
  }

  /**
   * @see de.willuhn.jameica.rmi.ServiceData#getType()
   */
  public String getType()
	{
		return type;
	}

  /**
   * @see de.willuhn.jameica.rmi.ServiceData#getName()
   */
  public String getName()
  {
    return name;
  }

  /**
   * @see de.willuhn.datasource.rmi.ServiceData#setClassName(java.lang.String)
   */
  public void setClassName(String className)
  {
    this.className = className;
  }

  /**
   * @see de.willuhn.datasource.rmi.ServiceData#setName(java.lang.String)
   */
  public void setName(String name)
  {
    this.name = name;
  }

  /**
   * @see de.willuhn.datasource.rmi.ServiceData#setType(java.lang.String)
   */
  public void setType(String type)
  {
    this.type = type;
  }

  /**
   * @see de.willuhn.datasource.rmi.ServiceData#getDescription()
   */
  public String getDescription() {
    return description;
  }

  /**
   * @see de.willuhn.datasource.rmi.ServiceData#setDescription(java.lang.String)
   */
  public void setDescription(String description) {
  	this.description = description;
  }

  /**
   * @see java.lang.Object#toString()
   */
  public String toString() {
  	return getName();
  }

}


/*********************************************************************
 * $Log: AbstractServiceData.java,v $
 * Revision 1.2  2004/02/11 00:10:55  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/01/23 00:25:52  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/01/10 14:52:19  willuhn
 * @C package removings
 *
 * Revision 1.1  2004/01/08 20:46:44  willuhn
 * @N database stuff separated from jameica
 *
 * Revision 1.4  2004/01/04 18:48:36  willuhn
 * @N config store support
 *
 * Revision 1.3  2004/01/03 18:08:06  willuhn
 * @N Exception logging
 * @C replaced bb.util xml parser with nanoxml
 *
 * Revision 1.2  2003/11/12 00:58:54  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2003/10/29 00:41:27  willuhn
 * *** empty log message ***
 *
 **********************************************************************/