package com.zeyad.genericusecase.data.mockable;

import java.util.List;

import io.reactivex.Observable;

public class ListObservable extends Observable<List> {
    protected ListObservable(OnSubscribe<List> f) {
        super(f);
    }
}
