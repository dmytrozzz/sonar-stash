package org.sonar.plugins.stash.issue;

import org.junit.Before;
import org.junit.Test;
import org.sonar.plugins.stash.client.bitbucket.models.BitbucketComment;
import org.sonar.plugins.stash.client.bitbucket.models.BitbucketDiff;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class BitbucketDiffTest {
  
  BitbucketDiff diff1;
  BitbucketDiff diff2;
  BitbucketDiff diff3;

  @Before
  public void setUp(){
    BitbucketComment comment1 = mock(BitbucketComment.class);
    when(comment1.getId()).thenReturn((long) 12345);
    
    BitbucketComment comment2 = mock(BitbucketComment.class);
    when(comment2.getId()).thenReturn((long) 54321);
    
//    diff1 = new BitbucketDiff(BitbucketPlugin.CONTEXT_TYPE, "path/to/diff1", (long) 10, (long) 20);
//    diff1.addComment(comment1);
//
//    diff2 = new BitbucketDiff(BitbucketPlugin.ADDED_TYPE, "path/to/diff2", (long) 20, (long) 30);
//    diff2.addComment(comment2);
//
//    diff3 = new BitbucketDiff(BitbucketPlugin.CONTEXT_TYPE, "path/to/diff3", (long) 30, (long) 40);
  }
  
  @Test
  public void testIsTypeOfContext(){
     //assertTrue(diff1.isTypeOfContext());
    //assertFalse(diff2.isTypeOfContext());
  }
  
  @Test
  public void testContainsComment(){
    assertTrue(diff1.containsComment(12345));
    assertFalse(diff1.containsComment(54321));
    assertFalse(diff3.containsComment(12345));
  }

}
