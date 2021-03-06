package com.zeyad.usecases.data.repository.stores;

import android.support.annotation.NonNull;

import com.zeyad.usecases.Config;
import com.zeyad.usecases.data.db.DataBaseManager;
import com.zeyad.usecases.data.db.RealmManager;
import com.zeyad.usecases.data.mappers.IDAOMapper;
import com.zeyad.usecases.data.utils.ModelConverters;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;
import java.util.List;

import rx.Observable;
import st.lowlevel.storo.Storo;

public class DiskDataStore implements DataStore {
    private static final String IO_DB_ERROR = "Can not IO file to local DB";
    private DataBaseManager mDataBaseManager;
    private IDAOMapper mEntityDataMapper;

    /**
     * Construct a {@link DataStore} based file system data store.
     *
     * @param realmManager A {@link DataBaseManager} to cache data retrieved from the api.
     */
    DiskDataStore(DataBaseManager realmManager, IDAOMapper entityDataMapper) {
        mDataBaseManager = realmManager;
        mEntityDataMapper = entityDataMapper;
    }

    @NonNull
    @Override
    public Observable<?> dynamicGetObject(String url, String idColumnName, int itemId, Class domainClass,
                                          Class dataClass, boolean persist, boolean shouldCache) {
        if (Config.isWithCache() && Storo.contains(dataClass.getSimpleName() + itemId))
            return Storo.get(dataClass.getSimpleName() + itemId, dataClass).async()
                    .map(realmModel -> mEntityDataMapper.mapToDomain(realmModel, domainClass));
        else
            return mDataBaseManager.getById(idColumnName, itemId, dataClass)
                    .map(realmModel -> {
                        try {
                            if (Config.isWithCache() && !Storo.contains(dataClass.getSimpleName() + itemId))
                                cacheObject(idColumnName, new JSONObject(gson.toJson(realmModel)),
                                        dataClass);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        if (domainClass == dataClass)
                            return realmModel;
                        else return mEntityDataMapper.mapToDomain(realmModel, domainClass);
                    });
    }

    @NonNull
    @Override
    public Observable<List> dynamicGetList(String url, Class domainClass, Class dataClass, boolean persist,
                                           boolean shouldCache) {
        return mDataBaseManager.getAll(dataClass)
                .map(realmModels -> {
                    if (domainClass == dataClass)
                        return realmModels;
                    else return mEntityDataMapper.mapAllToDomain(realmModels, domainClass);
                });
    }

    @Override
    public Observable<?> dynamicPatchObject(String url, String idColumnName, @NonNull JSONObject jsonObject,
                                            Class domainClass, Class dataClass, boolean persist, boolean queuable) {
        return mDataBaseManager.put(jsonObject, idColumnName, dataClass)
                .doOnNext(o -> {
                    if (Config.isWithCache())
                        cacheObject(idColumnName, jsonObject, dataClass);
                });
    }

    @NonNull
    @Override
    public Observable<List> queryDisk(RealmManager.RealmQueryProvider queryFactory, Class domainClass) {
        return mDataBaseManager.getQuery(queryFactory)
                .map(realmModel -> {
                    if (domainClass == realmModel.getClass())
                        return realmModel;
                    else return mEntityDataMapper.mapToDomain(realmModel, domainClass);
                });
    }

    @NonNull
    @Override
    public Observable<?> dynamicDeleteCollection(String url, String idColumnName, JSONArray jsonArray,
                                                 Class dataClass, boolean persist, boolean queuable) {
        List<Long> convertToListOfId = ModelConverters.convertToListOfId(jsonArray);
        return mDataBaseManager.evictCollection(idColumnName, convertToListOfId, dataClass)
                .doOnNext(o -> {
                    if (Config.isWithCache()) {
                        for (int i = 0, convertToListOfIdSize = convertToListOfId != null ? convertToListOfId.size() : 0;
                             i < convertToListOfIdSize; i++)
                            Storo.delete(dataClass.getSimpleName() + convertToListOfId.get(i));
                    }
                });
    }

    @NonNull
    @Override
    public Observable<Boolean> dynamicDeleteAll(Class dataClass) {
        return mDataBaseManager.evictAll(dataClass);
    }

    @NonNull
    @Override
    public Observable<?> dynamicPostObject(String url, String idColumnName, JSONObject jsonObject,
                                           Class domainClass, Class dataClass, boolean persist, boolean queuable) {
        return mDataBaseManager.put(jsonObject, idColumnName, dataClass)
                .doOnNext(o -> {
                    if (Config.isWithCache())
                        cacheObject(idColumnName, jsonObject, dataClass);
                });
    }

    @NonNull
    @Override
    public Observable<?> dynamicPostList(String url, String idColumnName, JSONArray jsonArray,
                                         Class domainClass, Class dataClass, boolean persist, boolean queuable) {
        return mDataBaseManager.putAll(jsonArray, idColumnName, dataClass);
    }

    @NonNull
    @Override
    public Observable<?> dynamicPutObject(String url, String idColumnName, JSONObject jsonObject,
                                          Class domainClass, Class dataClass, boolean persist, boolean queuable) {
        return mDataBaseManager.put(jsonObject, idColumnName, dataClass)
                .doOnNext(o -> {
                    if (Config.isWithCache())
                        cacheObject(idColumnName, jsonObject, dataClass);
                });
    }

    @NonNull
    @Override
    public Observable<?> dynamicPutList(String url, String idColumnName, JSONArray jsonArray,
                                        Class domainClass, Class dataClass, boolean persist, boolean queuable) {
        return mDataBaseManager.putAll(jsonArray, idColumnName, dataClass);
    }

    private void cacheObject(String idColumnName, JSONObject jsonObject, Class dataClass) {
        Storo.put(dataClass.getSimpleName() + jsonObject.optString(idColumnName), gson
                .fromJson(jsonObject.toString(), dataClass))
                .setExpiry(Config.getCacheAmount(), Config.getCacheTimeUnit())
                .execute();
    }

    private void cacheList(String idColumnName, JSONArray jsonArray, Class dataClass) {
        for (int i = 0, size = jsonArray.length(); i < size; i++) {
            cacheObject(idColumnName, jsonArray.optJSONObject(i), dataClass);
        }
    }

    @NonNull
    @Override
    public Observable<?> dynamicUploadFile(String url, File file, String key, HashMap<String, Object> parameters,
                                           boolean onWifi, boolean whileCharging, boolean queuable, Class domainClass) {
        return Observable.error(new IllegalStateException(IO_DB_ERROR));
    }

    @NonNull
    @Override
    public Observable<?> dynamicDownloadFile(String url, File file, boolean onWifi, boolean whileCharging,
                                             boolean queuable) {
        return Observable.error(new IllegalStateException(IO_DB_ERROR));
    }
}
