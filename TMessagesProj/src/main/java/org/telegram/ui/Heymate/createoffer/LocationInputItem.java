package org.telegram.ui.Heymate.createoffer;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Point;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatAutoCompleteTextView;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONException;
import org.json.JSONObject;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.LocaleController;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Heymate.MeetingType;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import works.heymate.beta.R;
import works.heymate.core.Texts;
import works.heymate.core.Utils;

public class LocationInputItem extends ExpandableItem {

    public static class LocationInfo {

        private static final String ADDRESS = "address";
        private static final String LATITUDE = "latitude";
        private static final String LONGITUDE = "longitude";

        public final String address;
        public final double latitude;
        public final double longitude;

        public LocationInfo(String address, double latitude, double longitude) {
            this.address = address;
            this.latitude = latitude;
            this.longitude = longitude;
        }

        public LocationInfo(JSONObject json) {
            try {
                address = json.getString(ADDRESS);
                latitude = json.getDouble(LATITUDE);
                longitude = json.getDouble(LONGITUDE);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }

        public JSONObject asJSON() {
            JSONObject json = new JSONObject();

            try {
                json.put(ADDRESS, address);
                json.put(LATITUDE, latitude);
                json.put(LONGITUDE, longitude);
            } catch (JSONException e) { }

            return json;
        }

    }

    private AppCompatCheckBox mOnlineMeetingCheckBox;
    private AppCompatAutoCompleteTextView mInputAddress;
    private MapView mMapView;

    private Marker mSelectedLocation = null;

    private GoogleMap mMap;
    private LatLng mLocation = null;

    public LocationInputItem(@NonNull Context context) {
        super(context);
        setTitle(LocaleController.getString("HtLocation", works.heymate.beta.R.string.HtLocation));
        setIcon(ResourcesCompat.getDrawable(getResources(), works.heymate.beta.R.drawable.location_on_24_px_1, null));
    }

    public LocationInfo getLocationInfo() {
        if (mLocation == null) {
            return null;
        }

        String address = mInputAddress.getText().toString().trim();

        if (address.length() == 0 || mSelectedLocation == null) {
            return null;
        }

        return new LocationInfo(
                address,
                mSelectedLocation.getPosition().latitude,
                mSelectedLocation.getPosition().longitude
        );
    }

    public void setLocationInfo(LocationInfo locationInfo) {
        setLocationInfo(locationInfo.address, locationInfo.latitude, locationInfo.longitude);
    }

    public void setLocationInfo(String address, double latitude, double longitude) {
        mInputAddress.setText(address);

        mLocation = new LatLng(latitude, longitude);

        if (mSelectedLocation != null) {
            mSelectedLocation.setPosition(mLocation);
        }
        else {
            putMarker(mLocation);
        }

        if (mMap != null) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mLocation, 13));
        }
    }

    @Override
    protected View createContent() {
        FrameLayout content = new FrameLayout(getContext());

        mOnlineMeetingCheckBox = new AppCompatCheckBox(getContext());
        mOnlineMeetingCheckBox.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        mOnlineMeetingCheckBox.setTextSize(14);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mOnlineMeetingCheckBox.setButtonTintList(ColorStateList.valueOf(ContextCompat.getColor(getContext(), R.color.ht_theme)));
        }
        mOnlineMeetingCheckBox.setText(Texts.get(Texts.ONLINE_MEETING));
        mOnlineMeetingCheckBox.setChecked(false);
        mOnlineMeetingCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> checkMeetingType());
        content.addView(mOnlineMeetingCheckBox, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.TOP | Gravity.LEFT, HEADER_LEFT_MARGIN, 4, 0, 0));

        mInputAddress = new AppCompatAutoCompleteTextView(getContext());
        mInputAddress.setInputType(EditorInfo.TYPE_CLASS_TEXT);
        mInputAddress.setSingleLine(true);
        mInputAddress.setMaxLines(1);
        mInputAddress.setImeOptions(EditorInfo.IME_ACTION_DONE);
        mInputAddress.setHint(Texts.get(Texts.SEARCH_ADDRESS));
        mInputAddress.setHintTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteHintText));
        mInputAddress.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        mInputAddress.setThreshold(2);
        mInputAddress.setBackground(Theme.createEditTextDrawable(getContext(), false));
        content.addView(mInputAddress, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.TOP, HEADER_LEFT_MARGIN, 36, 20, 0));

        mInputAddress.setAdapter(new ArrayAdapter<WrappedAddress>(getContext(), works.heymate.beta.R.layout.autocomplete_item) {

            private Filter mFilter = null;

            @NonNull
            @Override
            public Filter getFilter() {
                if (mFilter == null) {
                    final Geocoder geocoder = new Geocoder(ApplicationLoader.applicationContext, LocaleController.getInstance().getSystemDefaultLocale());

                    mFilter = new Filter() {

                        @Override
                        protected FilterResults performFiltering(CharSequence constraint) {
                            FilterResults results = new FilterResults();

                            List<Address> addresses = null;

                            if (mLocation != null) {
                                class Holder {
                                    LatLng leftBottom = null;
                                    LatLng rightTop = null;
                                }

                                final Holder holder = new Holder();

                                Utils.runOnUIThread(() -> {
                                    if (mMap != null) {
                                        holder.leftBottom = mMap.getProjection().fromScreenLocation(new Point(0, mMapView.getHeight()));
                                        holder.rightTop = mMap.getProjection().fromScreenLocation(new Point(mMapView.getWidth(), 0));
                                    }
                                    synchronized (holder) {
                                        holder.notifyAll();
                                    }
                                });

                                synchronized (holder) {
                                    try {
                                        holder.wait();
                                    } catch (InterruptedException e) { }
                                }

                                if (holder != null) {
                                    try {
                                        addresses = geocoder.getFromLocationName(constraint.toString(), 5, holder.leftBottom.latitude, holder.leftBottom.longitude, holder.rightTop.latitude, holder.rightTop.longitude);
                                    } catch (IOException e) { }
                                }
                            }
                            else {
                                try {
                                    addresses = geocoder.getFromLocationName(constraint.toString(), 5);
                                } catch (IOException e) { }
                            }

                            if (addresses != null) {
                                results.values = wrap(addresses);
                                results.count = addresses.size();
                            }

                            return results;
                        }

                        @Override
                        protected void publishResults(CharSequence constraint, FilterResults results) {
                            clear();

                            if (results.values instanceof List) {
                                addAll(((List<WrappedAddress>) results.values));
                            }
                        }

                    };
                }

                return mFilter;
            }

        });
        mInputAddress.setOnItemClickListener((parent, view, position, id) -> {
            Address address = ((WrappedAddress) mInputAddress.getAdapter().getItem(position)).address;

            if (mMap != null) {
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(address.getLatitude(), address.getLongitude()), 14));
            }

            InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(mInputAddress.getWindowToken(), 0);

            mLocation = new LatLng(address.getLatitude(), address.getLongitude());
        });

        mMapView = new MapView(getContext()) {

            @Override
            public boolean dispatchTouchEvent(MotionEvent ev) {
                boolean result = super.dispatchTouchEvent(ev);

                getParent().requestDisallowInterceptTouchEvent(result);

                return result;
            }

        };
        content.addView(mMapView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 172, Gravity.TOP, CONTENT_HORIZONTAL_MARGIN, 68, CONTENT_HORIZONTAL_MARGIN, 0));

