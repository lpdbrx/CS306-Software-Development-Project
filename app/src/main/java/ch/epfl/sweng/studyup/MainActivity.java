package ch.epfl.sweng.studyup;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.firebase.storage.StorageReference;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.squareup.picasso.Picasso;

import ch.epfl.sweng.studyup.firebase.FirebaseCloud;

import ch.epfl.sweng.studyup.map.BackgroundLocation;
import ch.epfl.sweng.studyup.player.CustomActivity;
import ch.epfl.sweng.studyup.player.Player;
import ch.epfl.sweng.studyup.utils.Navigation;
import ch.epfl.sweng.studyup.utils.Utils;

import antonkozyriatskyi.circularprogressindicator.CircularProgressIndicator;
import static ch.epfl.sweng.studyup.utils.Utils.XP_STEP;

public class MainActivity extends Navigation {
    private static final CircularProgressIndicator.ProgressTextAdapter LEVEL_PROGRESS_TEXT = new CircularProgressIndicator.ProgressTextAdapter() {
        @Override
        public String formatText(double progress) {
            return (progress * 100 + "% of level ").concat(String.valueOf(Player.get().getLevel()));
        }
    };
    private final int MY_PERMISSION_REQUEST_FINE_LOCATION = 202;
    private ImageView image_view;

    // Text that will be displayed in the levelProgress layout
    CircularProgressIndicator levelProgress;
    ImageButton pic_button2;


    // Display login success message from intent set by authentication activity
    public void displayLoginSuccessMessage(Intent intent) {
        String successMessage = intent.getStringExtra(getString(R.string.post_login_message_value));
        if (successMessage != null) {
            Toast.makeText(getApplicationContext(), successMessage, Toast.LENGTH_LONG).show();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("GPS_MAP", "Destroyed main and canceled Background location service");
        JobScheduler scheduler = (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);
        scheduler.cancel(BackgroundLocation.BACKGROUND_LOCATION_ID);
    }

    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        displayLoginSuccessMessage(getIntent());

        Log.d("GPS_MAP", "Started main");
        // GPS Job scheduler
        ActivityCompat.requestPermissions(
                MainActivity.this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                MY_PERMISSION_REQUEST_FINE_LOCATION);
        Utils.mainContext = this.getApplicationContext();
        Utils.locationProviderClient = new FusedLocationProviderClient(this);

        if (!Utils.isMockEnabled) {
            scheduleBackgroundLocation();
        }

        // User picture
        ImageButton pic_button = findViewById(R.id.pic_btn);
        pic_button2 = findViewById(R.id.pic_btn2);
        image_view = findViewById(R.id.pic_imageview);

        StorageReference ref = FirebaseCloud.getFileStorageRef("user_pictures", Integer.toString(Player.get().getSciper()));

        ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                //Pass the URL to Picasso to download and show in ImageView
                Picasso.get().load(uri.toString()).into(image_view);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                exception.printStackTrace();
            }
        });


        pic_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, CustomActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.go_right_in, R.anim.go_right_out);
            }
        });
        pic_button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, CustomActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.go_right_in, R.anim.go_right_out);
                pic_button2.setBackground(getResources().getDrawable(R.drawable.ic_mode_edit_clicked_24dp));
            }
        });

        //username
        TextView view_username = findViewById(R.id.usernameText);
        view_username.setText(Player.get().getUserName());
        view_username.setMaxLines(1);
        view_username.setMaxWidth(300);

        // Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(null);

        //bottom navigation bar
        navigationSwitcher(MainActivity.this, MainActivity.class, Utils.DEFAULT_INDEX);

        // Level progression bar
        ActivityCompat.requestPermissions(
                MainActivity.this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                123);

        levelProgress = findViewById(R.id.level_progress);
        levelProgress.setProgress(Player.get().getLevelProgress(), 1);
        levelProgress.setStartAngle(270);
        levelProgress.setProgressTextAdapter(LEVEL_PROGRESS_TEXT);
        TextView lvl = findViewById(R.id.levelText);
        TextView curr = findViewById(R.id.currText);
        lvl.setText(Utils.LEVEL_DISPLAY + Player.get().getLevel());
        curr.setText(Utils.CURR_DISPLAY + Player.get().getCurrency());
        updateCurrDisplay();
        updateXpAndLvlDisplay();

    }



    @Override
    protected void onResume() {
        super.onResume();
        updateCurrDisplay();
        updateXpAndLvlDisplay();
    }

    // Display the toolbar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater i = getMenuInflater();
        i.inflate(R.menu.top_navigation, menu);
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case MY_PERMISSION_REQUEST_FINE_LOCATION:

                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.i("GPS_MAP", "Permission granted");
                } else {
                    Toast.makeText(getApplicationContext(), "This app requires location", Toast.LENGTH_SHORT).show();
                }

                break;
        }
    }

    // Allows you to do an action with the toolbar (in a different way than with the navigation bar)
    // Corresponding activities are not created yet
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.top_navigation_settings) {
            Toast.makeText(MainActivity.this,
                    "You have clicked on Settings :)",
                    Toast.LENGTH_SHORT).show();
        }
        if (item.getItemId() == R.id.top_navigation_infos) {
            Toast.makeText(MainActivity.this,
                    "You have clicked on Infos :)",
                    Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Function that is called when adding xp with the button
     *
     * @param view
     */
    public void addExpPlayer(View view) {
        Player.get().addExperience(XP_STEP, this);
    }


    public void updateXpAndLvlDisplay() {
        levelProgress.setCurrentProgress(Player.get().getLevelProgress());
        ((TextView) findViewById(R.id.levelText)).setText(Utils.LEVEL_DISPLAY + Player.get().getLevel());
    }

    public void updateCurrDisplay() {
        ((TextView) findViewById(R.id.currText)).setText(Utils.CURR_DISPLAY + Player.get().getCurrency());
    }

    public void scheduleBackgroundLocation(){
        JobScheduler scheduler = (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);
        JobInfo jobInfo = new JobInfo.Builder(BackgroundLocation.BACKGROUND_LOCATION_ID, new ComponentName(this, BackgroundLocation.class)).setPeriodic(15 * 60 * 1000).build();
        scheduler.schedule(jobInfo);
        for(JobInfo job: scheduler.getAllPendingJobs()){
            Log.d("GPS_MAP", "Scheduled: " + job);
        }
        Log.d("GPS_MAP", "schedule");
    }

}


