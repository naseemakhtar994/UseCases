package com.zeyad.genericusecase.data.network;

import android.support.annotation.NonNull;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import io.reactivex.Observable;

interface IApiConnection {

    @NonNull
    HttpLoggingInterceptor provideHttpLoggingInterceptor();

    Observable<ResponseBody> dynamicDownload(String url);

    Observable<Object> dynamicGetObject(String url);

    Observable<Object> dynamicGetObject(String url, boolean shouldCache);

    Observable<List> dynamicGetList(String url);

    Observable<List> dynamicGetList(String url, boolean shouldCache);

    Observable<Object> dynamicPostObject(String url, RequestBody requestBody);

    Observable<List> dynamicPostList(String url, RequestBody requestBody);

    Observable<Object> dynamicPutObject(String url, RequestBody requestBody);

    Observable<List> dynamicPutList(String url, RequestBody requestBody);

    Observable<ResponseBody> upload(String url, MultipartBody.Part file, RequestBody description);

    Observable<Object> upload(String url, RequestBody requestBody);

    Observable<ResponseBody> upload(String url, MultipartBody.Part file);

    Observable<List> dynamicDeleteList(String url, RequestBody body);

    Observable<Object> dynamicDeleteObject(String url, RequestBody body);
}
