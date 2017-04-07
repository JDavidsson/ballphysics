package com.example.j.balltest;
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
    private static final double friction = 0.001;

    public Handler handler;

    private double xPixelsPerMeter;
    private double yPixelsPerMeter;
    private double startXBall;
    private double startYBall;
    private double ballParkWidth;
    private double ballParkHeight;

    private float[] acc = new float[3];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);

        // 1 inch = 0.0254 m
        xPixelsPerMeter = metrics.xdpi / 0.0254;
        yPixelsPerMeter = metrics.ydpi / 0.0254;
        ((TextView) findViewById(R.id.ppi)).setText("ppi x: " + String.valueOf(metrics.xdpi)+ "ppi y: " + String.valueOf(metrics.ydpi));
        ((TextView) findViewById(R.id.ppm)).setText("ppm x: " + String.valueOf(xPixelsPerMeter)+ "ppm x: " + String.valueOf(yPixelsPerMeter));

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        handler = new Handler();

        // this is for fetching initial ball position
        final View initialBall = findViewById(R.id.ball);
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

        Runnable ball = new Runnable() {

            private double vx;
            private double vy;
            private double prevVx;
            private double prevVy;
            private double alphaY;
            private double alphaX;
            private double interval;

            private double X = 0;
            private double Y = 0;
            private double prevX = 0;
            private double prevY = 0;

            private double a;
            private double b;

            private final static double g = 9.807;
            private final static double c = Math.PI / (2*g);

            @Override
            public void run() {

                do {
                    a = (ballParkWidth / xPixelsPerMeter) / 2;
                    b = (ballParkHeight / yPixelsPerMeter) / 2;

                    // shrink for margins
                    a *=  0.9;
                    b *=  0.9;


                } while( ballParkWidth == 0 || ballParkHeight == 0 || a == 0 || b == 0);



                while(true) {

                    interval = 0.001; // Adjust to make slower/faster. Should in "reality" be the same as the length (time) of every iteration
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    alphaX = c * - acc[0];
                    alphaY = c * acc[1];

                    vx = prevVx + g * Math.sin(alphaX) * interval;
                    vy = prevVy + g * Math.sin(alphaY) * interval;

                    X = prevX + vx * interval;
                    Y = prevY + vy * interval;

                    //if ((X < radius/2 && X > -radius/2)) {
                    // if ((Math.abs(X) < Math.sqrt(Math.pow(radius,2)-Math.pow(Y,2)))) {
                    if (Math.abs(X) < Math.sqrt( Math.pow(a,2) * (1-Math.pow((Y/b),2)) ) ) {
                        prevX = X;
                        prevVx = vx;
                    } else {
                        vx = 0;
                        prevVx = 0;
                        X = prevX;
                    }

                    //if((Y < radius/2 && Y > -radius/2)) {
                    //if ((Math.abs(Y) < Math.sqrt(Math.pow(radius,2)-Math.pow(X,2)))) {
                    if (Math.abs(Y) < Math.sqrt( Math.pow(b,2) * (1-Math.pow((X/a),2)) ) ){
                        prevY = Y;
                        prevVy = vy;
                    } else {
                        vy = 0;
                        Y = prevY;
                        prevVy = 0;
                    }

                    handler.post(new Runnable(){
                        public void run() {
                            TextView ballMan = (TextView) findViewById(R.id.ball);
                            double bX = startXBall + X * xPixelsPerMeter;
                            double  bY = startYBall + Y * yPixelsPerMeter;
                            ((TextView) findViewById(R.id.xys)).setText("X-pos " + String.valueOf(X) + "\n" + "Y-pos: " +String.valueOf(Y) + "\n" + "X pixels = " + String.valueOf(bX) + "\n" + "Y pixels = "+ String.valueOf(bY) + "\n" + "startPX x: " + String.valueOf(startXBall) + "\n" + "startPX y: " +  String.valueOf(startYBall) + "\n" + "Layout pixelwidth: " +  String.valueOf(ballParkWidth) + "\n"  + "Layout pixelheight: " +  String.valueOf(ballParkHeight) + "\n" + "a: " +  String.valueOf(a) + "\n" + "b: " +  String.valueOf(b));
                            ballMan.setX(Double.valueOf(bX).floatValue());
                            ballMan.setY(Double.valueOf(bY).floatValue());
                        }
                    });
                }
            }
        };

        new Thread(ball).start();
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