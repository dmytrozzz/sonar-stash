package org.sonar.plugins.stash.issue;


import org.junit.Before;
import org.junit.Test;
import org.sonar.plugins.stash.client.bitbucket.models.BitbucketTask;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class BitbucketTaskTest {

  BitbucketTask myTask;
  
  @Before
  public void setUp(){
    myTask = new BitbucketTask((long) 1111, "Text", "State", true);
  }
  
  @Test
  public void testGetId() {
    assertEquals(1111, (long) myTask.getId());
  }

  @Test
  public void testGetState() {
    assertEquals("State", myTask.getState());
  }

  @Test
  public void testGetText() {
    assertEquals("Text", myTask.getText());
  }

  @Test
  public void testIsDeletable() {
    assertTrue(myTask.isDeletable());
  }
}
