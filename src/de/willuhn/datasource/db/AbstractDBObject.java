/**********************************************************************
 * $Source: /cvsroot/jameica/datasource/src/de/willuhn/datasource/db/AbstractDBObject.java,v $
 * $Revision: 1.24 $
 * $Date: 2004/12/09 23:22:25 $
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
import java.rmi.server.UnicastRemoteObject;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import de.willuhn.datasource.GenericObject;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.datasource.rmi.DBObject;
import de.willuhn.datasource.rmi.Event;
import de.willuhn.datasource.rmi.Listener;
import de.willuhn.datasource.rmi.ObjectNotFoundException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

/**
 * Basisklasse fuer alle Business-Objekte 
 * @author willuhn
 */
public abstract class AbstractDBObject extends UnicastRemoteObject implements DBObject 
{

  // Der Primary-Key des Objektes
  private String id;

  // Haelt die Eigenschaften des Objektes.
  private HashMap properties = new HashMap();
  
  // Haelt die Datentypen der Properties.
  private HashMap types      = new HashMap();

  // definiert, ob das Objekt gerade in einer manuellen Transaktion ist
  private boolean inTransaction = false;

  // ein Cache fuer ForeignObjects
  private HashMap foreignObjectCache = new HashMap();

	private transient DBServiceImpl service = null;
	private transient Connection conn = null;

	private transient ArrayList deleteListeners = null;
	private transient ArrayList storeListeners  = null;

  /**
   * Attribute dieses Typs werden als java.util.Date erkannt.
   */
  private final static String ATTRIBUTETYPE_DATE      = "date";

  /**
   * Attribute dieses Typs werden als java.util.Date erkannt.
   */
  private final static String ATTRIBUTETYPE_TIMESTAMP = "timestamp";

  /**
   * Attribute dieses Typs werden als java.util.Date erkannt.
   */
  private final static String ATTRIBUTETYPE_DATETIME  = "datetime";

  /**
   * Attribute dieses Typs werden als java.lang.Integer erkannt.
   */
  private final static String ATTRIBUTETYPE_INT       = "int";

  /**
   * Attribute dieses Typs werden als java.lang.Double erkannt.
   */
  private final static String ATTRIBUTETYPE_DOUBLE    = "double";

  /**
   * Attribute dieses Typs werden als java.lang.Double erkannt.
   */
  private final static String ATTRIBUTETYPE_DECIMAL   = "decimal";

  /**
   * ct
   * @throws RemoteException
   */
	public AbstractDBObject() throws RemoteException
	{
		super(); // Konstruktor von UnicastRemoteObject
	}

  /**
   * Speichert den Service-Provider.
   * @param service
   * @throws Exception
   */
  void setService(DBServiceImpl service) throws Exception
  {
  	this.service = service;
  	conn = service.getConnection();
    if (conn == null)
      throw new SQLException("connection is null");

    conn.setAutoCommit(false); // Auto-Commit schalten wir aus weil wir vorsichtig sind ;)
  }
  
  /**
   * Liefert die Exception, die dieses Objekt gerade benutzt.
   * @return die Connection dieses Objektes.
   */
  private Connection getConnection()
  {
  	return conn;
  }

	/**
	 * Liefert den Service-Provider.
   * @return Service.
   */
  protected DBServiceImpl getService()
	{
		return service;  
	}

  /**
   * Prueft, ob die Datenbankverbindung existiert und funktioniert.
   * @throws RemoteException wird geworfen, wenn die Connection kaputt ist.
   */
  private void checkConnection() throws RemoteException
  {
    if (getConnection() == null)
      throw new RemoteException("database connection not set.");
  }


  /**
   * Holt sich die Meta-Daten der Tabelle und erzeugt die Properties.
   * @throws SQLException Wenn beim Laden der Meta-Daten ein Datenbank-Fehler auftrat.
   */
  void init() throws SQLException
  {
    try {
      checkConnection();
    }
    catch (RemoteException e)
    {
      throw new SQLException(e.getMessage());
    }
    
    if (isInitialized())
      return; // allready initialized

    HashMap cachedMeta = ObjectMetaCache.getMetaData(this.getClass());

    if (cachedMeta != null)
    {
      // Treffer. Die Daten nehmen wir.
      this.types = cachedMeta;
      Iterator i = cachedMeta.keySet().iterator();
      while (i.hasNext())
      {
        String s = (String) i.next();
        if (s == null) continue;
        this.properties.put(s,null);
      }
      return;
    }

		String tableName = getTableName();
		ResultSet meta = null;
		try {
			meta = getConnection().getMetaData().getColumns(null,null,tableName,null);
			String field;
			while (meta.next())
			{
				field = meta.getString("COLUMN_NAME");
				if (field == null || field.equalsIgnoreCase(this.getIDField())) // skip empty fields and primary key
					continue;
				properties.put(field,null);
        types.put(field,meta.getString("TYPE_NAME"));
			}
      ObjectMetaCache.addMetaData(this.getClass(),types);
		}
		catch (SQLException e)
		{
      throw e;
		}
		finally {
			try {
				meta.close();
			} catch (Exception e) {/*useless*/}
		}
  	
  }

