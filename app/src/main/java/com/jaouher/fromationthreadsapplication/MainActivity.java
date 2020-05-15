package com.jaouher.fromationthreadsapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    Button startBtn;
    Button stopBtn;
    TextView textView;
    TextView textViewStarted;
    boolean isLooping;
    int count;
    MyAsyncTask myAsyncTask;
    Handler handler;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startBtn = findViewById(R.id.startBtn);
        stopBtn = findViewById(R.id.stopBtn);
        textView = findViewById(R.id.textView);
        textViewStarted = findViewById(R.id.textViewStarted);

        handler = new Handler(this.getMainLooper());


        startBtn.setOnClickListener(v -> {
            myAsyncTask = new MyAsyncTask();
            myAsyncTask.execute(count);
        });


        stopBtn.setOnClickListener(v -> {
            myAsyncTask.cancel(true);
        });
    }


    private void ex1() {
        isLooping = true;
        while (isLooping) {
            Log.i(TAG, "Loop in Main thread " + Thread.currentThread().getId());
        }
    }

    private void ex2() {
        isLooping = true;

        new Thread(new Runnable() {
            @Override
            public void run() {
                while (isLooping) {
                    Log.i(TAG, "Loop in Second thread " + Thread.currentThread().getId());
                }
            }
        }).start();
    }

    private void ex3() {
        isLooping = true;

        new Thread(new Runnable() {
            @Override
            public void run() {
                while (isLooping) {
                    try {
                        Thread.sleep(2000);
                        count++;
                        Log.i(TAG, Thread.currentThread().getId() + " \n Counter " + count);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }
            }
        }).start();
    }

    private void ex4() {
        isLooping = true;

        new Thread(new Runnable() {
            @Override
            public void run() {

                while (isLooping) {

                    try {
                        Thread.sleep(1000);
                        count++;
                        //textView.setText(String.valueOf(count));

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            stopBtn.setText(String.valueOf(count));
                        }
                    });

                    textView.post(new Runnable() {
                        @Override
                        public void run() {
                            textView.setText(String.valueOf(count));
                        }
                    });
                }
            }
        }).start();
    }


    //ex5
    private class MyAsyncTask extends AsyncTask<Integer, Integer, Integer> {

        private int customCount;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            customCount = 0;
            textViewStarted.setVisibility(View.VISIBLE);
        }

        @Override
        protected Integer doInBackground(Integer... integers) {
            isLooping = true;
            while (isLooping) {
                try {
                    Thread.sleep(1000);
                    customCount++;

                    publishProgress(customCount);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                if (isCancelled()) {
                    break;
                }
            }

            return count;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            textView.setText(String.valueOf(values[0]));
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);

            Toast.makeText(MainActivity.this, "onPostExecute", Toast.LENGTH_SHORT).show();

        }

        @Override
        protected void onCancelled(Integer integer) {
            super.onCancelled(integer);

            textViewStarted.setText("Canceled");
        }
    }

}
