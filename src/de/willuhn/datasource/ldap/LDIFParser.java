/**********************************************************************
 * $Source: /cvsroot/jameica/datasource/src/de/willuhn/datasource/ldap/Attic/LDIFParser.java,v $
 * $Revision: 1.1 $
 * $Date: 2004/01/23 00:25:52 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.datasource.ldap;

import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * Kleine Util-Klasse, die LDIF-Strings zerlegen kann.
 */
public class LDIFParser
{


	/**
	 * Liefert den CN zu einem DN.
   * @param dn zu pruefender DN.
   * @return CN.
   */
  public static String getCN(String dn)
	{
		return dn == null ? null : (String) splitDN(dn).get("cn");
	}

	public static String getBaseDN(String dn)
	{
		return dn == null ? null : dn.substring(dn.indexOf(",")+1);
	}

	/**
	 * Zerlegt einen DN in seine Bestandteile.
   * @param dn zu zerlegender DN.
   * @return Hashmap mit den Werten.
   */
  public static Map splitDN(String dn)
	{
		StringTokenizer st = new StringTokenizer(dn,",");
		Map map = new HashMap();
		while(st.hasMoreTokens())
		{
			String token = st.nextToken();
			if (token.indexOf('=') == -1)
				continue; 
			String key = token.substring(0,token.indexOf('=')).toLowerCase();
			String value = token.substring(token.indexOf('=')+1);
			map.put(key,value);
		}
		
		return map;
	}
}


/**********************************************************************
 * $Log: LDIFParser.java,v $
 * Revision 1.1  2004/01/23 00:25:52  willuhn
 * *** empty log message ***
 *
 **********************************************************************/