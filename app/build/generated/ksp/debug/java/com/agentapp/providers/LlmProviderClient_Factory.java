package com.agentapp.providers;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava",
    "cast",
    "deprecation"
})
public final class LlmProviderClient_Factory implements Factory<LlmProviderClient> {
  @Override
  public LlmProviderClient get() {
    return newInstance();
  }

  public static LlmProviderClient_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static LlmProviderClient newInstance() {
    return new LlmProviderClient();
  }

  private static final class InstanceHolder {
    private static final LlmProviderClient_Factory INSTANCE = new LlmProviderClient_Factory();
  }
}
