package com.agentapp.worker;

import android.content.Context;
import androidx.work.WorkerParameters;
import com.agentapp.agent.AgentCore;
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
public final class HeartbeatWorker_Factory {
  private final Provider<AgentCore> agentCoreProvider;

  private final Provider<SettingsRepository> settingsRepoProvider;

  public HeartbeatWorker_Factory(Provider<AgentCore> agentCoreProvider,
      Provider<SettingsRepository> settingsRepoProvider) {
    this.agentCoreProvider = agentCoreProvider;
    this.settingsRepoProvider = settingsRepoProvider;
  }

  public HeartbeatWorker get(Context context, WorkerParameters workerParams) {
    return newInstance(context, workerParams, agentCoreProvider.get(), settingsRepoProvider.get());
  }

  public static HeartbeatWorker_Factory create(Provider<AgentCore> agentCoreProvider,
      Provider<SettingsRepository> settingsRepoProvider) {
    return new HeartbeatWorker_Factory(agentCoreProvider, settingsRepoProvider);
  }

  public static HeartbeatWorker newInstance(Context context, WorkerParameters workerParams,
      AgentCore agentCore, SettingsRepository settingsRepo) {
    return new HeartbeatWorker(context, workerParams, agentCore, settingsRepo);
  }
}
