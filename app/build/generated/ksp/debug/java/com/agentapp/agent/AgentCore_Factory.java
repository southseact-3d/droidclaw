package com.agentapp.agent;

import com.agentapp.data.db.MessageDao;
import com.agentapp.data.db.SkillDao;
import com.agentapp.data.repository.SettingsRepository;
import com.agentapp.providers.LlmProviderClient;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

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
public final class AgentCore_Factory implements Factory<AgentCore> {
  private final Provider<LlmProviderClient> llmClientProvider;

  private final Provider<MessageDao> messageDaoProvider;

  private final Provider<SkillDao> skillDaoProvider;

  private final Provider<SettingsRepository> settingsRepoProvider;

  public AgentCore_Factory(Provider<LlmProviderClient> llmClientProvider,
      Provider<MessageDao> messageDaoProvider, Provider<SkillDao> skillDaoProvider,
      Provider<SettingsRepository> settingsRepoProvider) {
    this.llmClientProvider = llmClientProvider;
    this.messageDaoProvider = messageDaoProvider;
    this.skillDaoProvider = skillDaoProvider;
    this.settingsRepoProvider = settingsRepoProvider;
  }

  @Override
  public AgentCore get() {
    return newInstance(llmClientProvider.get(), messageDaoProvider.get(), skillDaoProvider.get(), settingsRepoProvider.get());
  }

  public static AgentCore_Factory create(Provider<LlmProviderClient> llmClientProvider,
      Provider<MessageDao> messageDaoProvider, Provider<SkillDao> skillDaoProvider,
      Provider<SettingsRepository> settingsRepoProvider) {
    return new AgentCore_Factory(llmClientProvider, messageDaoProvider, skillDaoProvider, settingsRepoProvider);
  }

  public static AgentCore newInstance(LlmProviderClient llmClient, MessageDao messageDao,
      SkillDao skillDao, SettingsRepository settingsRepo) {
    return new AgentCore(llmClient, messageDao, skillDao, settingsRepo);
  }
}
