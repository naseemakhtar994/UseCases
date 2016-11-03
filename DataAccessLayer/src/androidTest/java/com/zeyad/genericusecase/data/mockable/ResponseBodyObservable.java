package com.zeyad.genericusecase.data.mockable;

import okhttp3.ResponseBody;
import io.reactivex.Observable;

public class ResponseBodyObservable extends Observable<ResponseBody> {
    protected ResponseBodyObservable(OnSubscribe<ResponseBody> f) {
        super(f);
    }
}