  /**
   * Prueft, ob das Objekt initialisiert ist.
   * @return
   */
  private boolean isInitialized()
  {
    return (
      this.properties != null &&
      this.properties.size() > 0 &&
      this.types != null &&
      this.types.size() > 0
    );
    
  }

  /**
   * @see de.willuhn.datasource.rmi.DBObject#load(java.lang.String)
   */
  public final void load(String id) throws RemoteException
	{
    checkConnection();

		this.id = ((id == null || id.equals("")) ? null : id);
		if (this.id == null)
			return; // nothing to load

    if (!isInitialized())
      throw new RemoteException("object not initialized.");
    
		String tableName = getTableName();
		Statement stmt = null;
		ResultSet data = null;
		try {
			stmt = getConnection().createStatement();
      data = stmt.executeQuery(getLoadQuery());
			if (!data.next())
      {
      	throw new ObjectNotFoundException("object [id: " + id + ", type: " + this.getClass().getName() + "] not found");
//        this.id = null;
//        return; // record not found.
      }

			String[] attributes = getAttributeNames();
			for (int i=0;i<attributes.length;++i)
			{
				setAttribute(attributes[i],data.getObject(attributes[i]));
			}
		}
		catch (SQLException e)
		{
			throw new RemoteException("unable to load data from table " + tableName,e);
		}
		finally {
			try {
				data.close();
			} catch (Throwable t) {/*useless*/}
			try {
				stmt.close();
			} catch (Throwable t) {/*useless*/}
		}
		 
	}
  
  /**
   * @see de.willuhn.datasource.rmi.DBObject#store()
   */
  public void store() throws RemoteException, ApplicationException
  {
    if (isNewObject())
      insert();
    else 
      update();
    
  }

  /**
   * @see de.willuhn.datasource.rmi.DBObject#clear()
   */
  public final void clear() throws RemoteException
  {
    if (!isInitialized())
      throw new RemoteException("object not initialized.");

    this.id = null;
    String attributes[] = this.getAttributeNames();
    for (int i=0;i<attributes.length;++i)
    {
      this.setAttribute(attributes[i],null);
    }
  }
  
  /**
   * @see de.willuhn.datasource.rmi.DBObject#delete()
   */
  public void delete() throws RemoteException, ApplicationException
  {
    if (isNewObject())
      return; // no, we delete no new objects ;)

    checkConnection();

    if (!isInitialized())
      throw new RemoteException("object not initialized.");

    deleteCheck();

		// Wir benachrichtigen die Listeners.
		notify(deleteListeners);

		Statement stmt = null;
    try {
    	stmt = getConnection().createStatement();
      String sql = null;
      try {
        sql = "delete from " + getTableName() + " where "+this.getIDField()+" = "+Integer.parseInt(id);
      }
      catch (NumberFormatException e)
      {
        sql = "delete from " + getTableName() + " where "+this.getIDField()+" = '"+id+"'";
      }
      stmt.execute(sql);
      if (!this.inTransaction)
      {
				getConnection().commit();
      }
			this.id = null;
    }
    catch (SQLException e)
    {
      if (!this.inTransaction) {
        try {
          getConnection().rollback();
          throw new RemoteException("delete failed, rollback successful",e);
        }
        catch (SQLException e2)
        {
					throw new RemoteException("delete failed, rollback failed",e2);
        }
      }
			throw new RemoteException("delete failed",e);
    }
    finally {
			try {
				stmt.close();
			} catch (SQLException se) {/*useless*/}
    }
  }

  /**
   * @see de.willuhn.datasource.GenericObject#getID()
   */
  public final String getID() throws RemoteException
  {
    return id;
  }

  /**
   * Speichert die uebergeben ID in diesem Objekt. Diese Funktion
   * ist mit aeusserster Vorsicht zu geniessen. Sie wird z.Bsp. dann
   * gebraucht, wenn ein Objekt von einer DB auf eine andere kopiert
   * wird und dabei zwingend mit der ID der Ursprungs-Datenbank
   * angelegt werden muss.
   * @param id
   * @throws RemoteException
   */
  public final void setID(String id) throws RemoteException
  {
    this.id = id;
  }

