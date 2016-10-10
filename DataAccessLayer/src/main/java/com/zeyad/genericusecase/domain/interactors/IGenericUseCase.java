package com.zeyad.genericusecase.domain.interactors;

import android.support.annotation.NonNull;

import com.zeyad.genericusecase.domain.interactors.requests.FileIORequest;
import com.zeyad.genericusecase.domain.interactors.requests.GetListRequest;
import com.zeyad.genericusecase.domain.interactors.requests.GetObjectRequest;
import com.zeyad.genericusecase.domain.interactors.requests.PostRequest;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;

import io.realm.RealmQuery;
import rx.Observable;
import rx.Subscriber;

public interface IGenericUseCase {
    /**
     * Gets list from full url.
     *
     * @param getListRequest contains the attributes of the request.
     * @return Observable with the list.
     */
    Observable getList(@NonNull GetListRequest getListRequest);

    /**
     * Gets object from full url.
     *
     * @param getObjectRequest contains the attributes of the request.
     * @return Observable with the Object.
     */
    Observable getObject(@NonNull GetObjectRequest getObjectRequest);

    /**
     * Post Object to full url.
     *
     * @param postRequest contains the attributes of the request.
     * @return Observable with the Object.
     */
    Observable postObject(@NonNull PostRequest postRequest);

    /**
     * Post list to full url.
     *
     * @param postRequest contains the attributes of the request.
     * @return Observable with the list.
     */
    Observable postList(@NonNull PostRequest postRequest);

    /**
     * Put Object to full url.
     *
     * @param postRequest contains the attributes of the request.
     * @return Observable with the Object.
     */
    Observable putObject(@NonNull PostRequest postRequest);

    /**
     * Put list to full url.
     *
     * @param postRequest contains the attributes of the request.
     * @return Observable with the list.
     */
    Observable putList(@NonNull PostRequest postRequest);

    /**
     * Deletes list from full url.
     *
     * @param deleteRequest contains the attributes of the request.
     * @return Observable with the list.
     */
    Observable deleteCollection(@NonNull PostRequest deleteRequest);

    /**
     * Deletes All.
     *
     * @param deleteRequest contains the attributes of the request.
     * @return Observable with the list.
     */
    Observable deleteAll(@NonNull PostRequest deleteRequest);

    /**
     * Get list of items according to the query passed.
     *
     * @param column            The key used to look for inside the DB.
     * @param query             The query used to look for inside the DB.
     * @param dataClass         Class type of the items to be deleted.
     * @param presentationClass Class type of the items to be returned.
     * @return
     */
    Observable searchDisk(String query, String column, @NonNull Class presentationClass, Class dataClass);

    /**
     * Get list of items according to the query passed.
     *
     * @param realmQuery        The query used to look for inside the DB.
     * @param presentationClass Class type of the items to be returned.
     * @return
     */
    Observable searchDisk(RealmQuery realmQuery, @NonNull Class presentationClass);

    /**
     * Uploads a file to a url.
     *
     * @param fileIORequest contains the attributes of the request,
     * @return Observable with the Object response.
     */
    Observable uploadFile(@NonNull FileIORequest fileIORequest);

    /**
     * Downloads file from the give url.
     *
     * @param fileIORequest contains the attributes of the request,
     * @return Observable with the ResponseBody
     */
    Observable downloadFile(@NonNull FileIORequest fileIORequest);

    /**
     * Returns a string of contents of the file.
     *
     * @param filePath path of the file to read.
     * @return Observable with the String.
     */
    Observable readFromResource(String filePath);

    /**
     * Returns a string of contents of the file.
     *
     * @param fullFilePath path of the file to read.
     * @return Observable with the String.
     */
    @NonNull
    Observable<String> readFromFile(String fullFilePath);

    /**
     * Saves a string of data to a file.
     *
     * @param fullFilePath path of the file to read.
     * @return Observable with the boolean of success.
     */
    Observable<Boolean> saveToFile(String fullFilePath, String data);

    /**
     * Saves a byte array of data to a file.
     *
     * @param fullFilePath path of the file to read.
     * @return Observable with the boolean of success.
     */
    Observable<Boolean> saveToFile(String fullFilePath, byte[] data);

    @SuppressWarnings("unchecked")
    @Deprecated
    void executeDynamicGetList(@NonNull Subscriber UseCaseSubscriber, String url, @NonNull Class presentationClass,
                               Class dataClass, boolean persist);

    @SuppressWarnings("unchecked")
    void executeDynamicGetList(@NonNull GetListRequest genericUseCaseRequest) throws Exception;

    @Deprecated
    void executeDynamicGetList(@NonNull Subscriber UseCaseSubscriber, String url, @NonNull Class presentationClass,
                               Class dataClass, boolean persist, boolean shouldCache);

    @Deprecated
    void executeGetObject(@NonNull Subscriber UseCaseSubscriber, String url, String idColumnName, int itemId,
                          @NonNull Class presentationClass, Class dataClass, boolean persist);

    @Deprecated
    void executeGetObject(@NonNull Subscriber UseCaseSubscriber, String url, String idColumnName, int itemId,
                          @NonNull Class presentationClass, Class dataClass, boolean persist,
                          boolean shouldCache);

    @Deprecated
    void executeGetObject(@NonNull GetObjectRequest getObjectRequest);

    @Deprecated
    void executeDynamicPostObject(@NonNull Subscriber UseCaseSubscriber, String url,
                                  String idColumnName, HashMap<String, Object> keyValuePairs,
                                  @NonNull Class presentationClass,
                                  Class dataClass, boolean persist);

    @Deprecated
    void executeDynamicPostObject(@NonNull Subscriber UseCaseSubscriber, String idColumnName, String url, JSONObject keyValuePairs,
                                  @NonNull Class presentationClass, Class dataClass,
                                  boolean persist);

    @Deprecated
    void executeDynamicPostObject(@NonNull PostRequest postRequest);

    @Deprecated
    void executeDynamicPostList(@NonNull Subscriber UseCaseSubscriber, String url, String idColumnName, JSONArray jsonArray,
                                Class dataClass, boolean persist);

    @Deprecated
    void executeDynamicPostList(@NonNull PostRequest postRequest);

    @Deprecated
    void executeDeleteCollection(@NonNull Subscriber UseCaseSubscriber, String url, HashMap<String,
            Object> keyValuePairs, Class dataClass, boolean persist);

    @Deprecated
    void executeDeleteCollection(@NonNull PostRequest deleteRequest);

    @Deprecated
    void executeDynamicPutObject(@NonNull PostRequest postRequest);

    @Deprecated
    void executeDynamicPutObject(@NonNull Subscriber UseCaseSubscriber, String url, String idColumnName, HashMap<String,
            Object> keyValuePairs, @NonNull Class presentationClass, Class dataClass,
                                 boolean persist);

    @Deprecated
    void executeDynamicPutList(@NonNull Subscriber UseCaseSubscriber, String url, String idColumnName, HashMap<String,
            Object> keyValuePairs, @NonNull Class presentationClass, Class dataClass,
                               boolean persist);

    @Deprecated
    void executeDynamicPutList(@NonNull PostRequest postRequest);

    @Deprecated
    void executeDynamicDeleteAll(@NonNull Subscriber UseCaseSubscriber, String url, Class dataClass,
                                 boolean persist);

    @Deprecated
    void executeDynamicDeleteAll(@NonNull PostRequest postRequest);

    void unsubscribe();
}
