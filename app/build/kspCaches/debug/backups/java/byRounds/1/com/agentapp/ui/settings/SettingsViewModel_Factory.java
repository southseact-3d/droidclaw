package com.agentapp.ui.settings;

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
public final class SettingsViewModel_Factory implements Factory<SettingsViewModel> {
  private final Provider<SettingsRepository> settingsRepoProvider;

  public SettingsViewModel_Factory(Provider<SettingsRepository> settingsRepoProvider) {
    this.settingsRepoProvider = settingsRepoProvider;
  }

  @Override
  public SettingsViewModel get() {
    return newInstance(settingsRepoProvider.get());
  }

  public static SettingsViewModel_Factory create(
      Provider<SettingsRepository> settingsRepoProvider) {
    return new SettingsViewModel_Factory(settingsRepoProvider);
  }

  public static SettingsViewModel newInstance(SettingsRepository settingsRepo) {
    return new SettingsViewModel(settingsRepo);
  }
}
