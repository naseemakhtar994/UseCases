package com.zeyad.genericusecase.domain.interactors;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;

import com.google.gson.Gson;
import com.zeyad.genericusecase.Config;
import com.zeyad.genericusecase.R;
import com.zeyad.genericusecase.UIThread;
import com.zeyad.genericusecase.data.db.DatabaseManagerFactory;
import com.zeyad.genericusecase.data.executor.JobExecutor;
import com.zeyad.genericusecase.data.repository.DataRepository;
import com.zeyad.genericusecase.data.repository.generalstore.DataStoreFactory;
import com.zeyad.genericusecase.data.utils.IEntityMapperUtil;
import com.zeyad.genericusecase.data.utils.ModelConverters;
import com.zeyad.genericusecase.data.utils.Utils;
import com.zeyad.genericusecase.domain.executors.PostExecutionThread;
import com.zeyad.genericusecase.domain.executors.ThreadExecutor;
import com.zeyad.genericusecase.domain.interactors.requests.FileIORequest;
import com.zeyad.genericusecase.domain.interactors.requests.GetListRequest;
import com.zeyad.genericusecase.domain.interactors.requests.GetObjectRequest;
import com.zeyad.genericusecase.domain.interactors.requests.PostRequest;
import com.zeyad.genericusecase.domain.repository.Repository;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;

import io.realm.RealmQuery;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.schedulers.Schedulers;


/**
 * This class is a general implementation that represents a use case for retrieving data.
 */
public class GenericUseCase implements IGenericUseCase {

    private static GenericUseCase sGenericUseCase;
    private final Repository mRepository;
    private final ThreadExecutor mThreadExecutor;
    private final PostExecutionThread mPostExecutionThread;
    private Subscription mSubscription;

    private GenericUseCase(Repository repository, ThreadExecutor threadExecutor, PostExecutionThread postExecutionThread) {
        mThreadExecutor = threadExecutor;
        mPostExecutionThread = postExecutionThread;
        mRepository = repository;
    }

    /**
     * This function should be called at-least once before calling getInstance() method
     * This function should not be called multiple times, but only when required.
     * Ideally this function should be called once when application  is started or created.
     * This function may be called n number of times if required, during mocking and testing.
     */
    static void initWithoutDB(IEntityMapperUtil entityMapper) {
        sGenericUseCase = new GenericUseCase(new DataRepository(new DataStoreFactory(Config.getInstance()
                .getContext()), entityMapper), new JobExecutor(), new UIThread());
    }

    /**
     * This function should be called at-least once before calling getInstance() method
     * This function should not be called multiple times, but only when required.
     * Ideally this function should be called once when application  is started or created.
     * This function may be called n number of times if required, during mocking and testing.
     */
    static void initWithRealm(IEntityMapperUtil entityMapper) {
        Context context = Config.getInstance().getContext();
        DatabaseManagerFactory.initRealm(context);
        sGenericUseCase = new GenericUseCase(new DataRepository(new DataStoreFactory(DatabaseManagerFactory
                .getInstance(), context), entityMapper), new JobExecutor(), new UIThread());
    }

    /**
     * Testing only!
     * This function should be called at-least once before calling getInstance() method
     * This function should not be called multiple times, but only when required.
     * Ideally this function should be called once when application  is started or created.
     * This function may be called n number of times if required, during mocking and testing.
     *
     * @param dataRepository data repository
     * @param jobExecutor    job executor
     * @param uiThread       ui thread implementation
     */
    @VisibleForTesting
    public static void init(DataRepository dataRepository, JobExecutor jobExecutor, UIThread uiThread) {
        sGenericUseCase = new GenericUseCase(dataRepository, jobExecutor, uiThread);
    }

    public static GenericUseCase getInstance() {
        if (sGenericUseCase == null)
            throw new NullPointerException("GenericUseCase#initRealm must be called before calling getInstance()");
        return sGenericUseCase;
    }

