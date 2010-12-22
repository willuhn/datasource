/**********************************************************************
 * $Source: /cvsroot/jameica/datasource/src/de/willuhn/datasource/db/AbstractDBObject.java,v $
 * $Revision: 1.71 $
 * $Date: 2010/12/22 11:16:04 $
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import de.willuhn.datasource.GenericObject;
import de.willuhn.datasource.db.types.Type;
import de.willuhn.datasource.db.types.TypeRegistry;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.datasource.rmi.DBObject;
import de.willuhn.datasource.rmi.Event;
import de.willuhn.datasource.rmi.Listener;
import de.willuhn.datasource.rmi.ObjectNotFoundException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.Session;

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
  
  // Backup der Eigenschaften des Objektes, um Aenderungen zu ueberwachen
  private HashMap origProperties = new HashMap();

  // Haelt die Datentypen der Properties.
  private HashMap types      = new HashMap();

  // definiert, ob das Objekt gerade in einer manuellen Transaktion ist
  private transient static Session transactions = new Session(5 * 60 * 1000l); // 5 Minuten

  // ein Cache fuer ForeignObjects
  private HashMap foreignObjectCache = new HashMap();

	private transient DBServiceImpl service = null;
	private transient Connection conn = null;

	private transient ArrayList deleteListeners = null;
	private transient ArrayList storeListeners  = null;


  private boolean upper = false;

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
  protected void setService(DBServiceImpl service) throws Exception
  {
  	this.service = service;
  	conn = service.getConnection();
    if (conn == null)
      throw new SQLException("connection is null");
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
  protected void init() throws SQLException
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
    
    // Checken, ob die Datenbank Uppercase ist
    this.upper = Boolean.getBoolean(getService().getClass().getName() + ".uppercase");

    HashMap cachedMeta = ObjectMetaCache.getMetaData(getService().getClass(),this.getClass());

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
    if (this.upper)
      tableName = tableName.toUpperCase();
    
		ResultSet meta = null;
		try {
		  
		  String schema = System.getProperty(getService().getClass().getName() + ".schema",null); // BUGZILLA 960
			meta = getConnection().getMetaData().getColumns(null,schema,tableName,null);
			String field;
      if (!meta.next())
        throw new SQLException("unable to determine meta data for table " + tableName);
      
			do
			{
				field = meta.getString("COLUMN_NAME");
				if (field == null || field.equalsIgnoreCase(this.getIDField())) // skip empty fields and primary key
					continue;
				properties.put(this.upper ? field.toLowerCase() : field,null);
        types.put(this.upper ? field.toLowerCase() : field,meta.getString("TYPE_NAME"));
			}
      while (meta.next());
      ObjectMetaCache.setMetaData(getService().getClass(),this.getClass(),types);
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
   * @return true, wenn es initialisiert ist.
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
   * Prueft, ob wir gerade in einer Transaktion sind.
   * @return true, wenn wir in einer Transaktion sind.
   */
  private boolean inTransaction()
  {
    synchronized(transactions)
    {
      Transaction t = (Transaction) getTransaction();
      return (t != null && t.count > 0);
    }
  }
  
  /**
   * Liefert die aktuelle Transaktion oder null.
   * @return Transaktion oder null.
   */
  private Transaction getTransaction()
  {
    synchronized(transactions)
    {
      return (Transaction) transactions.get(getConnection());
    }
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
    if (this.upper)
      tableName = tableName.toUpperCase();
    
		Statement stmt = null;
		ResultSet data = null;
		try {
			stmt = getConnection().createStatement();
      String load = getLoadQuery();
      Logger.debug("executing query: " + load);
      data = stmt.executeQuery(load);
			if (!data.next())
      {
      	throw new ObjectNotFoundException("object [id: " + id + ", type: " + this.getClass().getName() + "] not found");
      }
			fill(data);
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
   * Fuellt das Objekt mit den Daten aus dem Resultset.
   * @param rs
   * @throws SQLException
   * @throws RemoteException
   */
  void fill(ResultSet rs) throws SQLException, RemoteException
  {
    String[] attributes = getAttributeNames();
    for (int i=0;i<attributes.length;++i)
    {
      Type t = TypeRegistry.getType((String) types.get(attributes[i]));
      setAttribute(attributes[i],t.get(rs,this.upper ? attributes[i].toUpperCase() : attributes[i]));
    }
    // Jetzt kopieren wir noch die Eigenschaften in die Backup-Tabelle um Aenderungen ueberwachen zu koennen
    this.origProperties.putAll(this.properties);
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

		Statement stmt = null;
    try {
    	stmt = getConnection().createStatement();
      String sql = null;

      String tableName = getTableName();
      String idField   = getIDField();
      
      if (this.upper)
      {
        tableName = tableName.toUpperCase();
        idField   = idField.toUpperCase();
      }

      try {
        sql = "delete from " + tableName + " where " + idField + " = "+Integer.parseInt(id);
      }
      catch (NumberFormatException e)
      {
        sql = "delete from " + tableName + " where " + idField + " = '"+id+"'";
      }
      int count = stmt.executeUpdate(sql);
      if (count != 1)
        throw new SQLException("delete failed, executeUpdate returned " + count);
      if (!this.inTransaction())
      {
				getConnection().commit();
      }

      // Wir benachrichtigen die Listeners.
      notify(deleteListeners);

			this.id = null;
    }
    catch (SQLException e)
    {
      if (!this.inTransaction()) {
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
   */
  public final void setID(String id)
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

      return null;
    }

    // wir checken erstmal, ob es sich um ein Objekt aus einer Fremdtabelle
    // handelt. Wenn das der Fall ist, liefern wir das statt der
    // lokalen ID aus.
    // "o" kann auch vom Typ DBObject sein. Naemlich dann, wenn es noch nicht
    // in der Datenbank existiert. Weil dann setAttribute(String,Object) nicht
    // etwa die ID des Objektes in "properties" speichert sondern das Objekt
    // selbst. Die Faelle fischen wir mit dem "instanceof" raus
    Class foreign = getForeignObject(fieldName);
    if (foreign != null && !(o instanceof DBObject))
    {
      DBObject cachedObject = (DBObject) foreignObjectCache.get(foreign.getName() + fieldName);
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
					foreignObjectCache.put(foreign.getName() + fieldName,cachedObject);
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
   * Prueft, ob das Objekt seit dem Laden geaendert wurde.
   * @return true, wenn es geaendert wurde.
   */
  protected boolean hasChanged()
  {
    return !this.properties.equals(this.origProperties);
  }

  /**
   * Prueft, ob sich der Wert des genannten Attributs seit dem Laden geaendert hat.
   * @param attribute Name des Attributes.
   * @return true, wenn es sich geaendert hat.
   */
  protected boolean hasChanged(String attribute)
  {
    Object o = this.origProperties.get(attribute);
    Object n = this.properties.get(attribute);
    if ((o == null && n != null) || (o != null && n == null))
      return true; // einer der beiden Werte ist jetzt null.

    if (o == null && n == null)
      return false; // immer noch leer

    return !o.equals(n);
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
  public Object setAttribute(String fieldName, Object value) throws RemoteException
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
   * @return die letzte ID.
   * @throws SQLException
   * @throws RemoteException
   */
  private String getLastId() throws SQLException, RemoteException
	{
    checkConnection();

		Statement stmt = null;
		ResultSet rs = null;
		try {

      String tableName = getTableName();
      String idField   = getIDField();
      if (this.upper)
      {
        tableName = tableName.toUpperCase();
        idField   = idField.toUpperCase();
      }
      
			stmt = getConnection().createStatement();
			rs = stmt.executeQuery("select max(" + idField + ") from " + tableName);
			if (!rs.next())
        throw new SQLException("select max(id) returned empty resultset");
			return rs.getString(1);
		}
		finally
		{
		  if (rs != null) {
		    try {
		      rs.close();
		    } catch (SQLException se) {/*useless*/}
		  }
      if (stmt != null) {
        try {
          stmt.close();
        } catch (SQLException se) {/*useless*/}
      }
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
		ResultSet rs           = null;
    try {
      stmt = getInsertSQL();
      stmt.executeUpdate();
      
      // Wenn wir noch keine ID haben (das ist immer dann der Fall, wenn
      // wir sie nicht explizit vor dem Insert angegeben haben - also der
      // Normalfall), dann holen wir sie uns
      if (this.id == null)
      {
        try
        {
          rs = stmt.getGeneratedKeys();
          if (rs.next())
            this.id = rs.getString(1);
        }
        catch (SQLException e)
        {
          // Das darf passieren, wenn die Datenbank das nicht unterstuetzt
          // In dem Fall greifen dann die folgenden Zeilen mit getLastId()
        }
      }
      
      // Es kann sein, dass der Treiber "Statement.RETURN_GENERATED_KEYS"
      // nicht unterstuetzt. In dem Fall muessen wir uns die ID selbst
      // holen.
      if (this.id == null)
        this.id = getLastId();
      
      if (!this.inTransaction())
  			getConnection().commit();
      
			notify(storeListeners);
      this.created = true;
    }
    catch (SQLException e)
    {
      this.id = null; // Der Datensatz gilt als nicht gespeichert

      if (!this.inTransaction()) {
        try {
          getConnection().rollback();
          throw new RemoteException("insert failed, rollback successful",e);
        }
        catch (SQLException e2)
        {
					throw new RemoteException("insert failed, rollback failed",e2);
        }
      }
			throw new RemoteException("insert failed",e);
    }
		finally
		{
      if (rs != null) {
        try {
          rs.close();
        } catch (SQLException se) {/*useless*/}
      }
      if (stmt != null) {
        try {
          stmt.close();
        } catch (SQLException se) {/*useless*/}
      }
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
      if (stmt == null)
        return;
      affected = stmt.executeUpdate();
      if (affected != 1)
      {
        // Wenn nicht genau ein Datensatz geaendert wurde, ist was faul.
        throw new SQLException("update ambiguous");
      }
      if (!this.inTransaction())
        getConnection().commit();
      notify(storeListeners);
      this.origProperties.putAll(this.properties);
    }
    catch (SQLException e)
    {
      if (!this.inTransaction()) {
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
        if (stmt != null)
          stmt.close();
			} catch (SQLException se) {/*useless*/}
		}
    
  }

  /**
   * Liefert das automatisch erzeugte SQL-Statement fuer ein Update.
   * Kann bei Bedarf �berschrieben um ein vom dynamisch erzeugten
   * abweichendes Statement f�r die Speicherung zu verwenden.
   * Die Funktion darf <null> zurueckliefern, wenn nichts zu aendern ist.  
   * @return das erzeugte SQL-Statement.
   * @throws RemoteException wenn beim Erzugen des Statements ein Fehler auftrat.
   */
  protected PreparedStatement getUpdateSQL() throws RemoteException
  {
    checkConnection();

    String tableName = getTableName();
    String idField   = getIDField();

    if (this.upper)
    {
      tableName = tableName.toUpperCase();
      idField   = idField.toUpperCase();
    }
    
    String sql = "update " + tableName + " set ";
    String[] attributes = getAttributeNames();

    int count = 0;
    for (int i=0;i<attributes.length;++i)
    {
			if (attributes[i].equalsIgnoreCase(idField))
				continue; // skip the id field
      if (!hasChanged(attributes[i]))
        continue; // wurde nicht geaendert
      if (this.upper)
        sql += attributes[i].toUpperCase() + "=?,";
      else
        sql += attributes[i] + "=?,";
      count++;
    }
    if (count == 0)
    {
      Logger.debug("nothing changed in this object, skipping update");
      return null;
    }
    sql = sql.substring(0,sql.length()-1); // remove last ","
    try {
      sql += " where " + idField + "=" + Integer.parseInt(getID());
    }
    catch (NumberFormatException e)
    {
      sql += " where " + idField + "='"+getID()+"'";
    }
    try {
      PreparedStatement stmt = getConnection().prepareStatement(sql);
      count = 0;
      for (int i=0;i<attributes.length;++i)
      {
        if (attributes[i].equalsIgnoreCase(idField))
          continue; // skip the id field
        if (!hasChanged(attributes[i]))
          continue; // wurde nicht geaendert
        String type  = (String) types.get(attributes[i]);
        Object value = properties.get(attributes[i]);
        setStmtValue(stmt,count++,type,value);
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
   * Kann bei Bedarf ueberschrieben um ein vom dynamisch erzeugten
   * abweichendes Statement fuer die Speicherung zu verwenden.  
   * @return das erzeugte SQL-Statement.
   * @throws RemoteException Wenn beim Erzeugen des Statements ein Fehler auftrat.
   */
  protected PreparedStatement getInsertSQL() throws RemoteException
  {
    checkConnection();

    String[] attributes = getAttributeNames();

    StringBuffer names = new StringBuffer();
    StringBuffer values = new StringBuffer();

    names.append("(");
    values.append(" values (");

    for (int i=0;i<attributes.length;++i)
    {
      if (attributes[i] == null || attributes[i].length() == 0) // die sollte es zwar eigentlich nicht geben, aber sicher ist sicher ;)
        continue; // skip empty fields

      names.append(this.upper ? attributes[i].toUpperCase() : attributes[i]);
      values.append('?');

      // Beim letzten lassen wir die Kommas weg
      if (i+1 < attributes.length)
      {
        names.append(',');
        values.append(',');
      }
    }
    
    // Wenn das Objekt eine ID hat, dann haengen wir sie an's Insert-Statement mit dran.
    this.id = getID();

    // Wenn wir noch keine ID haben, aber eine erstellen sollen, dann tun wir das jetzt
    // Wir passen aber auf, dass wir eine ggf. vorhandene nicht ueberschreiben
    if (this.id == null && getService().getInsertWithID())
      this.id = createID();

    // Haben wir eine ID?
    // Wenn ja, dann haengen wirs ans Statement
    if (this.id != null)
    {
      names.append(',');
      names.append(this.upper ? getIDField().toUpperCase() : getIDField());

      values.append(',');
      try {
        values.append(Integer.parseInt(this.id));
      }
      catch (NumberFormatException e)
      {
        // Keine Zahl, also quoten wir es
        values.append('\'');
        values.append(this.id);
        values.append('\'');
      }
    }

    names.append(')');
    values.append(')');

    try {
      StringBuffer sql = new StringBuffer();
      sql.append("insert into ");
      sql.append(this.upper ? getTableName().toUpperCase() : getTableName());
      sql.append(' ');
      sql.append(names.toString());
      sql.append(values.toString());

      PreparedStatement stmt = getConnection().prepareStatement(sql.toString(),Statement.RETURN_GENERATED_KEYS);
      for (int i=0;i<attributes.length;++i)
      {
        if (attributes[i] == null || attributes[i].length() == 0) // die sollte es zwar eigentlich nicht geben, aber sicher ist sicher ;)
          continue; // skip empty fields
        
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
      String tableName = getTableName();
      String idField   = getIDField();
      if (this.upper)
      {
        tableName = tableName.toUpperCase();
        idField   = idField.toUpperCase();
      }
      
      stmt = getConnection().createStatement();
      rs = stmt.executeQuery("select (max(" + idField + ") + 1) from " + tableName);
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
      if (rs != null) {
        try {
          rs.close();
        } catch (SQLException se) {/*useless*/}
      }
      if (stmt != null) {
        try {
          stmt.close();
        } catch (SQLException se) {/*useless*/}
      }
    }
  }

  /**
   * Liefert das automatisch erzeugte SQL-Statement fuer die Erzeugung einer Liste
   * dieses Typs.
   * ACHTUNG: Das Statement muss ein Feld mit der Bezeichnung zurueckgeben,
   * die <code>getIDField</code> auch liefert, da das von DBIteratorImpl gelesen wird.
   * Also z.Bsp. "select " + getIDField() + " from " + getTableName().
   * Kann bei Bedarf �berschrieben um ein abweichendes Statement zu verwenden.
   * Die Funktion muss das Statement nur dewegen als String zurueckliefern,
   * weil es typischerweise von DBIterator weiterverwendet wird und dort eventuell
   * noch weitere Filterkriterien hinzugefuegt werden koennen muessen.  
   * @return das erzeugte SQL-Statement.
   */
  protected String getListQuery()
  {
    // return "select " + getIDField() + " from " + getTableName();
    String tableName = getTableName();
    if (this.upper)
      tableName = tableName.toUpperCase();
    
    return "select " + tableName + ".* from " + tableName;
  }

	/**
	 * Liefert das automatisch erzeugte SQL-Statement zum Laden des Objektes.
	 * Hierbei werden die Eigenschaften des Objektes geladen, dessen ID aktuell
	 * von <code>getID()</code> geliefert wird.
	 * ACHTUNG: Das Statement muss alle Felder selecten (*).
	 * Also z.Bsp. "select * from " + getTableName() + " where " + getIDField() + " = " + getID();
	 * Kann bei Bedarf �berschrieben um ein abweichendes Statement zu verwenden.
	 * @return das erzeugte SQL-Statement.
	 * @throws RemoteException Wenn beim Erzeugen des Statements ein Fehler auftrat.
	 */
	protected String getLoadQuery() throws RemoteException
	{
    String tableName = getTableName();
    String idField   = getIDField();
    if (this.upper)
    {
      tableName = tableName.toUpperCase();
      idField   = idField.toUpperCase();
    }
    
		try {
			return "select * from " + tableName + " where " + idField + " = "+Integer.parseInt(this.getID());
		}
		catch (NumberFormatException e)
		{
			return "select * from " + tableName + " where " + idField + " = '"+this.getID()+"'";
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
   * @throws SQLException
   */
  private void setStmtValue(PreparedStatement stmt, int index, String type, Object value) throws SQLException
  {
    index++;  // Wer zur Hoelle hat sich ausgedacht, dass Arrays bei Index 0, PreparedStatements aber bei 1 anfangen?? Grr
    Type t = TypeRegistry.getType(type);
    t.set(stmt,index,value);
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
    return upper ? "ID" : "id";
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
   * aufgerufen. Sie muss �berschrieben werden, damit das Fachobjekt
   * vor dem Durchf�hren der L�schaktion Pr�fungen vornehmen kann.
   * Z.Bsp. ob eventuell noch Abhaengigkeiten existieren und
   * das Objekt daher nicht gel�scht werden kann.
   * @throws ApplicationException wenn das Objekt nicht gel�scht werden darf.
   */
  protected void deleteCheck() throws ApplicationException
  {
  }

  /**
   * Diese Methode wird intern vor der Ausfuehrung von insert()
   * aufgerufen. Sie muss �berschrieben werden, damit das Fachobjekt
   * vor dem Durchf�hren der Speicherung Pr�fungen vornehmen kann.
   * Z.Bsp. ob alle Pflichtfelder ausgef�llt sind und korrekte Werte enthalten.
   * @throws ApplicationException wenn das Objekt nicht gespeichert werden darf.
   */
  protected void insertCheck() throws ApplicationException
  {
  }

  /**
   * Diese Methode wird intern vor der Ausfuehrung von update()
   * aufgerufen. Sie muss �berschrieben werden, damit das Fachobjekt
   * vor dem Durchf�hren der Speicherung Pr�fungen vornehmen kann.
   * Z.Bsp. ob alle Pflichtfelder ausgef�llt sind und korrekte Werte enthalten.
   * @throws ApplicationException wenn das Objekt nicht gespeichert werden darf.
   */
  protected void updateCheck() throws ApplicationException
  {
  }

  /**
   * Prueft, ob das angegebene Feld ein Fremschluessel zu einer
   * anderen Tabelle ist. Wenn das der Fall ist, liefert es die
   * Klasse, die die Fremd-Tabelle abbildet. Andernfalls null.
   * @param field das zu pruefende Feld.
   * @return Klasse (abgeleitet von DBObject) welche den Fremdschluessel abbildet oder null. 
   * @throws RemoteException im Fehlerfall.
   */
  protected Class getForeignObject(String field) throws RemoteException
  {
    return null;
  }
  
  /**
   * @see de.willuhn.datasource.rmi.DBObject#transactionBegin()
   */
  public final void transactionBegin() throws RemoteException
  {
    synchronized(transactions)
    {
      checkConnection();

      Transaction tr = getTransaction();
      if (tr == null)
        tr = new Transaction();
      tr.count++;
      if (tr.count > 5)
        Logger.warn("[begin] transaction count: " + tr.count + " - forgotten to rollback/commit?");
      
      Logger.debug("[begin] transaction count: " + tr.count);
    }
  }
  
  private boolean created = false;

  /**
   * @see de.willuhn.datasource.rmi.DBObject#transactionRollback()
   */
  public final void transactionRollback() throws RemoteException
  {
    synchronized(transactions)
    {
      // Erkennt, ob das rollback nach einem Insert ausgefuehrt wurde.
      // Ist das der Fall, muss das Member mit der ID geloescht werden,
      // denn es existiert ja nicht in der DB.
      if (created)
      {
        this.id = null;
        this.created = false;
      }

      if (!this.inTransaction())
      {
        Logger.debug("[rollback] rollback without begin or transaction allready rolled back");
        return;
      }
      
      checkConnection();

      Transaction tr = getTransaction();
      if (tr == null)
      {
        Logger.debug("[rollback] rollback called, but no transaction found");
        return;
      }

      tr.count--;
      Logger.debug("[rollback] transaction count: " + tr.count);

      if (tr.count > 0)
        return;

      try {
        Logger.debug("[rollback] transaction rollback");
        getConnection().rollback();
      }
      catch (SQLException e)
      {
        throw new RemoteException("rollback failed",e);
      }
    }
  }  

  /**
   * @see de.willuhn.datasource.rmi.DBObject#transactionCommit()
   */
  public final void transactionCommit() throws RemoteException
  {
    synchronized(transactions)
    {
      if (!this.inTransaction())
      {
        Logger.debug("[commit] transaction commit without begin or transaction allready commited, skipping");
        return;
      }

      checkConnection();

      Transaction tr = getTransaction();
      if (tr == null)
      {
        Logger.debug("[commit] commit called, but no transaction found");
        return;
      }
      
      tr.count--;
      Logger.debug("[commit] transaction count: " + tr.count);

      if (tr.count > 0)
        return;

      try {
        Logger.debug("[commit] transaction commit");
        getConnection().commit();
      }
      catch (SQLException se)
      {
        try {
          getConnection().rollback();
          throw new RemoteException("commit failed, rollback successful",se);
        }
        catch (SQLException se2)
        {
          throw new RemoteException("commit failed, rollback failed",se2);
        }
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
   * @see de.willuhn.datasource.rmi.DBObject#removeDeleteListener(de.willuhn.datasource.rmi.Listener)
   */
  public void removeDeleteListener(Listener l) throws RemoteException
  {
    if (deleteListeners == null)
      return;
    deleteListeners.remove(l);
  }

  /**
   * @see de.willuhn.datasource.rmi.DBObject#removeStoreListener(de.willuhn.datasource.rmi.Listener)
   */
  public void removeStoreListener(Listener l) throws RemoteException
  {
    if (storeListeners == null)
      return;
    storeListeners.remove(l);
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
  
  private class Transaction
  {
    private int count = 0;
    private Connection myConn = null;
    
    private Transaction()
    {
      myConn = getConnection();
      transactions.put(myConn,this);
    }
  }
}

/*********************************************************************
 * $Log: AbstractDBObject.java,v $
 * Revision 1.71  2010/12/22 11:16:04  willuhn
 * @B BUGZILLA 960
 *
 * Revision 1.70  2010-11-24 12:39:34  willuhn
 * @R SQLFeatureNotSupportedException gibts erst in Java 1.6
 *
 * Revision 1.69  2010-11-24 12:38:48  willuhn
 * @N SQLFeatureNotSupportedException fangen
 *
 * Revision 1.68  2010-11-24 12:32:28  willuhn
 * @N Erzeugte ID eines neuen Datensatz beim Insert direkt ueber die JDBC-API holen (via Statement.RETURN_GENERATED_KEYS und stmt.getGeneratedKeys())
 *
 * Revision 1.67  2010-10-24 22:00:25  willuhn
 * @R UNDO - das sollte nicht geaendert werden duerfen, weil die Funktion die Parameter fuer die SQL-Queries liefert. Das Aendern der Attribut-Namen wuerde zu ungueltigen Statements fuehren.
 *
 * Revision 1.66  2010-10-24 21:50:21  willuhn
 * @C getAttributeNames() nicht mehr final
 *
 * Revision 1.65  2010-08-29 22:09:20  willuhn
 * *** empty log message ***
 *
 * Revision 1.64  2010/05/04 10:38:14  willuhn
 * @N rudimentaere Joins
 *
 * Revision 1.63  2008/07/11 09:30:17  willuhn
 * @N Support fuer Byte-Arrays
 * @N SQL-Typen sind jetzt erweiterbar
 *
 * Revision 1.62  2008/02/08 00:26:51  willuhn
 * @R temporaeres UNDO
 *
 * Revision 1.57  2008/01/22 12:03:09  willuhn
 * @N Objekt-Serializer/-Deserializer fuer XML-Format
 *
 * Revision 1.56  2008/01/04 23:51:07  willuhn
 * @R Debug-Ausgabe entfernt
 *
 * Revision 1.55  2007/10/18 10:24:46  willuhn
 * @B Foreign-Objects in AbstractDBObject auch dann korrekt behandeln, wenn sie noch nicht gespeichert wurden
 * @C Beim Abrufen der Dauerauftraege nicht mehr nach Konten suchen sondern hart dem Konto zuweisen, ueber das sie abgerufen wurden
 *
 * Revision 1.54  2007/10/05 15:16:27  willuhn
 * @N Objekt-Metadaten pro Service speichern
 *
 * Revision 1.53  2007/08/23 13:05:21  willuhn
 * @C changed log level
 *
 * Revision 1.52  2007/08/23 12:51:40  willuhn
 * @C Uppercase-Verhalten nicht global sondern pro DBService konfigurierbar. Verhindert Fehler, wenn mehrere Plugins installiert sind
 *
 * Revision 1.51  2007/06/25 11:12:09  willuhn
 * @N Durch Aktivierung des System-Property "de.willuhn.datasource.db.uppercase" werden nun auch Datenbanken unterstuetzt, die Identifier in Uppercase umwandeln
 *
 * Revision 1.50  2007/06/22 17:46:34  willuhn
 * *** empty log message ***
 *
 * Revision 1.49  2007/06/22 17:42:54  willuhn
 * *** empty log message ***
 *
 * Revision 1.48  2007/06/14 16:32:51  willuhn
 * @C throw sql exception if meta data could not be loaded
 *
 * Revision 1.47  2007/06/04 15:48:22  willuhn
 * @B "DATETIME" muss mit setTimestamp statt setDate gesetzt werden
 *
 * Revision 1.46  2007/04/24 19:09:15  willuhn
 * @B typo
 *
 * Revision 1.45  2007/03/02 15:25:03  willuhn
 * @N getInsertWithID um festlegen zu koennen, ob INSERTs mit ID erzeugt werden sollen
 * @C last_insert_id() nur aufrufen, wenn nach dem INSERT noch keine ID vorhanden ist
 *
 * Revision 1.44  2007/01/29 10:55:42  willuhn
 * @N Check der geloeschten Datensaetze
 *
 * Revision 1.43  2007/01/12 14:31:39  willuhn
 * @N made metadata methods public
 **********************************************************************/