  /**
   * @see de.willuhn.datasource.GenericObject#getAttribute(java.lang.String)
   */
  public Object getAttribute(String fieldName) throws RemoteException
  {
    if (!isInitialized())
      throw new RemoteException("object not initialized.");

		if (fieldName == null)
			return null;

    Object o = properties.get(fieldName);
    if (o == null)
    {
    	// Mhh, wir haben keinen Wert hierfuer.
    	// Aber vielleicht ist es ja der Primary-Key
    	if (fieldName.equalsIgnoreCase(getIDField()))
    		return getID();
    	else
				return null;
    }

    // wir checken erstmal, ob es sich um ein Objekt aus einer Fremdtabelle
    // handelt. Wenn das der Fall ist, liefern wir das statt der
    // lokalen ID aus.
    Class foreign = getForeignObject(fieldName);
    if (foreign != null)
    {
      DBObject cachedObject = (DBObject) foreignObjectCache.get(foreign);
      if (cachedObject != null)
      {
        String value = o.toString();
        if (!value.equals(cachedObject.getID()))
          cachedObject.load(value);
      }
      else {
        try
        {
          cachedObject = (DBObject) service.createObject(foreign,o.toString());
					foreignObjectCache.put(foreign,cachedObject);
        }
        catch (RemoteException re)
        {
					throw re;
        }
        catch (Exception e)
        {
        	throw new RemoteException("unable to create foreign object",e);
        }
      }
			return cachedObject;
    }

    return o;
  }

  /**
   * @see de.willuhn.datasource.rmi.DBObject#getAttributeType(java.lang.String)
   */
  public final String getAttributeType(String attributeName) throws RemoteException
  {
    if (!isInitialized())
      throw new RemoteException("object not initialized.");

    try {
      return (String) types.get(attributeName);
    }
    catch (Exception e)
    {
      throw new RemoteException("unable to determine filed type of attribute " + attributeName);
    }
  }

  /**
   * Speichert einen neuen Wert in den Properties
   * und liefert den vorherigen zurueck.
   * @param fieldName Name des Feldes.
   * @param value neuer Wert des Feldes.
   * Muss vom Typ String, Date, Timestamp, Double, Integer oder DBObject sein.<br>
   * Ist der Parameter vom Typ <code>dbObject</code> nimmt die Funktion an, dass
   * es sich um einen Fremdschluessel handelt und speichert automatisch statt
   * des Objektes selbst nur dessen ID mittels <code>new Integer(((DBObject)value).getID())</code>.
   * @return vorheriger Wert des Feldes.
   * @throws RemoteException
   */
  protected final Object setAttribute(String fieldName, Object value) throws RemoteException
  {
    if (fieldName == null)
      return null;

		// Null-Werte fischen wir uns vorher raus, damit wir uns beim folgenden Code NPEs ersparen
		if (value == null)
			return properties.put(fieldName, null);
		
		if ((value instanceof DBObject) && value != null)
		{
			String id = ((DBObject)value).getID();
			if (id != null && id.length() > 0)
			try
			{
				value = new Integer(id);
			}
			catch (Exception e)
			{/*ignore*/}
		}
    return properties.put(fieldName, value);
  }

  /**
   * @see de.willuhn.datasource.GenericObject#getAttributeNames()
   */
  public final String[] getAttributeNames() throws RemoteException
  {
    Set s = properties.keySet();
    return (String[]) s.toArray(new String[s.size()]);
    
  }

	/**
   * Wird bei einem Insert aufgerufen, ermittelt die ID des erzeugten Datensatzes und speichert sie in diesem Objekt.
   * @throws RemoteException
   */
  private void setLastId() throws RemoteException
	{
    checkConnection();

		Statement stmt = null;
		try {
			stmt = getConnection().createStatement();
			ResultSet rs = stmt.executeQuery("select max("+this.getIDField()+") from " + getTableName());
			rs.next();
			this.id = rs.getString(1);
		}
		catch (Exception e)
		{
			throw new RemoteException("unable to read id of last insert",e);
		}
	}

  /**
   * Speichert das Objekt explizit als neuen Datensatz in der Datenbank.
   * Die Funktion wird auch dann ein Insert versuchen, wenn das Objekt
   * bereits eine ID besitzt. Das ist z.Bsp. sinnvoll, wenn das Objekt
   * von einer Datenbank auf eine andere kopiert werden soll. Es kann jedoch
   * durchaus fehlschlagen, wenn ein Objekt mit dieser ID bereits in
   * der Datenbank existiert.
   * @throws RemoteException Wenn beim Speichern Fehler aufgetreten sind.
   * @throws ApplicationException Durch <code>insertCheck()</code> erzeugte Benutzerfehler.
   */
  public void insert() throws RemoteException, ApplicationException
  {
    checkConnection();

    if (!isInitialized())
      throw new RemoteException("object not initialized.");

    insertCheck();

		PreparedStatement stmt = null;
    try {
      stmt = getInsertSQL();
      stmt.execute();
      setLastId();
      if (!this.inTransaction)
  			getConnection().commit();
			notify(storeListeners);
    }
    catch (SQLException e)
    {
      if (!this.inTransaction) {
        try {
          getConnection().rollback();
          throw new RemoteException("insert failed, rollback successful",e);
        }
        catch (SQLException e2)
        {
					throw new RemoteException("delete failed, rollback failed",e2);
        }
      }
			throw new RemoteException("delete failed",e);
    }
		finally {
			try {
				stmt.close();
			} catch (SQLException se) {/*useless*/}
		}
  }
  
