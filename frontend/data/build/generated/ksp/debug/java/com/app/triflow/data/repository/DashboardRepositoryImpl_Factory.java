package com.app.triflow.data.repository;

import com.app.triflow.core.network.ApiCallExecutor;
import com.app.triflow.data.remote.api.DashboardApi;
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
public final class DashboardRepositoryImpl_Factory implements Factory<DashboardRepositoryImpl> {
  private final Provider<DashboardApi> apiProvider;

  private final Provider<ApiCallExecutor> executorProvider;

  public DashboardRepositoryImpl_Factory(Provider<DashboardApi> apiProvider,
      Provider<ApiCallExecutor> executorProvider) {
    this.apiProvider = apiProvider;
    this.executorProvider = executorProvider;
  }

  @Override
  public DashboardRepositoryImpl get() {
    return newInstance(apiProvider.get(), executorProvider.get());
  }

  public static DashboardRepositoryImpl_Factory create(Provider<DashboardApi> apiProvider,
      Provider<ApiCallExecutor> executorProvider) {
    return new DashboardRepositoryImpl_Factory(apiProvider, executorProvider);
  }

  public static DashboardRepositoryImpl newInstance(DashboardApi api, ApiCallExecutor executor) {
    return new DashboardRepositoryImpl(api, executor);
  }
}
