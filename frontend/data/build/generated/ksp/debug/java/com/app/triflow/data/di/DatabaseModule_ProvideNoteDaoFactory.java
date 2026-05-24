package com.app.triflow.data.di;

import com.app.triflow.data.local.db.TriFlowDatabase;
import com.app.triflow.data.local.db.dao.NoteDao;
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
public final class DatabaseModule_ProvideNoteDaoFactory implements Factory<NoteDao> {
  private final Provider<TriFlowDatabase> dbProvider;

  public DatabaseModule_ProvideNoteDaoFactory(Provider<TriFlowDatabase> dbProvider) {
    this.dbProvider = dbProvider;
  }

  @Override
  public NoteDao get() {
    return provideNoteDao(dbProvider.get());
  }

  public static DatabaseModule_ProvideNoteDaoFactory create(Provider<TriFlowDatabase> dbProvider) {
    return new DatabaseModule_ProvideNoteDaoFactory(dbProvider);
  }

  public static NoteDao provideNoteDao(TriFlowDatabase db) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.provideNoteDao(db));
  }
}
