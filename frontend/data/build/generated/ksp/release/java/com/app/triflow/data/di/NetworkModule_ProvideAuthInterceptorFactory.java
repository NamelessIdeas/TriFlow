package com.app.triflow.data.di;

import com.app.triflow.core.security.EncryptedTokenStore;
import com.app.triflow.data.remote.auth.AuthInterceptor;
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
public final class NetworkModule_ProvideAuthInterceptorFactory implements Factory<AuthInterceptor> {
  private final Provider<EncryptedTokenStore> tokenStoreProvider;

  public NetworkModule_ProvideAuthInterceptorFactory(
      Provider<EncryptedTokenStore> tokenStoreProvider) {
    this.tokenStoreProvider = tokenStoreProvider;
  }

  @Override
  public AuthInterceptor get() {
    return provideAuthInterceptor(tokenStoreProvider.get());
  }

  public static NetworkModule_ProvideAuthInterceptorFactory create(
      Provider<EncryptedTokenStore> tokenStoreProvider) {
    return new NetworkModule_ProvideAuthInterceptorFactory(tokenStoreProvider);
  }

  public static AuthInterceptor provideAuthInterceptor(EncryptedTokenStore tokenStore) {
    return Preconditions.checkNotNullFromProvides(NetworkModule.INSTANCE.provideAuthInterceptor(tokenStore));
  }
}
