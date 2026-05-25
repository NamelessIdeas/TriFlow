package com.app.triflow.data.di;

import android.content.Context;
import com.app.triflow.core.security.EncryptedTokenStore;
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
public final class StorageModule_ProvideTokenStoreFactory implements Factory<EncryptedTokenStore> {
  private final Provider<Context> contextProvider;

  public StorageModule_ProvideTokenStoreFactory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public EncryptedTokenStore get() {
    return provideTokenStore(contextProvider.get());
  }

  public static StorageModule_ProvideTokenStoreFactory create(Provider<Context> contextProvider) {
    return new StorageModule_ProvideTokenStoreFactory(contextProvider);
  }

  public static EncryptedTokenStore provideTokenStore(Context context) {
    return Preconditions.checkNotNullFromProvides(StorageModule.INSTANCE.provideTokenStore(context));
  }
}
