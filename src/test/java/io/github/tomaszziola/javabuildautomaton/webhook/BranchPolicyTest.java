package io.github.tomaszziola.javabuildautomaton.webhook;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import io.github.tomaszziola.javabuildautomaton.utils.BaseUnit;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class BranchPolicyTest extends BaseUnit {

  @Test
  @DisplayName("Given null ref, when checking policy, then do not trigger")
  void shouldNotTrigger_whenNullRef() {
    // when & then
    assertThat(branchPolicyImpl.isTriggerRef(null)).isFalse();
  }

  @Test
  @DisplayName("Given main/master refs, when checking policy, then trigger")
  void shouldTrigger_whenMainOrMaster() {
    // when & then
    assertThat(branchPolicyImpl.isTriggerRef(mainBranch)).isTrue();
    assertThat(branchPolicyImpl.isTriggerRef(masterBranch)).isTrue();
  }

  @Test
  @DisplayName("Given feature ref, when checking policy, then do not trigger")
  void shouldNotTrigger_whenFeatureRef() {
    // when & then
    assertThat(branchPolicyImpl.isTriggerRef("refs/heads/feature/abc")).isFalse();
  }
}
