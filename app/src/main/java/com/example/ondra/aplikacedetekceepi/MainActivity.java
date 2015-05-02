package com.example.ondra.aplikacedetekceepi;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONArray;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;


public class MainActivity extends AppCompatActivity implements SensorEventListener{
    private static final int nanoToMili = (int) 1e6;

    private SensorManager mSenMan;
    private Sensor senzor;

    private TextView viewX;
    private TextView viewY;
    private TextView viewZ;
    private TextView cas;
    private TextView acc;

    private final static int pocetHodnot = 50;
    /**
     * Budu ukladat data ve formatu:
     * [
     *      [X values]
     *      [Y values]
     *      [Z values]
     *      [timeStamps]
     * ]
     *
     */
    private SenzorValue [] values = new SenzorValue[pocetHodnot];
    private int indexToPush = 0;

    private long lastTimeStamp = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO: fuck
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSenMan = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        senzor = mSenMan.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        viewX = (TextView) findViewById(R.id.textViewID_X);
        viewY = (TextView) findViewById(R.id.textViewID_Y);
        viewZ = (TextView) findViewById(R.id.textViewID_Z);
        cas = (TextView) findViewById(R.id.casID);
        acc = (TextView) findViewById(R.id.acur);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSenMan.registerListener(this, senzor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSenMan.unregisterListener(this);
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

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (indexToPush < values.length) {
            SenzorValue tmp = new SenzorValue(event.timestamp, event.values);
            values[indexToPush] = tmp;
            indexToPush++;
        } else {
            float[] values = event.values;
            viewX.setText(Float.toString(values[0]));
            viewY.setText(Float.toString(values[1]));
            viewZ.setText(Float.toString(values[2]));
            //acc.setText(Long.toString(event.timestamp));

            this.cas.setText(Long.toString(Math.round((event.timestamp - lastTimeStamp) / nanoToMili)));
            //this.cas.setText(Long.toString(event.timestamp - lastTimeStamp));
            lastTimeStamp = event.timestamp;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    /* Checks if external storage is available for read and write */
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    public File getAlbumStorageDir(String fileName) {
        // Get the directory for the user's public pictures directory.
        File file = new File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                fileName);
        return file;
    }

    public void save(View view){
        File file = getAlbumStorageDir("testJson.txt");
        Button but = (Button) findViewById(R.id.butID);
        try {
            FileOutputStream stream = new FileOutputStream(file);
            OutputStreamWriter writer = new OutputStreamWriter(stream, "UTF-8");


            /*JsonWriter jsonWriter = new JsonWriter(writer);

            jsonWriter.beginArray();

            //JSONObject jsonObj = new JSONObject();
            //jsonObj.put("name","value");
            jsonWriter.name("name").value(5);
            jsonWriter.name("name2").value(6);


            jsonWriter.endArray();
            jsonWriter.close();*/

            // funkcni zapis
/*
            JSONObject obj = new JSONObject();
            obj.put("name", "mkyong.com");
            obj.put("age", new Integer(100));

            JSONArray list = new JSONArray();
            list.put("msg 1");
            list.put("msg 2");
            list.put("msg 3");

            obj.put("messages", list);

            writer.write(obj.toString(4));
*/


            JSONArray list = new JSONArray();
            float [] xyzPom;
            for (int i = 0; i < values.length; i++){
                JSONArray record = new JSONArray();
                JSONArray xyz = new JSONArray();
                xyzPom = values[i].getValues();
                for (int j = 0; j < xyzPom.length; j++){
                    xyz.put(xyzPom[j]);
                }
                record.put(xyz);
                record.put(values[i].getTimeStamp());

                list.put(record);
            }



            writer.write(list.toString(4));

            writer.close();
            stream.close();
            but.setText("Gut");




        } catch (Exception e) {
            but.setText("Fail");
            acc.setText(e.getLocalizedMessage());
            e.printStackTrace();
        }
    }

    // když kliknu na button zavola se mi nasledujici metoda sendToServer. data jsou uloženy v promene float [] values;
// mam pronajatou nasledujici domenu http://kendy.besaba.com/ na něm skript http://paste2.org/Vdvnb6KZ
    public void send(View view) {
        Button but = (Button) findViewById(R.id.butSendID);
        but.setText("klik");
        try {
            // overeni že jsem online Internet je zapnuty
            ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
            if (networkInfo != null && networkInfo.isConnected()) {
                // fetch data
            } else {
                return;
            }
            RetrieveFeedTask task = new RetrieveFeedTask();
            task.execute("nothing");

            but.setText("spusteno");
        } catch (Exception e){
            e.printStackTrace();
            but.setText("Fail");
        }






    }

    class RetrieveFeedTask extends AsyncTask<String, Void, Boolean> {

        private Exception exception;

        protected Boolean doInBackground(String... params) {
            try {
                URL url = new URL("http://kendy.besaba.com/skript.php"); // skript http://paste2.org/Vdvnb6KZ
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setDoOutput(true);

                con.setDoInput(true);

                con.setRequestProperty("Content-Type", "application/json");

                con.setRequestProperty("Accept", "application/json");

                con.setRequestMethod("POST");
/*
                JSONObject cred = new JSONObject();
                cred.put("username","adm");
                cred.put("password", "pwd");
*/
                // nevim do dal
                OutputStream stream = con.getOutputStream();
                OutputStreamWriter writer = new OutputStreamWriter(stream, "UTF-8");

                //data k zapisu jako JSON
                // array a v nem 4x array X,Y,Z,timestamp
                JSONArray list = new JSONArray();
                JSONArray X = new JSONArray();
                JSONArray Y = new JSONArray();
                JSONArray Z = new JSONArray();
                JSONArray time = new JSONArray();
                float [] xyzPom;
                for (int i = 0; i < values.length; i++){
                    xyzPom = values[i].getValues();
                    X.put(xyzPom[0]);
                    Y.put(xyzPom[1]);
                    Z.put(xyzPom[2]);
                    time.put(values[i].getTimeStamp());
                }
                list.put(X);
                list.put(Y);
                list.put(Z);
                list.put(time);

                // zapis dat do streamu




                writer.write(list.toString());
                writer.flush();

                StringBuilder sb = new StringBuilder();

                int HttpResult =con.getResponseCode();

                if(HttpResult != HttpURLConnection.HTTP_OK){
                    throw  new Exception("spatna odezva");
                }

                writer.close();
                stream.close();
                con.disconnect();
            } catch (Exception e){
                e.printStackTrace();
                return false;
            }

            return true;
        }


        protected void onPostExecute(Boolean feed) {
            Button but = (Button) findViewById(R.id.butSendID);
            if(feed){
                but.setText("done");
            } else {
                but.setText("falnuto");
            }
        }
    }


}
