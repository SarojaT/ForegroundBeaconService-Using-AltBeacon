package com.example.pocble;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.InternalBeaconConsumer;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;
import org.altbeacon.beacon.startup.BootstrapNotifier;
import org.altbeacon.beacon.startup.RegionBootstrap;

import java.util.Collection;

public class BeaconService extends Service implements BootstrapNotifier, InternalBeaconConsumer,RangeNotifier {

    private RegionBootstrap regionBootstrap;
    private BeaconManager mBeaconManager;

    @Override
    public void onCreate() {
        super.onCreate();
        mBeaconManager = BeaconManager.getInstanceForApplication(this.getApplicationContext());
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O)
            startMyOwnForeground("Message");
        else startForeground(1, new Notification());
        Log.e("BeaconService","onCreate");
        // Detect the URL frame:
        mBeaconManager.getBeaconParsers().add(new BeaconParser().
                setBeaconLayout(BeaconParser.EDDYSTONE_URL_LAYOUT));
        mBeaconManager.getBeaconParsers().add(new BeaconParser().
                setBeaconLayout(BeaconParser.EDDYSTONE_UID_LAYOUT));
        mBeaconManager.getBeaconParsers().add(new BeaconParser().
                setBeaconLayout(BeaconParser.EDDYSTONE_TLM_LAYOUT));
        mBeaconManager.setBackgroundBetweenScanPeriod(0);
        mBeaconManager.setBackgroundScanPeriod(1100);
        mBeaconManager.setRegionStatePersistenceEnabled(false);
        mBeaconManager.bindInternal(this);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (regionBootstrap != null) {
            regionBootstrap.disable();
        }
       /* if (mBeaconManager.foregroundServiceStartFailed()) {
            mBeaconManager.retryForegroundServiceScanning();
        }*/
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void didEnterRegion(Region region) {
        // Handle beacon region entry here
        Log.e("BeaconService","didEnter");
        Log.d("BeaconService", "I detected a beacon in the region with namespace id " + region.getId1() +
                " and instance id: " + region.getId2());
    }

    @Override
    public void didExitRegion(Region region) {
        // Handle beacon region exit here
        Log.e("BeaconService","didExit");
    }

    @Override
    public void didDetermineStateForRegion(int state, Region region) {
        // Handle region state changes (inside/outside) here if needed
        Log.e("BeaconService","didDetermineStateForRegion");

    }



    @Override
    public void onBeaconServiceConnect() {
        Log.e("BeaconService","onBeaconServiceConnect");
//        Identifier myBeaconNamespaceId = Identifier.parse("0x626C7565636861726D31");
//        Identifier myBeaconInstanceId = Identifier.parse("0x000000000001");
//        Region region = new Region("426C7565-4368-6172-6D42-6561636F6E43", myBeaconNamespaceId, myBeaconInstanceId, null);
        Identifier myBeaconNamespaceId = Identifier.parse("0x626C7565636861726D38");
        Identifier myBeaconInstanceId = Identifier.parse("0x000000000001");
        Region region = new Region("426C7565-4368-6172-6D42-6561636F6E42", myBeaconNamespaceId, myBeaconInstanceId, null);
//        regionBootstrap = new RegionBootstrap(this, region);
        mBeaconManager.startRangingBeacons(region);
        mBeaconManager.startMonitoring(region);
        mBeaconManager.addRangeNotifier(this);
        mBeaconManager.addMonitorNotifier(this);
    }

    @Override
    public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
      //  Log.e("BeaconService","didRangeBeaconsInRegion"+beacons.size());
    }

    private void startMyOwnForeground(String message) {
        String NOTIFICATION_CHANNEL_ID = "example.permanence";
        String channelName = "Background Service";
        NotificationChannel chan = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID,channelName, NotificationManager.IMPORTANCE_NONE);
            chan.setLightColor(Color.BLUE);
            chan.setLockscreenVisibility( Notification.VISIBILITY_PRIVATE);
            NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            manager.createNotificationChannel(chan);
        }
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
        Notification notification = notificationBuilder.setOngoing(true)
                .setContentTitle(message)
                .setPriority(NotificationManager.IMPORTANCE_MIN)
                .setCategory(Notification.CATEGORY_SERVICE)
                .build();
        mBeaconManager.enableForegroundServiceScanning(notificationBuilder.build(), 456);
        mBeaconManager.setEnableScheduledScanJobs(false);
        startForeground(2, notification);
    }

}
