package us.wilmothit.multistopwatch;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private int TIMER_STARTED_BGCOLOR;
    private int TIMER_STARTED_FGCOLOR;
    private int TIMER_STOPPED_BGCOLOR;
    private int TIMER_STOPPED_FGCOLOR;

    private TimerViewModel timerViewModel;
    final private TextView[] timerViews = new TextView[6];
    final private TextViewTimerWrapper[] textViewTimerWrappers = new TextViewTimerWrapper[6];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar timerSettingsToolbar = findViewById(R.id.timerSettingsToolbar);
        setSupportActionBar(timerSettingsToolbar);


        //Setup our theme colors
        TIMER_STARTED_BGCOLOR = getResources().getColor(R.color.colorPrimaryLight);
        TIMER_STARTED_FGCOLOR = getResources().getColor(R.color.colorAccent);
        TIMER_STOPPED_BGCOLOR = getResources().getColor(R.color.colorPrimary);
        TIMER_STOPPED_FGCOLOR = getResources().getColor(R.color.colorPrimaryDark);

        //Get our views
        timerViews[0] = findViewById(R.id.timer1TxtVw);
        timerViews[1] = findViewById(R.id.timer2TxtVw);
        timerViews[2] = findViewById(R.id.timer3TxtVw);
        timerViews[3] = findViewById(R.id.timer4TxtVw);
        timerViews[4] = findViewById(R.id.timer5TxtVw);
        timerViews[5] = findViewById(R.id.timer6TxtVw);

        //Get our viewmodel
        timerViewModel = ViewModelProviders.of(this).get(TimerViewModel.class);

        //Loop through our timer views and setup some additional key things
        for(int i=0; i<timerViews.length; i++) {

            //Get the uuid which we use for lookup into the viewmodel
            UUID uuid = timerViewModel.getTimerUuid(timerViews[i].getId());
            //Setup a copy of the index for inner class/method use
            final int index = i;

            Log.d(TAG, "onCreate: i = " + i + ", index = " + index);
            //Observe the viewmodel and the timer value
            timerViewModel.getTimerValue(uuid).observe(this, new Observer<String>() {
                @Override
                public void onChanged(@Nullable String s) {
                    timerViews[index].setText(s);
                }
            });

            //Wrap the timer views with listeners; I wrap the views so that I don't have to
            //duplicate code
            textViewTimerWrappers[i] = new TextViewTimerWrapper(uuid, timerViews[i], timerViewModel);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.timer_settings, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.hoursMinutesSeconds:
                timerViewModel.setSpeed(CustomTimer.TimerSpeeds.HHMMSS);
                break;
            case R.id.minutesSecondsMilliseconds:
                timerViewModel.setSpeed(CustomTimer.TimerSpeeds.MMSSMS);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    class TextViewTimerWrapper {

        private static final String TAG = "TextViewTimerWrapper";

        public TextViewTimerWrapper(final UUID uuid, View view, final TimerViewModel viewModel) {

            Log.d(TAG, "TextViewTimerWrapper: Wrapping a text view, uuid = " + uuid);

            if(viewModel.isActive(uuid) && viewModel.isRunning(uuid)) {
                view.setBackgroundColor(TIMER_STARTED_BGCOLOR);
                ((TextView) view).setTextColor(TIMER_STARTED_FGCOLOR);
            } else {
                view.setBackgroundColor(TIMER_STOPPED_BGCOLOR);
                ((TextView) view).setTextColor(TIMER_STOPPED_FGCOLOR);
            }

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(viewModel.isActive(uuid)) {
                        if (viewModel.isRunning(uuid)) {
                            v.setBackgroundColor(TIMER_STOPPED_BGCOLOR);
                            ((TextView) v).setTextColor(TIMER_STOPPED_FGCOLOR);
                            viewModel.suspend(uuid);
                        } else {
                            v.setBackgroundColor(TIMER_STARTED_BGCOLOR);
                            ((TextView) v).setTextColor(TIMER_STARTED_FGCOLOR);
                            viewModel.resume(uuid);
                        }
                    } else {
                        v.setBackgroundColor(TIMER_STARTED_BGCOLOR);
                        ((TextView) v).setTextColor(TIMER_STARTED_FGCOLOR);
                        viewModel.start(uuid);
                    }
                }
            });

            view.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    v.setBackgroundColor(TIMER_STOPPED_BGCOLOR);
                    ((TextView)v).setTextColor(TIMER_STOPPED_FGCOLOR);
                    viewModel.finish(uuid);
                    return true;
                }
            });

        }

    }
}
