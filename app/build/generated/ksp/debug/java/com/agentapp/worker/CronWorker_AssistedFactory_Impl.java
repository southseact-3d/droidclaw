package com.agentapp.worker;

import android.content.Context;
import androidx.work.WorkerParameters;
import dagger.internal.DaggerGenerated;
import dagger.internal.InstanceFactory;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

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
public final class CronWorker_AssistedFactory_Impl implements CronWorker_AssistedFactory {
  private final CronWorker_Factory delegateFactory;

  CronWorker_AssistedFactory_Impl(CronWorker_Factory delegateFactory) {
    this.delegateFactory = delegateFactory;
  }

  @Override
  public CronWorker create(Context p0, WorkerParameters p1) {
    return delegateFactory.get(p0, p1);
  }

  public static Provider<CronWorker_AssistedFactory> create(CronWorker_Factory delegateFactory) {
    return InstanceFactory.create(new CronWorker_AssistedFactory_Impl(delegateFactory));
  }

  public static dagger.internal.Provider<CronWorker_AssistedFactory> createFactoryProvider(
      CronWorker_Factory delegateFactory) {
    return InstanceFactory.create(new CronWorker_AssistedFactory_Impl(delegateFactory));
  }
}
