package com.zeyad.usecases.data.db;

import android.support.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

import io.realm.RealmModel;
import io.realm.RealmObject;
import rx.Observable;

/**
 * Interface for the Database modules.
 */
public interface DataBaseManager {

    /**
     * Gets an {@link Observable} which will emit an Object.
     *
     * @param clazz        Class type of the items to get.
     * @param idColumnName Name of the id field.
     * @param userId       The user id to retrieve data.
     */
    @NonNull
    Observable<?> getById(final String idColumnName, final int userId, Class clazz);

    /**
     * Gets an {@link Observable} which will emit a List of Objects.
     *
     * @param clazz Class type of the items to get.
     */
    @NonNull
    Observable<List> getAll(Class clazz);

    /**
     * Puts and element into the DB.
     *
     * @param realmModel Element to insert in the DB.
     * @param dataClass  Class type of the items to be put.
     */
    @NonNull
    <M extends RealmModel> Observable<?> put(M realmModel, Class dataClass);

    /**
     * Puts and element into the DB.
     *
     * @param jsonObject Element to insert in the DB.
     * @param dataClass  Class type of the items to be put.
     */
    @NonNull
    Observable<?> put(JSONObject jsonObject, String idColumnName, Class dataClass);

    /**
     * Puts and element into the DB.
     *
     * @param realmObjects Element to insert in the DB.
     * @param dataClass    Class type of the items to be put.
     */
    <M extends RealmModel> Observable<?> putAll(List<M> realmObjects, Class dataClass);

    /**
     * Puts and element into the DB.
     *
     * @param jsonArray    Element to insert in the DB.
     * @param idColumnName Name of the id field.
     * @param dataClass    Class type of the items to be put.
     */
    @NonNull
    Observable<?> putAll(JSONArray jsonArray, String idColumnName, Class dataClass);

    /**
     * Evict all elements of the DB.
     *
     * @param clazz Class type of the items to be deleted.
     */
    @NonNull
    Observable<Boolean> evictAll(Class clazz);

    /**
     * Evict element of the DB.
     *
     * @param realmModel Element to deleted from the DB.
     * @param clazz      Class type of the items to be deleted.
     */
    void evict(final RealmObject realmModel, Class clazz);

    /**
     * Evict element by id of the DB.
     *
     * @param clazz        Class type of the items to be deleted.
     * @param idFieldName  The id used to look for inside the DB.
     * @param idFieldValue Name of the id field.
     */
    boolean evictById(Class clazz, String idFieldName, long idFieldValue);

    /**
     * Evict a collection elements of the DB.
     *
     * @param idFieldName The id used to look for inside the DB.
     * @param list        List of ids to be deleted.
     * @param dataClass   Class type of the items to be deleted.
     */
    @NonNull
    Observable<Boolean> evictCollection(String idFieldName, List<Long> list, Class dataClass);

    /**
     * Get list of items according to the query passed.
     *
     * @param queryFactory The query used to look for inside the DB.
     */
    @NonNull
    <T extends RealmModel> Observable<List<T>> getQuery(RealmManager.RealmQueryProvider<T> queryFactory);
}