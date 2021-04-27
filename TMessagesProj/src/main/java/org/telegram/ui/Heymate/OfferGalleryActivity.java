package org.telegram.ui.Heymate;

import android.content.Context;
import android.content.res.Configuration;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import org.telegram.messenger.ContactsController;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.MessagesStorage;
import works.heymate.beta.R;
import org.telegram.messenger.SendMessagesHelper;
import org.telegram.messenger.UserConfig;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.ProfileGalleryView;
import org.telegram.ui.Components.RecyclerListView;

public class OfferGalleryActivity extends BaseFragment {

    private Context context;
    private OfferDto dto;
    public OfferGalleryActivity(Context context){
        this.context = context;
        this.dto = dto;
    }

    public void setDto(OfferDto dto) {
        this.dto = dto;
    }

    @Override
    public View createView(Context context) {
        super.createView(context);
        Configuration configuration = context.getResources().getConfiguration();
        int dpWidth = configuration.screenWidthDp;
        int dpHeight = configuration.screenHeightDp;

        actionBar.setBackButtonImage(works.heymate.beta.R.drawable.ic_ab_back);
        actionBar.setAllowOverlayTitle(true);
        actionBar.setSearchTextColor(0xff4488, true);
        actionBar.setTitle(dto.getTitle());
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) {
                    finishFragment();
                }
            }
        });

        fragmentView = new LinearLayout(context);
        LinearLayout mainLayout = (LinearLayout) fragmentView;

        ScrollView galleryScroll = new ScrollView(context);
        RelativeLayout galleryLayout = new RelativeLayout(context);
        int i = 0;
        galleryLayout.addView(new HtImageCell(context, this, false), LayoutHelper.createFrame(dpWidth / 3 - 15,dpWidth / 3 - 15, Gravity.CENTER, 5 + (i % 3) * (dpWidth / 3),5 + (i++ / 3) * (dpWidth / 3),5,5));
        galleryLayout.addView(new HtImageCell(context, this, false), LayoutHelper.createFrame(dpWidth / 3 - 15,dpWidth / 3 - 15, Gravity.CENTER, 5 + (i % 3) * (dpWidth / 3),5 + (i++ / 3) * (dpWidth / 3),5,5));
        galleryLayout.addView(new HtImageCell(context, this, false), LayoutHelper.createFrame(dpWidth / 3 - 15,dpWidth / 3 - 15, Gravity.CENTER, 5 + (i % 3) * (dpWidth / 3),5 + (i++ / 3) * (dpWidth / 3),5,5));
        galleryLayout.addView(new HtImageCell(context, this, false), LayoutHelper.createFrame(dpWidth / 3 - 15,dpWidth / 3 - 15, Gravity.CENTER, 5 + (i % 3) * (dpWidth / 3),5 + (i++ / 3) * (dpWidth / 3),5,5));
        galleryLayout.addView(new HtImageCell(context, this, false), LayoutHelper.createFrame(dpWidth / 3 - 15,dpWidth / 3 - 15, Gravity.CENTER, 5 + (i % 3) * (dpWidth / 3),5 + (i++ / 3) * (dpWidth / 3),5,5));
        galleryLayout.addView(new HtImageCell(context, this, false), LayoutHelper.createFrame(dpWidth / 3 - 15,dpWidth / 3 - 15, Gravity.CENTER, 5 + (i % 3) * (dpWidth / 3),5 + (i++ / 3) * (dpWidth / 3),5,5));
        galleryLayout.addView(new HtImageCell(context, this, false), LayoutHelper.createFrame(dpWidth / 3 - 15,dpWidth / 3 - 15, Gravity.CENTER, 5 + (i % 3) * (dpWidth / 3),5 + (i++ / 3) * (dpWidth / 3),5,5));
        galleryLayout.addView(new HtImageCell(context, this, false), LayoutHelper.createFrame(dpWidth / 3 - 15,dpWidth / 3 - 15, Gravity.CENTER, 5 + (i % 3) * (dpWidth / 3),5 + (i++ / 3) * (dpWidth / 3),5,5));
        galleryLayout.addView(new HtImageCell(context, this, false), LayoutHelper.createFrame(dpWidth / 3 - 15,dpWidth / 3 - 15, Gravity.CENTER, 5 + (i % 3) * (dpWidth / 3),5 + (i++ / 3) * (dpWidth / 3),5,5));
        galleryLayout.addView(new HtImageCell(context, this, false), LayoutHelper.createFrame(dpWidth / 3 - 15,dpWidth / 3 - 15, Gravity.CENTER, 5 + (i % 3) * (dpWidth / 3),5 + (i++ / 3) * (dpWidth / 3),5,5));
        galleryLayout.addView(new HtImageCell(context, this, false), LayoutHelper.createFrame(dpWidth / 3 - 15,dpWidth / 3 - 15, Gravity.CENTER, 5 + (i % 3) * (dpWidth / 3),5 + (i++ / 3) * (dpWidth / 3),5,5));
        galleryLayout.addView(new HtImageCell(context, this, false), LayoutHelper.createFrame(dpWidth / 3 - 15,dpWidth / 3 - 15, Gravity.CENTER, 5 + (i % 3) * (dpWidth / 3),5 + (i++ / 3) * (dpWidth / 3),5,5));
        galleryLayout.addView(new HtImageCell(context, this, false), LayoutHelper.createFrame(dpWidth / 3 - 15,dpWidth / 3 - 15, Gravity.CENTER, 5 + (i % 3) * (dpWidth / 3),5 + (i++ / 3) * (dpWidth / 3),5,5));
        galleryLayout.addView(new HtImageCell(context, this, false), LayoutHelper.createFrame(dpWidth / 3 - 15,dpWidth / 3 - 15, Gravity.CENTER, 5 + (i % 3) * (dpWidth / 3),5 + (i++ / 3) * (dpWidth / 3),5,5));
        galleryLayout.addView(new HtImageCell(context, this, false), LayoutHelper.createFrame(dpWidth / 3 - 15,dpWidth / 3 - 15, Gravity.CENTER, 5 + (i % 3) * (dpWidth / 3),5 + (i++ / 3) * (dpWidth / 3),5,5));
        galleryLayout.addView(new HtImageCell(context, this, false), LayoutHelper.createFrame(dpWidth / 3 - 15,dpWidth / 3 - 15, Gravity.CENTER, 5 + (i % 3) * (dpWidth / 3),5 + (i++ / 3) * (dpWidth / 3),5,5));
        galleryLayout.addView(new HtImageCell(context, this, false), LayoutHelper.createFrame(dpWidth / 3 - 15,dpWidth / 3 - 15, Gravity.CENTER, 5 + (i % 3) * (dpWidth / 3),5 + (i++ / 3) * (dpWidth / 3),5,5));
        galleryLayout.addView(new HtImageCell(context, this, false), LayoutHelper.createFrame(dpWidth / 3 - 15,dpWidth / 3 - 15, Gravity.CENTER, 5 + (i % 3) * (dpWidth / 3),5 + (i++ / 3) * (dpWidth / 3),5,5));
        galleryLayout.addView(new HtImageCell(context, this, false), LayoutHelper.createFrame(dpWidth / 3 - 15,dpWidth / 3 - 15, Gravity.CENTER, 5 + (i % 3) * (dpWidth / 3),5 + (i++ / 3) * (dpWidth / 3),5,5));
        galleryLayout.addView(new HtImageCell(context, this, false), LayoutHelper.createFrame(dpWidth / 3 - 15,dpWidth / 3 - 15, Gravity.CENTER, 5 + (i % 3) * (dpWidth / 3),5 + (i++ / 3) * (dpWidth / 3),5,5));
        galleryLayout.addView(new HtImageCell(context, this, false), LayoutHelper.createFrame(dpWidth / 3 - 15,dpWidth / 3 - 15, Gravity.CENTER, 5 + (i % 3) * (dpWidth / 3),5 + (i++ / 3) * (dpWidth / 3),5,5));
        galleryScroll.addView(galleryLayout, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));
        mainLayout.addView(galleryScroll, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));
        return fragmentView;
    }
}
