package com.example.j.balltest;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.widget.TextView;
import android.view.View;
import android.view.ViewTreeObserver;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor accelerometer;

    private static final float ALPHA = 0.5f;
    public Handler handler;

    private double xPixelsPerMeter;
    private double yPixelsPerMeter;
    private double startXBall;
    private double startYBall;
    private double ballParkWidth;
    private double ballParkHeight;
    private int collideCount;
    private int totalLives;

    private float[] acc = new float[3];

    //exp
    private TextView circle;
    private TextView ball;
    private TextView lives;
    private TextView livesLeft;
    private boolean gameOn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        DisplayMetrics metrics = new DisplayMetrics();
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
                        startXBall = initialBall.getX();
                        startYBall = initialBall.getY();
                        ballParkWidth = findViewById(R.id.ballpark).getWidth();
                        ballParkHeight = findViewById(R.id.ballpark).getHeight();
                        initialBall.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                    }
                });

        //exp
        collideCount = 0;
        totalLives = 10;
        gameOn = true;

        circle = (TextView) findViewById(R.id.circle);
        ball = (TextView) findViewById(R.id.ball1);
        lives = (TextView) findViewById(R.id.lives);
        livesLeft = (TextView) findViewById(R.id.livesLeft);
        lives.setText(String.valueOf(totalLives - collideCount));

        Runnable ball = new Runnable() {

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

            private final static double g = 9.807;
            private final static double c = Math.PI / (2*g);
            private final static double bounce = 0.4; // bounce factor

            private double interval = 0.0008; // Adjust to make slower/faster. Should in "reality" be the same as the length (time) of every iteration

            //exp
            private boolean collision = false;

            @Override
            public void run() {

                do {
                    a = (ballParkWidth / xPixelsPerMeter) / 2;
                    b = (ballParkHeight / yPixelsPerMeter) / 2;

                    // shrink for margins
                    a *=  0.95;
                    b *=  0.95;

                } while(ballParkWidth == 0 || ballParkHeight == 0 || a == 0 || b == 0);

                while(gameOn) {

                    try {
                        Thread.sleep(5);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    alphaX = c * - acc[0];
                    alphaY = c * acc[1];

                    vx = prevVx + g * Math.sin(alphaX) * interval;
                    vy = prevVy + g * Math.sin(alphaY) * interval;

                    X = prevX + vx * interval;
                    Y = prevY + vy * interval;

                    if (Math.abs(X) < Math.sqrt( Math.pow(a,2) * (1-Math.pow((Y/b),2)))) {
                        prevX = X;
                        prevVx = vx;
                    } else {
                        vx = 0;
                        prevVx = -prevVx * bounce; // for no bounce set to 0
                        X = prevX;

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

                    if (Math.abs(Y) < Math.sqrt( Math.pow(b,2) * (1-Math.pow((X/a),2)))){
                        prevY = Y;
                        prevVy = vy;
                    } else {
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

                    handler.post(new Runnable(){
                        public void run() {
                            TextView ball1 = (TextView) findViewById(R.id.ball1);
                            double b1X = startXBall + X * xPixelsPerMeter;
                            double  b1Y = startYBall + Y * yPixelsPerMeter;
                            ((TextView) findViewById(R.id.info)).setText("x (meter): " + String.valueOf(X) + "\n" + "y (meter): " +String.valueOf(Y) + "\n" + "x (pixels):  = " + String.valueOf(b1X) + "\n" + "y (meter):  = "+ String.valueOf(b1Y) + "\n" + "start pixel x: " + String.valueOf(startXBall) + "\n" + "start pixel y: " +  String.valueOf(startYBall) + "\n" + "Layout pixel width: " +  String.valueOf(ballParkWidth) + "\n"  + "Layout pixel height: " +  String.valueOf(ballParkHeight) + "\n" + "a: " +  String.valueOf(a) + "\n" + "b: " +  String.valueOf(b));
                            ball1.setX(Double.valueOf(b1X).floatValue());
                            ball1.setY(Double.valueOf(b1Y).floatValue());

                            //experiments
                            if (collision) {
                                collideCount++;
                                collide();
                                collision= false;
                            }


                        }
                    });
                }
            }
        };

        new Thread(ball).start();
    }

    public void collide(){


        GradientDrawable shape1 = (GradientDrawable) circle.getBackground();
        int color = getResources().getColor(R.color.circleBase);
        color = color << collideCount;
        shape1.setColor(color | 0xFF000000); // the | 0xFF000000 is to ensure alpha value always being 1

        GradientDrawable shape2 = (GradientDrawable) circle.getBackground();
        int strokeColor = getResources().getColor(R.color.circleStroke);
        strokeColor = strokeColor << collideCount;
        shape2.setStroke(10,strokeColor | 0xFF000000);

        GradientDrawable shape3 = (GradientDrawable) ball.getBackground();
        int ballColor = getResources().getColor(R.color.ballColor);
        ballColor = ballColor << collideCount;
        shape3.setColor(ballColor | 0xFF000000);

        lives.setText(String.valueOf(totalLives - collideCount));
        GradientDrawable shape4 = (GradientDrawable) lives.getBackground();
        int livesColor = getResources().getColor(R.color.lives);
        livesColor = livesColor << collideCount;
        lives.setTextColor(livesColor | 0xFF000000);
        livesLeft.setTextColor(livesColor | 0xFF000000);


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
        if (output == null) {return input;}
        for (int i = 0; i<input.length; i++) {
            output[i] = output[i] + ALPHA * (input[i] - output[i]);
        }
        return output;
    }
}