package com.app.triflow.data.repository;

import com.app.triflow.core.network.ApiCallExecutor;
import com.app.triflow.data.remote.api.QuizApi;
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
public final class QuizRepositoryImpl_Factory implements Factory<QuizRepositoryImpl> {
  private final Provider<QuizApi> apiProvider;

  private final Provider<ApiCallExecutor> executorProvider;

  public QuizRepositoryImpl_Factory(Provider<QuizApi> apiProvider,
      Provider<ApiCallExecutor> executorProvider) {
    this.apiProvider = apiProvider;
    this.executorProvider = executorProvider;
  }

  @Override
  public QuizRepositoryImpl get() {
    return newInstance(apiProvider.get(), executorProvider.get());
  }

  public static QuizRepositoryImpl_Factory create(Provider<QuizApi> apiProvider,
      Provider<ApiCallExecutor> executorProvider) {
    return new QuizRepositoryImpl_Factory(apiProvider, executorProvider);
  }

  public static QuizRepositoryImpl newInstance(QuizApi api, ApiCallExecutor executor) {
    return new QuizRepositoryImpl(api, executor);
  }
}
