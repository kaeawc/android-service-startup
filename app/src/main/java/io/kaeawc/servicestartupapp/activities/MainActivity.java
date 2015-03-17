package io.kaeawc.servicestartupapp.activities;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import io.kaeawc.servicestartupapp.App;
import io.kaeawc.servicestartupapp.R;
import io.kaeawc.servicestartupapp.services.BootstrapService;
import io.kaeawc.servicestartupapp.services.MurderousService;
import io.kaeawc.servicestartupapp.services.ServiceKey;


public class MainActivity extends BoundActivity {

    Button mSaySomethingButton;
    Button mKillSlowServiceButton;
    Button mKillFastServiceButton;
    Button mKillAllServicesButton;
    Button mSuicidalButton;

    App mApp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mApp = App.getInstance();

        mSaySomethingButton = (Button) findViewById(R.id.say_something_button);
        mKillSlowServiceButton = (Button) findViewById(R.id.kill_slow_service_button);
        mKillFastServiceButton = (Button) findViewById(R.id.kill_fast_service_button);
        mKillAllServicesButton = (Button) findViewById(R.id.kill_all_services_button);
        mSuicidalButton = (Button) findViewById(R.id.suicidal_button);

        mSaySomethingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mApp.sendServiceMessage(ServiceKey.Fast, "Hello!");
            }
        });

        mKillSlowServiceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mApp.sendCommand(ServiceKey.Murderous, BootstrapService.COMMAND_KILL_SLOW);
            }
        });

        mKillFastServiceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mApp.sendCommand(ServiceKey.Murderous, BootstrapService.COMMAND_KILL_FAST);
            }
        });

        mKillAllServicesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mApp.sendCommand(ServiceKey.Murderous, BootstrapService.COMMAND_KILL_ALL);
            }
        });

        mSuicidalButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mApp.sendServiceMessage(ServiceKey.Fast, "Goodbye!");
                mApp.sendCommand(ServiceKey.Suicidal, BootstrapService.COMMAND_KILL_SELF);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