  /**
   * Aktualisiert das Objekt explizit in der Datenbank.
   * Wenn es sich um ein neues Objekt handelt, wird das Update fehlschlagen.
   * @throws RemoteException Wenn beim Update Fehler aufgetreten sind.
   * @throws ApplicationException durch <code>updateCheck()</code> erzeugte Benutzer-Fehler.
   */
  private void update() throws RemoteException, ApplicationException
  {
    checkConnection();

    if (!isInitialized())
      throw new RemoteException("object not initialized.");

    if (isNewObject())
    {
      // Objekt hat keine ID. Von daher kann's auch nicht upgedated werden
      throw new RemoteException("object is new - cannot update");
    }
    updateCheck();

		PreparedStatement stmt = null;
    int affected = 0;
    try {
			stmt = getUpdateSQL();
      affected = stmt.executeUpdate();
      if (affected != 1)
      {
        // Wenn nicht genau ein Datensatz geaendert wurde, ist was faul.
        throw new SQLException();
      }
      if (!this.inTransaction)
        getConnection().commit();
      notify(storeListeners);
    }
    catch (SQLException e)
    {
      if (!this.inTransaction) {
        try {
          getConnection().rollback();
          throw new RemoteException("update failed, rollback successful",e);
        }
        catch (SQLException e2)
        {
					throw new RemoteException("update failed, rollback failed",e2);
        }
      }
			throw new RemoteException("update failed",e);
    }
		finally {
			try {
				stmt.close();
			} catch (SQLException se) {/*useless*/}
		}
    
  }

  /**
   * Liefert das automatisch erzeugte SQL-Statement fuer ein Update.
   * Kann bei Bedarf überschrieben um ein vom dynamisch erzeugten
   * abweichendes Statement für die Speicherung zu verwenden.  
   * @return das erzeugte SQL-Statement.
   * @throws RemoteException wenn beim Erzugen des Statements ein Fehler auftrat.
   */
  protected PreparedStatement getUpdateSQL() throws RemoteException
  {
    checkConnection();

    String sql = "update " + getTableName() + " set ";
    String[] attributes = getAttributeNames();

    for (int i=0;i<attributes.length;++i)
    {
			if (attributes[i].equalsIgnoreCase(this.getIDField()))
				continue; // skip the id field
      sql += attributes[i] + "=?,";
    }
    sql = sql.substring(0,sql.length()-1); // remove last ","
    try {
      sql += " where "+this.getIDField()+"="+Integer.parseInt(getID());
    }
    catch (NumberFormatException e)
    {
      sql += " where "+this.getIDField()+"='"+getID()+"'";
    }
    try {
      PreparedStatement stmt = getConnection().prepareStatement(sql);
      for (int i=0;i<attributes.length;++i)
      {
        String type  = (String) types.get(attributes[i]);
        Object value = properties.get(attributes[i]);
        setStmtValue(stmt,i,type,value);
      }
			Logger.debug("executing sql statement: " + stmt.toString());
      return stmt;
    }
    catch (Exception e)
    {
      throw new RemoteException("unable to prepare update sql statement",e);
    }
  }
  
  /**
   * Liefert das automatisch erzeugte SQL-Statement fuer ein Insert.
   * Kann bei Bedarf überschrieben um ein vom dynamisch erzeugten
   * abweichendes Statement für die Speicherung zu verwenden.  
   * @return das erzeugte SQL-Statement.
   * @throws RemoteException Wenn beim Erzeugen des Statements ein Fehler auftrat.
   */
  protected PreparedStatement getInsertSQL() throws RemoteException
  {
    checkConnection();

    String sql = "insert into " + getTableName() + " ";
    String[] attributes = getAttributeNames();

    String names = "(";
    String values = " values (";

    for (int i=0;i<attributes.length;++i)
    {
      if (attributes[i] == null || attributes[i].equals("")) // die sollte es zwar eigentlich nicht geben, aber sicher ist sicher ;)
        continue; // skip empty fields
      names += attributes[i] + ",";
      values += "?,";
    }

    names  += getIDField() + ")";

    // Wenn das Objekt eine ID hat, dann haengen wir sie an's Insert-Statement mit dran.
    if (getID() != null)
    {
      try {
        values += Integer.parseInt(getID()) + ")";
      }
      catch (NumberFormatException e)
      {
        values += "'" + getID() + "')";
      }
    }
    else {
      // Weil die UNIQUEKEY() Funktion von McKoi nicht "select (max(id) + 1)" macht sondern
      // selbst bei 1 anfaengt zu zaehlen, kommt es dauernd vor, dass es eine ID ermittelt,
      // die es schon gibt. Naemlich genau dann, wenn in die Tabelle Zeilen eingefuegt wurden,
      // die ihre eigene ID mitgebracht haben :(

      values += "'" + createID() + "')";
    }

    try {
      PreparedStatement stmt = getConnection().prepareStatement(sql + names + values);
      for (int i=0;i<attributes.length;++i)
      {
        String type  = (String) types.get(attributes[i]);
        Object value = properties.get(attributes[i]);
        setStmtValue(stmt,i,type,value);
      }
			Logger.debug("executing sql statement: " + stmt.toString());
      return stmt;
    }
    catch (Exception e)
    {
      throw new RemoteException("unable to prepare insert sql statement",e);
    }
  }

