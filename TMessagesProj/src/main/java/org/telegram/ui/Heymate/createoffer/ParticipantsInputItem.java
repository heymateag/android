package org.telegram.ui.Heymate.createoffer;

import android.content.Context;
import android.content.res.ColorStateList;
import android.os.Build;
import android.text.InputFilter;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.core.content.ContextCompat;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.LayoutHelper;

import works.heymate.beta.R;
import works.heymate.core.Texts;

public class ParticipantsInputItem extends ExpandableItem {

    private EditText mEditCount;
    private AppCompatCheckBox mCheckBox;

    public ParticipantsInputItem(@NonNull Context context) {
        super(context);
        setTitle(Texts.get(Texts.PARTICIPANTS_INPUT_TITLE));
        setIcon(AppCompatResources.getDrawable(context, R.drawable.ic_participants));
    }

    @Override
    protected View createContent() {
        RelativeLayout content = new RelativeLayout(getContext());

        TextView textDescription = new TextView(getContext());
        textDescription.setId(1);
        textDescription.setTextSize(14);
        textDescription.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText));
        textDescription.setText(Texts.get(Texts.PARTICIPANTS_INPUT_DESCRIPTION));
        content.addView(textDescription, LayoutHelper.createRelative(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, HEADER_LEFT_MARGIN, 0, HEADER_RIGHT_MARGIN, 0, RelativeLayout.ALIGN_PARENT_TOP));

        mEditCount = new AppCompatEditText(getContext()) {

            @Override
            public void setEnabled(boolean enabled) {
                super.setEnabled(enabled);
                setTextColor(enabled ? Theme.getColor(Theme.key_windowBackgroundWhiteBlackText) : Theme.getColor(Theme.key_windowBackgroundWhiteHintText));
            }

        };

        mEditCount.setId(2);
        mEditCount.setInputType(EditorInfo.TYPE_CLASS_NUMBER);
        mEditCount.setEnabled(true);
        mEditCount.setBackground(Theme.createEditTextDrawable(getContext(), false));
        mEditCount.setFilters(new InputFilter[] { new InputFilter.LengthFilter(3) });
        mEditCount.setText("1");

        TextView usersLabel = new TextView(getContext());
        usersLabel.setTextSize(16);
        usersLabel.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText));
        usersLabel.setText(Texts.get(Texts.PARTICIPANTS_INPUT_USERS));
        usersLabel.measure(
                MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
                MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
        int usersLabelWidth = usersLabel.getMeasuredWidth();

        content.addView(mEditCount, LayoutHelper.createRelative(usersLabelWidth / AndroidUtilities.density + 48, LayoutHelper.WRAP_CONTENT, HEADER_LEFT_MARGIN, 12, 0, 0, RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.BELOW, textDescription.getId()));

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.ALIGN_RIGHT, mEditCount.getId());
        params.addRule(RelativeLayout.ALIGN_BASELINE, mEditCount.getId());
        content.addView(usersLabel, params);

        LinearLayout radioLayout = new LinearLayout(getContext());
        radioLayout.setOrientation(LinearLayout.HORIZONTAL);

        mCheckBox = new AppCompatCheckBox(getContext());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mCheckBox.setButtonTintList(ColorStateList.valueOf(ContextCompat.getColor(getContext(), R.color.ht_theme)));
        }
        mCheckBox.setChecked(false);
        radioLayout.addView(mCheckBox, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.BOTTOM));

        TextView unlimitedLabel = new TextView(getContext());
        unlimitedLabel.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText));
        unlimitedLabel.setTextSize(16);
        unlimitedLabel.setText(Texts.get(Texts.PARTICIPANTS_INPUT_UNLIMITED));
        radioLayout.addView(unlimitedLabel, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.BOTTOM, 4, 0, 0, 0));

        params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.RIGHT_OF, mEditCount.getId());
        params.addRule(RelativeLayout.ALIGN_TOP, mEditCount.getId());
        params.leftMargin = AndroidUtilities.dp(24);
        content.addView(radioLayout, params);

        mCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> mEditCount.setEnabled(!mCheckBox.isChecked()));

        radioLayout.setOnClickListener(v -> {
            mCheckBox.setChecked(!mCheckBox.isChecked());
            mEditCount.setEnabled(!mCheckBox.isChecked());
        });

        return content;
    }

    public int getMaximumParticipants() {
        return mCheckBox.isChecked() ? 0 : (mEditCount.length() == 0 ? -1 : Integer.parseInt(mEditCount.getText().toString()));
    }

    public void setMaximumParticipants(int maximumParticipants) {
        switch (maximumParticipants) {
            case -1:
                mCheckBox.setChecked(false);
                mEditCount.setEnabled(true);
                mEditCount.setText("");
                break;
            case 0:
                mCheckBox.setChecked(true);
                mEditCount.setEnabled(false);
                break;
            default:
                mCheckBox.setChecked(false);
                mEditCount.setText(String.valueOf(maximumParticipants));
                break;
        }
    }

}