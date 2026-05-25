package com.app.triflow.data.repository;

import com.app.triflow.core.network.ApiCallExecutor;
import com.app.triflow.data.local.db.dao.TaskDao;
import com.app.triflow.data.remote.api.ContextsApi;
import com.app.triflow.data.remote.api.InboxApi;
import com.app.triflow.data.remote.api.ProjectsApi;
import com.app.triflow.data.remote.api.ReviewsApi;
import com.app.triflow.data.remote.api.TasksApi;
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
public final class GtdRepositoryImpl_Factory implements Factory<GtdRepositoryImpl> {
  private final Provider<TasksApi> tasksApiProvider;

  private final Provider<ProjectsApi> projectsApiProvider;

  private final Provider<ContextsApi> contextsApiProvider;

  private final Provider<InboxApi> inboxApiProvider;

  private final Provider<ReviewsApi> reviewsApiProvider;

  private final Provider<TaskDao> taskDaoProvider;

  private final Provider<ApiCallExecutor> executorProvider;

  public GtdRepositoryImpl_Factory(Provider<TasksApi> tasksApiProvider,
      Provider<ProjectsApi> projectsApiProvider, Provider<ContextsApi> contextsApiProvider,
      Provider<InboxApi> inboxApiProvider, Provider<ReviewsApi> reviewsApiProvider,
      Provider<TaskDao> taskDaoProvider, Provider<ApiCallExecutor> executorProvider) {
    this.tasksApiProvider = tasksApiProvider;
    this.projectsApiProvider = projectsApiProvider;
    this.contextsApiProvider = contextsApiProvider;
    this.inboxApiProvider = inboxApiProvider;
    this.reviewsApiProvider = reviewsApiProvider;
    this.taskDaoProvider = taskDaoProvider;
    this.executorProvider = executorProvider;
  }

  @Override
  public GtdRepositoryImpl get() {
    return newInstance(tasksApiProvider.get(), projectsApiProvider.get(), contextsApiProvider.get(), inboxApiProvider.get(), reviewsApiProvider.get(), taskDaoProvider.get(), executorProvider.get());
  }

  public static GtdRepositoryImpl_Factory create(Provider<TasksApi> tasksApiProvider,
      Provider<ProjectsApi> projectsApiProvider, Provider<ContextsApi> contextsApiProvider,
      Provider<InboxApi> inboxApiProvider, Provider<ReviewsApi> reviewsApiProvider,
      Provider<TaskDao> taskDaoProvider, Provider<ApiCallExecutor> executorProvider) {
    return new GtdRepositoryImpl_Factory(tasksApiProvider, projectsApiProvider, contextsApiProvider, inboxApiProvider, reviewsApiProvider, taskDaoProvider, executorProvider);
  }

  public static GtdRepositoryImpl newInstance(TasksApi tasksApi, ProjectsApi projectsApi,
      ContextsApi contextsApi, InboxApi inboxApi, ReviewsApi reviewsApi, TaskDao taskDao,
      ApiCallExecutor executor) {
    return new GtdRepositoryImpl(tasksApi, projectsApi, contextsApi, inboxApi, reviewsApi, taskDao, executor);
  }
}
