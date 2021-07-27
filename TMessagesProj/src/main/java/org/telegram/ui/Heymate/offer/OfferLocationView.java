package org.telegram.ui.Heymate.offer;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.yashoid.sequencelayout.SequenceLayout;

import org.jetbrains.annotations.NotNull;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Heymate.widget.RoundedCornersContainer;

import works.heymate.beta.R;

public class OfferLocationView extends SequenceLayout implements OnMapReadyCallback {

    private TextView mAddress;
    private MapView mMapView;

    private GoogleMap mMap = null;

    private LatLng mLocation = null;

    private Marker mMarker = null;

    public OfferLocationView(Context context) {
        super(context);
        initialize(context, null, 0);
    }

    public OfferLocationView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize(context, attrs, 0);
    }

    public OfferLocationView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize(context, attrs, defStyleAttr);
    }

    private void initialize(Context context, AttributeSet attrs, int defStyleAttr) {
        LayoutInflater.from(context).inflate(R.layout.offer_location, this, true);
        addSequences(R.xml.sequences_offer_location);

        TextView title = findViewById(R.id.title);
        mAddress = findViewById(R.id.address);
        RoundedCornersContainer mapContainer = findViewById(R.id.map_container);

        title.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlueText));
        title.setText("Location"); // TODO Texts

        mAddress.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));

        mapContainer.setCornerRadius(AndroidUtilities.dp(8));

        mMapView = new MapView(context);
        mapContainer.addView(mMapView, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

        mMapView.onCreate(null);
        mMapView.getMapAsync(this);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        mMapView.onStart();
        mMapView.onResume();
    }

    @Override
    protected void onDetachedFromWindow() {
        mMapView.onPause();
        mMapView.onStop();

        super.onDetachedFromWindow();
    }

    public void onDestroy() {
        mMapView.onDestroy();
    }

    @Override
    public void onMapReady(@NonNull @NotNull GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setScrollGesturesEnabled(false);
        mMap.getUiSettings().setRotateGesturesEnabled(false);
        mMap.getUiSettings().setZoomGesturesEnabled(false);
        mMap.getUiSettings().setTiltGesturesEnabled(false);

        showLocation();
    }

    public void setAddress(String address) {
        mAddress.setText(address);
    }

    public void setLocation(double latitude, double longitude) {
        mLocation = new LatLng(latitude, longitude);

        showLocation();
    }

    private void showLocation() {
        if (mMap == null || mLocation == null) {
            return;
        }

        if (mMarker == null) {
            mMarker = mMap.addMarker(new MarkerOptions()
                    .anchor(0.5f, 1)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.map_pin))
                    .position(mLocation)
            );
        }
        else {
            mMarker.setPosition(mLocation);
        }

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mLocation, 15));
    }

}
