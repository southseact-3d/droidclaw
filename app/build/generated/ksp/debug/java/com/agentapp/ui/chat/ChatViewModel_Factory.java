package com.agentapp.ui.chat;

import com.agentapp.agent.AgentCore;
import com.agentapp.data.db.MessageDao;
import com.agentapp.data.db.SessionDao;
import com.agentapp.data.repository.SettingsRepository;
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
public final class ChatViewModel_Factory implements Factory<ChatViewModel> {
  private final Provider<AgentCore> agentCoreProvider;

  private final Provider<MessageDao> messageDaoProvider;

  private final Provider<SessionDao> sessionDaoProvider;

  private final Provider<SettingsRepository> settingsRepoProvider;

  public ChatViewModel_Factory(Provider<AgentCore> agentCoreProvider,
      Provider<MessageDao> messageDaoProvider, Provider<SessionDao> sessionDaoProvider,
      Provider<SettingsRepository> settingsRepoProvider) {
    this.agentCoreProvider = agentCoreProvider;
    this.messageDaoProvider = messageDaoProvider;
    this.sessionDaoProvider = sessionDaoProvider;
    this.settingsRepoProvider = settingsRepoProvider;
  }

  @Override
  public ChatViewModel get() {
    return newInstance(agentCoreProvider.get(), messageDaoProvider.get(), sessionDaoProvider.get(), settingsRepoProvider.get());
  }

  public static ChatViewModel_Factory create(Provider<AgentCore> agentCoreProvider,
      Provider<MessageDao> messageDaoProvider, Provider<SessionDao> sessionDaoProvider,
      Provider<SettingsRepository> settingsRepoProvider) {
    return new ChatViewModel_Factory(agentCoreProvider, messageDaoProvider, sessionDaoProvider, settingsRepoProvider);
  }

  public static ChatViewModel newInstance(AgentCore agentCore, MessageDao messageDao,
      SessionDao sessionDao, SettingsRepository settingsRepo) {
    return new ChatViewModel(agentCore, messageDao, sessionDao, settingsRepo);
  }
}
