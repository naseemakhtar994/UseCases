package com.zeyad.genericusecase.data;

import android.annotation.TargetApi;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.OneoffTask;
import com.google.gson.Gson;
import com.zeyad.genericusecase.data.mappers.EntityDataMapper;
import com.zeyad.genericusecase.data.mappers.EntityMapper;
import com.zeyad.genericusecase.data.services.GenericGCMService;
import com.zeyad.genericusecase.data.services.GenericJobService;
import com.zeyad.genericusecase.data.services.realm_test_models.TestModel;
import com.zeyad.genericusecase.data.utils.EntityMapperUtil;
import com.zeyad.genericusecase.data.utils.IEntityMapperUtil;
import com.zeyad.genericusecase.data.utils.Utils;

import org.hamcrest.Matchers;
import org.json.JSONException;
import org.json.JSONObject;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.exceptions.CompositeException;
import io.reactivex.subscribers.TestSubscriber;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;
import io.realm.rx.RealmObservableFactory;

import static android.app.job.JobInfo.NETWORK_TYPE_ANY;
import static android.app.job.JobInfo.NETWORK_TYPE_UNMETERED;
import static com.google.android.gms.gcm.Task.NETWORK_STATE_CONNECTED;
import static com.google.android.gms.gcm.Task.NETWORK_STATE_UNMETERED;
import static com.zeyad.genericusecase.data.services.GenericNetworkQueueIntentService.DOWNLOAD_FILE;
import static com.zeyad.genericusecase.data.services.GenericNetworkQueueIntentService.JOB_TYPE;
import static com.zeyad.genericusecase.data.services.GenericNetworkQueueIntentService.POST;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

public class TestUtility {

    public static final int TIMEOUT_TIME_VALUE = 10000;
    public static final int TIMEOUT_TIME_VALUE_LARGE = 100000;
    public static final TimeUnit TIMEOUT_TIME_UNIT = TimeUnit.MILLISECONDS;
    public static final int EXECUTION_COUNT_LARGE = 10000;
    public static final int EXECUTION_COUNT_MEDIUM = 1000;
    public static final int EXECUTION_COUNT_SMALL = 100;
    public static final int EXECUTION_COUNT_VERY_SMALL = 10;
    public static final int INVALID_ITEM_ID = -1234;
    public static final String EXCEPTION_PRINT_THROWABLE = "Exception While Executing Test Case.\nCause:";

    public static void printCurrentTestModels(@NonNull Realm realm) {
        RealmResults<TestModel> testModels = realm.where(TestModel.class).findAll();
        printObjects(testModels);
    }

    public static <T> void printObjects(@NonNull List<T> list) {
        System.out.println("Printing Objects");
        for (T object : list) {
            printDivider();
            System.out.println(object.toString());
        }
    }

    public static void printDivider() {
        System.out.println("=================================");
    }

    public static void printThrowables(@Nullable List<Throwable> list) {
        if (list != null && list.size() > 0) {
            System.out.println("Printing Throwables");
            for (Throwable throwable : list) {
                if (throwable != null) {
                    printDivider();
                    printThrowable(throwable);
                }
            }
        }
    }

    private static void printThrowable(@NonNull Throwable throwable) {
        if (throwable.getMessage() != null) {
            System.err.println(throwable.getMessage());
        } else {
            throwable.printStackTrace();
        }
    }

    public static void assertConvertedJsonArray(JSONObject[] jsonObject) {
        assertThat(jsonObject, Matchers.allOf(
                Matchers.notNullValue()
                , Matchers.arrayWithSize(1)
        ));
        assertThat(jsonObject[0], Matchers.notNullValue());
    }

    public static <T> void getJsonObjectFrom(JSONObject[] jsonObject, T model) {
        String json = new Gson().toJson(model);
        try {
            jsonObject[0] = new JSONObject(json);
        } catch (JSONException e) {
            assertThat("got exception:" + e.getMessage(), false);
        }
    }

