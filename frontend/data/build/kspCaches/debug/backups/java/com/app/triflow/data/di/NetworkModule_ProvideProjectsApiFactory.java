package com.app.triflow.data.di;

import com.app.triflow.data.remote.api.ProjectsApi;
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
public final class NetworkModule_ProvideProjectsApiFactory implements Factory<ProjectsApi> {
  private final Provider<Retrofit> rProvider;

  public NetworkModule_ProvideProjectsApiFactory(Provider<Retrofit> rProvider) {
    this.rProvider = rProvider;
  }

  @Override
  public ProjectsApi get() {
    return provideProjectsApi(rProvider.get());
  }

  public static NetworkModule_ProvideProjectsApiFactory create(Provider<Retrofit> rProvider) {
    return new NetworkModule_ProvideProjectsApiFactory(rProvider);
  }

  public static ProjectsApi provideProjectsApi(Retrofit r) {
    return Preconditions.checkNotNullFromProvides(NetworkModule.INSTANCE.provideProjectsApi(r));
  }
}
