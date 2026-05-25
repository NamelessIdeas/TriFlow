package com.app.triflow.data.di;

import com.app.triflow.data.remote.api.UsersApi;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import retrofit2.Retrofit;

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
public final class NetworkModule_ProvideUsersApiFactory implements Factory<UsersApi> {
  private final Provider<Retrofit> rProvider;

  public NetworkModule_ProvideUsersApiFactory(Provider<Retrofit> rProvider) {
    this.rProvider = rProvider;
  }

  @Override
  public UsersApi get() {
    return provideUsersApi(rProvider.get());
  }

  public static NetworkModule_ProvideUsersApiFactory create(Provider<Retrofit> rProvider) {
    return new NetworkModule_ProvideUsersApiFactory(rProvider);
  }

  public static UsersApi provideUsersApi(Retrofit r) {
    return Preconditions.checkNotNullFromProvides(NetworkModule.INSTANCE.provideUsersApi(r));
  }
}
