package org.sonar.plugins.stash;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.sonar.plugins.stash.utils.FormatUtils;

public class FormatUtilsTest {

  @Test
  public void testFormatDouble() {
    assertTrue(FormatUtils.formatDouble(10.90) == 10.9);
    assertTrue(FormatUtils.formatDouble(10.94) == 10.9);
    assertTrue(FormatUtils.formatDouble(10.96) == 11.0);
    assertTrue(FormatUtils.formatDouble(11.0) == 11.0);
    assertTrue(FormatUtils.formatDouble(11) == 11.0);
  }
  
}
