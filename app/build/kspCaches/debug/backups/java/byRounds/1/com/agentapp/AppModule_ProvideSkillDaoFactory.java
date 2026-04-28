package com.agentapp;

import com.agentapp.data.db.AgentDatabase;
import com.agentapp.data.db.SkillDao;
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
public final class AppModule_ProvideSkillDaoFactory implements Factory<SkillDao> {
  private final Provider<AgentDatabase> dbProvider;

  public AppModule_ProvideSkillDaoFactory(Provider<AgentDatabase> dbProvider) {
    this.dbProvider = dbProvider;
  }

  @Override
  public SkillDao get() {
    return provideSkillDao(dbProvider.get());
  }

  public static AppModule_ProvideSkillDaoFactory create(Provider<AgentDatabase> dbProvider) {
    return new AppModule_ProvideSkillDaoFactory(dbProvider);
  }

  public static SkillDao provideSkillDao(AgentDatabase db) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideSkillDao(db));
  }
}