    public static void assertNoErrors(@NonNull TestSubscriber<?> subscriber) {
        try {
            subscriber.assertNoErrors();
        } catch (Error e) {
            if (e.getCause() != null) {
                if (e.getCause() instanceof CompositeException) {
                    final CompositeException compositeException = (CompositeException) e.getCause();
                    assertThat(EXCEPTION_PRINT_THROWABLE + getStackTrace(compositeException.getExceptions()), false);
                    printThrowables(compositeException.getExceptions());
                } else {
                    printThrowable(e.getCause());
                    final Throwable cause = e.getCause();
                    assertThat(EXCEPTION_PRINT_THROWABLE + getStackTrace(cause), false);
                }
            } else {
                assertThat(EXCEPTION_PRINT_THROWABLE + "Unknown", false);
            }
        }
    }

    public static void executeMultipleTimes(int count, @NonNull Executor executor) {
        for (int i = 0; i < count; i++) {
            executor.run();
        }
    }

    @NonNull
    public static Context changeStateOfNetwork(@NonNull Context mockedContext, boolean toEnable) {

        ConnectivityManager connectivityManager = Mockito.mock(ConnectivityManager.class);
        Mockito.when(mockedContext.getSystemService(Context.CONNECTIVITY_SERVICE)).thenReturn(connectivityManager);
        if (Utils.hasLollipop()) {
            Network network = Mockito.mock(Network.class);
            Network[] networks = new Network[]{network};
            Mockito.when(connectivityManager.getAllNetworks()).thenReturn(networks);
            NetworkInfo networkInfo = Mockito.mock(NetworkInfo.class);
            Mockito.when(connectivityManager.getNetworkInfo(network)).thenReturn(networkInfo);
            Mockito.when(networkInfo.getState()).thenReturn(toEnable ? NetworkInfo.State.CONNECTED : NetworkInfo.State.DISCONNECTED);
        } else {
            NetworkInfo networkInfo = Mockito.mock(NetworkInfo.class);
            Mockito.when(connectivityManager.getAllNetworkInfo()).thenReturn(new NetworkInfo[]{networkInfo});
            Mockito.when(networkInfo.getState()).thenReturn(toEnable ? NetworkInfo.State.CONNECTED : NetworkInfo.State.DISCONNECTED);
        }
        return mockedContext;
    }

