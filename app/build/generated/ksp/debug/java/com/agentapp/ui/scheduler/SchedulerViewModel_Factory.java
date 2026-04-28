package com.agentapp.ui.scheduler;

import android.content.Context;
import com.agentapp.data.db.ScheduledJobDao;
import com.agentapp.data.repository.SettingsRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
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
public final class SchedulerViewModel_Factory implements Factory<SchedulerViewModel> {
  private final Provider<Context> contextProvider;

  private final Provider<ScheduledJobDao> scheduledJobDaoProvider;

  private final Provider<SettingsRepository> settingsRepoProvider;

  public SchedulerViewModel_Factory(Provider<Context> contextProvider,
      Provider<ScheduledJobDao> scheduledJobDaoProvider,
      Provider<SettingsRepository> settingsRepoProvider) {
    this.contextProvider = contextProvider;
    this.scheduledJobDaoProvider = scheduledJobDaoProvider;
    this.settingsRepoProvider = settingsRepoProvider;
  }

  @Override
  public SchedulerViewModel get() {
    return newInstance(contextProvider.get(), scheduledJobDaoProvider.get(), settingsRepoProvider.get());
  }

  public static SchedulerViewModel_Factory create(Provider<Context> contextProvider,
      Provider<ScheduledJobDao> scheduledJobDaoProvider,
      Provider<SettingsRepository> settingsRepoProvider) {
    return new SchedulerViewModel_Factory(contextProvider, scheduledJobDaoProvider, settingsRepoProvider);
  }

  public static SchedulerViewModel newInstance(Context context, ScheduledJobDao scheduledJobDao,
      SettingsRepository settingsRepo) {
    return new SchedulerViewModel(context, scheduledJobDao, settingsRepo);
  }
}
