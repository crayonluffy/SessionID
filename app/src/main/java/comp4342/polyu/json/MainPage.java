package comp4342.polyu.json;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class MainPage extends AppCompatActivity {
    Button join,create;
    DownloadManager downloadManager;
    EditText SessionID;
    TextView str;
    String id;
    String[] Sessionfiles;
    int count;

    BroadcastReceiver onComplete=new BroadcastReceiver() {
        public void onReceive(Context ctxt, Intent intent) {
            count++;
            if (count==Sessionfiles.length) {
                Toast.makeText(getApplicationContext(), "All files have been downloaded.", Toast.LENGTH_SHORT).show();
                Intent i = new Intent(MainPage.this, pdfViewer.class);
                startActivity(i);
                MainPage.this.finish();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_page);

        join = (Button)findViewById(R.id.join);
        create = (Button)findViewById(R.id.create);
        SessionID = (EditText)findViewById(R.id.SessionID);
        str = (TextView)findViewById(R.id.textView4);

        join.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                id = SessionID.getText().toString();
                new GetId().execute("http://www.crayonluffy.com:10000/join/" + id);
            }
        });

        create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainPage.this, MainActivity.class);
                startActivity(i);
                MainPage.this.finish();
            }
        });
    }
    //--------------------------------------------------------------------------------------------------------------------------------------------------------
    //This part is used to post the name and password to server and get the session id back
    public class GetId extends AsyncTask<String,String,String> {

        @Override
        protected String doInBackground(String... params) {
            HttpURLConnection connection = null;
            BufferedReader reader = null;

            try {
                URL url = new URL(params[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();
                InputStream stream = connection.getInputStream();
                reader = new BufferedReader(new InputStreamReader(stream));
                StringBuffer buffer = new StringBuffer();
                String line ="";
                while ((line = reader.readLine()) != null){
                    buffer.append(line);
                }

                String finalJson = buffer.toString();
                JSONArray jsonArray = new JSONArray(finalJson);
                Sessionfiles = new String[jsonArray.length()];

                for (int i = 0; i < jsonArray.length(); i++) {
                    Sessionfiles[i] = jsonArray.getString(i);
                }
                return finalJson;
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            } finally {
                if(connection != null) {
                    connection.disconnect();
                }
                try {
                    if(reader != null) {
                        reader.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            str.setText(result);
            DownloadFile();
//            File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath()
//                    + File.separator+"Android"+File.separator+"data"+File.separator+getApplicationContext().getPackageName()
//                    + File.separator+"2.png");
//            //Toast.makeText(getApplicationContext(), ""+hello, Toast.LENGTH_SHORT).show();

        }
    }

    public void DownloadFile()
    {
        for (int i = 0; i < Sessionfiles.length; i++) {
            downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
            Uri uri = Uri.parse("http://www.crayonluffy.com:10000/" + id + "/" + Sessionfiles[i]);
            DownloadManager.Request request = new DownloadManager.Request(uri);
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            request.setDestinationInExternalFilesDir(MainPage.this, Environment.DIRECTORY_DOWNLOADS, Sessionfiles[i]);
            Long reference = downloadManager.enqueue(request);
            registerReceiver(onComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
        }
    }

}
