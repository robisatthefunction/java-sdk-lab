package com.optimizely.ab.servlets;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;

import java.io.IOException;

public final class ProjectConfigTestUtils {
  private ProjectConfigTestUtils() {
  }

  public static String validConfigJsonV4() throws IOException {
    return Resources.toString(Resources.getResource("config/valid-project-config-v4.json"), Charsets.UTF_8);
  }
}
