package com.zeyad.genericusecase.data.requests;

import com.zeyad.genericusecase.data.services.realm_test_models.TestModel;
import com.zeyad.genericusecase.data.services.realm_test_models.TestViewModel;

 import io.reactivex.disposables.Disposable;
import io.reactivex.subscribers.TestSubscriber;

class GetRequestTestRobot {

    static final Class DATA_CLASS = TestModel.class;
    static final boolean TO_PERSIST = false;
    static final String ID_COLUMN_NAME = "id";
    static final Class PRESENTATION_CLASS = TestViewModel.class;
    static final Disposable SUBSCRIBER = new TestSubscriber<>();
    static final String URL = "www.google.com";
    static final boolean SHOULD_CACHE = true;
    static final Integer ID_COLUMN_ID = 1;

    static GetRequest createGetObjectRequest() {
        return new GetRequest.GetObjectRequestBuilder(DATA_CLASS, TO_PERSIST)
                .url(URL)
                .shouldCache(SHOULD_CACHE)
                .presentationClass(PRESENTATION_CLASS)
                .idColumnName(ID_COLUMN_NAME)
                .subscriber(SUBSCRIBER)
                .id(ID_COLUMN_ID)
                .build();


    }
}
