package com.zeyad.genericusecase.data.db;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.zeyad.genericusecase.Config;
import com.zeyad.genericusecase.R;
import com.zeyad.genericusecase.data.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;
import io.realm.Case;
import io.realm.Realm;
import io.realm.RealmModel;
import io.realm.RealmObject;
import io.realm.RealmQuery;

/**
 * {@link DataBaseManager} implementation.
 */
public class RealmManager implements DataBaseManager {

    private static final long EXPIRATION_TIME = 600000;
    private static DataBaseManager sInstance;
    final String TAG = RealmManager.class.getName();
    private Realm mRealm;
    private Context mContext;

    private RealmManager(Context context) {
        mContext = context;
    }

    /**
     * Use this function to re-instantiate general realm manager or instance for the first time.
     * Previous instances would be deleted and new created
     *
     * @param context Application Context
     */
    static void init(Context context) {
        sInstance = new RealmManager(context);
    }

    /**
     * @return RealmManager the implemented instance of the DatabaseManager.
     */
    static DataBaseManager getInstance() {
        if (sInstance == null)
            throw new NullPointerException(Config.getInstance().getContext().getString(R.string.realm_uninitialized));
        return sInstance;
    }

    /**
     * Gets an {@link Observable} which will emit an Object.
     *
     * @param dataClass    Class type of the items to get.
     * @param idColumnName Name of the id field.
     * @param itemId       The user id to retrieve data.
     */
    @NonNull
    @Override
    public Observable<?> getById(@NonNull final String idColumnName, final int itemId, Class dataClass) {
        return Observable.defer(() -> {
            int finalItemId = itemId;
            if (finalItemId <= 0)
                finalItemId = Utils.getMaxId(dataClass, idColumnName);
            mRealm = Realm.getDefaultInstance();
            return Observable.just(mRealm.copyFromRealm(mRealm.where(dataClass).equalTo(idColumnName,
                    finalItemId).findFirst()));
        });
    }

    /**
     * Gets an {@link Observable} which will emit a List of Objects.
     *
     * @param clazz Class type of the items to get.
     */
    @NonNull
    @Override
    public Observable<List> getAll(Class clazz) {
        return Observable.defer(() -> {
            mRealm = Realm.getDefaultInstance();
            return Observable.just(mRealm.copyFromRealm(mRealm.where(clazz).findAll()));
        });
    }

    /**
     * Get list of items according to the query passed.
     *
     * @param filterKey The key used to look for inside the DB.
     * @param query     The query used to look for inside the DB.
     * @param clazz     Class type of the items to be deleted.
     */
    @NonNull
    @Override
    public Observable<List> getWhere(Class clazz, String query, @NonNull String filterKey) {
        return Observable.defer(() -> {
            mRealm = Realm.getDefaultInstance();
            return Observable.just(mRealm.copyFromRealm(mRealm.where(clazz).beginsWith(filterKey,
                    query, Case.INSENSITIVE).findAll()));
        });
    }

    /**
     * Get list of items according to the query passed.
     *
     * @param realmQuery The query used to look for inside the DB.
     */
    @NonNull
    @Override
    public Observable<List> getWhere(@NonNull RealmQuery realmQuery) {
        return Observable.defer(() -> Observable.just(realmQuery.findAll()));
    }

    /**
     * Puts and element into the DB.
     *
     * @param realmObject Element to insert in the DB.
     * @param dataClass   Class type of the items to be put.
     */
    @NonNull
    @Override
    public Observable<?> put(@Nullable RealmObject realmObject, @NonNull Class dataClass) {
        if (realmObject != null) {
            return Observable.defer(() -> {
                mRealm = Realm.getDefaultInstance();
                RealmObject result = executeWriteOperationInRealm(mRealm, () -> mRealm.copyToRealmOrUpdate(realmObject));
                if (RealmObject.isValid(result)) {
                    writeToPreferences(System.currentTimeMillis(), DataBaseManager.DETAIL_SETTINGS_KEY_LAST_CACHE_UPDATE
                            + dataClass.getSimpleName(), "putRealmObject");
                    closeRealm();
                    return Observable.just(Boolean.TRUE);
                } else {
                    closeRealm();
                    return Observable.error(new IllegalArgumentException(mContext.getString(R.string.realm_object_invalid)));
                }
            });
        }
        return Observable.error(new IllegalArgumentException(mContext.getString(R.string.realm_object_invalid)));
    }

