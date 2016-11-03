package com.zeyad.genericusecase.data.mockable;

import io.reactivex.observers.DisposableObserver;

public class ObjectObservable extends DisposableObserver<Object> {
    protected ObjectObservable(OnSubscribe<Object> f) {
        super(f);
    }

}
