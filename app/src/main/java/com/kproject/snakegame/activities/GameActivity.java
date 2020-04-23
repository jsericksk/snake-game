package com.kproject.snakegame.activities;

import android.graphics.Point;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Display;
import com.kproject.snakegame.views.SnakeView;

public class GameActivity extends AppCompatActivity {
    private SnakeView snakeView;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        snakeView = new SnakeView(this, size.x, size.y);
        setContentView(snakeView);
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        snakeView.pauseGame();
    }
    
}