    /**
     * Puts and element into the DB.
     *
     * @param realmModel Element to insert in the DB.
     * @param dataClass  Class type of the items to be put.
     */
    @NonNull
    @Override
    public Observable<?> put(@Nullable RealmModel realmModel, @NonNull Class dataClass) {
        if (realmModel != null) {
            return Observable.defer(() -> {
                mRealm = Realm.getDefaultInstance();
                RealmModel result = executeWriteOperationInRealm(mRealm, () -> mRealm.copyToRealmOrUpdate(realmModel));
                if (RealmObject.isValid(result)) {
                    writeToPreferences(System.currentTimeMillis(), DataBaseManager.DETAIL_SETTINGS_KEY_LAST_CACHE_UPDATE
                            + dataClass.getSimpleName(), "putRealmModel");
                    closeRealm();
                    return Observable.just(Boolean.TRUE);
                } else {
                    closeRealm();
                    return Observable.error(new IllegalArgumentException(mContext.getString(R.string.realm_model_invalid)));
                }
            });
        }
        return Observable.error(new IllegalArgumentException(mContext.getString(R.string.realm_model_invalid)));
    }

    /**
     * Puts and element into the DB.
     *
     * @param jsonObject Element to insert in the DB.
     * @param dataClass  Class type of the items to be put.
     */
    @NonNull
    @Override
    public Observable<?> put(@Nullable JSONObject jsonObject, @Nullable String idColumnName, @NonNull Class dataClass) {
        if (jsonObject != null) {
            return Observable.defer(() -> {
                try {
                    updateJsonObjectWithIdValue(jsonObject, idColumnName, dataClass);
                } catch (@NonNull JSONException | IllegalArgumentException e) {
                    return Observable.error(e);
                }
                mRealm = Realm.getDefaultInstance();
                RealmModel result = executeWriteOperationInRealm(mRealm, () -> mRealm.createOrUpdateObjectFromJson(dataClass, jsonObject));
                if (RealmObject.isValid(result)) {
                    writeToPreferences(System.currentTimeMillis(),
                            DataBaseManager.DETAIL_SETTINGS_KEY_LAST_CACHE_UPDATE + dataClass.getSimpleName(),
                            "putJSON");
                    closeRealm();
                    return Observable.just(true);
                } else {
                    closeRealm();
                    return Observable.error(new IllegalArgumentException(mContext
                            .getString(R.string.realm_model_invalid)));
                }
            });
        } else
            return Observable.defer(() -> Observable.error(new IllegalArgumentException(mContext
                    .getString(R.string.json_object_invalid))));
    }

    /**
     * Puts and element into the DB.
     *
     * @param jsonArray    Element to insert in the DB.
     * @param idColumnName Name of the id field.
     * @param dataClass    Class type of the items to be put.
     */
    @NonNull
    @Override
    public Observable<?> putAll(@NonNull JSONArray jsonArray, String idColumnName, @NonNull Class dataClass) {
        return Observable.defer(() -> {
            try {
                updateJsonArrayWithIdValue(jsonArray, idColumnName, dataClass);
            } catch (@NonNull JSONException | IllegalArgumentException e) {
                return Observable.error(e);
            }
            mRealm = Realm.getDefaultInstance();
            executeWriteOperationInRealm(mRealm, () -> mRealm.createOrUpdateAllFromJson(dataClass, jsonArray));
            writeToPreferences(System.currentTimeMillis(), DataBaseManager.COLLECTION_SETTINGS_KEY_LAST_CACHE_UPDATE
                    + dataClass.getSimpleName(), "putAll");
            closeRealm();
            return Observable.just(Boolean.TRUE);
        });
    }

