package com.agentapp;

import androidx.hilt.work.HiltWorkerFactory;
import dagger.MembersInjector;
import dagger.internal.DaggerGenerated;
import dagger.internal.InjectedFieldSignature;
import dagger.internal.QualifierMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

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
public final class AgentApplication_MembersInjector implements MembersInjector<AgentApplication> {
  private final Provider<HiltWorkerFactory> workerFactoryProvider;

  public AgentApplication_MembersInjector(Provider<HiltWorkerFactory> workerFactoryProvider) {
    this.workerFactoryProvider = workerFactoryProvider;
  }

  public static MembersInjector<AgentApplication> create(
      Provider<HiltWorkerFactory> workerFactoryProvider) {
    return new AgentApplication_MembersInjector(workerFactoryProvider);
  }

  @Override
  public void injectMembers(AgentApplication instance) {
    injectWorkerFactory(instance, workerFactoryProvider.get());
  }

  @InjectedFieldSignature("com.agentapp.AgentApplication.workerFactory")
  public static void injectWorkerFactory(AgentApplication instance,
      HiltWorkerFactory workerFactory) {
    instance.workerFactory = workerFactory;
  }
}
