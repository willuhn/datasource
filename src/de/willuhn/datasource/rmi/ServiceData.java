/**********************************************************************
 * $Source: /cvsroot/jameica/datasource/src/de/willuhn/datasource/rmi/Attic/ServiceData.java,v $
 * $Revision: 1.1 $
 * $Date: 2004/01/10 14:52:19 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 **********************************************************************/
package de.willuhn.datasource.rmi;

/**
 * Bildet einen Datencontainer fuer die Konfiguration eines Services.
 */
public interface ServiceData {

  /**
   * Liefert den Klassennamen, der den Service implementiert.
   * @return Klassenname.
   */
  public String getClassName();

  /**
   * Liefert den Typ des Services als Freitext.
   * Z.Bsp. "database".
   * @return Typ des Services.
   */
  public String getType();

  /**
   * Liefert einen symbolischen Alias-Namen.
   * @return Alias-Name der Services.
   */
  public String getName();

	/**
	 * Speichert den Klassennamen des Implementors.
   * @param className Klassenname des Implementors.
   */
  public void setClassName(String className);

	/**
	 * Speichert den Typ des Services.
   * @param type Typ des Services.
   */
  public void setType(String type);

	/**
	 * Speichert den Alias-Namen des Services.
   * @param name Alias-Name des Services.
   */
  public void setName(String name);

}


/*********************************************************************
 * $Log: ServiceData.java,v $
 * Revision 1.1  2004/01/10 14:52:19  willuhn
 * @C package removings
 *
 * Revision 1.1  2004/01/08 20:46:44  willuhn
 * @N database stuff separated from jameica
 *
 * Revision 1.1  2003/11/12 00:58:54  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2003/10/29 00:41:27  willuhn
 * *** empty log message ***
 *
 **********************************************************************/