    /**
     * Puts and element into the DB.
     *
     * @param realmModels Element to insert in the DB.
     * @param dataClass   Class type of the items to be put.
     */
    @Override
    public void putAll(@NonNull List<RealmObject> realmModels, @NonNull Class dataClass) {
        Observable.defer(() -> {
            mRealm = Realm.getDefaultInstance();
            executeWriteOperationInRealm(mRealm, () -> mRealm.copyToRealmOrUpdate(realmModels));
            writeToPreferences(System.currentTimeMillis(), DataBaseManager.COLLECTION_SETTINGS_KEY_LAST_CACHE_UPDATE
                    + dataClass.getSimpleName(), "putAll");
            closeRealm();
            return Observable.just(realmModels);
        }).subscribeWith(new PutAllSubscriberClass(realmModels));
    }

    /**
     * Evict all elements of the DB.
     *
     * @param clazz Class type of the items to be deleted.
     */
    @NonNull
    @Override
    public Observable<Boolean> evictAll(@NonNull Class clazz) {
        return Observable.defer(() -> {
            mRealm = Realm.getDefaultInstance();
            executeWriteOperationInRealm(mRealm, () -> mRealm.delete(clazz));
            writeToPreferences(System.currentTimeMillis(), DataBaseManager.COLLECTION_SETTINGS_KEY_LAST_CACHE_UPDATE
                    + clazz.getSimpleName(), "evictAll");
            closeRealm();
            return Observable.just(Boolean.TRUE);
        });
    }

    /**
     * Evict element of the DB.
     *
     * @param realmModel Element to deleted from the DB.
     * @param clazz      Class type of the items to be deleted.
     */
    @Override
    public void evict(@NonNull final RealmObject realmModel, @NonNull Class clazz) {
        Observable.defer(() -> {
            mRealm = Realm.getDefaultInstance();
            executeWriteOperationInRealm(mRealm, (Executor) realmModel::deleteFromRealm);
            boolean isDeleted = !realmModel.isValid();
            writeToPreferences(System.currentTimeMillis(), DataBaseManager.DETAIL_SETTINGS_KEY_LAST_CACHE_UPDATE
                    + clazz.getSimpleName(), "evict");
            closeRealm();
            return Observable.just(isDeleted);
        }).subscribeWith(new EvictSubscriberClass(clazz));
    }

    /**
     * Evict element by id of the DB.
     *
     * @param clazz        Class type of the items to be deleted.
     * @param idFieldName  The id used to look for inside the DB.
     * @param idFieldValue Name of the id field.
     */
    @Override
    public boolean evictById(@NonNull Class clazz, @NonNull String idFieldName, final long idFieldValue) {
        mRealm = Realm.getDefaultInstance();
        RealmModel toDelete = mRealm.where(clazz).equalTo(idFieldName, idFieldValue).findFirst();
        if (toDelete != null) {
            executeWriteOperationInRealm(mRealm, () -> RealmObject.deleteFromRealm(toDelete));
            boolean isDeleted = !RealmObject.isValid(toDelete);
            writeToPreferences(System.currentTimeMillis(), DataBaseManager.DETAIL_SETTINGS_KEY_LAST_CACHE_UPDATE
                    + clazz.getSimpleName(), "evictById");
            closeRealm();
            return isDeleted;
        } else {
            closeRealm();
            return false;
        }
    }

    /**
     * Evict a collection elements of the DB.
     *
     * @param idFieldName The id used to look for inside the DB.
     * @param list        List of ids to be deleted.
     * @param dataClass   Class type of the items to be deleted.
     */
    @NonNull
    @Override
    public Observable<?> evictCollection(@NonNull String idFieldName, @NonNull List<Long> list,
                                         @NonNull Class dataClass) {
        return Observable.defer(() -> {
            boolean isDeleted = true;
            for (int i = 0, size = list.size(); i < size; i++)
                isDeleted = isDeleted && evictById(dataClass, idFieldName, list.get(i));
            return Observable.just(isDeleted);
        });
    }

    @Override
    public Context getContext() {
        return mContext;
    }

    @Override
    public void closeRealm() {
        if (!mRealm.isClosed())
            mRealm.close();
    }

    @Override
    public boolean isCached(int itemId, @NonNull String columnId, Class clazz) {
        if (columnId.isEmpty())
            return false;
        mRealm = Realm.getDefaultInstance();
        Object realmObject = mRealm.where(clazz).equalTo(columnId, itemId).findFirst();
        boolean isCached = realmObject != null;
        closeRealm();
        return isCached;
    }

