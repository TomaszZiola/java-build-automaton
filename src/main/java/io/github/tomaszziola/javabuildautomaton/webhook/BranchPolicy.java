package io.github.tomaszziola.javabuildautomaton.webhook;

import static java.util.Set.of;

import io.github.tomaszziola.javabuildautomaton.webhook.dto.WebhookPayloadWithHeaders;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class BranchPolicy {

  private static final String PULL_REQUEST = "pull_request";
  private static final String PUSH = "push";
  private static final Set<String> ALLOWED_REFS = of("refs/heads/main", "refs/heads/master");
  private static final Set<String> ALLOWED_BRANCHES = of("main", "master");

  public boolean isTriggerRef(WebhookPayloadWithHeaders payload) {
    var event = payload.eventType();
    var dto = payload.dto();

    if (PUSH.equals(event)) {
      var ref = dto.ref();
      return ref != null && ALLOWED_REFS.contains(ref);
    }
    if (PULL_REQUEST.equals(event)
        && dto.pullRequest() != null
        && dto.pullRequest().base() != null) {
      var baseBranch = dto.pullRequest().base().ref();
      return baseBranch != null && ALLOWED_BRANCHES.contains(baseBranch);
    }
    return false;
  }
}
