package com.app.triflow.data.di;

import com.app.triflow.data.remote.auth.TokenRefresher;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import retrofit2.Retrofit;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata("com.app.triflow.data.di.RefreshRetrofit")
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
public final class NetworkModule_ProvideTokenRefresherFactory implements Factory<TokenRefresher> {
  private final Provider<Retrofit> retrofitProvider;

  public NetworkModule_ProvideTokenRefresherFactory(Provider<Retrofit> retrofitProvider) {
    this.retrofitProvider = retrofitProvider;
  }

  @Override
  public TokenRefresher get() {
    return provideTokenRefresher(retrofitProvider.get());
  }

  public static NetworkModule_ProvideTokenRefresherFactory create(
      Provider<Retrofit> retrofitProvider) {
    return new NetworkModule_ProvideTokenRefresherFactory(retrofitProvider);
  }

  public static TokenRefresher provideTokenRefresher(Retrofit retrofit) {
    return Preconditions.checkNotNullFromProvides(NetworkModule.INSTANCE.provideTokenRefresher(retrofit));
  }
}