    /**
     * Executes the current use case.
     *
     * @param UseCaseSubscriber The guy who will be listen to the observable build with .
     */
    @Override
    @Deprecated
    public void executeDynamicGetList(@NonNull Subscriber UseCaseSubscriber, String url, @NonNull Class presentationClass,
                                      Class dataClass, boolean persist) {
        executeDynamicGetList(UseCaseSubscriber, url, presentationClass, dataClass, persist, false);
    }

    /**
     * Executes the current use case.
     *
     * @param genericUseCaseRequest The guy who will be listen to the observable build with .
     */
    @Override
    public void executeDynamicGetList(@NonNull GetListRequest genericUseCaseRequest) throws Exception {
        executeDynamicGetList(genericUseCaseRequest.getSubscriber(), genericUseCaseRequest.getUrl(),
                genericUseCaseRequest.getPresentationClass(), genericUseCaseRequest.getDataClass(),
                genericUseCaseRequest.isPersist(), genericUseCaseRequest.isShouldCache());
    }

    /**
     * Executes the current use case.
     *
     * @param UseCaseSubscriber The guy who will be listen to the observable build with .
     */
    @Override
    @Deprecated
    public void executeDynamicGetList(@NonNull Subscriber UseCaseSubscriber, String url, @NonNull Class presentationClass,
                                      Class dataClass, boolean persist, boolean shouldCache) {
        mSubscription = getList(new GetListRequest.GetListRequestBuilder(dataClass, persist)
                .presentationClass(presentationClass).url(url).shouldCache(shouldCache).build())
                .subscribe(UseCaseSubscriber);
    }

    /**
     * Executes the current use case.
     *
     * @param genericUseCaseRequest The guy who will be listen to the observable build with .
     */
    @Override
    public Observable getList(@NonNull GetListRequest genericUseCaseRequest) {
        return mRepository.getListDynamically(genericUseCaseRequest.getUrl(), genericUseCaseRequest
                .getPresentationClass(), genericUseCaseRequest.getDataClass(), genericUseCaseRequest
                .isPersist(), genericUseCaseRequest.isShouldCache()).compose(applySchedulers());
    }

    /**
     * Executes the current use case.
     *
     * @param UseCaseSubscriber The guy who will be listen to the observable build with .
     */
    @Override
    @Deprecated
    public void executeGetObject(@NonNull Subscriber UseCaseSubscriber, String url, String idColumnName,
                                 int itemId, @NonNull Class presentationClass, Class dataClass, boolean persist) {
        executeGetObject(UseCaseSubscriber, url, idColumnName, itemId, presentationClass, dataClass,
                persist, false);
    }

    /**
     * Executes the current use case.
     *
     * @param UseCaseSubscriber The guy who will be listen to the observable build with .
     */
    @Override
    @Deprecated
    public void executeGetObject(@NonNull Subscriber UseCaseSubscriber, String url, String idColumnName,
                                 int itemId, @NonNull Class presentationClass, Class dataClass,
                                 boolean persist, boolean shouldCache) {
        executeGetObject(new GetObjectRequest.GetObjectRequestBuilder(dataClass, persist).id(itemId)
                .subscriber(UseCaseSubscriber).idColumnName(idColumnName).presentationClass(presentationClass)
                .url(url).shouldCache(shouldCache).build());
    }

    /**
     * Executes the current use case.
     *
     * @param getObjectRequest The guy who will be listen to the observable build with .
     */
    @Override
    public void executeGetObject(@NonNull GetObjectRequest getObjectRequest) {
        mSubscription = getObject(getObjectRequest).subscribe(getObjectRequest.getSubscriber());
    }

    /**
     * Executes the current use case.
     *
     * @param getObjectRequest The guy who will be listen to the observable build with .
     */
    @Override
    public Observable getObject(@NonNull GetObjectRequest getObjectRequest) {
        return mRepository.getObjectDynamicallyById(getObjectRequest.getUrl(), getObjectRequest
                        .getIdColumnName(), getObjectRequest.getItemId(), getObjectRequest.getPresentationClass(),
                getObjectRequest.getDataClass(), getObjectRequest.isPersist(), getObjectRequest.isShouldCache())
                .compose(applySchedulers());
    }


