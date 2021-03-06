package com.zeyad.usecases.app.components.adapter;

/**
 * @author by zeyad on 20/05/16.
 */
public interface ItemBase<M> {
    void bindData(M data, boolean itemSelected, int position, boolean isEnabled);
}