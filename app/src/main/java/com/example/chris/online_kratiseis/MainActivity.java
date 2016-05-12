package com.example.chris.online_kratiseis;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    final String urlServer = "SERVER API URL HERE";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        final Spinner theatre = (Spinner) findViewById(R.id.spinnerTheatre);
        final Spinner performance = (Spinner) findViewById(R.id.spinnerPer);
        final EditText reservation = (EditText) findViewById(R.id.editTextNumResr);
        final TextView avaliableSeats = (TextView) findViewById(R.id.avaliableSeats);

        final ArrayAdapter<String> spinnerAdapterTheatre = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, android.R.id.text1);
        spinnerAdapterTheatre.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        theatre.setAdapter(spinnerAdapterTheatre);
        spinnerAdapterTheatre.add("--Choose Theatre--");
        spinnerAdapterTheatre.add("theatro1");
        spinnerAdapterTheatre.notifyDataSetChanged();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int selectedTheatre = theatre.getSelectedItemPosition();
                int selectedPerformance = performance.getSelectedItemPosition();
                if(selectedTheatre != 0 && selectedPerformance !=0){
                    String urlPerformance = performance.getSelectedItem().toString();                   
                    new Reservation().execute(urlServer);
                }
            }
        });

        theatre.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 1) {
                    String getParam = theatre.getSelectedItem().toString();                   
                    new LongOperation().execute(urlServer, "theatro", getParam);

                }else if(position == 0){
                    performance.setAdapter(null);
                    avaliableSeats.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        performance.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(position != 0) {
                    String getParam = performance.getSelectedItem().toString();
                    new GetInfos().execute(urlServer, "parastasi", getParam);
                }else  avaliableSeats.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }

    private class LongOperation extends AsyncTask<String, Void, Void>{

        //initialize
        private String content;
        private String Error = null;
        private ProgressDialog Dialog = new ProgressDialog(MainActivity.this);
        String data="";
        Spinner setPerformances = (Spinner) findViewById(R.id.spinnerPer);

        protected void onPreExecute(){

            //start progress bar
            Dialog.setMessage("Please wait...");
            Dialog.show();
            Spinner getTheatro = (Spinner) findViewById(R.id.spinnerTheatre);
            String theatre = getTheatro.getSelectedItem().toString();

            try {
                // set request parameter
                data = "?" + URLEncoder.encode("theatro","UTF-8") + "="+theatre;
                Log.d("DATA:", data);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }


        }

        @Override
        protected Void doInBackground(String... params) {
            BufferedReader reader=null;

            try{
                //connect with server
                URL url = new URL(params[0]+data);
                HttpURLConnection connection = (HttpURLConnection)url.openConnection();
               
                //Get the server Response
                reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line = null;

                //Read server Response
                while((line = reader.readLine()) != null){
                    sb.append(line+" ");
                }
                content = sb.toString();

            }catch(Exception e){
                Error = e.getMessage();
            }
            finally {
                try{
                    reader.close();
                }catch (Exception ex){}
            }
            return null;
        }

        protected void onPostExecute(Void unused){

            Dialog.dismiss();
            if(Error != null){
                Log.w("Error",Error);
            }else{
                /***Start parse Response JSON***/
                String OutputData = "";
                Spinner perf = (Spinner) findViewById(R.id.spinnerPer);
                JSONObject jsonResponse;

                try {
                    ArrayAdapter<String> adapter;
                    List<String> list;
                    list = new ArrayList<String>();
                    

                    /****** Creates a new JSONObject with name/value mappings from the JSON string. ********/
                    jsonResponse = new JSONObject(content);


                    //Show Parsed Output on screen (activity)
                    JSONObject json_perf = jsonResponse.getJSONObject("performances");
                    int nums = json_perf.getInt("numOfPerformances");
                    String str_performances[] = new String[nums];
                    list.add("--Choose Performance--");
                    for(int i=0;i<nums;i++) {
                        str_performances[i] = json_perf.getString("performance"+i);
                        Log.d("RETURN:",str_performances[i]);
                        list.add(str_performances[i]);
                    }
                    adapter = new ArrayAdapter<String>(getApplicationContext(),
                            R.layout.spinner_item, list);
                    adapter.setDropDownViewResource(R.layout.spinner_item);
                    setPerformances.setAdapter(adapter);


                }  catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private class GetInfos extends AsyncTask<String, Void, Void>{

        //initialize
        private String content;
        private String Error = null;
        private ProgressDialog Dialog = new ProgressDialog(MainActivity.this);
        String data="";
        TextView seats = (TextView) findViewById(R.id.avaliableSeats);

        protected void onPreExecute(){

            Dialog.setMessage("Please wait");
            Dialog.show();

            Spinner getPerformance = (Spinner) findViewById(R.id.spinnerPer);
            String performance = getPerformance.getSelectedItem().toString();
            performance = performance.replaceAll(" ", "%20");
            try {
                // set request parameter
                data = "?" + URLEncoder.encode("parastasi","UTF-8") + "=" + performance;
                Log.d("DATA:", data);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

        }

        @Override
        protected Void doInBackground(String... params) {
            BufferedReader reader=null;

            try{
                //connect with server
                URL url = new URL(params[0]+data);
                HttpURLConnection connection = (HttpURLConnection)url.openConnection();

                //Get the server Response
                reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line = null;

                //Read server Response
                while((line = reader.readLine()) != null){
                    sb.append(line+" ");
                }
                content = sb.toString();

            }catch(Exception e){
                Error = e.getMessage();
            }
            finally {
                try{
                    reader.close();
                }catch (Exception ex){}
            }
            return null;
        }

        protected void onPostExecute(Void unused){

            Dialog.dismiss();
            if(Error != null){
                Log.w("Error",Error);
            }else{
                /***Start parse Response JSON***/
                String OutputData = "";
                JSONObject jsonResponse;

                try {
                    Log.d("RESPONSE0: ",content);

                    /****** Creates a new JSONObject with name/value mappings from the JSON string. ********/
                    jsonResponse = new JSONObject(content);

                    //Show Parsed Output on screen (activity)
                    JSONObject json_perf = jsonResponse.getJSONObject("infos");
                    int nums = json_perf.getInt("theseisAvl");
                    String message="Avaliable seats: "+nums;
                    seats.setText(message);
                    seats.setVisibility(View.VISIBLE);



                }  catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    private class Reservation extends AsyncTask<String, Void, Void>{

        //initialize
        private String content;
        private String Error = null;
        private ProgressDialog Dialog = new ProgressDialog(MainActivity.this);
        String data="";
        AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();


        protected void onPreExecute(){
            //start progress bar
            Dialog.setMessage("Please wait...");
            Dialog.show();
            Spinner selectedPerformance = (Spinner) findViewById(R.id.spinnerPer);
            EditText reservationNum = (EditText) findViewById(R.id.editTextNumResr);
            String performanceValue = selectedPerformance.getSelectedItem().toString();
            performanceValue = performanceValue.replaceAll(" ", "%20");
            String resrvNumValue = reservationNum.getText().toString();

            try {
                // set request parameter
                data = URLEncoder.encode("parastasi","UTF-8") + "=" + performanceValue +"&"+ URLEncoder.encode("kratisi","UTF-8") + "=" + resrvNumValue;
                Log.d("DATA:", data);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

        }

        @Override
        protected Void doInBackground(String... params) {

            BufferedReader reader=null;

            try{
                //connect with server
                URL url = new URL(params[0]);
                HttpURLConnection connection = (HttpURLConnection)url.openConnection();
                //connection.setRequestProperty("User-Agent", "");
                connection.setRequestMethod("POST");
                //connection.setDoInput(true);
                connection.setDoOutput(true);
                connection.addRequestProperty("Accept", "application/json");
                connection.connect();


                // Send POST data request
                OutputStreamWriter wr = new OutputStreamWriter(connection.getOutputStream());
                wr.write(data);
                wr.flush();

                //Get the server Response
                reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line = null;

                //Read server Response
                while((line = reader.readLine()) != null){
                    sb.append(line+" ");
                }
                content = sb.toString();

            }catch(Exception e){
                Error = e.getMessage();
            }
            finally {
                try{
                    reader.close();
                }catch (Exception ex){}
            }
            return null;
        }

        protected void onPostExecute(Void unused){

            Dialog.dismiss();
            if(Error != null){
                Log.w("Error",Error);
            }else{
                /***Start parse Response JSON***/
                String OutputData = "";
                JSONObject jsonResponse;

                try {
                    Log.d("RESPONSEkratisi: ",content);

                    /****** Creates a new JSONObject with name/value mappings from
                     *  the JSON string. ********/
                    jsonResponse = new JSONObject(content);

                    //Show Parsed Output on screen (activity)
                    JSONObject json_perf = jsonResponse.getJSONObject("kratisi");
                    int successCode = json_perf.getInt("success");
                    if(successCode==1){
                        alertDialog.setTitle("Success");
                        alertDialog.setMessage("Reservation completed successful");
                    }
                    else {
                        alertDialog.setTitle("Error");
                        alertDialog.setMessage("Reservation cant complete, try again or contact with us");
                    }
                    alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                    alertDialog.show();

                }  catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        }
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