    /**
     * Executes the current use case.
     *
     * @param UseCaseSubscriber The guy who will be listen to the observable build with .
     * @param idColumnName
     */
    @Override
    @Deprecated
    public void executeDynamicPostObject(@NonNull Subscriber UseCaseSubscriber, String url,
                                         String idColumnName, HashMap<String, Object> keyValuePairs,
                                         @NonNull Class presentationClass, Class dataClass, boolean persist) {
        executeDynamicPostObject(new PostRequest(UseCaseSubscriber, idColumnName, url, keyValuePairs,
                presentationClass, dataClass, persist));
    }

    /**
     * Executes the current use case.
     *
     * @param UseCaseSubscriber The guy who will be listen to the observable build with .
     * @param idColumnName
     */
    @Override
    @Deprecated
    public void executeDynamicPostObject(@NonNull Subscriber UseCaseSubscriber, String idColumnName,
                                         String url, JSONObject keyValuePairs, @NonNull Class presentationClass,
                                         Class dataClass, boolean persist) {
        executeDynamicPostObject(new PostRequest(UseCaseSubscriber, idColumnName, url, keyValuePairs,
                presentationClass, dataClass, persist));
    }

    /**
     * Executes the current use case.
     *
     * @param postRequest The guy who will be listen to the observable build with .
     */
    @Override
    public void executeDynamicPostObject(@NonNull PostRequest postRequest) {
        mSubscription = postObject(postRequest).subscribe(postRequest.getSubscriber());
    }

    @Override
    public Observable postObject(@NonNull PostRequest postRequest) {
        if (postRequest.getObjectBundle() != null)
            return mRepository.postObjectDynamically(postRequest.getUrl(), postRequest.getIdColumnName(),
                    postRequest.getObjectBundle(), postRequest.getPresentationClass(), postRequest
                            .getDataClass(), postRequest.isPersist()).compose(applySchedulers());
        else if (postRequest.getContentValue() != null)
            return mRepository.postObjectDynamically(postRequest.getUrl(), postRequest.getIdColumnName(),
                    postRequest.getContentValue(), postRequest.getPresentationClass(), postRequest
                            .getDataClass(), postRequest.isPersist()).compose(applySchedulers());
        else
            return Observable.defer(() -> Observable.error(new IllegalArgumentException(Config.getInstance()
                    .getContext().getString(R.string.null_payload))));
    }

    /**
     * Executes the current use case.
     *
     * @param UseCaseSubscriber The guy who will be listen to the observable build with .
     * @param idColumnName
     */
    @Override
    @Deprecated
    public void executeDynamicPostList(@NonNull Subscriber UseCaseSubscriber, String url, String idColumnName,
                                       JSONArray jsonArray, Class dataClass, boolean persist) {
        executeDynamicPostList(new PostRequest.PostRequestBuilder(dataClass, persist)
                .subscriber(UseCaseSubscriber).url(url).idColumnName(idColumnName).jsonArray(jsonArray).build());
    }

    /**
     * Executes the current use case.
     *
     * @param postRequest The guy who will be listen to the observable build with .
     */
    @Override
    public void executeDynamicPostList(@NonNull PostRequest postRequest) {
        mSubscription = postList(postRequest).subscribe(postRequest.getSubscriber());
    }

