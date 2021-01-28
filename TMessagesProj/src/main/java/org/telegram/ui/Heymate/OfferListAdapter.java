/*
package org.telegram.ui.Heymate;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.telegram.messenger.R;
import org.telegram.ui.Components.Bulletin;

import java.util.List;

public class OfferListAdapter extends ArrayAdapter<Integer> {

    private Context context;

    public OfferListAdapter(@NonNull Context context, int resource, @NonNull List<Integer> objects) {
        super(context, resource, objects);
        this.context = context;
    }


    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        super.getView(position, convertView, parent);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (convertView == null){
            convertView = inflater.inflate(R.layout.offer_list, parent);
        }
        Button btn1 = convertView.findViewById(R.id.btn_1);
        Button btn2 = convertView.findViewById(R.id.btn_2);
        btn1.setText("" + getItemId(position));
        btn2.setText("" + getItemId(position));
        return convertView;
    }
}
*/
