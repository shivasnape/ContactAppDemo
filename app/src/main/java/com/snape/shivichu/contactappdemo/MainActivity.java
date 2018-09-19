package com.snape.shivichu.contactappdemo;

import android.Manifest;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.transition.Slide;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.astuetz.PagerSlidingTabStrip;
import com.snape.shivichu.contactappdemo.adapter.ContactsAdapter;

import br.com.customsearchable.SearchActivity;
import br.com.customsearchable.contract.CustomSearchableConstants;
import br.com.customsearchable.model.ResultItem;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private PagerSlidingTabStrip tabs;
    private ViewPager viewPager;
    private ContactsAdapter viewPagerAdapter;
    private Boolean isPageViewWithOffset = Boolean.FALSE;

    private static final int PERMISSION_CALLBACK_CONSTANT = 100;
    private static final int REQUEST_PERMISSION_SETTING = 101;
    String[] permissionsRequired = new String[]{Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.READ_CONTACTS};
    private TextView txtPermissions;
    private Button btnCheckPermissions;
    private SharedPreferences permissionStatus;
    private boolean sentToSettings = false;


    // Activity Callbacks __________________________________________________________________________
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.getSupportActionBar().setElevation(0);



        permissionStatus = getSharedPreferences("permissionStatus", MODE_PRIVATE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkandgrantpermission();
        }


        getWindow().setSharedElementExitTransition(new Slide());
        getWindow().setSharedElementEnterTransition(new Slide());

        tabs = (PagerSlidingTabStrip) findViewById(R.id.tabs);
        viewPager = (ViewPager) findViewById(R.id.pager);
        viewPagerAdapter = new ContactsAdapter(this.getSupportFragmentManager(), this);

        viewPager.setAdapter(viewPagerAdapter);

        tabs.setShouldExpand(true);
        tabs.setTextColor(getResources().getColor(R.color.text_primary));
        tabs.setDividerColor(getResources().getColor(R.color.primary));
        tabs.setIndicatorColorResource(R.color.text_primary);
        tabs.setIndicatorHeight(7);

        // Bind the tabs to the ViewPager
        tabs.setViewPager(viewPager);

        //Start in Contacts Fragment
        viewPager.setCurrentItem(1);

        // Listener for slide animation on searchable selected
        setOnSearchableListener();

        // Configure custom-searchable UI
        // CustomSearchableInfo.setTransparencyColor(Color.parseColor("#0288D1"));
    }

    private void checkandgrantpermission() {

        if (ActivityCompat.checkSelfPermission(MainActivity.this, permissionsRequired[0]) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(MainActivity.this, permissionsRequired[1]) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, permissionsRequired[0])
                    || ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, permissionsRequired[1])) {
                //Show Information about why you need the permission
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Need Multiple Permissions");
                builder.setMessage("This app needs Camera and Location permissions.");
                builder.setPositiveButton("Grant", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        ActivityCompat.requestPermissions(MainActivity.this, permissionsRequired, PERMISSION_CALLBACK_CONSTANT);
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.show();
            } else if (permissionStatus.getBoolean(permissionsRequired[0], false)) {
                //Previously Permission Request was cancelled with 'Dont Ask Again',
                // Redirect to Settings after showing Information about why you need the permission
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Need Multiple Permissions");
                builder.setMessage("This app needs Camera and Location permissions.");
                builder.setPositiveButton("Grant", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        sentToSettings = true;
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", getPackageName(), null);
                        intent.setData(uri);
                        startActivityForResult(intent, REQUEST_PERMISSION_SETTING);
                        Toast.makeText(getBaseContext(), "Go to Permissions to Grant  Camera and Location", Toast.LENGTH_LONG).show();
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.show();
            } else {
                //just request the permission
                ActivityCompat.requestPermissions(MainActivity.this, permissionsRequired, PERMISSION_CALLBACK_CONSTANT);
            }

//            txtPermissions.setText("Permissions Required");

            //saving the permission in to a Shared Preference
            SharedPreferences.Editor editor = permissionStatus.edit();
            editor.putBoolean(permissionsRequired[0], true);
            editor.commit();
        } else {
            //You already have the permission, just go ahead.
            proceedAfterPermission();
        }

    }



        @Override
    public void onResume() {
        super.onResume();

        if (isPageViewWithOffset) {
            tabs.animate().translationY(0);
            viewPager.animate().translationY(0);
            isPageViewWithOffset = Boolean.FALSE;
        }

        //this.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
    }

    // MENU ________________________________________________________________________________________
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }

        if (id == R.id.action_search) {
            Intent intent = new Intent(this, SearchActivity.class);
            startActivity(intent);

            tabs.animate().translationY(-tabs.getHeight());
            viewPager.animate().translationY(-tabs.getHeight());
            isPageViewWithOffset = Boolean.TRUE;

            //this.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

            View contactFragmentRootView = ((ContactsFragment) viewPagerAdapter.getItem(1)).getRootView();
            contactFragmentRootView.invalidate();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // SEARCHABLE INTERFACE ________________________________________________________________________
    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            Toast.makeText(this, "typed: " + query, Toast.LENGTH_SHORT).show();
        }else if (Intent.ACTION_VIEW.equals(intent.getAction())) {
            Bundle bundle = this.getIntent().getExtras();

            assert (bundle != null);

            if (bundle != null) {
                ResultItem receivedItem = bundle.getParcelable(CustomSearchableConstants.CLICKED_RESULT_ITEM);
                ((ContactsFragment) viewPagerAdapter.getItem(1)).updateRecyclerViewFromSearchSelection(receivedItem.getHeader());
            }
        }
    }

    private void setOnSearchableListener () {
        SearchManager searchDialog = (SearchManager) this.getSystemService(Context.SEARCH_SERVICE);
        searchDialog.setOnCancelListener(new SearchManager.OnCancelListener() {
            @Override
            public void onCancel() {
                tabs.animate().translationY(0);
                viewPager.animate().translationY(0);
            }
        });

        searchDialog.setOnDismissListener(new SearchManager.OnDismissListener() {
            @Override
            public void onDismiss() {
                tabs.animate().translationY(0);
                viewPager.animate().translationY(0);
            }
        });

    }

        @Override
        public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            if (requestCode == PERMISSION_CALLBACK_CONSTANT) {
                //check if all permissions are granted
                boolean allgranted = false;
                for (int i = 0; i < grantResults.length; i++) {
                    if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                        allgranted = true;
                    } else {
                        allgranted = false;
                        break;
                    }
                }

                if (allgranted) {
                    proceedAfterPermission();
                } else if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, permissionsRequired[0])
                        || ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, permissionsRequired[1])) {
                    txtPermissions.setText("Permissions Required");
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setTitle("Need Multiple Permissions");
                    builder.setMessage("This app needs Camera and Location permissions.");
                    builder.setPositiveButton("Grant", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                            ActivityCompat.requestPermissions(MainActivity.this, permissionsRequired, PERMISSION_CALLBACK_CONSTANT);
                        }
                    });
                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
                    builder.show();
                } else {
                    Toast.makeText(getBaseContext(), "Unable to get Permission", Toast.LENGTH_LONG).show();
                }
            }
        }

        @Override
        protected void onActivityResult(int requestCode, int resultCode, Intent data) {
            super.onActivityResult(requestCode, resultCode, data);
            if (requestCode == REQUEST_PERMISSION_SETTING) {
                if (ActivityCompat.checkSelfPermission(MainActivity.this, permissionsRequired[0]) == PackageManager.PERMISSION_GRANTED) {
                    //Got Permission
                    proceedAfterPermission();
                }
            }
        }

    private void proceedAfterPermission() {
//        txtPermissions.setText("We've got all permissions");
        Toast.makeText(getBaseContext(), "We got All Permissions", Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        if (sentToSettings) {
            if (ActivityCompat.checkSelfPermission(MainActivity.this, permissionsRequired[0]) == PackageManager.PERMISSION_GRANTED) {
                //Got Permission
                proceedAfterPermission();
            }
        }
    }
}


