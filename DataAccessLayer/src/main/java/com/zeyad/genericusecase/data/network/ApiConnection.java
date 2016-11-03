package com.zeyad.genericusecase.data.network;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.util.Log;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.GsonBuilder;
import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import com.zeyad.genericusecase.BuildConfig;
import com.zeyad.genericusecase.Config;
import com.zeyad.genericusecase.R;
import com.zeyad.genericusecase.data.executor.JobExecutor;
import com.zeyad.genericusecase.data.utils.Utils;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import io.realm.RealmObject;
import okhttp3.Cache;
import okhttp3.CacheControl;
import okhttp3.CertificatePinner;
import okhttp3.ConnectionSpec;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.TlsVersion;
import okhttp3.logging.HttpLoggingInterceptor;
import okio.Buffer;
import okio.BufferedSink;
import okio.GzipSink;
import okio.Okio;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import io.reactivex.Observable;

/**
 * Api Connection class used to retrieve data from the cloud.
 * Implements {@link Callable} so when executed asynchronously can
 * return a value.
 */
class ApiConnection implements com.zeyad.genericusecase.data.network.IApiConnection {

    private final static String CACHE_CONTROL = "Cache-Control";
    private static final int TIME_OUT = 15;
    private static ApiConnection sInstance;
    private final RestApi mRestApiWithoutCache;
    private final RestApi mRestApiWithCache;

    private ApiConnection(@Nullable OkHttpClient.Builder okhttpBuilder, @Nullable Cache cache) {
        if (okhttpBuilder == null)
            throw new NullPointerException(Config.getInstance().getContext().getString(R.string.builder_null));
        mRestApiWithCache = createRetro2Client(provideOkHttpClient(okhttpBuilder, cache))
                .create(RestApi.class);
        mRestApiWithoutCache = createRetro2Client(provideOkHttpClient(okhttpBuilder, null))
                .create(RestApi.class);
    }

    private ApiConnection() {
        Config.getInstance().setUseApiWithCache(true);
        mRestApiWithCache = createRetro2Client(provideOkHttpClient(getBuilderForOkhttp()
                , provideCache())).create(RestApi.class);
        mRestApiWithoutCache = createRetro2Client(provideOkHttpClient(getBuilderForOkhttp()
                , null)).create(RestApi.class);
    }

    /**
     * Meant only for mocking and tetsing purposed.
     *
     * @param restApiWithoutCache
     * @param restApiWithCache
     */
    @VisibleForTesting
    private ApiConnection(RestApi restApiWithoutCache, RestApi restApiWithCache) {
        mRestApiWithoutCache = restApiWithoutCache;
        mRestApiWithCache = restApiWithCache;
    }

    static IApiConnection getInstance() {
        if (sInstance == null)
            throw new NullPointerException(Config.getInstance().getContext().getString(R.string.api_connection_uninitialized));
        return sInstance;
    }

    static void init() {
        sInstance = new ApiConnection();
    }

    static void init(OkHttpClient.Builder okhttpBuilder, Cache cache) {
        sInstance = new ApiConnection(okhttpBuilder, cache);
    }

    /**
     * Meant only for mocking and tetsing purposed.
     *
     * @param restApiWithoutCache
     * @param restApiWithCache
     */
    static void init(RestApi restApiWithoutCache, RestApi restApiWithCache) {
        sInstance = new ApiConnection(restApiWithoutCache, restApiWithCache);
    }

    @NonNull
    @Override
    public HttpLoggingInterceptor provideHttpLoggingInterceptor() {
        return new HttpLoggingInterceptor(message -> Log.d("NetworkInfo", message))
                .setLevel(BuildConfig.DEBUG ? HttpLoggingInterceptor.Level.BODY : HttpLoggingInterceptor.Level.HEADERS);
    }

    @Override
    public Observable<ResponseBody> dynamicDownload(String url) {

        return getRestApi().dynamicDownload(url);
    }

    private RestApi getRestApi() {
        return Config.getInstance().isUseApiWithCache() ? mRestApiWithCache : mRestApiWithoutCache;
    }

    @Override
    public Observable<Object> dynamicGetObject(String url) {
        return getRestApi().dynamicGetObject(url);
    }

    @Override
    public Observable<Object> dynamicGetObject(String url, boolean shouldCache) {
        if (shouldCache && !Config.getInstance().isUseApiWithCache()) {
            logNoCache();
        }
        return getRestApi().dynamicGetObject(url);
    }

    @Override
    public Observable<List> dynamicGetList(String url) {
        return getRestApi().dynamicGetList(url);
    }

    @Override
    public Observable<List> dynamicGetList(String url, boolean shouldCache) {
        if (shouldCache && !Config.getInstance().isUseApiWithCache()) {
            logNoCache();
        }
        return getRestApi().dynamicGetList(url);
    }

    @Override
    public Observable<Object> dynamicPostObject(String url, RequestBody requestBody) {
        return getRestApi().dynamicPostObject(url, requestBody);
    }

    @Override
    public Observable<List> dynamicPostList(String url, RequestBody requestBody) {
        return getRestApi().dynamicPostList(url, requestBody);
    }

    @Override
    public Observable<Object> dynamicPutObject(String url, RequestBody requestBody) {
        return getRestApi().dynamicPutObject(url, requestBody);
    }

    @Override
    public Observable<List> dynamicPutList(String url, RequestBody requestBody) {
        return getRestApi().dynamicPutList(url, requestBody);
    }

    @Override
    public Observable<ResponseBody> upload(String url, MultipartBody.Part file, RequestBody description) {
        return getRestApi().upload(url, file);
    }

