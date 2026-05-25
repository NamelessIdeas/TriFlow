package com.app.triflow.data.repository;

import com.app.triflow.core.network.ApiCallExecutor;
import com.app.triflow.data.local.db.dao.NoteDao;
import com.app.triflow.data.remote.api.NotesApi;
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
public final class NotesRepositoryImpl_Factory implements Factory<NotesRepositoryImpl> {
  private final Provider<NotesApi> apiProvider;

  private final Provider<NoteDao> daoProvider;

  private final Provider<ApiCallExecutor> executorProvider;

  public NotesRepositoryImpl_Factory(Provider<NotesApi> apiProvider, Provider<NoteDao> daoProvider,
      Provider<ApiCallExecutor> executorProvider) {
    this.apiProvider = apiProvider;
    this.daoProvider = daoProvider;
    this.executorProvider = executorProvider;
  }

  @Override
  public NotesRepositoryImpl get() {
    return newInstance(apiProvider.get(), daoProvider.get(), executorProvider.get());
  }

  public static NotesRepositoryImpl_Factory create(Provider<NotesApi> apiProvider,
      Provider<NoteDao> daoProvider, Provider<ApiCallExecutor> executorProvider) {
    return new NotesRepositoryImpl_Factory(apiProvider, daoProvider, executorProvider);
  }

  public static NotesRepositoryImpl newInstance(NotesApi api, NoteDao dao,
      ApiCallExecutor executor) {
    return new NotesRepositoryImpl(api, dao, executor);
  }
}
