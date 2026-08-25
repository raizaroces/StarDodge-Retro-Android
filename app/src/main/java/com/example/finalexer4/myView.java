package com.example.finalexer4;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class myView extends View {

    int cx, cy, rad;
    Paint p;
    List<Bullet> bullets;
    List<Enemy> enemies;
    Handler handler;
    Random rand;
    int score = 0;
    long startTime = 0;
    long timeSurvived = 0;
    int waveCount;
    int playerColor = Color.rgb(255, 165, 0);
    private boolean isGameRunning = false;
    private boolean hasStartedOnce = false;
    public boolean hasGameStartedBefore() {
        return hasStartedOnce;
    }


    public boolean isGameRunning() {
        return isGameRunning;
    }

    public int getScore() {
        return score;
    }

    public void startGame() {
        isGameRunning = true;
        hasStartedOnce = true;
        enemies.clear();
        bullets.clear();
        waveCount = 1;
        score = 0;
        startTime = System.currentTimeMillis();
        handler.removeCallbacks(enemySpawner);
        handler.removeCallbacks(autoShooter);
        handler.postDelayed(enemySpawner, 1000);
        handler.postDelayed(autoShooter, 750);
        invalidate();
    }

    public void pauseGame() {
        isGameRunning = false;
        handler.removeCallbacks(autoShooter);
        handler.removeCallbacks(enemySpawner);
        timeSurvived = (System.currentTimeMillis() - startTime) / 1000;
        invalidate();
    }

    public void resumeGame() {
        if (!isGameRunning) {
            isGameRunning = true;
            startTime = System.currentTimeMillis() - (timeSurvived * 1000);
            handler.postDelayed(enemySpawner, 1000);
            handler.postDelayed(autoShooter, 750);
            invalidate();
        }
    }

    public myView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        p = new Paint();
        rad = 50;
        bullets = new ArrayList<>();
        enemies = new ArrayList<>();
        rand = new Random();
        handler = new Handler();
        waveCount = 1;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        cx = getWidth() / 2;
        cy = getHeight() / 2;
        super.onSizeChanged(w, h, oldw, oldh);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        drawBorders(canvas);

        p.setColor(playerColor);
        canvas.drawCircle(cx, cy, rad, p);

        p.setColor(Color.YELLOW);
        List<Bullet> toRemove = new ArrayList<>();
        for (Bullet b : bullets) {
            if (isGameRunning) b.update();
            b.draw(canvas, p);
            if (b.x < 0 || b.y < 0 || b.x > getWidth() || b.y > getHeight()) {
                toRemove.add(b);
            }
        }
        bullets.removeAll(toRemove);

        p.setColor(Color.WHITE);
        List<Enemy> deadEnemies = new ArrayList<>();
        for (Enemy e : enemies) {
            if (isGameRunning) e.update();
            e.draw(canvas, p);

            float dx = e.x - cx;
            float dy = e.y - cy;
            if (Math.sqrt(dx * dx + dy * dy) <= rad + 20) {
                timeSurvived = (System.currentTimeMillis() - startTime) / 1000;
                Toast.makeText(getContext(), "Game Over! Total Time: " + timeSurvived + "s", Toast.LENGTH_LONG).show();
                isGameRunning = false;
                handler.removeCallbacks(autoShooter);
                handler.removeCallbacks(enemySpawner);
                playerColor = Color.rgb(rand.nextInt(256), rand.nextInt(256), rand.nextInt(256));

                cx = getWidth() / 2;
                cy = getHeight() / 2;

                score = 0;
                startTime = 0;
                timeSurvived = 0;
                waveCount = 1;

                enemies.clear();
                bullets.clear();


                ((MainActivity) getContext()).runOnUiThread(() -> {
                    Button btnStart = ((MainActivity) getContext()).findViewById(R.id.btnStart);
                    btnStart.setText("Start");
                });

                break;
            }

            for (Bullet b : bullets) {
                float dist = (float) Math.sqrt(Math.pow(b.x - e.x, 2) + Math.pow(b.y - e.y, 2));
                if (dist < 25) {
                    deadEnemies.add(e);
                    score += 10;
                    break;
                }
            }
        }
        enemies.removeAll(deadEnemies);

        Paint bgPaint = new Paint();
        bgPaint.setColor(Color.argb(160, 0, 0, 0));
        canvas.drawRect(20, 20, 320, 150, bgPaint);

        p.setColor(Color.WHITE);
        p.setStyle(Paint.Style.FILL);
        p.setTextSize(50);
        p.setFakeBoldText(true);

        canvas.drawText("Score: " + score, 40, 80, p);

        long elapsed = isGameRunning ? (System.currentTimeMillis() - startTime) / 1000 : timeSurvived;
        canvas.drawText("Time: " + elapsed + "s", 40, 140, p);

        invalidate();
    }

    private void drawBorders(Canvas c) {
        p.setColor(Color.BLACK);
        p.setStyle(Paint.Style.FILL);

        int w = getWidth();
        int h = getHeight();
        int thickness = 20;
        int gap = 100;

        c.drawRect(0, 0, (w - gap) / 2, thickness, p);
        c.drawRect((w + gap) / 2, 0, w, thickness, p);
        c.drawRect(0, h - thickness, (w - gap) / 2, h, p);
        c.drawRect((w + gap) / 2, h - thickness, w, h, p);
        c.drawRect(0, 0, thickness, (h - gap) / 2, p);
        c.drawRect(0, (h + gap) / 2, thickness, h, p);
        c.drawRect(w - thickness, 0, w, (h - gap) / 2, p);
        c.drawRect(w - thickness, (h + gap) / 2, w, h, p);
    }

    private final Runnable autoShooter = new Runnable() {
        @Override
        public void run() {
            if (!enemies.isEmpty()) {
                Enemy nearest = null;
                double minDist = Double.MAX_VALUE;
                for (Enemy e : enemies) {
                    double dist = Math.hypot(e.x - cx, e.y - cy);
                    if (dist < minDist) {
                        minDist = dist;
                        nearest = e;
                    }
                }

                if (nearest != null) {
                    float dx = nearest.x - cx;
                    float dy = nearest.y - cy;
                    double mag = Math.sqrt(dx * dx + dy * dy);
                    dx = (float) (dx / mag * 30);
                    dy = (float) (dy / mag * 30);
                    bullets.add(new Bullet(cx, cy, dx, dy));
                }
            }
            handler.postDelayed(this, 750);
        }
    };

    private final Runnable enemySpawner = new Runnable() {
        @Override
        public void run() {
            for (int i = 0; i < waveCount; i++) {
                enemies.add(new Enemy());
            }
            waveCount++;
            handler.postDelayed(this, 3000);
        }
    };

    class Bullet {
        float x, y, dx, dy;
        final float speed = 12f;

        public Bullet(float startX, float startY, float dirX, float dirY) {
            float length = (float) Math.sqrt(dirX * dirX + dirY * dirY);
            this.dx = (dirX / length) * speed;
            this.dy = (dirY / length) * speed;
            this.x = startX;
            this.y = startY;
        }

        public void update() {
            x += dx;
            y += dy;
        }

        public void draw(Canvas canvas, Paint paint) {
            paint.setColor(Color.YELLOW);
            canvas.drawCircle(x, y, 6, paint);
        }

        public boolean isOutOfBounds(int width, int height) {
            return x < 0 || x > width || y < 0 || y > height;
        }
    }

    class Enemy {
        float x, y;
        float dx = 0, dy = 0;
        boolean directionSet = false;

        public Enemy() {
            int side = rand.nextInt(4);
            int w = getWidth();
            int h = getHeight();
            int offset = 100;

            switch (side) {
                case 0: x = (w - offset) / 2f + rand.nextInt(offset); y = 0; break;
                case 1: x = (w - offset) / 2f + rand.nextInt(offset); y = h; break;
                case 2: x = 0; y = (h - offset) / 2f + rand.nextInt(offset); break;
                case 3: x = w; y = (h - offset) / 2f + rand.nextInt(offset); break;
            }
        }

        public void update() {
            if (!directionSet && cx > 0 && cy > 0) {
                float dist = (float) Math.sqrt(Math.pow(cx - x, 2) + Math.pow(cy - y, 2));
                dx = (cx - x) / dist * 2;
                dy = (cy - y) / dist * 2;
                directionSet = true;
            }
            x += dx;
            y += dy;
        }

        public void draw(Canvas canvas, Paint paint) {
            canvas.drawCircle(x, y, 20, paint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isGameRunning) return false;

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                float touchX = event.getX();
                float touchY = event.getY();


                int minX = rad + 20;
                int maxX = getWidth() - rad - 20;
                int minY = rad + 20;
                int maxY = getHeight() - rad - 20;

                cx = (int) Math.max(minX, Math.min(touchX, maxX));
                cy = (int) Math.max(minY, Math.min(touchY, maxY));

                invalidate();
                return true;
        }
        return false;
    }


    public void Up() {
        cy -= 20;
        if (cy - rad <= 20) cy = rad + 20;
        invalidate();
    }

    public void Down() {
        cy += 20;
        if (cy + rad >= getHeight() - 20) cy = getHeight() - rad - 20;
        invalidate();
    }

    public void Left() {
        cx -= 20;
        if (cx - rad <= 20) cx = rad + 20;
        invalidate();
    }

    public void Right() {
        cx += 20;
        if (cx + rad >= getWidth() - 20) cx = getWidth() - rad - 20;
        invalidate();
    }
}
