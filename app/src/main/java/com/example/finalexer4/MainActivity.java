package com.example.finalexer4;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    myView v;
    Button btnUp, btnDown, btnLeft, btnRight, btnStart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("CNDC_LPR_RAR_FinalExer4");
        }

        v = findViewById(R.id.myView);
        btnUp = findViewById(R.id.btnUp);
        btnDown = findViewById(R.id.btnDown);
        btnLeft = findViewById(R.id.btnLeft);
        btnRight = findViewById(R.id.btnRight);
        btnStart = findViewById(R.id.btnStart);

        // Disable movement buttons until game starts
        setMovementButtonsEnabled(false);

        btnStart.setOnClickListener(view -> {
            if (!v.isGameRunning()) {
                if (v.hasGameStartedBefore()) {
                    v.resumeGame();                      // Resume if previously paused
                    btnStart.setText("Pause");
                } else {
                    v.startGame();                      // Start new game
                    btnStart.setText("Pause");
                }
                setMovementButtonsEnabled(true);
            } else {
                v.pauseGame();                           // Pause game
                btnStart.setText("Resume");
                setMovementButtonsEnabled(false);
            }
        });


    }

    private void setMovementButtonsEnabled(boolean enabled) {
        btnUp.setEnabled(enabled);
        btnDown.setEnabled(enabled);
        btnLeft.setEnabled(enabled);
        btnRight.setEnabled(enabled);
    }

    // Button click methods
    public void up(View view) {
        if (v.isGameRunning()) v.Up();
    }

    public void down(View view) {
        if (v.isGameRunning()) v.Down();
    }

    public void left(View view) {
        if (v.isGameRunning()) v.Left();
    }

    public void right(View view) {
        if (v.isGameRunning()) v.Right();
    }
}
