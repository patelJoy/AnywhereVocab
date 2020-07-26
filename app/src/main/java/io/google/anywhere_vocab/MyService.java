package io.google.anywhere_vocab;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.ClipboardManager.OnPrimaryClipChangedListener;
import android.graphics.PixelFormat;
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;

@SuppressLint("NewApi")
public class MyService extends Service{
    public int counter = 0;
    private ClipboardManager mCM;
    public static String text;
    IBinder mBinder;
    int mStartMode;
    Context context;
    private WindowManager mWindowManager;
    private View mFloatingView;
    private View collapsedView;
    private View expandedView;
    private TextView word;
    private TextView meaning;

    public MyService(){

    }

    public MyService(Context applicationContext){
        context = applicationContext;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mCM = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        mCM.addPrimaryClipChangedListener(new OnPrimaryClipChangedListener() {

            @Override
            public void onPrimaryClipChanged() {
                String newClip = String.valueOf(mCM.getPrimaryClip().getItemAt(0).getText());
                text = newClip;
                mFloatingView = LayoutInflater.from(getApplicationContext()).inflate(R.layout.floating_layout, null);
                meaning = mFloatingView.findViewById(R.id.fl_meaning);
                new RetrieveFeedTask().execute();
//                Toast.makeText(getApplicationContext(), newClip,  Toast.LENGTH_LONG).show();
                Log.i("LOG", newClip + "");
                //getting the widget layout from xml using layout inflater


                //setting the layout parameters
                final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                        WindowManager.LayoutParams.WRAP_CONTENT,
                        WindowManager.LayoutParams.WRAP_CONTENT,
                        WindowManager.LayoutParams.TYPE_PHONE,
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                        PixelFormat.TRANSLUCENT);


                //getting windows services and adding the floating view to it
                mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
                mWindowManager.addView(mFloatingView, params);


                //getting the collapsed and expanded view from the floating view
                collapsedView = mFloatingView.findViewById(R.id.layoutCollapsed);
                expandedView = mFloatingView.findViewById(R.id.layoutExpanded);

                word = mFloatingView.findViewById(R.id.fl_word);
                word.setText(newClip);
                //adding click listener to close button and expanded view
                mFloatingView.findViewById(R.id.buttonClose).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        stopSelf();
                    }
                });

                expandedView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        collapsedView.setVisibility(View.VISIBLE);
                        expandedView.setVisibility(View.GONE);
                    }
                });

                //adding an touchlistener to make drag movement of the floating widget
                mFloatingView.findViewById(R.id.relativeLayoutParent).setOnTouchListener(new View.OnTouchListener() {
                    private int initialX;
                    private int initialY;
                    private float initialTouchX;
                    private float initialTouchY;

                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        switch (event.getAction()) {
                            case MotionEvent.ACTION_DOWN:
                                initialX = params.x;
                                initialY = params.y;
                                initialTouchX = event.getRawX();
                                initialTouchY = event.getRawY();
                                return true;

                            case MotionEvent.ACTION_UP:
                                //when the drag is ended switching the state of the widget
                                collapsedView.setVisibility(View.GONE);
                                expandedView.setVisibility(View.VISIBLE);
                                return true;

                            case MotionEvent.ACTION_MOVE:
                                //this code is helping the widget to move around the screen with fingers
                                params.x = initialX + (int) (event.getRawX() - initialTouchX);
                                params.y = initialY + (int) (event.getRawY() - initialTouchY);
                                mWindowManager.updateViewLayout(mFloatingView, params);
                                return true;
                        }
                        return false;
                    }
                });
            }
        });
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mFloatingView != null) mWindowManager.removeView(mFloatingView);
        Intent broadcastIntent = new Intent("uk.ac.shef.oak.ActivityRecognition.RestartSensor");
        sendBroadcast(broadcastIntent);
        stoptimertask();
    }

    private Timer timer;
    private TimerTask timerTask;

    public void startTimer() {
        //set a new Timer
        timer = new Timer();

        //initialize the TimerTask's job
        initializeTimerTask();

        //schedule the timer, to wake up every 1 second
        timer.schedule(timerTask, 1000, 1000); //
    }

    public void initializeTimerTask() {
        timerTask = new TimerTask() {
            public void run() {
                Log.i("in timer", "in timer ++++  " + (counter++));
            }
        };
    }

    public void stoptimertask() {
        //stop the timer, if it's not already null
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        return null;
    }

    class RetrieveFeedTask extends AsyncTask<Void, Void, String> {

        private Exception exception;

        protected void onPreExecute() {
        }

        protected String doInBackground(Void... urls) {
            try {
                Log.i("LOG",MyService.text);
                URL url = new URL("https://www.dictionaryapi.com/api/v3/references/collegiate/json/"+MyService.text+"?key=e8264cdd-cc54-4a7a-ba04-c4e2bcce3347");
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                try {
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                    StringBuilder stringBuilder = new StringBuilder();
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        stringBuilder.append(line).append("\n");
                    }
                    bufferedReader.close();
                    return stringBuilder.toString();
                }
                finally{
                    urlConnection.disconnect();
                }
            }
            catch(Exception e) {
                Log.e("ERROR", e.getMessage(), e);
                return null;
            }
        }

        protected void onPostExecute(String response) {
            if(response == null) {
                response = "THERE WAS AN ERROR";
            }
            Log.i("INFO", response);
            JSONArray res;
            String str = "";
            try {
                JSONArray ja = new JSONArray(response);
                res = ja.getJSONObject(0).getJSONArray("shortdef");
                for(int i=0;i<res.length();i++){
                    str += res.get(i) + "\n";
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
//            Toast.makeText(getApplicationContext(),res,Toast.LENGTH_LONG).show();
            meaning.setText(str);
        }
    }

}



