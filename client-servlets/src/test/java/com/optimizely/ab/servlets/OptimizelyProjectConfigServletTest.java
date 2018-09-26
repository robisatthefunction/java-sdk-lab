package com.optimizely.ab.servlets;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.optimizely.ab.Optimizely;
import com.optimizely.ab.client.OptimizelyClientFactory;
import com.optimizely.ab.client.OptimizelyMDCClient;
import com.optimizely.ab.client.OptimizelyRegistry;
import com.optimizely.ab.event.EventHandler;
import org.eclipse.jetty.servlet.ServletTester;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class OptimizelyProjectConfigServletTest extends AbstractServletTest {

  private Optimizely optimizely;

  @Override
  protected void setUp(ServletTester tester) {
    tester.addServlet(OptimizelyProjectConfigServlet.class, "/project-config");
    tester.getContext().setInitParameter("com.optimizely.ab.servlets.OptimizelyProjectConfigServlet.allowedOrigin", "*");

    EventHandler eventHandler = logEvent -> { };

    try {
      optimizely = Optimizely.builder(ProjectConfigTestUtils.validConfigJsonV4(), eventHandler).build();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  public void returns200() throws Exception {
    OptimizelyClientFactory.setProvider(() -> new OptimizelyMDCClient(optimizely, new OptimizelyRegistry()));

    request.setMethod("GET");
    request.setURI("/project-config");
    processRequest();

    assertEquals(200, response.getStatus());
    assertEquals("application/json", response.get("Content-Type"));
    assertEquals("*", response.get("Access-Control-Allow-Origin"));
    ObjectWriter objectWriter = new ObjectMapper().writer();
    assertEquals(objectWriter.writeValueAsString(optimizely.getProjectConfig()), response.getContent());
  }

  @Test
  public void prettyPrints() throws Exception {
    OptimizelyClientFactory.setProvider(() -> new OptimizelyMDCClient(optimizely, new OptimizelyRegistry()));

    request.setMethod("GET");
    request.setURI("/project-config?pretty=true");
    processRequest();

    assertEquals(200, response.getStatus());
    assertEquals("application/json", response.get("Content-Type"));
    assertEquals("*", response.get("Access-Control-Allow-Origin"));
    ObjectWriter objectWriter = new ObjectMapper().writerWithDefaultPrettyPrinter();
    assertEquals(objectWriter.writeValueAsString(optimizely.getProjectConfig()), response.getContent());
  }

  @Test
  public void noOptimizely() throws Exception {
    request.setMethod("GET");
    request.setURI("/project-config");
    processRequest();
    assertEquals(200, response.getStatus());
    assertEquals("application/json", response.get("Content-Type"));
    assertEquals("null", response.getContent());
  }
}