package com.app.triflow.data.di;

import com.app.triflow.data.remote.api.QuizApi;
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
public final class NetworkModule_ProvideQuizApiFactory implements Factory<QuizApi> {
  private final Provider<Retrofit> rProvider;

  public NetworkModule_ProvideQuizApiFactory(Provider<Retrofit> rProvider) {
    this.rProvider = rProvider;
  }

  @Override
  public QuizApi get() {
    return provideQuizApi(rProvider.get());
  }

  public static NetworkModule_ProvideQuizApiFactory create(Provider<Retrofit> rProvider) {
    return new NetworkModule_ProvideQuizApiFactory(rProvider);
  }

  public static QuizApi provideQuizApi(Retrofit r) {
    return Preconditions.checkNotNullFromProvides(NetworkModule.INSTANCE.provideQuizApi(r));
  }
}
