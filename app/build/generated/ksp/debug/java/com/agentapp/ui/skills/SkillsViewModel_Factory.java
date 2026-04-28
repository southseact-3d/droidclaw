package com.agentapp.ui.skills;

import com.agentapp.data.db.SkillDao;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
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
public final class SkillsViewModel_Factory implements Factory<SkillsViewModel> {
  private final Provider<SkillDao> skillDaoProvider;

  public SkillsViewModel_Factory(Provider<SkillDao> skillDaoProvider) {
    this.skillDaoProvider = skillDaoProvider;
  }

  @Override
  public SkillsViewModel get() {
    return newInstance(skillDaoProvider.get());
  }

  public static SkillsViewModel_Factory create(Provider<SkillDao> skillDaoProvider) {
    return new SkillsViewModel_Factory(skillDaoProvider);
  }

  public static SkillsViewModel newInstance(SkillDao skillDao) {
    return new SkillsViewModel(skillDao);
  }
}
