package cloud.xcan.angus.idgen;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotatedTypeMetadata;

class IdGenAutoConfigurerConditionTest {

  @Test
  void coreConditionMatchesWhenEnabledTrue() {
    ConditionContext ctx = mock(ConditionContext.class);
    Environment env = mock(Environment.class);
    when(ctx.getEnvironment()).thenReturn(env);
    when(env.getProperty("xcan.idgen.enabled")).thenReturn("true");

    boolean matches = new IdGenAutoConfigurer.CoreCondition().matches(ctx,
        mock(AnnotatedTypeMetadata.class));

    assertThat(matches).isTrue();
  }

  @Test
  void coreConditionNoMatchWhenDisabledOrMissing() {
    ConditionContext ctx = mock(ConditionContext.class);
    Environment env = mock(Environment.class);
    when(ctx.getEnvironment()).thenReturn(env);
    when(env.getProperty("xcan.idgen.enabled")).thenReturn("false");

    assertThat(new IdGenAutoConfigurer.CoreCondition().matches(ctx,
        mock(AnnotatedTypeMetadata.class))).isFalse();

    when(env.getProperty("xcan.idgen.enabled")).thenReturn(null);
    assertThat(new IdGenAutoConfigurer.CoreCondition().matches(ctx,
        mock(AnnotatedTypeMetadata.class))).isFalse();
  }
}
