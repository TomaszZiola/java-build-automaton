package io.github.tomaszziola.javabuildautomaton.webhook;

import static java.util.Set.of;

import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class BranchPolicy {
  private static final String REF_MAIN = "refs/heads/main";
  private static final String REF_MASTER = "refs/heads/master";
  private static final Set<String> TRIGGER_REFS = of(REF_MAIN, REF_MASTER);

  public boolean isTriggerRef(final String ref) {
    return ref != null && TRIGGER_REFS.contains(ref);
  }
}