  /**
   * Erzeugt eine neue noch nicht vergebene ID fuer das neue Objekt.
   * @return die erzeugte ID.
   * @throws RemoteException im Fehlerfall.
   */
  private String createID() throws RemoteException
  {
    Statement stmt = null;
    ResultSet rs = null;
    try {
      stmt = getConnection().createStatement();
      rs = stmt.executeQuery("select (max(" + getIDField() + ") + 1) from " + getTableName());
      if (!rs.next())
        throw new SQLException("select max(id) returned empty resultset");
      return rs.getString(1);
    }
    catch (SQLException e)
    {
      throw new RemoteException("unable to create new insert id",e);
    }
    finally
    {
      try {
        rs.close();
        stmt.close();
      }
      catch (SQLException e) { /* useless */}
    }
  }

  /**
   * Liefert das automatisch erzeugte SQL-Statement fuer die Erzeugung einer Liste
   * dieses Typs.
   * ACHTUNG: Das Statement muss ein Feld mit der Bezeichnung zurueckgeben,
   * die <code>getIDField</code> auch liefert, da das von DBIteratorImpl gelesen wird.
   * Also z.Bsp. "select " + getIDField() + " from " + getTableName().
   * Kann bei Bedarf überschrieben um ein abweichendes Statement zu verwenden.
   * Die Funktion muss das Statement nur dewegen als String zurueckliefern,
   * weil es typischerweise von DBIterator weiterverwendet wird und dort eventuell
   * noch weitere Filterkriterien hinzugefuegt werden koennen muessen.  
   * @return das erzeugte SQL-Statement.
   * @throws RemoteException Wenn beim Erzeugen des Statements ein Fehler auftrat.
   */
  protected String getListQuery() throws RemoteException
  {
    return "select " + getIDField() + " from " + getTableName();
  }

	/**
	 * Liefert das automatisch erzeugte SQL-Statement zum Laden des Objektes.
	 * Hierbei werden die Eigenschaften des Objektes geladen, dessen ID aktuell
	 * von <code>getID()</code> geliefert wird.
	 * ACHTUNG: Das Statement muss alle Felder selecten (*).
	 * Also z.Bsp. "select * from " + getTableName() + " where " + getIDField() + " = " + getID();
	 * Kann bei Bedarf überschrieben um ein abweichendes Statement zu verwenden.
	 * @return das erzeugte SQL-Statement.
	 * @throws RemoteException Wenn beim Erzeugen des Statements ein Fehler auftrat.
	 */
	protected String getLoadQuery() throws RemoteException
	{
		try {
			return "select * from " + getTableName() + " where " + this.getIDField() + " = "+Integer.parseInt(this.getID());
		}
		catch (NumberFormatException e)
		{
			return "select * from " + getTableName() + " where " + this.getIDField() + " = '"+this.getID()+"'";
		}
	}

