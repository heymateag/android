package org.telegram.ui.Heymate.widget;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatAutoCompleteTextView;
import androidx.core.content.res.ResourcesCompat;

import com.google.android.gms.maps.MapView;

import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.HintEditText;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Heymate.AmplifyModels.ExpandableItem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import works.heymate.core.Texts;

public class LocationInputItem extends ExpandableItem {

    private AppCompatAutoCompleteTextView mInputAddress;
    private MapView mMapView;

    public LocationInputItem(@NonNull Context context) {
        super(context);
        setTitle(LocaleController.getString("HtLocation", R.string.HtLocation));
        setIcon(ResourcesCompat.getDrawable(getResources(), R.drawable.location_on_24_px_1, null));
    }

    @Override
    protected View createContent() {
        FrameLayout content = new FrameLayout(getContext());

        mInputAddress = new AppCompatAutoCompleteTextView(getContext());
        mInputAddress.setInputType(EditorInfo.TYPE_CLASS_TEXT | EditorInfo.TYPE_TEXT_VARIATION_POSTAL_ADDRESS);
        mInputAddress.setSingleLine(true);
        mInputAddress.setMaxLines(1);
        mInputAddress.setImeOptions(EditorInfo.IME_ACTION_DONE);
        mInputAddress.setHint(Texts.get(Texts.SEARCH_ADDRESS));
        mInputAddress.setHintTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteHintText));
        mInputAddress.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        mInputAddress.setThreshold(2);
        content.addView(mInputAddress, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.TOP, 92, 4, 20, 0));

        mInputAddress.setAdapter(new ArrayAdapter<String>(getContext(), 0) {

            private Filter mFilter = null;

            @NonNull
            @Override
            public Filter getFilter() {
                if (mFilter == null) {
                    mFilter = new Filter() {

                        @Override
                        protected FilterResults performFiltering(CharSequence constraint) {
                            FilterResults results = new FilterResults();

                            results.values = Arrays.asList(constraint.toString(), constraint.toString() + " 2");
                            results.count = 2;

                            return results;
                        }

                        @Override
                        protected void publishResults(CharSequence constraint, FilterResults results) {
                            clear();

                            if (results.values instanceof List) {
                                addAll(((List<String>) results.values));
                            }
                        }

                    };
                }

                return mFilter;
            }

        });

        mMapView = new MapView(getContext());
        content.addView(mMapView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 160, Gravity.TOP, 20, 48, 20, 18));

        mMapView.getMapAsync(googleMap -> {

        });

        return content;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        mMapView.onCreate(null);
        mMapView.onStart();
        mMapView.onResume();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mMapView.onPause();
        mMapView.onStop();
        mMapView.onDestroy();
    }

}
