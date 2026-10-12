package app.stationfinder;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.text.format.DateFormat;

import java.util.Date;

/** Nearest-station lookup. Result is cached in prefs so widget and activity share it. */
final class Finder {
    // Central Railway: CSMT -> Kalyan, then Kasara and Khopoli branches. Coordinates from OpenStreetMap,
    // except CSMT, Parel, Kanjurmarg (not in OSM query, approximate). Harbour line not included.
    private static final Object[][] STATIONS = {
        {"CSMT", 18.9398, 72.8355}, {"Masjid", 18.9523, 72.8382},
        {"Sandhurst Road", 18.9609, 72.8394}, {"Byculla", 18.9766, 72.8328},
        {"Chinchpokli", 18.9872, 72.8327}, {"Currey Road", 18.9937, 72.8329},
        {"Parel", 19.0000, 72.8410}, {"Dadar", 19.0174, 72.8430},
        {"Matunga", 19.0274, 72.8501}, {"Sion", 19.0465, 72.8633},
        {"Kurla", 19.0653, 72.8794}, {"Vidyavihar", 19.0795, 72.8971},
        {"Ghatkopar", 19.0857, 72.9084}, {"Vikhroli", 19.1115, 72.9280},
        {"Kanjurmarg", 19.1296, 72.9311}, {"Bhandup", 19.1428, 72.9377},
        {"Nahur", 19.1546, 72.9467}, {"Mulund", 19.1721, 72.9567},
        {"Thane", 19.1865, 72.9758}, {"Kalva", 19.1952, 72.9963},
        {"Mumbra", 19.1899, 73.0231}, {"Diva", 19.1888, 73.0414},
        {"Kopar", 19.2124, 73.0789}, {"Dombivli", 19.2180, 73.0861},
        {"Thakurli", 19.2263, 73.0980}, {"Kalyan", 19.2358, 73.1308},
        // Kasara branch
        {"Shahad", 19.2436, 73.1581}, {"Ambivli", 19.2683, 73.1722},
        {"Titwala", 19.2964, 73.2031}, {"Khadavli", 19.3567, 73.2190},
        {"Vasind", 19.4065, 73.2675}, {"Asangaon", 19.4397, 73.3080},
        {"Atgaon", 19.5035, 73.3283}, {"Khardi", 19.5802, 73.3938},
        {"Umbermali", 19.6284, 73.4230}, {"Kasara", 19.6482, 73.4736},
        // Khopoli branch
        {"Vithalwadi", 19.2284, 73.1491}, {"Ulhasnagar", 19.2179, 73.1631},
        {"Ambernath", 19.2101, 73.1847}, {"Badlapur", 19.1666, 73.2389},
        {"Vangani", 19.0943, 73.3009}, {"Shelu", 19.0635, 73.3179},
        {"Neral", 19.0274, 73.3184}, {"Bhivpuri Road", 18.9705, 73.3314},
        {"Karjat", 18.9100, 73.3210}, {"Palasdhari", 18.8844, 73.3208},
        {"Kelavli", 18.8445, 73.3189}, {"Dolavli", 18.8332, 73.3203},
        {"Lowjee", 18.8096, 73.3352}, {"Khopoli", 18.7885, 73.3461},
    };

    static String name(Context c) { return prefs(c).getString("name", "Tap refresh"); }
    static String detail(Context c) { return prefs(c).getString("detail", ""); }

    private static SharedPreferences prefs(Context c) { return c.getSharedPreferences("s", 0); }

    private static void save(Context c, String name, String detail) {
        prefs(c).edit().putString("name", name).putString("detail", detail).apply();
    }

    /** Fetches a location fix, stores the nearest station, then runs done on the main thread. */
    static void refresh(Context c, Runnable done) {
        LocationManager lm = c.getSystemService(LocationManager.class);
        String provider = null;
        for (String p : new String[]{LocationManager.FUSED_PROVIDER, LocationManager.GPS_PROVIDER,
                LocationManager.NETWORK_PROVIDER}) {
            if (lm.hasProvider(p) && lm.isProviderEnabled(p)) { provider = p; break; }
        }
        if (c.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            save(c, "No permission", "Open the app and allow location (\"all the time\")");
            done.run();
            return;
        }
        if (provider == null) {
            save(c, "Location is off", "Turn on device location");
            done.run();
            return;
        }
        final String prov = provider;
        lm.getCurrentLocation(prov, null, c.getMainExecutor(), loc -> {
            if (loc == null) loc = lm.getLastKnownLocation(prov);
            if (loc == null) {
                save(c, "No location fix", "Allow location \"all the time\" and retry");
            } else {
                int best = 0;
                float bestDist = Float.MAX_VALUE;
                float[] r = new float[1];
                for (int i = 0; i < STATIONS.length; i++) {
                    Location.distanceBetween(loc.getLatitude(), loc.getLongitude(),
                            (double) STATIONS[i][1], (double) STATIONS[i][2], r);
                    if (r[0] < bestDist) { bestDist = r[0]; best = i; }
                }
                String d = bestDist < 1000 ? Math.round(bestDist) + " m"
                        : String.format("%.1f km", bestDist / 1000);
                save(c, (String) STATIONS[best][0],
                        d + " away · " + DateFormat.getTimeFormat(c).format(new Date()));
            }
            done.run();
        });
    }
}
