package com.zeyad.usecases.app.presentation.screens.user_list;

import android.util.Log;

import com.zeyad.usecases.app.components.mvvm.BaseViewModel;
import com.zeyad.usecases.data.requests.GetRequest;
import com.zeyad.usecases.data.requests.PostRequest;
import com.zeyad.usecases.domain.interactors.data.DataUseCaseFactory;
import com.zeyad.usecases.domain.interactors.data.IDataUseCase;

import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static com.zeyad.usecases.app.utils.Constants.URLS.USERS;

/**
 * @author zeyad on 11/1/16.
 */
class UserListVM extends BaseViewModel implements UserListView {

    private final IDataUseCase dataUseCase;
    private int currentPage;
    private long lastId;
    private int counter = 0;

    UserListVM() {
        dataUseCase = DataUseCaseFactory.getInstance();
    }

    @Override
    public Observable<UserListModel> getUserListFromDB() {
//        Observable<List> result;
//        List lastList = dataUseCase.getLastList().getValue();
//        if (Utils.isNotEmpty(lastList) && lastList.get(0) instanceof UserRealm)
//            result = dataUseCase.getLastList().doOnRequest(aLong -> Log.d("getUserListFromDB", "Subject"));
//        else {
//            result = dataUseCase.getList(new GetRequest.GetRequestBuilder(UserRealm.class, true).build())
//                    .flatMap(list -> Utils.isNotEmpty(list) ? Observable.just(list) : getUserListFromServer()
//                            .doOnRequest(aLong -> Log.d("getUserListFromDB", "DB Empty, FromServer")))
//                    .onErrorResumeNext(throwable -> {
//                        throwable.printStackTrace();
//                        return getUserListFromServer().doOnRequest(aLong -> Log.d("getUserListFromDB", "DB Error, FromServer"));
//                    }).doOnRequest(aLong -> Log.d("getUserListFromDB", "fresherData"));
//        }
//        return result.compose(applyStates()).doOnNext(aLong -> Log.d("getUserListFromDB", "OnNextCalled"));
        return dataUseCase.getListFromOffLineFirst(new GetRequest.GetRequestBuilder(UserRealm.class, true)
                .url(String.format(USERS, currentPage, lastId)).build())
                .compose(applyStates());
    }

    @Override
    public Observable<List> getUserListFromServer() {
        return dataUseCase.getList(new GetRequest.GetRequestBuilder(UserRealm.class, true)
                .url(String.format(USERS, currentPage, lastId))
                .build());
    }

    private Observable.Transformer<List, UserListModel> applyStates() {
        return listObservable -> listObservable.flatMap(list -> Observable.just(UserListModel.onNext((List<UserRealm>) list)))
                .onErrorReturn(UserListModel::error)
                .startWith(UserListModel.loading());
    }

    @Override
    public Observable<Long> writePeriodic() {
        return Observable.interval(2000, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread())
                .takeWhile(aLong -> counter < 37)
                .observeOn(Schedulers.io())
                .doOnNext(aLong -> {
                    UserRealm userRealm = new UserRealm();
                    userRealm.setId(counter);
                    userRealm.setLogin(String.valueOf(counter + 1));
                    getCompositeSubscription().add(dataUseCase.postObject(new PostRequest
                            .PostRequestBuilder(UserRealm.class, true)
                            .idColumnName(UserRealm.ID)
                            .payLoad(userRealm)
                            .build())
                            .subscribe(new Subscriber<Object>() {
                                @Override
                                public void onCompleted() {
                                }

                                @Override
                                public void onError(Throwable e) {
                                    e.printStackTrace();
                                }

                                @Override
                                public void onNext(Object o) {
                                    counter++;
                                    Log.i("writePeriodic", "Realm write successful [" + counter
                                            + "] :: [" + userRealm.getLogin() + "].");
                                }
                            }));
                });
    }

    @Override
    public void incrementPage(long lastId) {
        this.lastId = lastId;
        currentPage++;
        getUserListFromServer().subscribe(list -> {
        }, Throwable::printStackTrace);
    }

    @Override
    public int getCurrentPage() {
        return currentPage;
    }

    @Override
    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }
}
