package com.app.triflow.data.di;

import com.app.triflow.data.remote.api.TasksApi;
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
public final class NetworkModule_ProvideTasksApiFactory implements Factory<TasksApi> {
  private final Provider<Retrofit> rProvider;

  public NetworkModule_ProvideTasksApiFactory(Provider<Retrofit> rProvider) {
    this.rProvider = rProvider;
  }

  @Override
  public TasksApi get() {
    return provideTasksApi(rProvider.get());
  }

  public static NetworkModule_ProvideTasksApiFactory create(Provider<Retrofit> rProvider) {
    return new NetworkModule_ProvideTasksApiFactory(rProvider);
  }

  public static TasksApi provideTasksApi(Retrofit r) {
    return Preconditions.checkNotNullFromProvides(NetworkModule.INSTANCE.provideTasksApi(r));
  }
}
