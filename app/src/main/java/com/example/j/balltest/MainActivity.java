package com.example.j.balltest;

import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.view.View;
import android.view.ViewTreeObserver;
import android.animation.ValueAnimator;

import java.math.BigInteger;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private static final float ALPHA = 0.5f;
    private Handler handler;
    private double xPixelsPerMeter;
    private double yPixelsPerMeter;
    private double startXBall;
    private double startYBall;
    private double ballParkWidth;
    private double ballParkHeight;
    private int collideCount;
    private static final int totalLives = 10;
    private Runnable ballThread;
    private float[] acc = new float[3];

    //exp
    private TextView circle;
    private TextView ball;
    private TextView lives;
    private TextView points;
    private TextView pointsText;
    private TextView livesLeft;
    private TextView restartButton;
    private boolean gameOn;
    private Thread T;
    private long p1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);

        // 1 inch = 0.0254 m
        xPixelsPerMeter = metrics.xdpi / 0.0254;
        yPixelsPerMeter = metrics.ydpi / 0.0254;
        // show
        ((TextView) findViewById(R.id.ppi)).setText("ppi x: " + String.valueOf(metrics.xdpi) + "\n" + "ppi y: " + String.valueOf(metrics.ydpi));
        ((TextView) findViewById(R.id.ppm)).setText("ppm x: " + String.valueOf(xPixelsPerMeter) + "\n" + "ppm x: " + String.valueOf(yPixelsPerMeter));

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        handler = new Handler();

        // this is for fetching initial ball position
        final View initialBall = findViewById(R.id.ball1);
        initialBall.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        //exp
                        LinearLayout view = (LinearLayout) findViewById(R.id.ballpark);
                        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) view.getLayoutParams();
                        params.width = (int) (metrics.widthPixels * 0.7);
                        params.height = (int) (metrics.widthPixels * 0.7);
                        view.setLayoutParams(params);

                        startXBall = view.getWidth() / 2;
                        startYBall = view.getHeight() / 2;
                        ballParkWidth = findViewById(R.id.ballpark).getWidth();
                        ballParkHeight = findViewById(R.id.ballpark).getHeight();
                        initialBall.getViewTreeObserver().removeGlobalOnLayoutListener(this);

                        GradientDrawable shape1 = (GradientDrawable) circle.getBackground();
                        int color = getResources().getColor(R.color.circleBase);
                        shape1.setColor(color);
                    }
                });

        //exp
        collideCount = 0;
        gameOn = true;
        p1 = 0;

        circle = (TextView) findViewById(R.id.circle);
        circle.setVisibility(View.VISIBLE);
        ball = (TextView) findViewById(R.id.ball1);
        ball.setVisibility(View.VISIBLE);
        lives = (TextView) findViewById(R.id.lives);
        livesLeft = (TextView) findViewById(R.id.livesLeft);
        points = (TextView) findViewById(R.id.points);
        pointsText = (TextView) findViewById(R.id.pointText);
        restartButton = (TextView) findViewById(R.id.restartButton);
        restartButton.setVisibility(View.GONE);
        lives.setText(String.valueOf(totalLives - collideCount));

        ballThread = new Runnable() {

            private double vx;
            private double vy;
            private double prevVx;
            private double prevVy;
            private double alphaY;
            private double alphaX;

            private double X = 0;
            private double Y = 0;
            private double prevX = 0;
            private double prevY = 0;

            private double a;
            private double b;

            private final static double g = 9.807; // 9.807
            private final static double c = Math.PI / (2 * g);
            private final static double bounce = 0.4; // bounce factor

            private double interval = 0.0002; // Adjust to make slower/faster. Should in "reality" be the same as the length (time) of every iteration

            //exp
            private boolean collision = false;
            private boolean flag = true;

            @Override
            public void run() {

                do {
                    a = (ballParkWidth / xPixelsPerMeter) / 2;
                    b = (ballParkHeight / yPixelsPerMeter) / 2;

                    // shrink for margins
                    a *= 0.95;
                    b *= 0.95;

                } while (ballParkWidth == 0 || ballParkHeight == 0 || a == 0 || b == 0);


                while (gameOn) {

                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    alphaX = c * -acc[0];
                    alphaY = c * acc[1];

                    vx = prevVx + g * Math.sin(alphaX) * interval;
                    vy = prevVy + g * Math.sin(alphaY) * interval;

                    X = prevX + vx * interval;
                    Y = prevY + vy * interval;

                    if (Math.abs(X) < Math.sqrt(Math.pow(a, 2) * (1 - Math.pow((Y / b), 2)))) {
                        prevX = X;
                        prevVx = vx;
                    } if (Math.abs(Y) < Math.sqrt(Math.pow(b, 2) * (1 - Math.pow((X / a), 2)))) {
                        prevY = Y;
                        prevVy = vy;

                    } else {
                        vx = 0;
                        prevVx = -prevVx * bounce; // for no bounce set to 0
                        X = prevX;
                        vy = 0;
                        prevVy = -prevVy * bounce;  // for no bounce set to 0
                        Y = prevY;
                        //exp
                        collision = true;
                        vx = 0;
                        prevVx = 0;
                        prevX = 0;
                        X = 0;
                        vy = 0;
                        prevVy = 0;
                        prevY = 0;
                        Y = 0;
                        try {
                            Thread.sleep(300);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    handler.post(new Runnable() {
                        public void run() {
                            TextView ball1 = (TextView) findViewById(R.id.ball1);
                            double b1X = startXBall + X * xPixelsPerMeter;
                            double b1Y = startYBall + Y * yPixelsPerMeter;
                            ball1.setX(Double.valueOf(b1X).floatValue());
                            ball1.setY(Double.valueOf(b1Y).floatValue());

                            //experiments
                            p1++;
                            points.setText(String.valueOf(p1));
                            if (collision) {
                                collideCount++;
                                collide();
                                collision = false;
                                if (collideCount == totalLives) {
                                    gameOn = false;
                                }
                            }
                        }
                    });
                }

                //game over

                handler.post(new Runnable() {
                    public void run() {
                        circle.setVisibility(View.GONE);
                        ball.setVisibility(View.GONE);
                        restartButton.setVisibility(View.VISIBLE);
                        livesLeft.setVisibility(View.VISIBLE);
                        ValueAnimator animation = ValueAnimator.ofFloat(0.5f,1f);
                        animation.setRepeatCount(3);
                        animation.setStartDelay(4000);
                        animation.setDuration(300);
                        animation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                            @Override
                            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                                float val = (float) valueAnimator.getAnimatedValue();
                                restartButton.setAlpha(val);

                            }});

                        animation.start();
                        }
                    });

                handler.post(new Runnable() {
                    public void run() {
                        ValueAnimator animation = ValueAnimator.ofFloat(0f,1f);
                        animation.setRepeatCount(5);
                        animation.setDuration(300);
                        animation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                            @Override
                            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                                float val = (float) valueAnimator.getAnimatedValue();
                                lives.setAlpha(val);
                                lives.setText("GAME OVER");
                            }
                        });

                        animation.start();
                    }
                });

                /**
                handler.post(new Runnable() {
                                 public void run() {
                                     Animation anim = new ScaleAnimation(
                                             1f, 0f, // Start and end values for the X axis scaling
                                             1f, 0f, // Start and end values for the Y axis scaling
                                             Animation.RELATIVE_TO_SELF, 0.5f, // Pivot point of X scaling
                                             Animation.RELATIVE_TO_SELF, 0.5f); // Pivot point of Y scaling
                                     anim.setFillAfter(true); // Needed to keep the result of the animation
                                     anim.setDuration(1000);
                                     anim.setStartOffset(1000);
                                     lives.startAnimation(anim);
                                 }
                             }
                );
                 */

                handler.post(new Runnable() {
                                 public void run() {
                                     final float size = pointsText.getTextSize();

                                     ValueAnimator animation = ValueAnimator.ofFloat(1f,2f);
                                     animation.setDuration(1000);
                                     animation.setStartDelay(2000);
                                     final float size2 = lives.getTextSize();
                                     final float size3 = livesLeft.getTextSize();
                                     animation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                                         @Override
                                         public void onAnimationUpdate(ValueAnimator valueAnimator) {
                                             float val = (float) valueAnimator.getAnimatedValue();
                                             float newSize = size * val;
                                             float newSize2 = size2 * (2f-val);
                                             float newSize3 = size3 * (2f-val);
                                             pointsText.setTextSize(TypedValue.COMPLEX_UNIT_PX, newSize);
                                             points.setTextSize(TypedValue.COMPLEX_UNIT_PX, newSize);
                                             lives.setTextSize(TypedValue.COMPLEX_UNIT_PX, newSize2);
                                             livesLeft.setTextSize(TypedValue.COMPLEX_UNIT_PX, newSize3);
                                         }
                                     });
                                     animation.start();
                                 }
                             }
                );



                acc[0] = acc[1] = acc[2] = 0;

            }
        };

        T = new Thread(ballThread);
        T.start();
    }

    public void restart(View view) {
        Intent intent = getIntent();
        finish();
        startActivity(intent);
    }
    private void collide() {

        GradientDrawable shape1 = (GradientDrawable) circle.getBackground();
        int color = getResources().getColor(R.color.circleBase);
        color = color << collideCount;
        shape1.setColor(color | 0xFF0000FF); // the | 0xFF000000 is to ensure alpha value always being 1

        GradientDrawable shape2 = (GradientDrawable) circle.getBackground();
        int strokeColor = getResources().getColor(R.color.circleStroke);
        strokeColor = strokeColor << collideCount;
        shape2.setStroke(10, strokeColor | 0xFF000088);

        GradientDrawable shape3 = (GradientDrawable) ball.getBackground();
        int ballColor = getResources().getColor(R.color.ballColor);
        ballColor = ballColor << collideCount;
        shape3.setColor(ballColor | 0xFF000088);

        lives.setText(String.valueOf(totalLives - collideCount));
        int livesColor = getResources().getColor(R.color.lives);
        livesColor = livesColor << collideCount;
        lives.setTextColor(livesColor | 0xFF000088);
        livesLeft.setTextColor(livesColor | 0xFF000088);

        GradientDrawable shape4 = (GradientDrawable) restartButton.getBackground();
        shape4.setColor(color | 0xFF0000FF);

        restartButton.setTextColor(livesColor | 0xFF000088);
        points.setTextColor(livesColor | 0xFF000088);
        pointsText.setTextColor(livesColor | 0xFF000088);

    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        acc = lowPass(event.values, acc);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    protected float[] lowPass(float[] input, float[] output) {
        if (output == null) {
            return input;
        }
        for (int i = 0; i < input.length; i++) {
            output[i] = output[i] + ALPHA * (input[i] - output[i]);
        }
        return output;
    }
}