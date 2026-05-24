package com.app.triflow.data.di;

import com.app.triflow.data.remote.api.ContextsApi;
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
public final class NetworkModule_ProvideContextsApiFactory implements Factory<ContextsApi> {
  private final Provider<Retrofit> rProvider;

  public NetworkModule_ProvideContextsApiFactory(Provider<Retrofit> rProvider) {
    this.rProvider = rProvider;
  }

  @Override
  public ContextsApi get() {
    return provideContextsApi(rProvider.get());
  }

  public static NetworkModule_ProvideContextsApiFactory create(Provider<Retrofit> rProvider) {
    return new NetworkModule_ProvideContextsApiFactory(rProvider);
  }

  public static ContextsApi provideContextsApi(Retrofit r) {
    return Preconditions.checkNotNullFromProvides(NetworkModule.INSTANCE.provideContextsApi(r));
  }
}
