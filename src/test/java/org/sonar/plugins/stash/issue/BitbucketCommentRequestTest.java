package org.sonar.plugins.stash.issue;

import org.junit.Test;
import org.mockito.Mock;
import org.sonar.plugins.stash.client.bitbucket.models.BitbucketComment;
import org.sonar.plugins.stash.client.bitbucket.models.BitbucketTask;
import org.sonar.plugins.stash.client.bitbucket.models.BitbucketUser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class BitbucketCommentRequestTest {

  @Mock
  BitbucketUser bitbucketUser = mock(BitbucketUser.class);
  
  @Test
  public void testGetLine() {
    BitbucketComment comment = new BitbucketComment(1, "message", "path", (long) 123456, bitbucketUser, 0);
    //assertEquals(comment.getLine(), 123456);
  }

  @Test
  public void testGetNoLine() {
    BitbucketComment comment = new BitbucketComment(1, "message", "path", null, bitbucketUser, 0);
    //assertEquals(comment.getLine(), 0);
  }
  
  @Test
  public void testEquals(){
    BitbucketComment comment1 = new BitbucketComment(1, "message", "path", (long) 1, bitbucketUser, 0);
    BitbucketComment comment2 = new BitbucketComment(2, "message", "path", (long) 1, bitbucketUser, 0);
    
    assertFalse(comment1.equals(comment2));
  
    BitbucketComment comment3 = new BitbucketComment(1, "message", "path", (long) 1, bitbucketUser, 0);
    assertTrue(comment1.equals(comment3));
  }
  
  @Test
  public void testAddTask() {
    BitbucketTask task1 = mock(BitbucketTask.class);
    when(task1.getId()).thenReturn((long) 1111);
    
    BitbucketTask task2 = mock(BitbucketTask.class);
    when(task2.getId()).thenReturn((long) 2222);
    
    BitbucketComment comment = new BitbucketComment(1, "message", "path", (long) 1, bitbucketUser, 0);
    assertEquals(0, comment.getTasks().size());
    
    comment.addTask(task1);
    assertEquals(1, comment.getTasks().size());
    assertTrue(comment.getTasks().get(0).getId() == 1111);
    
    comment.addTask(task2);
    assertEquals(2, comment.getTasks().size());
    assertTrue(comment.getTasks().get(0).getId() == 1111);
    assertTrue(comment.getTasks().get(1).getId() == 2222);
  }
  
  @Test
  public void testContainsNotDeletableTasks() {
    BitbucketComment comment = new BitbucketComment(1, "message", "path", (long) 1, bitbucketUser, 0);
    
    BitbucketTask task1 = mock(BitbucketTask.class);
    when(task1.isDeletable()).thenReturn(true);
    comment.addTask(task1);
    
    BitbucketTask task2 = mock(BitbucketTask.class);
    when(task2.isDeletable()).thenReturn(true);
    comment.addTask(task2);
    
    assertFalse(comment.isDeleteable());
    
    BitbucketTask task3 = mock(BitbucketTask.class);
    when(task3.isDeletable()).thenReturn(false);
    comment.addTask(task3);
    
    assertTrue(comment.isDeleteable());
  }
  
  @Test
  public void testContainsNotDeletableTasksWithoutTasks() {
    BitbucketComment comment = new BitbucketComment(1, "message", "path", (long) 1, bitbucketUser, 0);
    assertFalse(comment.isDeleteable());
  }
  
}
