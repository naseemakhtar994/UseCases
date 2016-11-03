package com.zeyad.generic.usecase.dataaccesslayer;

import android.support.annotation.Nullable;

import rx.Subscription;
import rx.subscriptions.CompositeDisposable;

/**
 * @author by ZIaDo on 10/1/16.
 */

public class Utils {

    @Nullable
    public static CompositeDisposable getNewCompositeSubIfUnsubscribed(@Nullable CompositeDisposable subscription) {
        if (subscription == null || subscription.isUnsubscribed())
            return new CompositeDisposable();
        return subscription;
    }

    public static void unsubscribeIfNotNull(@Nullable Subscription subscription) {
        if (subscription != null)
            subscription.unsubscribe();
    }
}
