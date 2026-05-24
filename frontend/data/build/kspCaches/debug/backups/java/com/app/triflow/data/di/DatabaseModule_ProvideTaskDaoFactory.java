package com.app.triflow.data.di;

import com.app.triflow.data.local.db.TriFlowDatabase;
import com.app.triflow.data.local.db.dao.TaskDao;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
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
public final class DatabaseModule_ProvideTaskDaoFactory implements Factory<TaskDao> {
  private final Provider<TriFlowDatabase> dbProvider;

  public DatabaseModule_ProvideTaskDaoFactory(Provider<TriFlowDatabase> dbProvider) {
    this.dbProvider = dbProvider;
  }

  @Override
  public TaskDao get() {
    return provideTaskDao(dbProvider.get());
  }

  public static DatabaseModule_ProvideTaskDaoFactory create(Provider<TriFlowDatabase> dbProvider) {
    return new DatabaseModule_ProvideTaskDaoFactory(dbProvider);
  }

  public static TaskDao provideTaskDao(TriFlowDatabase db) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.provideTaskDao(db));
  }
}
