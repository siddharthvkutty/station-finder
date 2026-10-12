package app.stationfinder;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

public class StationWidget extends AppWidgetProvider {
    private static final String REFRESH = "app.stationfinder.REFRESH";

    @Override
    public void onReceive(Context c, Intent i) {
        String a = i.getAction();
        if (REFRESH.equals(a) || AppWidgetManager.ACTION_APPWIDGET_UPDATE.equals(a)) {
            BroadcastReceiver.PendingResult r = goAsync();
            draw(c); // show cached value + wire buttons right away
            Finder.refresh(c, () -> { draw(c); r.finish(); });
        } else {
            super.onReceive(c, i);
        }
    }

    static void draw(Context c) {
        RemoteViews v = new RemoteViews(c.getPackageName(), R.layout.widget);
        v.setTextViewText(R.id.name, Finder.name(c));
        v.setTextViewText(R.id.detail, Finder.detail(c));
        int flags = PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT;
        v.setOnClickPendingIntent(R.id.refresh, PendingIntent.getBroadcast(c, 0,
                new Intent(c, StationWidget.class).setAction(REFRESH), flags));
        v.setOnClickPendingIntent(R.id.root, PendingIntent.getActivity(c, 0,
                new Intent(c, MainActivity.class), flags));
        AppWidgetManager.getInstance(c)
                .updateAppWidget(new ComponentName(c, StationWidget.class), v);
    }
}
