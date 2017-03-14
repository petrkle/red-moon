/*
 * Copyright (c) 2016  Marien Raat <marienraat@riseup.net>
 * Copyright (c) 2017  Stephen Michel <s@smichel.me>
 *
 *  This file is free software: you may copy, redistribute and/or modify it
 *  under the terms of the GNU General Public License as published by the Free
 *  Software Foundation, either version 3 of the License, or (at your option)
 *  any later version.
 *
 *  This file is distributed in the hope that it will be useful, but WITHOUT ANY
 *  WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 *  FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 *  details.
 *
 *  You should have received a copy of the GNU General Public License along with
 *  this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * This file incorporates work covered by the following copyright and
 * permission notice:
 *
 *     Copyright (c) 2015 Chris Nguyen
 *     Copyright (c) 2016 Zoraver <https://github.com/Zoraver>
 *
 *     Permission to use, copy, modify, and/or distribute this software
 *     for any purpose with or without fee is hereby granted, provided
 *     that the above copyright notice and this permission notice appear
 *     in all copies.
 *
 *     THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL
 *     WARRANTIES WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED
 *     WARRANTIES OF MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE
 *     AUTHOR BE LIABLE FOR ANY SPECIAL, DIRECT, INDIRECT, OR
 *     CONSEQUENTIAL DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING FROM LOSS
 *     OF USE, DATA OR PROFITS, WHETHER IN AN ACTION OF CONTRACT,
 *     NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR IN
 *     CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */

package com.jmstudios.redmoon.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.IBinder

import com.jmstudios.redmoon.event.*
import com.jmstudios.redmoon.model.Config
import com.jmstudios.redmoon.util.appContext
import com.jmstudios.redmoon.util.hasLocationPermission
import com.jmstudios.redmoon.util.Logger

import com.luckycatlabs.sunrisesunset.SunriseSunsetCalculator
import org.greenrobot.eventbus.EventBus

import java.util.*

/**
 * When the service starts, we request location updates. When we get a new
 * location fix, we shut down the service. When we shut down (for any reason),
 * we update the location with the last known location. This way, even if we
 * didn't get a fix before the service was stopped, we might be able to get
 * something more recent than the last time red moon updated location.
 */
class LocationUpdateService: Service(), LocationListener {

    private inner class Provider(val provider: String) {
        private var searching = false

        val exists: Boolean
            get() = locationManager.allProviders.contains(provider)

        /* val enabled: Boolean */
        /*     get() = exists && locationManager.isProviderEnabled(provider) */

        val lastKnownLocation: Location?
            get() = locationManager.getLastKnownLocation(provider)

        fun requestUpdates(listener: LocationListener): Boolean {
            if (!searching && exists) {
                Log.i("Requesting location updates from $provider")
                searching = true
                locationManager.requestLocationUpdates(provider, 0, 0f, listener)
                return true // success
            } else {
                return false // failure
            }
        }
    }

    private var mNetworkProvider = Provider(LocationManager.NETWORK_PROVIDER)
    private var mGpsProvider     = Provider(LocationManager.GPS_PROVIDER)

    private val locationManager: LocationManager
        get() = appContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    private val lastKnownLocation: Location?
        get() = mGpsProvider.lastKnownLocation ?: mNetworkProvider.lastKnownLocation

    /* private val locationUpToDate: Boolean */
    /*     get() = isRecent(lastKnownLocation?.time) */

    override fun onCreate() {
        super.onCreate()
        Log.i("onCreate")
        when {
            !hasLocationPermission -> {
                EventBus.getDefault().post(locationAccessDenied())
                stopSelf()
            /* } locationUpToDate -> { */
            /*     Log.i("Last known location is recent enough.") */
            /*     stopSelf() */
            } mNetworkProvider.exists -> {
                mNetworkProvider.requestUpdates(this)
            } mGpsProvider.exists -> {
                mGpsProvider.requestUpdates(this)
            } else -> {
                Log.i("No good providers, only: ${locationManager.allProviders}")
                stopSelf()
            }
        }
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Log.i("onStartCommand($intent, $flags, $startId)")
        // Do not attempt to restart if the hosting process is killed by Android
        return Service.START_NOT_STICKY
    }

    // Prevent binding.
    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onLocationChanged(location: Location) {
        Log.i("Location search succeeded")
        stopSelf()
    }

    override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {
        Log.i("Provider $provider changed to status $status with extras $extras")
    }

    override fun onProviderEnabled(provider: String) {
        EventBus.getDefault().post(locationUpdating())
    }

    // Called immediately if we register for updates on a disabled provider
    override fun onProviderDisabled(provider: String) {
        Log.i("$provider disabled")
        when (provider) {
            mNetworkProvider.provider, mGpsProvider.provider -> {
                if (!mGpsProvider.requestUpdates(this)) {
                    notifyLocationServicesDisabled()
                }
            } else -> Log.w("We shouldn't be getting updates $provider updates")
        }
    }

    override fun onDestroy() {
        Log.i("onDestroy")
        if (hasLocationPermission) {
            locationManager.removeUpdates(this)
            updateLocation(lastKnownLocation)
        }
        super.onDestroy()
    }

    private fun notifyLocationServicesDisabled() {
        Log.i("All available location providers are disabled.")
        EventBus.getDefault().post(locationServicesDisabled())
    }

    private fun updateLocation(location: Location?) {
        location?.apply {
            val sunLocation = com.luckycatlabs.sunrisesunset.dto.Location(latitude, longitude)
            val calculator  = SunriseSunsetCalculator(sunLocation, TimeZone.getDefault())
            Config.sunsetTime  = calculator.getOfficialSunsetForDate(Calendar.getInstance())
            Config.sunriseTime = calculator.getOfficialSunriseForDate(Calendar.getInstance())
            Config.location = latitude.toString() + "," + longitude.toString()
        }
    }

    companion object : Logger() {
        //val FOREGROUND = true
        //val BACKGROUND = false

        private val intent: Intent
            get() = Intent(appContext, LocationUpdateService::class.java)

        fun start() {
            Log.i("Received start request")
            appContext.startService(intent)
        }
    }
}
