package us.wilmothit.multistopwatch;

import android.os.Handler;
import android.util.Log;

import java.util.Locale;
import java.util.Observable;
import java.util.UUID;

public class CustomTimer implements Runnable {

    public enum TimerSpeeds {
        HHMMSS,
        MMSSMS
    }

    private static final String TAG = "CustomTimer";

    private int sleepTimeInSeconds = 1000;

    private boolean active = false;
    private boolean running = false;

    final private UUID uuid;
    final private TimerViewModel viewModel;

    final Handler mainHandler = new Handler();

    public CustomTimer(UUID uuid, TimerViewModel viewModel) {
        this.uuid = uuid;
        this.viewModel = viewModel;
    }

    public synchronized void setSpeed(TimerSpeeds speed) {
        switch(speed) {
            case HHMMSS:
                sleepTimeInSeconds = 1000;
                break;
            case MMSSMS:
                sleepTimeInSeconds = 1000 / 60;
                break;
        }
    }

    public UUID getUuid() { return uuid; }

    public boolean isActive() { return active; }

    public boolean isRunning() { return running; }

    public void suspend() {
        Log.d(TAG, "suspend: Timer suspending, uuid = " + uuid);
        running = false;
    }

    public synchronized void resume() {
        Log.d(TAG, "resume: Timer resuming, uuid = " + uuid);
        running = true;
        notify();
    }

    public synchronized void finish() {
        Log.d(TAG, "finish: Timer finishing, uuid = " + uuid);
        running = false;
        active = false;
        notify();
        updateUI(0);
    }

    /**
     * Keeps track of the total seconds and calls updates to the UI
     */
    @Override
    public void run() {
        active = running = true;
        //We set to -1 to account for the initial second of time
        int totalSeconds = -1;

        while(active) {

            if(running) {

                totalSeconds += 1;
                updateUI(totalSeconds);

                Log.d(TAG, "run: Timer running, uuid = " + uuid + ", totalSeconds = " + totalSeconds);
                try {
                    Thread.sleep(sleepTimeInSeconds);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            } else {

                Log.d(TAG, "run: Timer suspended, uuid = " + uuid + ", totalSeconds = " + totalSeconds);

                synchronized (this) {
                    try {
                        wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                Log.d(TAG, "run: Timer out of suspension, uuid = " + uuid + ", totalSeconds = " + totalSeconds);

            }

        }

        Log.d(TAG, "run: Timer finished, active = " + active);
    }

    /**
     * Updates the view model which updates the user interface on a separate thread
     * @param totalSeconds  A manual directive so we can specify zero if desired
     */
    private void updateUI(final int totalSeconds) {
        mainHandler.post(new Runnable() {
            @Override
            public void run() {

                int hours = (totalSeconds % 86400) / 3600;
                int minutes = ((totalSeconds % 86400) % 3600) / 60;
                int seconds = ((totalSeconds % 86400) % 3600) % 60;

                viewModel.update(uuid, String.format(
                        Locale.getDefault(),"%02d:%02d:%02d", hours, minutes, seconds)
                );
            }
        });
    }

}
