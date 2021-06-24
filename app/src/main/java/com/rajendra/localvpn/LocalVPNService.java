/*
** Copyright 2015, Mohamed Naufal
**
** Licensed under the Apache License, Version 2.0 (the "License");
** you may not use this file except in compliance with the License.
** You may obtain a copy of the License at
**
**     http://www.apache.org/licenses/LICENSE-2.0
**
** Unless required by applicable law or agreed to in writing, software
** distributed under the License is distributed on an "AS IS" BASIS,
** WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
** See the License for the specific language governing permissions and
** limitations under the License.
*/

package com.rajendra.localvpn;

import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.VpnService;
import android.os.Build;
import android.os.ParcelFileDescriptor;
import android.util.Log;

public class LocalVPNService extends VpnService
{
    private static final String TAG = LocalVPNService.class.getSimpleName();
    private static final String VPN_ADDRESS = "10.0.0.2"; // Only IPv4 support for now
    private static final String VPN_ROUTE = "0.0.0.0"; // Intercept everything
    String[] appPackages = {
            "com.android.chrome",
    };

    public static final String BROADCAST_VPN_STATE = "xyz.hexene.localvpn.VPN_STATE";

    private static boolean isRunning = false;

    private ParcelFileDescriptor vpnInterface = null;

    private PendingIntent pendingIntent;

    private String ip, dns;

    @Override
    public void onCreate()
    {
        super.onCreate();
        isRunning = true;

    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void setupVPN()
    {
        if (vpnInterface == null)
        {
            Builder builder = new Builder();

            PackageManager packageManager = getPackageManager();
            for (String appPackage: appPackages) {
                try {
                    packageManager.getPackageInfo(appPackage, 0);
                    builder.addAllowedApplication(appPackage);
                } catch (PackageManager.NameNotFoundException e) {
                    // The app isn't installed.
                }
            }

            builder.addAddress(ip, 32);
            builder.addDnsServer(dns);
            builder.addDnsServer("1.0.0.1");
            builder.addDnsServer("10.1.9.8");
            vpnInterface = builder.setSession(getString(R.string.app_name)).setConfigureIntent(pendingIntent).establish();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
         ip = intent.getStringExtra("ip");
         dns = intent.getStringExtra("dns");

        setupVPN();

        return START_STICKY;
    }

    public static boolean isRunning()
    {
        return isRunning;
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        isRunning = false;
        cleanup();
        Log.i(TAG, "Stopped");
    }

    private void cleanup()
    {
        ByteBufferPool.clear();
    }

}
