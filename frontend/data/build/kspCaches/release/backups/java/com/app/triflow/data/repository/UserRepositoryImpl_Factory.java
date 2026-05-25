package com.app.triflow.data.repository;

import com.app.triflow.core.network.ApiCallExecutor;
import com.app.triflow.data.local.datastore.SettingsStore;
import com.app.triflow.data.remote.api.UsersApi;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

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
    "deprecation",
    "nullness:initialization.field.uninitialized"
})
public final class UserRepositoryImpl_Factory implements Factory<UserRepositoryImpl> {
  private final Provider<UsersApi> apiProvider;

  private final Provider<SettingsStore> settingsProvider;

  private final Provider<ApiCallExecutor> executorProvider;

  public UserRepositoryImpl_Factory(Provider<UsersApi> apiProvider,
      Provider<SettingsStore> settingsProvider, Provider<ApiCallExecutor> executorProvider) {
    this.apiProvider = apiProvider;
    this.settingsProvider = settingsProvider;
    this.executorProvider = executorProvider;
  }

  @Override
  public UserRepositoryImpl get() {
    return newInstance(apiProvider.get(), settingsProvider.get(), executorProvider.get());
  }

  public static UserRepositoryImpl_Factory create(Provider<UsersApi> apiProvider,
      Provider<SettingsStore> settingsProvider, Provider<ApiCallExecutor> executorProvider) {
    return new UserRepositoryImpl_Factory(apiProvider, settingsProvider, executorProvider);
  }

  public static UserRepositoryImpl newInstance(UsersApi api, SettingsStore settings,
      ApiCallExecutor executor) {
    return new UserRepositoryImpl(api, settings, executor);
  }
}
