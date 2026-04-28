package com.agentapp;

import com.agentapp.data.db.AgentDatabase;
import com.agentapp.data.db.ScheduledJobDao;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
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
public final class AppModule_ProvideScheduledJobDaoFactory implements Factory<ScheduledJobDao> {
  private final Provider<AgentDatabase> dbProvider;

  public AppModule_ProvideScheduledJobDaoFactory(Provider<AgentDatabase> dbProvider) {
    this.dbProvider = dbProvider;
  }

  @Override
  public ScheduledJobDao get() {
    return provideScheduledJobDao(dbProvider.get());
  }

  public static AppModule_ProvideScheduledJobDaoFactory create(Provider<AgentDatabase> dbProvider) {
    return new AppModule_ProvideScheduledJobDaoFactory(dbProvider);
  }

  public static ScheduledJobDao provideScheduledJobDao(AgentDatabase db) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideScheduledJobDao(db));
  }
}
