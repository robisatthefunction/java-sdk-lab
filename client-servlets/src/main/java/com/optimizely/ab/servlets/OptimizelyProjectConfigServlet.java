package com.optimizely.ab.servlets;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.google.common.base.Supplier;
import com.optimizely.ab.Optimizely;
import com.optimizely.ab.client.OptimizelyClient;
import com.optimizely.ab.client.OptimizelyClientFactory;
import com.optimizely.ab.config.ProjectConfig;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;

import static com.google.common.base.Preconditions.checkNotNull;

public class OptimizelyProjectConfigServlet extends HttpServlet {
  public static final String ALLOWED_ORIGIN = OptimizelyProjectConfigServlet.class.getCanonicalName() + ".allowedOrigin";
  private static final String CONTENT_TYPE = "application/json";
  private static final long serialVersionUID = -6784199093741714438L;

  protected transient ObjectMapper mapper;
  protected transient Supplier<Optimizely> optimizelySupplier;
  protected String allowedOrigin;

  public OptimizelyProjectConfigServlet() {
  }

  public OptimizelyProjectConfigServlet(Supplier<Optimizely> optimizelySupplier) {
    this.optimizelySupplier = checkNotNull(optimizelySupplier);
  }

  @Override
  public void init() {
    this.allowedOrigin = getServletContext().getInitParameter(ALLOWED_ORIGIN);
    this.mapper = new ObjectMapper();

    if (optimizelySupplier == null) {
      optimizelySupplier = () -> {
        OptimizelyClient optimizelyClient = OptimizelyClientFactory.getClient();
        return optimizelyClient != null ? optimizelyClient.getOptimizely() : null;
      };
    }
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    resp.setContentType(CONTENT_TYPE);
    if (allowedOrigin != null) {
      resp.setHeader("Access-Control-Allow-Origin", allowedOrigin);
    }
    resp.setHeader("Cache-Control", "must-revalidate,no-cache,no-store");
    resp.setStatus(HttpServletResponse.SC_OK);

    Optimizely optimizely = optimizelySupplier.get();

    ProjectConfig projectConfig = null;
    if (optimizely != null) {
      projectConfig = optimizely.getProjectConfig();
    }

    try (OutputStream output = resp.getOutputStream()) {
      getWriter(req).writeValue(output, projectConfig);
    }
  }

  protected ObjectWriter getWriter(HttpServletRequest request) {
    final boolean prettyPrint = Boolean.parseBoolean(request.getParameter("pretty"));
    if (prettyPrint) {
      return mapper.writerWithDefaultPrettyPrinter();
    }
    return mapper.writer();
  }
}
