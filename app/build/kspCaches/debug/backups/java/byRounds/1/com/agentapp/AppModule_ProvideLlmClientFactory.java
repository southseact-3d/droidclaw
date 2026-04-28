package com.agentapp;

import com.agentapp.providers.LlmProviderClient;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
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
public final class AppModule_ProvideLlmClientFactory implements Factory<LlmProviderClient> {
  @Override
  public LlmProviderClient get() {
    return provideLlmClient();
  }

  public static AppModule_ProvideLlmClientFactory create() {
    return InstanceHolder.INSTANCE;
  }

  public static LlmProviderClient provideLlmClient() {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideLlmClient());
  }

  private static final class InstanceHolder {
    private static final AppModule_ProvideLlmClientFactory INSTANCE = new AppModule_ProvideLlmClientFactory();
  }
}
