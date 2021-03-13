package org.telegram.ui.Heymate;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.BackDrawable;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;

import works.heymate.core.Texts;
import works.heymate.core.wallet.Wallet;

public class WalletActivity extends BaseFragment {

    private ActionBar mActionBar;
    private ImageView mImageWallet;
    private TextView mTextTitle;
    private TextView mTextStatus;
    private TextView mTextLeftButton;
    private ImageView mImageLeftButton;
    private View mLeftButton;
    private TextView mTextRightButton;
    private ImageView mImageRightButton;
    private View mRightButton;

    @Override
    public boolean onFragmentCreate() {
        return super.onFragmentCreate();
    }

    @Override
    public void onFragmentDestroy() {
        super.onFragmentDestroy();
    }

    @Override
    public View createView(Context context) {
        View content = LayoutInflater.from(context).inflate(R.layout.activity_wallet, null, false);

        mActionBar = content.findViewById(R.id.actionbar);
        mImageWallet = content.findViewById(R.id.image_wallet);
        mTextTitle = content.findViewById(R.id.text_title);
        mTextStatus = content.findViewById(R.id.text_status);
        mTextLeftButton = content.findViewById(R.id.text_leftbutton);
        mImageLeftButton = content.findViewById(R.id.image_leftbutton);
        mLeftButton = content.findViewById(R.id.leftbutton);
        mTextRightButton = content.findViewById(R.id.text_rightbutton);
        mImageRightButton = content.findViewById(R.id.image_rightbutton);
        mRightButton = content.findViewById(R.id.rightbutton);

        mActionBar.setBackButtonDrawable(new BackDrawable(false));
        mActionBar.setTitle(Texts.get(Texts.WALLET));
        mActionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (getParentActivity() == null) {
                    return;
                }

                if (id == -1) {
                    finishFragment();
                }
            }
        });

        Wallet wallet = Wallet.get(TG2HM.getCurrentPhoneNumber());

//        R.drawable.ic_ab_back;
//        R.drawable.floating_check;
//        R.drawable.msg_arrowright;
//        Theme.getColor(Theme.key_actionBarDefaultTitle)
//        AndroidUtilities.getTypeface("fonts/rmedium.ttf")
        return super.createView(context);
    }

}
