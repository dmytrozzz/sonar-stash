package org.sonar.plugins.stash.client;

import java.util.concurrent.TimeUnit;

import lombok.NonNull;
import okhttp3.Authenticator;
import okhttp3.Credentials;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
/**
 * Created by dmytro.khaynas on 3/29/17.
 */
class ClientFactory {

    private static OkHttpClient.Builder okHttpClientBuilder(long timeout) {
        return new OkHttpClient.Builder()
                .connectTimeout(timeout, TimeUnit.SECONDS)
                .writeTimeout(timeout, TimeUnit.SECONDS)
                .readTimeout(timeout, TimeUnit.SECONDS)
                .addInterceptor(chain -> chain.proceed(
                        chain.request().newBuilder()
                             .addHeader("Context-Type", "application/json")
                             .addHeader("Accept", "application/json")
                             .build()))
                .addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY));
    }

    static Retrofit.Builder buildRetrofit(String baseUri, long timeout) {
        return new Retrofit.Builder()
                .client(ClientFactory.okHttpClientBuilder(timeout).build())
                .baseUrl(baseUri)
                .addConverterFactory(GsonConverterFactory.create());
    }

    static Retrofit.Builder buildRetrofit(String baseUri, String login, String password, long timeout) {
        return buildRetrofit(baseUri, timeout)
                .client(ClientFactory.okHttpClientBuilder(timeout).authenticator(createBasicAuthenticator(login, password)).build());
    }

    private static Authenticator createBasicAuthenticator(@NonNull String username, @NonNull String password) {
        return (route, response) ->
                response
                        .request()
                        .newBuilder()
                        .addHeader("Authorization", Credentials.basic(username, password))
                        .build();
    }
}