    @Override
    public Observable postList(@NonNull PostRequest postRequest) {
        JSONArray jsonArray = null;
        if (postRequest.getKeyValuePairs() != null)
            jsonArray = ModelConverters.convertToJsonArray(postRequest.getKeyValuePairs());
        else if (postRequest.getJsonArray() != null)
            jsonArray = postRequest.getJsonArray();
        if (jsonArray != null)
            return mRepository.postListDynamically(postRequest.getUrl(), postRequest.getIdColumnName(),
                    jsonArray, postRequest.getPresentationClass(), postRequest.getDataClass(),
                    postRequest.isPersist()).compose(applySchedulers());
        else if (postRequest.getContentValues() != null)
            return mRepository.postListDynamically(postRequest.getUrl(), postRequest.getIdColumnName(),
                    postRequest.getContentValues(), postRequest.getPresentationClass(), postRequest
                            .getDataClass(), postRequest.isPersist()).compose(applySchedulers());
        else return Observable.error(new IllegalArgumentException(Config.getInstance().getContext()
                    .getString(R.string.null_payload)));
    }


    /**
     * Executes the current use case.
     */
    @Override
    public Observable searchDisk(String query, String column, @NonNull Class presentationClass,
                                 Class dataClass) {
        return mRepository.searchDisk(query, column, presentationClass, dataClass)
                .compose(applySchedulers());
    }

    /**
     * Executes the current use case.
     */
    @Override
    public Observable searchDisk(RealmQuery realmQuery, @NonNull Class presentationClass) {
        return mRepository.searchDisk(realmQuery, presentationClass)
                .compose(applySchedulers());
    }

    /**
     * Executes the current use case.
     *
     * @param UseCaseSubscriber The guy who will be listen to the observable build with .
     */
    @Override
    @Deprecated
    public void executeDeleteCollection(@NonNull Subscriber UseCaseSubscriber, String url, HashMap<String,
            Object> keyValuePairs, Class dataClass, boolean persist) {
        executeDeleteCollection(new PostRequest.PostRequestBuilder(dataClass, persist)
                .subscriber(UseCaseSubscriber).url(url).hashMap(keyValuePairs).build());
    }

    /**
     * Executes the current use case.
     *
     * @param deleteRequest The guy who will be listen to the observable build with .
     */
    @Override
    public void executeDeleteCollection(@NonNull PostRequest deleteRequest) {
        mSubscription = deleteCollection(deleteRequest).subscribe(deleteRequest.getSubscriber());
    }

    @Override
    public Observable deleteCollection(@NonNull PostRequest deleteRequest) {
        JSONArray jsonArray = null;
        if (deleteRequest.getKeyValuePairs() != null)
            jsonArray = ModelConverters.convertToJsonArray(deleteRequest.getKeyValuePairs());
        else if (deleteRequest.getJsonArray() != null)
            jsonArray = deleteRequest.getJsonArray();
        return mRepository.deleteListDynamically(deleteRequest.getUrl(), jsonArray,
                deleteRequest.getPresentationClass(), deleteRequest.getDataClass(), deleteRequest.isPersist())
                .compose(applySchedulers());
    }

    /**
     * Executes the current use case.
     *
     * @param UseCaseSubscriber The guy who will be listen to the observable build with .
     * @param idColumnName
     */
    @Override
    @Deprecated
    public void executeDynamicPutObject(@NonNull Subscriber UseCaseSubscriber, String url, String idColumnName,
                                        HashMap<String, Object> keyValuePairs, @NonNull Class presentationClass,
                                        Class dataClass, boolean persist) {
        executeDynamicPutObject(new PostRequest(UseCaseSubscriber, idColumnName, url, keyValuePairs,
                presentationClass, dataClass, persist));
    }

    /**
     * Executes the current use case.
     *
     * @param postRequest The guy who will be listen to the observable build with .
     */
    @Override
    public void executeDynamicPutObject(@NonNull PostRequest postRequest) {
        mSubscription = putObject(postRequest).subscribe(postRequest.getSubscriber());
    }

