/**********************************************************************
 * $Source: /cvsroot/jameica/datasource/src/de/willuhn/datasource/common/Attic/RemoteServiceData.java,v $
 * $Revision: 1.1 $
 * $Date: 2004/01/23 00:25:52 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 **********************************************************************/
package de.willuhn.datasource.common;


/**
 * Datencontainer zur Konfiguration eines Remote-Service.
 */
public class RemoteServiceData extends AbstractServiceData {

  private String host;

  /**
   * Liefert die URL, unter der der Service verfuegbar ist.
   * @return RMI URL des Services.
   */
  public String getUrl()
  {
    return ("//" + host + "/" + getClassName() + "." + getName());
  }

	/**
	 * Liefert den Hostnamen, auf dem der Service verfuegbar ist.
   * @return Hostname des Providers.
   */
  public String getHost()
	{
		return host;
	}

	/**
	 * Speichert den Namen des Hosts, unter dem der Service verfuegbar ist.
   * @param host Hostname.
   */
  public void setHost(String host)
	{
		this.host = host;
	}
}


/*********************************************************************
 * $Log: RemoteServiceData.java,v $
 * Revision 1.1  2004/01/23 00:25:52  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/01/10 14:52:19  willuhn
 * @C package removings
 *
 * Revision 1.1  2004/01/08 20:46:44  willuhn
 * @N database stuff separated from jameica
 *
 * Revision 1.3  2004/01/04 18:48:36  willuhn
 * @N config store support
 *
 * Revision 1.2  2004/01/03 18:08:06  willuhn
 * @N Exception logging
 * @C replaced bb.util xml parser with nanoxml
 *
 * Revision 1.1  2003/10/29 00:41:26  willuhn
 * *** empty log message ***
 *
 **********************************************************************/