    public static boolean isGooglePlayerServicesEnabled(@NonNull Context context) {
        return GoogleApiAvailability.getInstance() != null && GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context) == ConnectionResult.SUCCESS;
    }

    private static String getStackTrace(@NonNull Throwable throwable) {

        StringWriter errors = new StringWriter();
        throwable.printStackTrace(new PrintWriter(errors));
        return errors.toString();
    }

    private static String getStackTrace(@NonNull List<Throwable> throwables) {
        StringWriter errors = new StringWriter();
        for (int i = 0; i < throwables.size(); i++) {
            errors.append("exception #" + (i + 1));
            (throwables.get(i)).printStackTrace(new PrintWriter(errors));
        }
        return errors.toString();
    }

    @NonNull
    public static IEntityMapperUtil createEntityMapper() {
        return new EntityMapperUtil() {
            @NonNull
            @Override
            public EntityMapper getDataMapper(Class dataClass) {
//                if (dataClass == ProfileRealmModel.class) {
//                    return new ProfileEntityMapper();
//                } else if (dataClass == IncomingRealmModel.class) {
//                    return new IncomingEntityMapper();
//                } else if (dataClass == OrdersRealmModel.class) {
//                    return new OrderEntityMapper();
//                } else if (dataClass == RoutesRealmModel.class) {
//                    return new RoutesEntityMapper();
//                } else {
                return new EntityDataMapper();
//                }
            }
        };
    }

    public static void performInitialSetupOfDb(@NonNull Context context) {
        Realm.setDefaultConfiguration(new RealmConfiguration.Builder()
                .name("app.realm")
                .modules(Realm.getDefaultModule(), new LibraryModule())
                .rxFactory(new RealmObservableFactory())
                .deleteRealmIfMigrationNeeded()
                .build());
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static void applyAssertsOnSchedulingForeFileIO(boolean onWifi,
                                                          boolean whileCharging,
                                                          GcmNetworkManager gcmNetworkManager,
                                                          boolean isGoogleServicesAvailable,
                                                          boolean hasLollipop,
                                                          JobScheduler jobScheduler) {
        if (isGoogleServicesAvailable) {
            ArgumentCaptor<OneoffTask> peopleCaptor = ArgumentCaptor.forClass(OneoffTask.class);
            Mockito.verify(gcmNetworkManager).schedule(peopleCaptor.capture());
            assertThat(peopleCaptor.getValue().getWindowEnd(), is(30L));
            assertThat(peopleCaptor.getValue().getWindowStart(), is(0L));
            assertThat(peopleCaptor.getValue().getRequiresCharging(), is(false));
            assertThat(peopleCaptor.getValue().getExtras(), is(notNullValue()));
            assertThat(peopleCaptor.getValue().getExtras().getString(JOB_TYPE), is(DOWNLOAD_FILE));
            assertThat(peopleCaptor.getValue().getServiceName(), is(GenericGCMService.class.getName()));
            assertThat(peopleCaptor.getValue().getRequiredNetwork(), is(onWifi ? NETWORK_STATE_UNMETERED : NETWORK_STATE_CONNECTED));
        } else if (hasLollipop) {
            ArgumentCaptor<JobInfo> argumentCaptor = ArgumentCaptor.forClass(JobInfo.class);
            Mockito.verify(jobScheduler).schedule(argumentCaptor.capture());
            assertThat(argumentCaptor.getValue().getService().getClassName(), is(equalTo(GenericJobService.class.getName())));
            assertThat(argumentCaptor.getValue().isRequireCharging(), is(whileCharging));
            assertThat(argumentCaptor.getValue().isPersisted(), is(true));
            assertThat(argumentCaptor.getValue().getNetworkType(), is(onWifi ? NETWORK_TYPE_UNMETERED : NETWORK_TYPE_ANY));
            assertThat(argumentCaptor.getValue().getExtras(), is(notNullValue()));
            assertThat(argumentCaptor.getValue().getExtras().getString(JOB_TYPE), is(DOWNLOAD_FILE));
        } else {
            Mockito.verify(jobScheduler).schedule(Mockito.any(JobInfo.class));
            Mockito.verify(gcmNetworkManager).schedule(Mockito.any(OneoffTask.class));
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static void applyAssertsOnSchedulingForPost(GcmNetworkManager gcmNetworkManager,
                                                       boolean isGoogleServicesAvailable,
                                                       boolean hasLollipop,
                                                       JobScheduler jobScheduler) {
        if (isGoogleServicesAvailable) {
            ArgumentCaptor<OneoffTask> peopleCaptor = ArgumentCaptor.forClass(OneoffTask.class);
            Mockito.verify(gcmNetworkManager).schedule(peopleCaptor.capture());
            assertThat(peopleCaptor.getValue().getWindowEnd(), is(30L));
            assertThat(peopleCaptor.getValue().getWindowStart(), is(0L));
            assertThat(peopleCaptor.getValue().getRequiresCharging(), is(false));
            assertThat(peopleCaptor.getValue().getExtras(), is(notNullValue()));
            assertThat(peopleCaptor.getValue().getExtras().getString(JOB_TYPE), is(POST));
            assertThat(peopleCaptor.getValue().getServiceName(), is(GenericGCMService.class.getName()));
            assertThat(peopleCaptor.getValue().getRequiredNetwork(), is(NETWORK_STATE_CONNECTED));
        } else if (hasLollipop) {
            ArgumentCaptor<JobInfo> argumentCaptor = ArgumentCaptor.forClass(JobInfo.class);
            Mockito.verify(jobScheduler).schedule(argumentCaptor.capture());
            assertThat(argumentCaptor.getValue().getService().getClassName(), is(equalTo(GenericJobService.class.getName())));
            assertThat(argumentCaptor.getValue().isRequireCharging(), is(false));
            assertThat(argumentCaptor.getValue().isPersisted(), is(true));
            assertThat(argumentCaptor.getValue().getNetworkType(), is(NETWORK_TYPE_ANY));
            assertThat(argumentCaptor.getValue().getExtras(), is(notNullValue()));
            assertThat(argumentCaptor.getValue().getExtras().getString(JOB_TYPE), is(POST));
        } else {
            Mockito.verify(jobScheduler).schedule(Mockito.any(JobInfo.class));
            Mockito.verify(gcmNetworkManager).schedule(Mockito.any(OneoffTask.class));
        }
    }

    public interface Executor {

        void run();

    }
}