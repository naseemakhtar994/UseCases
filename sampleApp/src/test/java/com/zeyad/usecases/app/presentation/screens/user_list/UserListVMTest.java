package com.zeyad.usecases.app.presentation.screens.user_list;

import com.zeyad.usecases.data.requests.GetRequest;
import com.zeyad.usecases.domain.interactors.data.DataUseCaseFactory;
import com.zeyad.usecases.domain.interactors.data.IDataUseCase;

import org.junit.Before;
import org.junit.Test;
import org.powermock.api.mockito.PowerMockito;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author by ZIaDo on 2/7/17.
 */
//@PowerMockIgnore({"org.mockito.*", "org.robolectric.*", "android.*"})
//@PrepareForTest({DataUseCaseFactory.class})
public class UserListVMTest {

    IDataUseCase mockDataUseCase;
    UserListState userListState;
    List<UserRealm> userRealmList;
    UserListVM userListVM;

    @Before
    public void setUp() throws Exception {
        mockDataUseCase = mock(IDataUseCase.class);

        PowerMockito.mockStatic(DataUseCaseFactory.class);
//        DataUseCaseFactory.init(new DataUseCaseConfig.Builder(any())
//                .baseUrl(API_BASE_URL)
//                .withRealm()
//                .entityMapper(new AutoMap_DAOMapperFactory())
//                .okHttpBuilder(new OkHttpClient.Builder())
//                .build());

        PowerMockito.when(DataUseCaseFactory.getInstance()).thenReturn(mockDataUseCase);
        userListVM = new UserListVM();
    }

    @Test
    public void returnUserListStateObservableWhenGetUserIsCalled() {
        UserRealm userRealm = new UserRealm();
        userRealm.setLogin("testUser");
        userRealm.setId(1);
        userRealmList = new ArrayList<>();
        userRealmList.add(userRealm);
        Observable<List> observableUserRealm = Observable.just(userRealmList);

        when(mockDataUseCase.getListOffLineFirst(new GetRequest.GetRequestBuilder(UserRealm.class, true)
                .url(eq("String by matcher"))
                .build()))
                .thenReturn(observableUserRealm);

        userListVM.getUsers();

        // Verify repository interactions
        verify(mockDataUseCase, times(1)).getListOffLineFirst(any(GetRequest.class));

        // Verify state reduction interactions
        verify(userListVM, times(1)).applyStates();
        verify(userListVM, atLeast(3)).reduce(any(UserListState.class), any(UserListState.class));
    }

    @Test
    public void deleteCollection() throws Exception {

    }

    @Test
    public void search() throws Exception {

    }

    @Test
    public void reduce() throws Exception {
        UserListState previous = UserListState.builder().build();
        UserListState changes = UserListState.loading();
        UserListState result = userListVM.reduce(previous, changes);

        assertEquals(result.isLoading(), true);
    }

    @Test
    public void incrementPage() throws Exception {

    }
}
