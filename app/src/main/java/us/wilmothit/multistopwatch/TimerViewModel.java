package us.wilmothit.multistopwatch;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.MutableLiveData;
import android.support.annotation.NonNull;
import android.util.Log;

import java.util.Hashtable;
import java.util.UUID;

public class TimerViewModel extends AndroidViewModel {

    private static final String TAG = "TimerViewModel";

    //This maps view IDs to UUIDs
    final private Hashtable<Integer, UUID> timerIndexes = new Hashtable<>();
    //This maps UUIDs to timers
    final private Hashtable<UUID, CustomTimer> timers = new Hashtable<>();
    //This maps UUIDs to observable string time values
    final private Hashtable<UUID, MutableLiveData<String>> timerValues = new Hashtable<>();

    public TimerViewModel(@NonNull Application application) {
        super(application);
    }



    public void setSpeed(CustomTimer.TimerSpeeds speed) {
        for(UUID key: timers.keySet())
            timers.get(key).setSpeed(speed);
    }

    /**
     * Obtains a UUID if one already exists for the id; if not, then creates one and sets up the
     * backend and such
     * @param id    Whatever you are using as an identifier; in this case, the view's id
     * @return
     */
    public UUID getTimerUuid(int id) {
        UUID uuid = timerIndexes.get(id);
        if(uuid == null) {
            uuid = UUID.randomUUID();
            timerIndexes.put(id, uuid);
            timers.put(uuid, new CustomTimer(uuid, this));
            timerValues.put(uuid, new MutableLiveData<String>());
        }
        return uuid;
    }

    public void start(UUID uuid) {
        CustomTimer timer = timers.get(uuid);
        if(timer == null) {
            Log.d(TAG, "start: Timers hashtable does NOT contain uuid = " + uuid);
            timer = new CustomTimer(uuid, this);
            timers.put(uuid, timer);
        }
        new Thread(timer).start();
    }

    public boolean isActive(UUID uuid) {
        CustomTimer timer = timers.get(uuid);
        return timer != null && timer.isActive();
    }

    public boolean isRunning(UUID uuid) {
        CustomTimer timer = timers.get(uuid);
        return timer != null && timer.isRunning();
    }

    public void suspend(UUID uuid) {
        CustomTimer timer = timers.get(uuid);
        if(timer != null) timer.suspend();
    }

    public void resume(UUID uuid) {
        CustomTimer timer = timers.get(uuid);
        if(timer != null) timer.resume();
    }

    public void finish(UUID uuid) {
        CustomTimer timer = timers.get(uuid);
        if(timer != null) {
            timer.finish();
        }
    }

    public MutableLiveData<String> getTimerValue(UUID uuid) {
        MutableLiveData<String> val = timerValues.get(uuid);
        try {
            if(val == null) {
                throw new NullPointerException(
                        "timerValues.get(uuid) where uuid = " + uuid + " returned a null."
                );
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
            val = new MutableLiveData<>();
            val.postValue("Error");
        }
        return val;
    }

    public synchronized void update(UUID uuid, String formattedTime) {
        if(timerValues.containsKey(uuid)) {
            try {
                timerValues.get(uuid).postValue(formattedTime);
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Calls the finish method on all timers once this viewmodel is no longer used so that we don't
     * have any lost threads that (might) keep the app from closing
     */
    @Override
    protected void onCleared() {
        super.onCleared();
        for(UUID key: timers.keySet()) {
            try {
                timers.get(key).finish();
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }
    }
}