  /**
   * Macht sozusagen das Typ-Mapping bei Insert und Update.
   * Hintergrund: Die Funktionen <code>getInsertSQL()</code> und
   * <code>getUpdateSQL()</code> erzeugen ja die Statements fuer
   * Insert und Update. Da ein PreparedStatement ja typsichere
   * Werte haben muss, rufen beide Funktion diese hier auf, um
   * hier die Werte korrekt setzen zu lassen.
   * @param stmt das PreparedStatement.
   * @param index der Index im Statement.
   * @param type Bezeichnung des Feld-Typs entspechend der types-Mappingtabelle.
   * @param value der Wert.
   */
  private void setStmtValue(PreparedStatement stmt, int index, String type, Object value)
  {
    index++;  // Wer zur Hoelle hat sich ausgedacht, dass Arrays bei Index 0, PreparedStatements aber bei 1 anfangen?? Grr
    try {
      if (type == null || value == null)
        stmt.setNull(index,Types.NULL);

      else if (ATTRIBUTETYPE_DATE.equalsIgnoreCase(type) || ATTRIBUTETYPE_DATETIME.equalsIgnoreCase(type))
        stmt.setDate(index,new java.sql.Date(((Date) value).getTime()));

      else if (ATTRIBUTETYPE_TIMESTAMP.equalsIgnoreCase(type))
        stmt.setTimestamp(index,new Timestamp(((Date) value).getTime()));

      else if (ATTRIBUTETYPE_INT.equalsIgnoreCase(type))
        stmt.setInt(index,((Integer) value).intValue());

      else if (ATTRIBUTETYPE_DOUBLE.equalsIgnoreCase(type) || ATTRIBUTETYPE_DECIMAL.equalsIgnoreCase(type))
        stmt.setDouble(index,((Double) value).doubleValue());

      else stmt.setString(index,(String) value);
    }
    catch (Exception e)
    {
      try {
        stmt.setString(index,""+value);
      }
      catch (Exception e2) {/* useless */}
    }
  }

  /**
   * @see de.willuhn.datasource.rmi.DBObject#isNewObject()
   */
  public final boolean isNewObject() throws  RemoteException
  {
    return getID() == null;
  }

  /**
   * Liefert den Namen der Spalte, in der sich der Primary-Key befindet.
   * Default: "id".
   * @return Name der Spalte mit dem Primary-Key.
   */
  protected String getIDField()
  {
    return "id";
  }

  /**
   * Liefert den Namen der repraesentierenden SQL-Tabelle.
   * Muss von allen abgeleiteten Klassen implementiert werden.
   * @return Name der repraesentierenden SQL-Tabelle.
   */
  protected abstract String getTableName();

  /**
   * @see de.willuhn.datasource.GenericObject#getPrimaryAttribute()
   */
  public abstract String getPrimaryAttribute() throws RemoteException;

  /**
   * Diese Methode wird intern vor der Ausfuehrung von delete()
   * aufgerufen. Sie muss überschrieben werden, damit das Fachobjekt
   * vor dem Durchführen der Löschaktion Prüfungen vornehmen kann.
   * Z.Bsp. ob eventuell noch Abhaengigkeiten existieren und
   * das Objekt daher nicht gelöscht werden kann.
   * @throws ApplicationException wenn das Objekt nicht gelöscht werden darf.
   */
  protected abstract void deleteCheck() throws ApplicationException;

  /**
   * Diese Methode wird intern vor der Ausfuehrung von insert()
   * aufgerufen. Sie muss überschrieben werden, damit das Fachobjekt
   * vor dem Durchführen der Speicherung Prüfungen vornehmen kann.
   * Z.Bsp. ob alle Pflichtfelder ausgefüllt sind und korrekte Werte enthalten.
   * @throws ApplicationException wenn das Objekt nicht gespeichert werden darf.
   */
  protected abstract void insertCheck() throws ApplicationException;

  /**
   * Diese Methode wird intern vor der Ausfuehrung von update()
   * aufgerufen. Sie muss überschrieben werden, damit das Fachobjekt
   * vor dem Durchführen der Speicherung Prüfungen vornehmen kann.
   * Z.Bsp. ob alle Pflichtfelder ausgefüllt sind und korrekte Werte enthalten.
   * @throws ApplicationException wenn das Objekt nicht gespeichert werden darf.
   */
  protected abstract void updateCheck() throws ApplicationException;

  /**
   * Prueft, ob das angegebene Feld ein Fremschluessel zu einer
   * anderen Tabelle ist. Wenn das der Fall ist, liefert es die
   * Klasse, die die Fremd-Tabelle abbildet. Andernfalls null.
   * @param field das zu pruefende Feld.
   * @return Klasse (abgeleitet von DBObject) welche den Fremdschluessel abbildet oder null. 
   * @throws RemoteException im Fehlerfall.
   */
  protected abstract Class getForeignObject(String field) throws RemoteException;
  
  /**
   * @see de.willuhn.datasource.rmi.DBObject#transactionBegin()
   */
  public final void transactionBegin() throws RemoteException
  {
    checkConnection();

    if (this.inTransaction)
      return;

    this.inTransaction = true;
  }

  /**
   * @see de.willuhn.datasource.rmi.DBObject#transactionRollback()
   */
  public final void transactionRollback() throws RemoteException
  {
    checkConnection();

    if (!this.inTransaction)
      return;

    try {
      getConnection().rollback();
      this.inTransaction = false;
    }
    catch (SQLException e)
    {
      throw new RemoteException("rollback failed",e);
    }

  }  