    /**
     * Executes the current use case.
     *
     * @param postRequest The guy who will be listen to the observable build with .
     */
    @Override
    public Observable putObject(@NonNull PostRequest postRequest) {
        if (postRequest.getObjectBundle() != null)
            return mRepository.putObjectDynamically(postRequest.getUrl(), postRequest.getIdColumnName(),
                    postRequest.getObjectBundle(), postRequest.getPresentationClass(),
                    postRequest.getDataClass(), postRequest.isPersist()).compose(applySchedulers());
        else if (postRequest.getContentValue() != null)
            return mRepository.putObjectDynamically(postRequest.getUrl(), postRequest.getIdColumnName(),
                    postRequest.getContentValue(), postRequest.getPresentationClass(), postRequest
                            .getDataClass(), postRequest.isPersist()).compose(applySchedulers());
        else return Observable.error(new IllegalArgumentException(Config.getInstance().getContext()
                    .getString(R.string.null_payload)));
    }

    @Override
    public Observable uploadFile(@NonNull FileIORequest fileIORequest) {
        return mRepository.uploadFileDynamically(fileIORequest.getUrl(), fileIORequest.getFile(),
                fileIORequest.onWifi(), fileIORequest.isWhileCharging(), fileIORequest.getPresentationClass(),
                fileIORequest.getDataClass())
                .compose(applySchedulers());
    }

    @Override
    public Observable downloadFile(@NonNull FileIORequest fileIORequest) {
        return mRepository.downloadFileDynamically(fileIORequest.getUrl(), fileIORequest.getFile(),
                fileIORequest.onWifi(), fileIORequest.isWhileCharging(), fileIORequest.getPresentationClass(),
                fileIORequest.getDataClass()).compose(applySchedulers());
    }

    /**
     * Executes the current use case.
     *
     * @param UseCaseSubscriber The guy who will be listen to the observable build with .
     * @param idColumnName
     */
    @Override
    @Deprecated
    public void executeDynamicPutList(@NonNull Subscriber UseCaseSubscriber, String url, String idColumnName,
                                      HashMap<String, Object> keyValuePairs, @NonNull Class presentationClass,
                                      Class dataClass, boolean persist) {
        executeDynamicPutList(new PostRequest(UseCaseSubscriber, idColumnName, url, keyValuePairs,
                presentationClass, dataClass, persist));
    }

    /**
     * Executes the current use case.
     *
     * @param postRequest The guy who will be listen to the observable build with .
     */

    @Override
    public void executeDynamicPutList(@NonNull PostRequest postRequest) {
        mSubscription = putList(postRequest).subscribe(postRequest.getSubscriber());
    }

    /**
     * Executes the current use case.
     */
    @Override
    public Observable putList(@NonNull PostRequest postRequest) {
        JSONArray jsonArray = null;
        if (postRequest.getKeyValuePairs() != null)
            jsonArray = ModelConverters.convertToJsonArray(postRequest.getKeyValuePairs());
        else if (postRequest.getJsonArray() != null)
            jsonArray = postRequest.getJsonArray();
        if (jsonArray != null)
            return mRepository.putListDynamically(postRequest.getUrl(), postRequest.getIdColumnName(),
                    jsonArray, postRequest.getPresentationClass(), postRequest.getDataClass(),
                    postRequest.isPersist()).compose(applySchedulers());
        else if (postRequest.getContentValues() != null)
            return mRepository.putListDynamically(postRequest.getUrl(), postRequest.getIdColumnName(),
                    postRequest.getContentValues(), postRequest.getPresentationClass(),
                    postRequest.getDataClass(), postRequest.isPersist()).compose(applySchedulers());
        else
            return Observable.error(new IllegalArgumentException(Config.getInstance().getContext()
                    .getString(R.string.null_payload)));
    }

    /**
     * Executes the current use case.
     *
     * @param UseCaseSubscriber The guy who will be listen to the observable build with .
     */
    @Override
    @Deprecated
    public void executeDynamicDeleteAll(@NonNull Subscriber UseCaseSubscriber, String url,
                                        Class dataClass, boolean persist) {
        executeDynamicDeleteAll(new PostRequest.PostRequestBuilder(dataClass, persist)
                .subscriber(UseCaseSubscriber).url(url).build());
    }