    @Override
    public Observable<Object> upload(String url, RequestBody requestBody) {
        return getRestApi().upload(url, requestBody);
    }

    @Override
    public Observable<ResponseBody> upload(String url, MultipartBody.Part file) {
        return getRestApi().upload(url, file);
    }

    @Override
    public Observable<List> dynamicDeleteList(String url, RequestBody body) {
        return getRestApi().dynamicDeleteList(url, body);
    }

    @Override
    public Observable<Object> dynamicDeleteObject(String url, RequestBody body) {
        return getRestApi().dynamicDeleteObject(url, body);
    }

    RestApi getRestApiWithoutCache() {
        return mRestApiWithoutCache;
    }

    RestApi getRestApiWithCache() {
        return mRestApiWithCache;
    }

    private Interceptor provideCacheInterceptor() {
        return chain -> {
            Response response = chain.proceed(chain.request());
            // re-write response header to force use of cache
            CacheControl cacheControl = new CacheControl.Builder()
                    .maxAge(2, TimeUnit.MINUTES)
                    .build();
            return response.newBuilder()
                    .header(CACHE_CONTROL, cacheControl.toString())
                    .build();
        };
    }

    private Interceptor provideOfflineCacheInterceptor() {
        return chain -> {
            Request request = chain.request();
            if (!Utils.isNetworkAvailable(Config.getInstance().getContext())) {
                CacheControl cacheControl = new CacheControl.Builder()
                        .maxStale(1, TimeUnit.DAYS)
                        .build();
                request = request.newBuilder()
                        .cacheControl(cacheControl)
                        .build();
            }
            return chain.proceed(request);
        };
    }

    private Interceptor provideGzipRequestInterceptor() {
        return chain -> {
            Request originalRequest = chain.request();
            if (originalRequest.body() == null || originalRequest.header("Content-Encoding") != null)
                return chain.proceed(originalRequest);
            Request compressedRequest = originalRequest.newBuilder()
                    .header("Content-Encoding", "gzip")
                    .method(originalRequest.method(), forceContentLength(gzip(originalRequest.body())))
                    .build();
            return chain.proceed(compressedRequest);
        };
    }

    private CertificatePinner provideCertificatePinner() {
        return new CertificatePinner.Builder()
//                .add("api.github.com", "sha256/6wJsqVDF8K19zxfLxV5DGRneLyzso9adVdUN/exDacw=")
                .build();
    }

    private List<ConnectionSpec> provideConnectionSpecsList() {
        return Collections.singletonList(new ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
                .tlsVersions(TlsVersion.TLS_1_2)
                .build());
    }

    @NonNull
    private RequestBody forceContentLength(@NonNull final RequestBody requestBody) throws IOException {
        final Buffer buffer = new Buffer();
        requestBody.writeTo(buffer);
        return new RequestBody() {
            @Override
            public MediaType contentType() {
                return requestBody.contentType();
            }

            @Override
            public long contentLength() {
                return buffer.size();
            }

            @Override
            public void writeTo(@NonNull BufferedSink sink) throws IOException {
                sink.write(buffer.snapshot());
            }
        };
    }

    @NonNull
    private RequestBody gzip(@NonNull final RequestBody body) {
        return new RequestBody() {
            @Override
            public MediaType contentType() {
                return body.contentType();
            }

            @Override
            public long contentLength() {
                return -1; // We don't know the compressed length in advance!
            }

            @Override
            public void writeTo(@NonNull BufferedSink sink) throws IOException {
                BufferedSink gzipSink = Okio.buffer(new GzipSink(sink));
                body.writeTo(gzipSink);
                gzipSink.close();
            }
        };
    }

    @NonNull
    private OkHttpClient.Builder getBuilderForOkhttp() {
        return new OkHttpClient.Builder()
                .addInterceptor(provideHttpLoggingInterceptor())
                .addInterceptor(provideOfflineCacheInterceptor())
                .addNetworkInterceptor(provideCacheInterceptor())
                .connectTimeout(TIME_OUT, TimeUnit.SECONDS)
                .readTimeout(TIME_OUT, TimeUnit.SECONDS)
                .writeTimeout(TIME_OUT, TimeUnit.SECONDS);
    }

    private OkHttpClient provideOkHttpClient(@NonNull OkHttpClient.Builder okHttpBuilder, @Nullable Cache cache) {
        if (cache != null) {
            okHttpBuilder.cache(cache);
        }
        return okHttpBuilder.build();
    }

    private Retrofit createRetro2Client(@NonNull OkHttpClient okHttpClient) {
        return new Retrofit.Builder()
                .baseUrl("http://www.google.com")
                .client(okHttpClient)
                .callbackExecutor(new JobExecutor())
                .addConverterFactory(GsonConverterFactory.create(new GsonBuilder()
                        .setExclusionStrategies(new ExclusionStrategy() {
                            @Override
                            public boolean shouldSkipField(@NonNull FieldAttributes f) {
                                return f.getDeclaringClass().equals(RealmObject.class);
                            }

                            @Override
                            public boolean shouldSkipClass(Class<?> clazz) {
                                return false;
                            }
                        }).create()))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();
    }

    @Nullable
    private Cache provideCache() {
        Cache cache = null;
        try {
            cache = new Cache(new File(Config.getInstance().getContext().getCacheDir(), "http-cache"),
                    10 * 1024 * 1024); // 10 MB
        } catch (Exception e) {
            e.printStackTrace();
        }
        return cache;
    }

    private void logNoCache() {
        Log.e(getClass().getSimpleName(), Config.getInstance().getContext().getString(R.string.caching_disabled));
    }
}