  /**
   * @see de.willuhn.datasource.rmi.DBObject#transactionCommit()
   */
  public final void transactionCommit() throws RemoteException
  {
    checkConnection();

    if (!this.inTransaction)
      return;

    try {
      getConnection().commit();
      this.inTransaction = false;
    }
    catch (SQLException se)
    {
      try {
        getConnection().rollback();
        this.inTransaction = false;
        throw new RemoteException("commit failed, rollback successful",se);
      }
      catch (SQLException se2)
      {
				throw new RemoteException("commit failed, rollback failed",se2);
      }
    }
  }

  /**
   * @see de.willuhn.datasource.rmi.DBObject#getList()
   */
  public DBIterator getList() throws RemoteException
  {
    return new DBIteratorImpl(this,service);
  }

  /**
   * @see de.willuhn.datasource.rmi.DBObject#overwrite(de.willuhn.datasource.rmi.DBObject)
   */
  public void overwrite(DBObject object) throws RemoteException
  {
    if (object == null)
      return;
    if (!object.getClass().equals(this.getClass()))
      return;

    String[] attributes = getAttributeNames();
    
    for (int i=0;i<attributes.length;++i)
    {
      Class foreign = getForeignObject(attributes[i]);
      if (foreign != null)
      {
        // Fremdschluessel. Also ID holen
        DBObject fObject = (DBObject) object.getAttribute(attributes[i]);
        if (fObject == null)
          continue;
        setAttribute(attributes[i],fObject.getID());
      }
      else {
        setAttribute(attributes[i],object.getAttribute(attributes[i]));
      }
    }
  }

  /**
   * @see de.willuhn.datasource.GenericObject#equals(de.willuhn.datasource.GenericObject)
   */
  public boolean equals(GenericObject other) throws RemoteException
  {
    if (other == null)
      return false;

		DBObject o = null;
		try {
			o = (DBObject) other;
		}
		catch (ClassCastException e)
		{
			return false;
		}
    String id        = o.getID();
    String className = o.getClass().getName();

    if (id == null)
      return false;
      
    return (this.getClass().getName().equals(className)) && id.equals(this.getID());
    
  }

  /**
   * @see de.willuhn.datasource.rmi.DBObject#addDeleteListener(de.willuhn.datasource.rmi.Listener)
   */
  public synchronized void addDeleteListener(Listener l) throws RemoteException
  {
  	if (deleteListeners == null)
  		deleteListeners = new ArrayList();
  	deleteListeners.add(l);
  }

  /**
   * @see de.willuhn.datasource.rmi.DBObject#addStoreListener(de.willuhn.datasource.rmi.Listener)
   */
  public synchronized void addStoreListener(Listener l) throws RemoteException
  {
  	if (storeListeners == null)
  		storeListeners = new ArrayList();
  	storeListeners.add(l);
  }

	/**
	 * Private Hilfs-Funktion, die die Listeners der uebergebenen Liste benachrichtigt.
   * @param listeners Liste der Listeners.
   * @throws RemoteException
   */
  private synchronized void notify(ArrayList listeners) throws RemoteException
	{
		if (listeners == null)
			return;

		Event e = new Event()
    {
      public DBObject getObject() throws RemoteException
      {
        return AbstractDBObject.this;
      }
    };
		for (int i=0;i<listeners.size();++i)
		{
			((Listener) listeners.get(i)).handleEvent(e);
		}
	}
}

