package io.github.tomaszziola.javabuildautomaton.webhook;

import static io.github.tomaszziola.javabuildautomaton.constants.FilterOrders.WEBHOOK_SIGNATURE;
import static jakarta.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static jakarta.servlet.http.HttpServletResponse.SC_NOT_FOUND;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.springframework.http.HttpMethod.POST;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.tomaszziola.javabuildautomaton.project.ProjectRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@Component
@Order(WEBHOOK_SIGNATURE)
@RequiredArgsConstructor
public class WebhookSignatureFilter extends OncePerRequestFilter {

  private static final String WEBHOOK_PATH = "/webhook";
  private static final String SIGNATURE_HEADER = "X-Hub-Signature-256";

  private final WebhookSecurityService securityService;
  private final ProjectRepository projectRepository;
  private final ObjectMapper objectMapper;

  @Override
  protected void doFilterInternal(
      @NonNull HttpServletRequest request,
      @NonNull HttpServletResponse response,
      @NonNull FilterChain filterChain) throws ServletException, IOException {

    log.info("Request method: {}, URI: {}, content-length: {}",
        request.getMethod(), request.getRequestURI(), request.getContentLength());

    if (!isWebhookRequest(request)) {
      filterChain.doFilter(request, response);
      return;
    }

    var bodyBytes = request.getInputStream().readAllBytes();
    var reReadableRequest = new CachedBodyHttpServletRequest(request, bodyBytes);

    var repositoryFullName = extractRepositoryFullName(bodyBytes);
    if (repositoryFullName == null) {
      log.warn("Could not extract repository full name from webhook payload");
      response.sendError(SC_BAD_REQUEST, "Invalid webhook payload");
      return;
    }

    var projectOpt = projectRepository.findByRepositoryFullName(repositoryFullName);
    if (projectOpt.isEmpty()) {
      log.warn("No project found for repository: {}", repositoryFullName);
      response.sendError(SC_NOT_FOUND, "Project not found");
      return;
    }

    var project = projectOpt.get();
    var webhookSecret = project.getWebhookSecret();

    if (webhookSecret == null || webhookSecret.isBlank()) {
      log.error("Project {} has no webhook secret configured", project.getId());
      response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
          "Webhook secret not configured");
      return;
    }

    var signatureHeader = reReadableRequest.getHeader(SIGNATURE_HEADER);
    if (!securityService.isSignatureValid(signatureHeader, bodyBytes, webhookSecret)) {
      log.warn("Invalid webhook signature for project: {}", repositoryFullName);
      response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid signature");
      return;
    }

    filterChain.doFilter(reReadableRequest, response);
  }

  private boolean isWebhookRequest(HttpServletRequest request) {
    return POST.matches(request.getMethod())
        && WEBHOOK_PATH.equals(request.getRequestURI());
  }

  private String extractRepositoryFullName(byte[] bodyBytes) {
    try {
      var root = objectMapper.readTree(bodyBytes);
      var repository = root.get("repository");
      if (repository != null && repository.has("full_name")) {
        return repository.get("full_name").asText();
      }
    } catch (IOException e) {
      log.error("Failed to parse webhook payload", e);
    }
    return null;
  }

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    return !isWebhookRequest(request);
  }

  private static class CachedBodyHttpServletRequest extends HttpServletRequestWrapper {
    private final byte[] cachedBody;

    CachedBodyHttpServletRequest(HttpServletRequest request, byte[] body) {
      super(request);
      this.cachedBody = body;
    }

    @Override
    public ServletInputStream getInputStream() {
      return new CachedBodyServletInputStream(cachedBody);
    }

    @Override
    public BufferedReader getReader() {
      return new BufferedReader(new InputStreamReader(new ByteArrayInputStream(cachedBody), UTF_8));
    }
  }

  private static class CachedBodyServletInputStream extends ServletInputStream {
    private final ByteArrayInputStream inputStream;

    CachedBodyServletInputStream(byte[] body) {
      this.inputStream = new ByteArrayInputStream(body);
    }

    @Override
    public boolean isFinished() {
      return inputStream.available() == 0;
    }

    @Override
    public boolean isReady() {
      return true;
    }

    @Override
    public void setReadListener(ReadListener listener) {
      throw new UnsupportedOperationException();
    }

    @Override
    public int read() {
      return inputStream.read();
    }
  }
}
