package app.stationfinder;

import android.Manifest;
import android.app.Activity;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends Activity {
    private TextView name, detail;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        name = new TextView(this);
        name.setTextSize(32);
        detail = new TextView(this);
        detail.setTextSize(16);
        Button refresh = new Button(this);
        refresh.setText("Refresh");
        refresh.setOnClickListener(v -> refresh());

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER);
        root.addView(name);
        root.addView(detail);
        root.addView(refresh);
        setContentView(root);

        askPermissions();
        show();
    }

    @Override
    protected void onResume() { super.onResume(); refresh(); }

    private void askPermissions() {
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != 0) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
        } else if (checkSelfPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION) != 0) {
            // Needed so the widget can update while the app is closed. Opens system settings.
            requestPermissions(new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION}, 2);
        }
    }

    @Override
    public void onRequestPermissionsResult(int code, String[] p, int[] r) {
        if (code == 1) askPermissions(); // chain into background request
        refresh();
    }

    private void refresh() {
        Finder.refresh(this, () -> { show(); StationWidget.draw(this); });
    }

    private void show() {
        name.setText(Finder.name(this));
        detail.setText(Finder.detail(this));
    }
}
