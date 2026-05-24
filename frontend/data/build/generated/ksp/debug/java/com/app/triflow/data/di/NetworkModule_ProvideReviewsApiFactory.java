package com.app.triflow.data.di;

import com.app.triflow.data.remote.api.ReviewsApi;
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
public final class NetworkModule_ProvideReviewsApiFactory implements Factory<ReviewsApi> {
  private final Provider<Retrofit> rProvider;

  public NetworkModule_ProvideReviewsApiFactory(Provider<Retrofit> rProvider) {
    this.rProvider = rProvider;
  }

  @Override
  public ReviewsApi get() {
    return provideReviewsApi(rProvider.get());
  }

  public static NetworkModule_ProvideReviewsApiFactory create(Provider<Retrofit> rProvider) {
    return new NetworkModule_ProvideReviewsApiFactory(rProvider);
  }

  public static ReviewsApi provideReviewsApi(Retrofit r) {
    return Preconditions.checkNotNullFromProvides(NetworkModule.INSTANCE.provideReviewsApi(r));
  }
}