    /**
     * Executes the current use case.
     *
     * @param postRequest The guy who will be listen to the observable build with .
     */
    @Override
    public void executeDynamicDeleteAll(@NonNull PostRequest postRequest) {
        mSubscription = deleteAll(postRequest).subscribe(postRequest.getSubscriber());
    }

    /**
     * Executes the current use case.
     *
     * @param postRequest The guy who will be listen to the observable build with .
     */
    @Override
    public Observable<?> deleteAll(@NonNull PostRequest postRequest) {
        return mRepository.deleteAllDynamically(postRequest.getUrl(), postRequest.getDataClass(),
                postRequest.isPersist()).compose(applySchedulers());
    }


    @Override
    public Observable<String> readFromResource(String filePath) {
        return Observable.defer(() -> {
            StringBuilder returnString = new StringBuilder();
            InputStream fIn = null;
            InputStreamReader isr = null;
            BufferedReader input = null;
            try {
                fIn = Config.getInstance().getContext().getResources().getAssets().open(filePath);
                isr = new InputStreamReader(fIn);
                input = new BufferedReader(isr);
                String line;
                while ((line = input.readLine()) != null)
                    returnString.append(line);
            } catch (IOException e) {
                e.printStackTrace();
                return Observable.error(e);
            } finally {
                if (isr != null)
                    try {
                        isr.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                if (fIn != null)
                    try {
                        fIn.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                if (input != null)
                    try {
                        input.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
            }
            return Observable.just(returnString.toString());
        }).compose(applySchedulers());
    }

    @NonNull
    @Override
    public Observable<String> readFromFile(@NonNull String fullFilePath) {
        try {
            FileInputStream fis = Config.getInstance().getContext().openFileInput(fullFilePath);
            ObjectInputStream is = new ObjectInputStream(fis);
            String data = (String) is.readObject();
            is.close();
            return Observable.just(data);
        } catch (@NonNull ClassNotFoundException | IOException e) {
            e.printStackTrace();
            try {
                return Observable.just(new Gson()
                        .fromJson(new InputStreamReader(new FileInputStream(new File(fullFilePath))),
                                String.class));
            } catch (FileNotFoundException e1) {
                e1.printStackTrace();
                return Observable.error(e1);
            }
        }
    }

    @Override
    public Observable<Boolean> saveToFile(String fullFilePath, String data) {
        return Observable.defer(() -> {
            FileOutputStream fos = null;
            try {
                fos = Config.getInstance().getContext().openFileOutput(fullFilePath, Context.MODE_PRIVATE);
                ObjectOutputStream os = new ObjectOutputStream(fos);
                os.writeObject(data);
                os.flush();
                os.close();
                return Observable.just(true);
            } catch (IOException e) {
                e.printStackTrace();
                return Observable.error(e);
            }
        }).compose(applySchedulers());
    }

    @Override
    public Observable<Boolean> saveToFile(@NonNull String fullFilePath, @NonNull byte[] data) {
        return Observable.defer(() -> {
            try {
                FileOutputStream outStream = new FileOutputStream(new File(fullFilePath));
                outStream.write(data);
                outStream.flush();
                outStream.close();
                return Observable.just(true);
            } catch (IOException e) {
                e.printStackTrace();
                return Observable.error(e);
            }
        }).compose(applySchedulers());
    }

    /**
     * Unsubscribes from current {@link Subscription}.
     */
    @Override
    public void unsubscribe() {
        Utils.unsubscribeIfNotNull(mSubscription);
    }

    /**
     * Apply the default android schedulers to a observable
     *
     * @param <T> the current observable
     * @return the transformed observable
     */
    @NonNull
    private <T> Observable.Transformer<T, T> applySchedulers() {
        return observable -> observable.subscribeOn(Schedulers.from(mThreadExecutor))
                .observeOn(mPostExecutionThread.getScheduler())
                .unsubscribeOn(Schedulers.from(mThreadExecutor));
    }
}