/*********************************************************************
 * $Log: AbstractDBObject.java,v $
 * Revision 1.24  2004/12/09 23:22:25  willuhn
 * @N getAttributeNames nun Bestandteil der API
 *
 * Revision 1.23  2004/11/12 18:21:56  willuhn
 * *** empty log message ***
 *
 * Revision 1.22  2004/11/05 19:48:24  willuhn
 * *** empty log message ***
 *
 * Revision 1.21  2004/11/05 01:50:38  willuhn
 * *** empty log message ***
 *
 * Revision 1.20  2004/10/31 18:46:42  willuhn
 * *** empty log message ***
 *
 * Revision 1.19  2004/10/25 17:58:37  willuhn
 * @N Delete/Store-Listeners
 *
 * Revision 1.18  2004/08/26 23:19:33  willuhn
 * @N added ObjectNotFoundException
 *
 * Revision 1.17  2004/08/18 23:21:38  willuhn
 * *** empty log message ***
 *
 * Revision 1.16  2004/08/18 23:14:00  willuhn
 * @D Javadoc
 *
 * Revision 1.15  2004/08/11 22:23:51  willuhn
 * @N AbstractDBObject.getLoadQuery
 *
 * Revision 1.14  2004/08/03 22:42:57  willuhn
 * *** empty log message ***
 *
 * Revision 1.13  2004/08/03 22:11:09  willuhn
 * *** empty log message ***
 *
 * Revision 1.12  2004/08/03 21:46:16  willuhn
 * @C Speichern des Primaer-Schluessels als regulaeres Feld wieder erlaubt
 *
 * Revision 1.11  2004/08/03 00:44:13  willuhn
 * *** empty log message ***
 *
 * Revision 1.10  2004/07/21 23:53:56  willuhn
 * @C massive Refactoring ;)
 *
 * Revision 1.9  2004/07/13 22:19:30  willuhn
 * @C paar Funktionsnamen umbenannt
 *
 * Revision 1.8  2004/06/30 21:58:12  willuhn
 * @N md5 check for database
 *
 * Revision 1.7  2004/06/17 00:05:51  willuhn
 * @N GenericObject, GenericIterator
 *
 * Revision 1.6  2004/06/10 20:22:40  willuhn
 * @D javadoc comments fixed
 *
 * Revision 1.5  2004/03/18 01:24:17  willuhn
 * @C refactoring
 *
 * Revision 1.4  2004/03/06 18:24:34  willuhn
 * @D javadoc
 *
 * Revision 1.3  2004/02/23 20:31:26  willuhn
 * @C refactoring in AbstractDialog
 *
 * Revision 1.2  2004/01/23 00:25:52  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/01/10 14:52:19  willuhn
 * @C package removings
 *
 * Revision 1.1  2004/01/08 20:46:43  willuhn
 * @N database stuff separated from jameica
 *
 * Revision 1.30  2004/01/04 18:48:36  willuhn
 * @N config store support
 *
 * Revision 1.29  2004/01/03 18:08:05  willuhn
 * @N Exception logging
 * @C replaced bb.util xml parser with nanoxml
 *
 * Revision 1.28  2003/12/30 02:10:57  willuhn
 * @N updateChecker
 *
 * Revision 1.27  2003/12/29 22:07:40  willuhn
 * *** empty log message ***
 *
 * Revision 1.26  2003/12/29 20:07:19  willuhn
 * @N Formatter
 *
 * Revision 1.25  2003/12/29 16:29:47  willuhn
 * @N javadoc
 *
 * Revision 1.24  2003/12/28 22:58:27  willuhn
 * @N synchronize mode
 *
 * Revision 1.23  2003/12/27 21:23:33  willuhn
 * @N object serialization
 *
 * Revision 1.22  2003/12/26 21:43:29  willuhn
 * @N customers changable
 *
 * Revision 1.21  2003/12/22 16:41:19  willuhn
 * *** empty log message ***
 *
 * Revision 1.20  2003/12/19 01:43:26  willuhn
 * @N added Tree
 *
 * Revision 1.19  2003/12/18 21:47:12  willuhn
 * @N AbstractDBObjectNode
 *
 * Revision 1.18  2003/12/16 02:27:44  willuhn
 * *** empty log message ***
 *
 * Revision 1.17  2003/12/15 19:08:01  willuhn
 * *** empty log message ***
 *
 * Revision 1.16  2003/12/13 20:05:21  willuhn
 * *** empty log message ***
 *
 * Revision 1.15  2003/12/12 21:11:29  willuhn
 * @N ObjectMetaCache
 *
 * Revision 1.14  2003/12/11 21:00:54  willuhn
 * @C refactoring
 *
 * Revision 1.13  2003/12/05 17:12:23  willuhn
 * @C SelectInput
 *
 * Revision 1.12  2003/11/30 16:23:09  willuhn
 * *** empty log message ***
 *
 * Revision 1.11  2003/11/27 00:22:18  willuhn
 * @B paar Bugfixes aus Kombination RMI + Reflection
 * @N insertCheck(), deleteCheck(), updateCheck()
 * @R AbstractDBObject#toString() da in RemoteObject ueberschrieben (RMI-Konflikt)
 *
 * Revision 1.10  2003/11/24 23:01:58  willuhn
 * @N added settings
 *
 * Revision 1.9  2003/11/24 17:27:50  willuhn
 * @N Context menu in table
 *
 * Revision 1.8  2003/11/24 16:25:53  willuhn
 * @N AbstractDBObject is now able to resolve foreign keys
 *
 * Revision 1.7  2003/11/24 14:21:53  willuhn
 * *** empty log message ***
 *
 * Revision 1.6  2003/11/22 20:43:05  willuhn
 * *** empty log message ***
 *
 * Revision 1.5  2003/11/21 02:10:21  willuhn
 * @N prepared Statements in AbstractDBObject
 * @N a lot of new SWT parts
 *
 * Revision 1.4  2003/11/20 03:48:42  willuhn
 * @N first dialogues
 *
 * Revision 1.3  2003/11/13 00:37:36  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2003/11/12 00:58:54  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2003/11/05 22:46:19  willuhn
 * *** empty log message ***
 **********************************************************************/