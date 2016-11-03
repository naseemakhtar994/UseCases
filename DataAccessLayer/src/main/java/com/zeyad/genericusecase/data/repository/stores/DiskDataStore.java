package com.zeyad.genericusecase.data.repository.stores;

import android.support.annotation.NonNull;

import com.zeyad.genericusecase.Config;
import com.zeyad.genericusecase.R;
import com.zeyad.genericusecase.data.db.DataBaseManager;
import com.zeyad.genericusecase.data.mappers.EntityMapper;
import com.zeyad.genericusecase.data.utils.ModelConverters;
import com.zeyad.genericusecase.data.utils.Utils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.List;

import io.realm.RealmQuery;
import io.reactivex.Observable;

public class DiskDataStore implements DataStore {

    private final String TAG = DiskDataStore.class.getName();
    private DataBaseManager mDataBaseManager;
    private EntityMapper mEntityDataMapper;

    /**
     * Construct a {@link DataStore} based file system data store.
     *
     * @param realmManager A {@link DataBaseManager} to cache data retrieved from the api.
     */
    DiskDataStore(DataBaseManager realmManager, EntityMapper entityDataMapper) {
        mDataBaseManager = realmManager;
        mEntityDataMapper = entityDataMapper;
    }

    @NonNull
    @Override
    public Observable<List> dynamicGetList(String url, Class domainClass, Class dataClass, boolean persist,
                                           boolean shouldCache) {
        return mDataBaseManager.getAll(dataClass)
                .map(realmModels -> mEntityDataMapper.transformAllToDomain(realmModels))
                .compose(Utils.logSources(TAG, mDataBaseManager));
    }

    @NonNull
    @Override
    public Observable<?> dynamicGetObject(String url, String idColumnName, int itemId, Class domainClass,
                                          Class dataClass, boolean persist, boolean shouldCache) {
        return mDataBaseManager.getById(idColumnName, itemId, dataClass)
                .map(realmModel -> mEntityDataMapper.transformToDomain(realmModel));
    }

    @NonNull
    @Override
    public Observable<List> searchDisk(String query, String column, Class domainClass, Class dataClass) {
        return mDataBaseManager.getWhere(dataClass, query, column)
                .map(realmModel -> mEntityDataMapper.transformAllToDomain(realmModel));
    }

    @NonNull
    @Override
    public Observable<List> searchDisk(RealmQuery query, Class domainClass) {
        return mDataBaseManager.getWhere(query)
                .map(realmModel -> mEntityDataMapper.transformAllToDomain(realmModel));
    }

    @NonNull
    @Override
    public Observable<?> dynamicDownloadFile(String url, File file, boolean onWifi, boolean whileCharging,
                                             boolean queuable) {
        return Observable.error(new IllegalStateException(Config.getInstance().getContext().getString(R.string.io_to_db_error)));
    }

    @NonNull
    @Override
    public Observable<?> dynamicDeleteCollection(String url, String idColumnName, JSONArray jsonArray,
                                                 Class dataClass, boolean persist, boolean queuable) {
        return Observable.defer(() -> mDataBaseManager.evictCollection(idColumnName,
                ModelConverters.convertToListOfId(jsonArray), dataClass));
    }

    @NonNull
    @Override
    public Observable<Boolean> dynamicDeleteAll(String url, Class dataClass, boolean persist) {
        return mDataBaseManager.evictAll(dataClass);
    }

    @NonNull
    @Override
    public Observable<?> dynamicPostObject(String url, String idColumnName, JSONObject jsonObject,
                                           Class domainClass, Class dataClass, boolean persist, boolean queuable) {
        return Observable.defer(() -> mDataBaseManager.put(jsonObject, idColumnName, dataClass));
    }

    @NonNull
    @Override
    public Observable<?> dynamicPutObject(String url, String idColumnName, JSONObject jsonObject,
                                          Class domainClass, Class dataClass, boolean persist, boolean queuable) {
        return Observable.defer(() -> mDataBaseManager.put(jsonObject, idColumnName, dataClass));
    }

    @NonNull
    @Override
    public Observable<?> dynamicUploadFile(String url, File file, boolean onWifi, boolean whileCharging,
                                           boolean queuable, Class domainClass) {
        return Observable.error(new IllegalStateException(Config.getInstance().getContext().getString(R.string.io_to_db_error)));
    }

    @NonNull
    @Override
    public Observable<?> dynamicPostList(String url, String idColumnName, JSONArray jsonArray,
                                         Class domainClass, Class dataClass, boolean persist, boolean queuable) {
        return Observable.defer(() -> mDataBaseManager.putAll(jsonArray, idColumnName, dataClass));
    }

    @NonNull
    @Override
    public Observable<?> dynamicPutList(String url, String idColumnName, JSONArray jsonArray,
                                        Class domainClass, Class dataClass, boolean persist, boolean queuable) {
        return Observable.defer(() -> mDataBaseManager.putAll(jsonArray, idColumnName, dataClass));
    }
}
