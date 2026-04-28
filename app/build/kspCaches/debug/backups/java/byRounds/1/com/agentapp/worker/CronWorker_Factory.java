package com.agentapp.worker;

import android.content.Context;
import androidx.work.WorkerParameters;
import com.agentapp.agent.AgentCore;
import com.agentapp.data.db.ScheduledJobDao;
import com.agentapp.data.repository.SettingsRepository;
import dagger.internal.DaggerGenerated;
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
public final class CronWorker_Factory {
  private final Provider<AgentCore> agentCoreProvider;

  private final Provider<ScheduledJobDao> scheduledJobDaoProvider;

  private final Provider<SettingsRepository> settingsRepoProvider;

  public CronWorker_Factory(Provider<AgentCore> agentCoreProvider,
      Provider<ScheduledJobDao> scheduledJobDaoProvider,
      Provider<SettingsRepository> settingsRepoProvider) {
    this.agentCoreProvider = agentCoreProvider;
    this.scheduledJobDaoProvider = scheduledJobDaoProvider;
    this.settingsRepoProvider = settingsRepoProvider;
  }

  public CronWorker get(Context context, WorkerParameters workerParams) {
    return newInstance(context, workerParams, agentCoreProvider.get(), scheduledJobDaoProvider.get(), settingsRepoProvider.get());
  }

  public static CronWorker_Factory create(Provider<AgentCore> agentCoreProvider,
      Provider<ScheduledJobDao> scheduledJobDaoProvider,
      Provider<SettingsRepository> settingsRepoProvider) {
    return new CronWorker_Factory(agentCoreProvider, scheduledJobDaoProvider, settingsRepoProvider);
  }

  public static CronWorker newInstance(Context context, WorkerParameters workerParams,
      AgentCore agentCore, ScheduledJobDao scheduledJobDao, SettingsRepository settingsRepo) {
    return new CronWorker(context, workerParams, agentCore, scheduledJobDao, settingsRepo);
  }
}
