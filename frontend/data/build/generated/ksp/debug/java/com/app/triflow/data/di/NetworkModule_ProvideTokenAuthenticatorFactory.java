package com.app.triflow.data.di;

import com.app.triflow.core.security.EncryptedTokenStore;
import com.app.triflow.data.remote.auth.TokenAuthenticator;
import com.app.triflow.data.remote.auth.TokenRefresher;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
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
public final class NetworkModule_ProvideTokenAuthenticatorFactory implements Factory<TokenAuthenticator> {
  private final Provider<EncryptedTokenStore> tokenStoreProvider;

  private final Provider<TokenRefresher> refresherProvider;

  public NetworkModule_ProvideTokenAuthenticatorFactory(
      Provider<EncryptedTokenStore> tokenStoreProvider,
      Provider<TokenRefresher> refresherProvider) {
    this.tokenStoreProvider = tokenStoreProvider;
    this.refresherProvider = refresherProvider;
  }

  @Override
  public TokenAuthenticator get() {
    return provideTokenAuthenticator(tokenStoreProvider.get(), refresherProvider.get());
  }

  public static NetworkModule_ProvideTokenAuthenticatorFactory create(
      Provider<EncryptedTokenStore> tokenStoreProvider,
      Provider<TokenRefresher> refresherProvider) {
    return new NetworkModule_ProvideTokenAuthenticatorFactory(tokenStoreProvider, refresherProvider);
  }

  public static TokenAuthenticator provideTokenAuthenticator(EncryptedTokenStore tokenStore,
      TokenRefresher refresher) {
    return Preconditions.checkNotNullFromProvides(NetworkModule.INSTANCE.provideTokenAuthenticator(tokenStore, refresher));
  }
}
