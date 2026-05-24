package com.app.triflow.data.repository;

import com.app.triflow.core.network.ApiCallExecutor;
import com.app.triflow.core.security.EncryptedTokenStore;
import com.app.triflow.data.local.datastore.SettingsStore;
import com.app.triflow.data.remote.api.AuthApi;
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
public final class AuthRepositoryImpl_Factory implements Factory<AuthRepositoryImpl> {
  private final Provider<AuthApi> apiProvider;

  private final Provider<EncryptedTokenStore> tokenStoreProvider;

  private final Provider<SettingsStore> settingsProvider;

  private final Provider<ApiCallExecutor> executorProvider;

  public AuthRepositoryImpl_Factory(Provider<AuthApi> apiProvider,
      Provider<EncryptedTokenStore> tokenStoreProvider, Provider<SettingsStore> settingsProvider,
      Provider<ApiCallExecutor> executorProvider) {
    this.apiProvider = apiProvider;
    this.tokenStoreProvider = tokenStoreProvider;
    this.settingsProvider = settingsProvider;
    this.executorProvider = executorProvider;
  }

  @Override
  public AuthRepositoryImpl get() {
    return newInstance(apiProvider.get(), tokenStoreProvider.get(), settingsProvider.get(), executorProvider.get());
  }

  public static AuthRepositoryImpl_Factory create(Provider<AuthApi> apiProvider,
      Provider<EncryptedTokenStore> tokenStoreProvider, Provider<SettingsStore> settingsProvider,
      Provider<ApiCallExecutor> executorProvider) {
    return new AuthRepositoryImpl_Factory(apiProvider, tokenStoreProvider, settingsProvider, executorProvider);
  }

  public static AuthRepositoryImpl newInstance(AuthApi api, EncryptedTokenStore tokenStore,
      SettingsStore settings, ApiCallExecutor executor) {
    return new AuthRepositoryImpl(api, tokenStore, settings, executor);
  }
}
