/**********************************************************************
 * $Source: /cvsroot/jameica/datasource/src/de/willuhn/datasource/common/Attic/LocalServiceData.java,v $
 * $Revision: 1.1 $
 * $Date: 2004/01/23 00:25:52 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 **********************************************************************/
package de.willuhn.datasource.common;

import java.util.HashMap;


/**
 * Haelt die Konfigurationsdaten von lokalen Services vor.
 * @author willuhn
 */
public class LocalServiceData extends AbstractServiceData {

	private boolean shared = false;
  private HashMap initParams = new HashMap();


  /**
   * Prueft, ob der Service im Netzwerk freigegeben werden soll.
   * @return true, wenn er freigegeben wird, andernfalls false.
   */
  public boolean isShared()
	{
		return shared;
	}
  
	/**
	 * Speichert, ob der Service im Netzwerk freigegeben werden soll.
   * @param b true, wenn er freigegeben werden soll.
   */
  public void setShared(boolean b)
	{
		shared = b;
	}

  /**
   * Liefert den Wert des genannten Init-Params.
   * @param name Name des Init-Params.
   * @return Wert des Init-Params.
   */
  public String getInitParam(String name)
  {
    return (String) initParams.get(name);
  }

	/**
	 * Speichert einen weiteren Init-Parameter.
   * @param name Name des Parameters.
   * @param value Wert des Parameters.
   */
  public void addInitParam(String name, String value)
	{
		if (name == null) return;
		initParams.put(name,value);
	}
  /**
   * Liefert eine Hashmap mit allen Init-Params.
   * @return hashMap mit allen Init-Params.
   */
  public HashMap getInitParams()
  {
    return initParams;
  }

  /**
   * Liefert die URL unter der dieser Service via RMI freigegeben wird.
   * @return RMI URL des Services.
   */
  public String getUrl()
	{
		return ("//127.0.0.1/" + getClassName() + "." + getName());
	}

}


/*********************************************************************
 * $Log: LocalServiceData.java,v $
 * Revision 1.1  2004/01/23 00:25:52  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/01/10 14:52:19  willuhn
 * @C package removings
 *
 * Revision 1.1  2004/01/08 20:46:44  willuhn
 * @N database stuff separated from jameica
 *
 * Revision 1.4  2004/01/03 18:08:06  willuhn
 * @N Exception logging
 * @C replaced bb.util xml parser with nanoxml
 *
 * Revision 1.3  2003/11/20 03:48:42  willuhn
 * @N first dialogues
 *
 * Revision 1.2  2003/11/12 00:58:54  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2003/10/29 00:41:27  willuhn
 * *** empty log message ***
 *
 **********************************************************************/