    @Override
    public boolean isItemValid(int itemId, @NonNull String columnId, @NonNull Class clazz) {
        return isCached(itemId, columnId, clazz) && areItemsValid(DataBaseManager.DETAIL_SETTINGS_KEY_LAST_CACHE_UPDATE
                + clazz.getSimpleName());
    }

    @Override
    public boolean areItemsValid(String destination) {
        return (System.currentTimeMillis() - getFromPreferences(destination)) <= EXPIRATION_TIME;
    }

    private void executeWriteOperationInRealm(@NonNull Realm realm, @NonNull Executor executor) {
        if (realm.isInTransaction())
            realm.cancelTransaction();
        realm.beginTransaction();
        executor.run();
        realm.commitTransaction();
    }

    private <T> T executeWriteOperationInRealm(@NonNull Realm realm, @NonNull ExecuteAndReturn<T> executor) {
        T toReturnValue;
        if (realm.isInTransaction())
            realm.cancelTransaction();
        realm.beginTransaction();
        toReturnValue = executor.run();
        realm.commitTransaction();
        return toReturnValue;
    }

    /**
     * Write a value to a user preferences file.
     *
     * @param value  A long representing the value to be inserted.
     * @param source which method is making this call
     */
    void writeToPreferences(long value, String destination, String source) {
        SharedPreferences.Editor editor = mContext.getSharedPreferences(Config.getInstance().getPrefFileName(),
                Context.MODE_PRIVATE).edit();
        if (editor == null)
            return;
        editor.putLong(destination, value);
        editor.apply();
        Log.d(TAG, source + " writeToPreferencesTo " + destination + ": " + value);
    }

    /**
     * Get a value from a user preferences file.
     *
     * @return A long representing the value retrieved from the preferences file.
     */
    long getFromPreferences(String destination) {
        return mContext.getSharedPreferences(Config.getInstance().getPrefFileName(), Context.MODE_PRIVATE)
                .getLong(destination, 0);
    }

    public Realm getRealm() {
        return mRealm;
    }

    @NonNull
    private JSONArray updateJsonArrayWithIdValue(@NonNull JSONArray jsonArray, @Nullable String idColumnName,
                                                 Class dataClass)
            throws JSONException, IllegalArgumentException {
        if (idColumnName == null || idColumnName.isEmpty())
            throw new IllegalArgumentException(mContext.getString(R.string.no_id));
        for (int i = 0, length = jsonArray.length(); i < length; i++)
            if (jsonArray.get(i) instanceof JSONObject)
                updateJsonObjectWithIdValue(jsonArray.getJSONObject(i), idColumnName, dataClass);
        return jsonArray;
    }

    @NonNull
    private JSONObject updateJsonObjectWithIdValue(@NonNull JSONObject jsonObject, @Nullable String idColumnName,
                                                   Class dataClass)
            throws JSONException, IllegalArgumentException {
        if (idColumnName == null || idColumnName.isEmpty())
            throw new IllegalArgumentException(mContext.getString(R.string.no_id));
        if (jsonObject.getInt(idColumnName) == 0)
            jsonObject.put(idColumnName, Utils.getNextId(dataClass, idColumnName));
        return jsonObject;
    }

    private interface Executor {
        void run();
    }

    private interface ExecuteAndReturn<T> {
        @NonNull
        T run();
    }

    private class EvictSubscriberClass extends DisposableObserver<Object> {

        private final Class mClazz;

        EvictSubscriberClass(Class clazz) {
            mClazz = clazz;
        }

        @Override
        public void onError(@NonNull Throwable e) {
            e.printStackTrace();
        }

        @Override
        public void onComplete() {

        }

        @Override
        public void onNext(Object o) {
            Log.d(TAG, mClazz.getName() + " deleted!");
        }

    }

    private class PutAllSubscriberClass extends DisposableObserver<Object> {

        private final List<RealmObject> mRealmModels;

        PutAllSubscriberClass(List<RealmObject> realmModels) {
            mRealmModels = realmModels;
        }

        @Override
        public void onError(@NonNull Throwable e) {
            e.printStackTrace();
        }

        @Override
        public void onComplete() {

        }

        @Override
        public void onNext(Object o) {
            Log.d(TAG, "all " + mRealmModels.getClass().getName() + "s added!");
        }
    }
}
