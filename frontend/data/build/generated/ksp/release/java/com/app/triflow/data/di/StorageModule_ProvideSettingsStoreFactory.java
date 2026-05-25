package com.app.triflow.data.di;

import android.content.Context;
import com.app.triflow.data.local.datastore.SettingsStore;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
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
public final class StorageModule_ProvideSettingsStoreFactory implements Factory<SettingsStore> {
  private final Provider<Context> contextProvider;

  public StorageModule_ProvideSettingsStoreFactory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public SettingsStore get() {
    return provideSettingsStore(contextProvider.get());
  }

  public static StorageModule_ProvideSettingsStoreFactory create(
      Provider<Context> contextProvider) {
    return new StorageModule_ProvideSettingsStoreFactory(contextProvider);
  }

  public static SettingsStore provideSettingsStore(Context context) {
    return Preconditions.checkNotNullFromProvides(StorageModule.INSTANCE.provideSettingsStore(context));
  }
}
