/**********************************************************************
 * $Source: /cvsroot/jameica/datasource/src/de/willuhn/datasource/ldap/Attic/QueryFilter.java,v $
 * $Revision: 1.1 $
 * $Date: 2004/01/23 00:25:52 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by  bbv AG
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.datasource.ldap;

import javax.naming.NamingException;
import javax.naming.directory.InvalidSearchFilterException;

/**
 * @author willuhn
 * Dieser Filter soll die LDAP-Abfragesprache fuer den Benutzer etwas
 * vereinfachen da sie von der Syntax her nicht mit SQL anderen Abfrage-
 * Sprachen vergleichbar ist. Instanzen von <code>QueryFilter</code>
 * koennen kaskadiert werden, um komplexe Abfragen zu gestalten. 
 */
public class QueryFilter {

	private String filter = null;
	private String bool = null;
	private Class objectClass = null;
	private String type = null;
	
	/**
	 * Erzeugt ein neues Query-Objekt.
   * @param clazz Klasse von Objekten, nach denen gesucht werden soll.
   *        Muss von <code>de.bbvag.ldap.AbstractDBObject</code> abgeleitet sein.
   *        Das macht die Suche im LDAP-Verzeichnis einigermassen typsicher.
   * @throws NamingException
   */
  public QueryFilter(Class clazz) throws NamingException
	{
		if (clazz == null)
			throw new NamingException("please use a class as parameter which inherits de.bbvag.ldap.LdapObject");

		objectClass = clazz;
		type = objectClass.getName();
		type = type.substring(type.lastIndexOf(".")+1);
		if (type.endsWith("Impl"))
			type = type.substring(0,type.length()-4);
	}

  /**
	 * Fuegt ein Filterkriterium mit dem boolschen Operator AND hinzu.
	 * @param field Name des Feldes.
	 * @param value Wert des Feldes. Muss genau uebereinstimmen.
   * @throws InvalidSearchFilterException
	 */
	public void and(String field, String value) throws InvalidSearchFilterException {
		if (bool != null && !bool.equals("&"))
			throw new InvalidSearchFilterException("it makes no sense to combine AND + OR");
		bool = "&";
		add(field,value);
	}

	/**
	 * Fuegt ein Filterkriterium mit dem boolschen Operator AND hinzu.
	 * @param field Name des Feldes.
	 * @param value Wert des Feldes. Muss enthalten sein.
   * @throws InvalidSearchFilterException
	 */
	public void andContains(String field, String value) throws InvalidSearchFilterException {
		and(field,"*"+value+"*");
	}

	/**
	 * Fuegt ein Filterkriterium mit dem boolschen Operator OR hinzu.
	 * @param field Name des Feldes.
	 * @param value Wert des Feldes.
   * @throws InvalidSearchFilterException
	 */
	public void or(String field, String value) throws InvalidSearchFilterException {
		if (bool != null && !bool.equals("|"))
			throw new InvalidSearchFilterException("it makes no sense to combine AND + OR");
		bool = "|";
		add(field,value);
	}

	/**
	 * Fuegt ein Filterkriterium mit dem boolschen Operator OR hinzu.
	 * @param field Name des Feldes.
	 * @param value Wert des Feldes. Muss enthalten sein.
   * @throws InvalidSearchFilterException
	 */
	public void orContains(String field, String value) throws InvalidSearchFilterException {
		or(field,"*"+value+"*");
	}

  /**
	 * Fuegt einen QueryFilter mit dem boolschen Operator OR hinzu.
	 * @param filter ein existierender QueryFilter.
   * @throws InvalidSearchFilterException
	 */
	public void or(QueryFilter filter) throws InvalidSearchFilterException {
		add("|",filter);
	}

	/**
	 * Fuegt einen QueryFilter mit dem boolschen Operator AND hinzu.
	 * @param filter ein existierender QueryFilter.
   * @throws InvalidSearchFilterException
	 */
	public void and(QueryFilter filter) throws InvalidSearchFilterException {
		add("&",filter);
	}

	/**
   * Speichert das Filterkriterium.
   * @param bool
   * @param field
   * @param value
   * @throws InvalidSearchFilterException
   */
  private void add(String field, String value) throws InvalidSearchFilterException
	{
		if (field == null)
			throw new InvalidSearchFilterException("field cannot be null");

		if (bool == null)
			throw new InvalidSearchFilterException("please enter at least one filter criteria");

		if (value == null)
			value = "";

		String append = "("+field+"="+value+")";
		if (this.filter == null) // first param?
			this.filter = bool+append;
		else
			this.filter += append;
	}

  /**
	 * Speichert das Filterkriterium.
   * @param bool
   * @param filter
   * @throws InvalidSearchFilterException
	 */
	private void add(String bool, QueryFilter other) throws InvalidSearchFilterException
	{
		if (!other.getObjectClass().equals(objectClass))
			throw new InvalidSearchFilterException("not allowed to mix QueryFilters with different class definitions.");
			
		if (this.filter == null) // first param?
			this.filter = other.filter;
		else
		{
			this.filter = bool + "(" + this.filter + ")" + other.filter.substring(1);
		}
	}

	/**
   * Gibt den erzeugten Query String aus.
   * @return erzeugter Query-String.
   */
  public String toString() {
  	return "(&(objectClass="+type+")" + (filter == null ? "" : "("+filter+")") + ")";
	}

	/**
   * Gibt die Klasse zurueck, nach der dieser Filter suchen soll.
   * Es wird nur nach Objekten gesucht, die von dieser Klasse
   * repaesentiert werden.
   * @return
   */
  public Class getObjectClass()
	{
		return objectClass;
	}
}


/*********************************************************************
 * $Log: QueryFilter.java,v $
 * Revision 1.1  2004/01/23 00:25:52  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2003/08/28 16:27:18  willuhn
 * @N inetOrgPerson
 *
 **********************************************************************/