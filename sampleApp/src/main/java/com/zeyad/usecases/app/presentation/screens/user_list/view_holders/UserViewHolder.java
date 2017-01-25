package com.zeyad.usecases.app.presentation.screens.user_list.view_holders;

import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.zeyad.usecases.app.R;
import com.zeyad.usecases.app.components.adapter.GenericRecyclerViewAdapter;
import com.zeyad.usecases.app.presentation.screens.user_list.UserRealm;
import com.zeyad.usecases.data.utils.Utils;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * @author zeyad on 12/1/16.
 */
public class UserViewHolder extends GenericRecyclerViewAdapter.ViewHolder {
    @BindView(R.id.title)
    TextView textViewTitle;
    @BindView(R.id.avatar)
    ImageView avatar;
    @BindView(R.id.rl_row_user)
    RelativeLayout rowUser;

    public UserViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }

    @Override
    public void bindData(Object data, boolean isItemSelected, int position, boolean isEnabled) {
        if (data != null) {
            UserRealm userModel = (UserRealm) data;
            if (Utils.isNotEmpty(userModel.getAvatarUrl()))
                Glide.with(itemView.getContext())
                        .load(userModel.getAvatarUrl())
                        .into(avatar);
            if (Utils.isNotEmpty(userModel.getLogin()))
                textViewTitle.setText(userModel.getLogin());
        }
    }
}
