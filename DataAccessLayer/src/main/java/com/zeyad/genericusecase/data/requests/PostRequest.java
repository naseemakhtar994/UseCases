package com.zeyad.genericusecase.data.requests;

import android.support.annotation.NonNull;

import com.zeyad.genericusecase.data.utils.ModelConverters;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;

import io.reactivex.disposables.Disposable;

/**
 * @author zeyad on 7/29/16.
 */
public class PostRequest {
    public static final String POST = "post", DELETE = "delete", PUT = "put";
    private String mUrl, mIdColumnName, mMethod;
    private Disposable mSubscriber;
    private Class mDataClass, mPresentationClass;
    private boolean mPersist, mQueuable;
    private JSONObject mJsonObject;
    private JSONArray mJsonArray;
    private HashMap<String, Object> mKeyValuePairs;

    public PostRequest(@NonNull PostRequestBuilder postRequestBuilder) {
        mUrl = postRequestBuilder.mUrl;
        mDataClass = postRequestBuilder.mDataClass;
        mPresentationClass = postRequestBuilder.mPresentationClass;
        mPersist = postRequestBuilder.mPersist;
        mQueuable = postRequestBuilder.mQueuable;
        mSubscriber = postRequestBuilder.mSubscriber;
        mKeyValuePairs = postRequestBuilder.mKeyValuePairs;
        mJsonObject = postRequestBuilder.mJsonObject;
        mJsonArray = postRequestBuilder.mJsonArray;
        mIdColumnName = postRequestBuilder.mIdColumnName;
        mMethod = postRequestBuilder.mMethod;
    }

    public PostRequest(Disposable subscriber, String idColumnName, String url, JSONObject keyValuePairs,
                       Class presentationClass, Class dataClass, boolean persist) {
        mIdColumnName = idColumnName;
        mJsonObject = keyValuePairs;
        mPersist = persist;
        mPresentationClass = presentationClass;
        mDataClass = dataClass;
        mSubscriber = subscriber;
        mUrl = url;
    }

    // for test
    public PostRequest(Disposable subscriber, String idColumnName, String url, JSONArray keyValuePairs,
                       Class presentationClass, Class dataClass, boolean persist) {
        mIdColumnName = idColumnName;
        mJsonArray = keyValuePairs;
        mPersist = persist;
        mPresentationClass = presentationClass;
        mDataClass = dataClass;
        mSubscriber = subscriber;
        mUrl = url;
    }

    public PostRequest(Disposable subscriber, String idColumnName, String url, HashMap<String, Object> keyValuePairs,
                       Class presentationClass, Class dataClass, boolean persist) {
        mIdColumnName = idColumnName;
        mKeyValuePairs = keyValuePairs;
        mPersist = persist;
        mPresentationClass = presentationClass;
        mDataClass = dataClass;
        mSubscriber = subscriber;
        mUrl = url;
    }

    public JSONObject getObjectBundle() {
        if (mJsonObject != null)
            return mJsonObject;
        else if (mKeyValuePairs != null)
            return new JSONObject(mKeyValuePairs);
        else return new JSONObject();
    }

    public JSONArray getArrayBundle() {
        if (mJsonArray != null)
            return mJsonArray;
        else if (mKeyValuePairs != null)
            return ModelConverters.convertToJsonArray(mKeyValuePairs);
        else return new JSONArray();
    }

    public String getUrl() {
        return mUrl;
    }

    public Disposable getSubscriber() {
        return mSubscriber;
    }

    public Class getDataClass() {
        return mDataClass;
    }

    public Class getPresentationClass() {
        return mPresentationClass;
    }

    public boolean isPersist() {
        return mPersist;
    }

    public boolean isQueuable() {
        return mQueuable;
    }

    public JSONArray getJsonArray() {
        return mJsonArray;
    }

    public JSONObject getJsonObject() {
        return mJsonObject;
    }

    public HashMap<String, Object> getKeyValuePairs() {
        return mKeyValuePairs;
    }

    public String getIdColumnName() {
        return mIdColumnName;
    }

    public String getMethod() {
        return mMethod;
    }

    public static class PostRequestBuilder {
        JSONArray mJsonArray;
        JSONObject mJsonObject;
        HashMap<String, Object> mKeyValuePairs;
        String mUrl, mIdColumnName, mMethod;
        Disposable mSubscriber;
        Class mDataClass, mPresentationClass;
        boolean mPersist, mQueuable;

        public PostRequestBuilder(Class dataClass, boolean persist) {
            mDataClass = dataClass;
            mPersist = persist;
        }

        @NonNull
        public PostRequestBuilder url(String url) {
            mUrl = url;
            return this;
        }

        @NonNull
        public PostRequestBuilder queuable(boolean queuable) {
            mQueuable = queuable;
            return this;
        }

        @NonNull
        public PostRequestBuilder presentationClass(Class presentationClass) {
            mPresentationClass = presentationClass;
            return this;
        }

        @NonNull
        public PostRequestBuilder subscriber(Disposable subscriber) {
            mSubscriber = subscriber;
            return this;
        }

        @NonNull
        public PostRequestBuilder idColumnName(String idColumnName) {
            mIdColumnName = idColumnName;
            return this;
        }

        @NonNull
        public PostRequestBuilder method(String method) {
            mMethod = method;
            return this;
        }

        @NonNull
        public PostRequestBuilder payLoad(JSONObject jsonObject) {
            mJsonObject = jsonObject;
            return this;
        }

        @NonNull
        public PostRequestBuilder payLoad(JSONArray jsonArray) {
            mJsonArray = jsonArray;
            return this;
        }

        @NonNull
        public PostRequestBuilder payLoad(HashMap hashMap) {
            mKeyValuePairs = hashMap;
            return this;
        }

        @NonNull
        public PostRequest build() {
            return new PostRequest(this);
        }
    }
}
