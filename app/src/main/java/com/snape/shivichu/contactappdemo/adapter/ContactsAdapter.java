package com.snape.shivichu.contactappdemo.adapter;

import android.content.Context;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.snape.shivichu.contactappdemo.ContactsFragment;
import com.snape.shivichu.contactappdemo.FavoritesFragments;


/**
 * Created by Edgar on 30/04/2015.
 */
public class ContactsAdapter extends FragmentPagerAdapter {

    private static ContactsFragment CONTACTS_FRAGMENT = new ContactsFragment();
    private static FavoritesFragments FAVORITES_FRAGMENT = new FavoritesFragments();

    private Context mContext;

    public ContactsAdapter(FragmentManager fm, Context context) {
        super(fm);
        mContext = context;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return "FAVORITES";
            case 1:
                return "ALL CONTACTS";
            default:
                return "Unkown";
        }
    }
    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public android.support.v4.app.Fragment getItem(int position) {
        switch (position) {
            case 0:
                return FAVORITES_FRAGMENT;
            case 1:
                return CONTACTS_FRAGMENT;
            default:
                return CONTACTS_FRAGMENT;
        }
    }
}