//        ImageView imagePin = new ImageView(getContext());
//        imagePin.setImageResource(works.heymate.beta.R.drawable.map_pin);
//        content.addView(imagePin, LayoutHelper.createFrame(26, 42, Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM, 0, 0, 0, 98));

        return content;
    }

    private void checkMeetingType() {
        boolean isChecked = mOnlineMeetingCheckBox.isChecked();
        mInputAddress.setVisibility(isChecked ? GONE : VISIBLE);
        mMapView.setVisibility(isChecked ? GONE : VISIBLE);
        updateLayoutHeight();
    }

    public String getMeetingType() {
        return mOnlineMeetingCheckBox.isChecked() ? MeetingType.ONLINE_MEETING : MeetingType.DEFAULT;
    }

    public void setMeetingType(String type) {
        mOnlineMeetingCheckBox.setChecked(MeetingType.ONLINE_MEETING.equals(type));
        checkMeetingType();
    }

    @SuppressLint("MissingPermission")
    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        mMapView.onCreate(null);

        mMapView.getMapAsync(googleMap -> {
            mMap = googleMap;

            try {
                googleMap.setMyLocationEnabled(true);
            } catch (Exception e) { }
            googleMap.getUiSettings().setMyLocationButtonEnabled(true);

            if (mLocation == null) {
                if (Build.VERSION.SDK_INT >= 23) {
                    if (getContext().checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        Location location = getLastLocation();
                        if (location != null) {
                            mLocation = new LatLng(location.getLatitude(), location.getLongitude());
                        }
                    }
                }
                else {
                    Location location = getLastLocation();
                    if (location != null) {
                        mLocation = new LatLng(location.getLatitude(), location.getLongitude());
                    }
                }
            }

            if (mLocation != null) {
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mLocation, 13));

                if (mSelectedLocation != null) {
                    mSelectedLocation.setPosition(mLocation);
                }
                else {
                    putMarker(mLocation);
                }
            }
            else {
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(0, 0), 1));
            }

            googleMap.setOnCameraIdleListener(() -> mLocation = mMap.getCameraPosition().target);
            googleMap.setOnMapClickListener(latLng -> {
                if (mSelectedLocation != null) {
                    mSelectedLocation.setPosition(latLng);
                }
                else {
                    putMarker(latLng);
                }

            });
        });
    }

    private void putMarker(LatLng latLng) {
        if (mMap == null) {
            return;
        }

        MarkerOptions options = new MarkerOptions()
                .position(latLng)
                .icon(BitmapDescriptorFactory.fromResource(works.heymate.beta.R.drawable.map_pin))
                .anchor(0.5f, 1)
                .draggable(true);

        mSelectedLocation = mMap.addMarker(options);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        if (w > 0 && h > 0) {
            mMapView.onStart();
            mMapView.onResume();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mMapView.onPause();
        mMapView.onStop();
        mMapView.onDestroy();
    }

    @SuppressLint("MissingPermission")
    private Location getLastLocation() {
        LocationManager lm = (LocationManager) ApplicationLoader.applicationContext.getSystemService(Context.LOCATION_SERVICE);
        List<String> providers = lm.getProviders(true);
        Location l = null;
        for (int i = providers.size() - 1; i >= 0; i--) {
            l = lm.getLastKnownLocation(providers.get(i));
            if (l != null) {
                break;
            }
        }
        return l;
    }

    private static List<WrappedAddress> wrap(List<Address> addresses) {
        List<WrappedAddress> wrapped = new ArrayList<>(addresses.size());

        for (Address address: addresses) {
            wrapped.add(new WrappedAddress(address));
        }

        return wrapped;
    }

    private static class WrappedAddress {

        public final Address address;

        public WrappedAddress(Address address) {
            this.address = address;
        }

        @Override
        public String toString() {
            return address.getAddressLine(0);
        }
    }

}
