package com.zeyad.usecases.app.presentation.user_list.events;

import com.zeyad.usecases.app.components.redux.BaseEvent;

/**
 * @author by ZIaDo on 4/20/17.
 */

public class SearchUsersEvent extends BaseEvent {

    private final String query;

    public SearchUsersEvent(String s) {
        query = s;
    }

    public String getQuery() {
        return query;
    }
}
