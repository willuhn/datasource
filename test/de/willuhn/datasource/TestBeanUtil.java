/**********************************************************************
 *
 * Copyright (c) by Olaf Willuhn
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.datasource;

import junit.framework.Assert;

import org.junit.Test;

/**
 * Unit-Tests fuer die Bean-Utils.
 */
public class TestBeanUtil
{

  /**
   * @throws Exception
   */
  @Test
  public void test001() throws Exception
  {
    Customer c = new Customer();
    String s = (String) BeanUtil.get(c,"project.task.name");
    Assert.assertEquals("Test-Task",s);
    
    Project p = (Project) BeanUtil.get(c,"project");
    Assert.assertEquals(p.getClass(),Project.class);
    
    Task t = (Task) BeanUtil.get(c,"project.task");
    Assert.assertEquals(t.getClass(),Task.class);
    
    s = (String) BeanUtil.get(t,"name");
    Assert.assertEquals("Test-Task",s);
  }
  
  /**
   * @throws Exception
   */
  @Test
  public void test002() throws Exception
  {
    Project p = new Project();
    String s = (String) BeanUtil.get(p,"task.name");
    Assert.assertEquals("Test-Task",s);
    
    Task t = (Task) BeanUtil.get(p,"task");
    Assert.assertEquals(t.getClass(),Task.class);
  }

  /**
   * @throws Exception
   */
  @Test
  public void test003() throws Exception
  {
    Project p = new Project();
    String s = (String) BeanUtil.get(p,"name");
    Assert.assertEquals("Test-Project",s);
  }
  
  /**
   * @throws Exception
   */
  @Test
  public void test004() throws Exception
  {
    Customer c = new Customer();
    Task t = (Task) BeanUtil.get(c,"project.task");
    Assert.assertEquals(t.getClass(),Task.class);
  }

  /**
   * @throws Exception
   */
  @Test
  public void test005() throws Exception
  {
    Customer c = new Customer();
    Assert.assertNull(BeanUtil.get(c,"project.invalid"));
  }
  
  /**
   * @throws Exception
   */
  @Test
  public void test006() throws Exception
  {
    Customer c = new Customer();
    Assert.assertNull(BeanUtil.get(c,"project.invalid.name"));
  }
  

  @SuppressWarnings("javadoc")
  public class Customer
  {
    private String name = "Test-Customer";
    private Project project = new Project();
    
    public String getName()
    {
      return this.name;
    }
    
    public Project getProject()
    {
      return this.project;
    }
  }
  
  @SuppressWarnings("javadoc")
  public class Project
  {
    private String name = "Test-Project";
    private Task task = new Task();
    
    public String getName()
    {
      return this.name;
    }
    
    public Task getTask()
    {
      return this.task;
    }
  }
  
  @SuppressWarnings("javadoc")
  public class Task
  {
    private String name = "Test-Task";
    
    public String getName()
    {
      return this.name;
    }
  }
}


