package org.telegram.ui.Heymate;

import android.content.Context;
import android.widget.LinearLayout;

import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.ProfileActivity;

import java.util.ArrayList;

public class HtSPOfferLayout extends LinearLayout {
    public HtSPOfferLayout(Context context, ProfileActivity parentFragment) {
        super(context);
        setOrientation(VERTICAL);
        ArrayList<OfferDto> offers = HtSQLite.getInstance().getOffers("All", "All", 1);
        for (OfferDto offerDto : offers) {
            OfferCell offerCell = new OfferCell(context, parentFragment) {
                @Override
                public void setEnabled(boolean enabled) {
                    super.setEnabled(enabled);
                    setAlpha(enabled ? 1.0f : 0.5f);
                }
            };
            offerCell.setPlace(2);
            offerCell.setDto(offerDto);
            offerCell.setEnabled(true);
            offerCell.setHovered(true);
            offerCell.setOnClickListener((v) -> {
                offerCell.onClick();
            });
            addView(offerCell, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
        }
    }

}
