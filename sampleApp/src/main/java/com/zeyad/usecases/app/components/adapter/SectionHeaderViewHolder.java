package com.zeyad.usecases.app.components.adapter;

import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import com.zeyad.usecases.app.R;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * @author by zeyad on 13/06/16.
 */
public class SectionHeaderViewHolder extends GenericRecyclerViewAdapter.ViewHolder {
    @BindView(R.id.tvSectionHeader)
    TextView tvSectionHeader;

    public SectionHeaderViewHolder(LayoutInflater layoutInflater, ViewGroup parent) {
        super(layoutInflater.inflate(R.layout.list_section_header_layout, parent, false));
        ButterKnife.bind(this, itemView);
    }

    @Override
    public void bindData(Object data, boolean itemSelected, int position, boolean isEnabled) {
        itemView.setEnabled(isEnabled);
        itemView.setBackgroundColor(ContextCompat.getColor(itemView.getContext().getApplicationContext(),
                R.color.gray_background));
        if (data instanceof String)
            tvSectionHeader.setText((String) data);
    }
}
