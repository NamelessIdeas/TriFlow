package com.app.triflow.data.repository;

import com.app.triflow.core.network.ApiCallExecutor;
import com.app.triflow.data.remote.api.PomodoroApi;
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
public final class PomodoroRepositoryImpl_Factory implements Factory<PomodoroRepositoryImpl> {
  private final Provider<PomodoroApi> apiProvider;

  private final Provider<ApiCallExecutor> executorProvider;

  public PomodoroRepositoryImpl_Factory(Provider<PomodoroApi> apiProvider,
      Provider<ApiCallExecutor> executorProvider) {
    this.apiProvider = apiProvider;
    this.executorProvider = executorProvider;
  }

  @Override
  public PomodoroRepositoryImpl get() {
    return newInstance(apiProvider.get(), executorProvider.get());
  }

  public static PomodoroRepositoryImpl_Factory create(Provider<PomodoroApi> apiProvider,
      Provider<ApiCallExecutor> executorProvider) {
    return new PomodoroRepositoryImpl_Factory(apiProvider, executorProvider);
  }

  public static PomodoroRepositoryImpl newInstance(PomodoroApi api, ApiCallExecutor executor) {
    return new PomodoroRepositoryImpl(api, executor);
  }
}
