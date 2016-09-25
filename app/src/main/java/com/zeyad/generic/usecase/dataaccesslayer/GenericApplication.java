package com.zeyad.generic.usecase.dataaccesslayer;

import android.app.Application;
import android.util.Log;
import android.widget.Toast;

import com.workable.errorhandler.ErrorHandler;
import com.zeyad.generic.usecase.dataaccesslayer.di.components.ApplicationComponent;
import com.zeyad.generic.usecase.dataaccesslayer.di.components.DaggerApplicationComponent;
import com.zeyad.generic.usecase.dataaccesslayer.di.modules.ApplicationModule;
import com.zeyad.genericusecase.domain.interactors.GenericUseCaseFactory;

import java.io.File;
import java.net.ConnectException;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.exceptions.RealmIOException;
import io.realm.rx.RealmObservableFactory;
import okhttp3.Cache;
import okhttp3.CacheControl;
import okhttp3.Interceptor;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.adapter.rxjava.HttpException;

/**
 * @author by ZIaDo on 9/24/16.
 */

public class GenericApplication extends Application {

    private ApplicationComponent applicationComponent;
    private static GenericApplication sInstance;

    public static GenericApplication getInstance() {
        return sInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sInstance = this;
        initializeInjector();
        initializeRealm();
        GenericUseCaseFactory.init(getApplicationContext(), null);
        initErrorHandler();
    }

    private void initErrorHandler() {
        ErrorHandler.defaultErrorHandler()
                // Bind certain exceptions to "offline"
                .bindErrorCode("offline", errorCode -> throwable ->
                        throwable instanceof UnknownHostException || throwable instanceof ConnectException)
                // Bind HTTP 401 status to 401
                .bindErrorCode(401, errorCode -> throwable -> ((HttpException) throwable).code() == 401)
                // Bind HTTP 404 status to 404
                .bindErrorCode(404, errorCode -> throwable -> ((HttpException) throwable).code() == 404)
                // Bind HTTP 500 status to 500
                .bindErrorCode(500, errorCode -> throwable -> ((HttpException) throwable).code() == 500)
                // Bind all DB errors to a custom enumeration
                .bindErrorCodeClass(RealmIOException.class, errorCode -> throwable -> throwable == errorCode)
                // Handle HTTP 500 errors
                .on(500, (throwable, errorHandler) -> Toast.makeText(getApplicationContext(),
                        "Error del servidor", Toast.LENGTH_SHORT).show())
                // Handle HTTP 404 errors
                .on(404, (throwable, errorHandler) -> Toast.makeText(getApplicationContext(),
                        "URL not found 404", Toast.LENGTH_SHORT).show())
                // Handle "offline" errors
                .on("offline", (throwable, errorHandler) -> Toast.makeText(getApplicationContext(),
                        "No tienes internet :/", Toast.LENGTH_SHORT).show())
                .on(RealmIOException.class, (throwable, errorHandler) -> Toast.makeText(getApplicationContext(),
                        "Error del app", Toast.LENGTH_SHORT).show())
                // Handle unknown errors
                .otherwise((throwable, errorHandler) -> Toast.makeText(getApplicationContext(),
                        throwable.getMessage(), Toast.LENGTH_SHORT).show())
                // Always log to a crash/error reporting service
                .always((throwable, errorHandler) -> throwable.printStackTrace());
    }

    private Cache provideCache() {
        try {
            return new Cache(new File(getCacheDir(), "http-cache"),
                    10 * 1024 * 1024); // 10 MB
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private Interceptor provideCacheInterceptor() {
        return chain -> {
            // re-write response header to force use of cache
            return chain.proceed(chain.request())
                    .newBuilder()
                    .header("Cache-Control", new CacheControl.Builder()
                            .maxAge(2, TimeUnit.MINUTES)
                            .build().toString())
                    .build();
        };
    }

    private HttpLoggingInterceptor provideHttpLoggingInterceptor() {
        return new HttpLoggingInterceptor(message -> Log.d("NetworkInfo", message))
                .setLevel(HttpLoggingInterceptor.Level.BODY);
    }

    private void initializeRealm() {
        Realm.setDefaultConfiguration(new RealmConfiguration.Builder(this)
                .name("app.realm")
                .modules(Realm.getDefaultModule(), new LibraryModule())
                .rxFactory(new RealmObservableFactory())
                .deleteRealmIfMigrationNeeded()
                .build());
    }

    private void initializeInjector() {
        applicationComponent = DaggerApplicationComponent.builder()
                .applicationModule(new ApplicationModule(this))
                .build();
    }

    public ApplicationComponent getApplicationComponent() {
        return applicationComponent;
    }
}
