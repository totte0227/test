package jp.ac.oit.is.lab261.demonio;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public final class HttpPostTask extends AsyncTask<URL, Void, String> {
    private String data = "";
    private String dataPost = "";
    Context context;

    public HttpPostTask(Context context){
        this.context = context;
    }

    @Override
    protected String doInBackground(URL... urls) {
        dataPost += "data=" + this.data;

        final URL url = urls[0];
        HttpURLConnection con = null;
        StringBuilder bufStr = new StringBuilder();

        try {
            con = (HttpURLConnection) url.openConnection();
            con.setDoOutput(true);
            con.setDoInput(true);
            con.setChunkedStreamingMode(0);
            con.connect();

            // POSTデータ送信処理
            OutputStream out = null;
            try {
                out = con.getOutputStream();
                out.write(dataPost.getBytes("UTF-8"));
                Log.d("Post=", data);

                out.flush();
            } catch (IOException e) {
                // POST送信エラー
                e.printStackTrace();
            } finally {
                if (out != null) {
                    out.close();
                }
            }

            int status = con.getResponseCode();
            if (status == HttpURLConnection.HTTP_OK) {
                InputStream in = con.getInputStream();
                BufferedReader br = new BufferedReader(new InputStreamReader(in));
                String result;

                // InputStreamからのデータを文字列として取得する
                while ((result = br.readLine()) != null) {
                    bufStr.append(result);
                }
                in.close();
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (con != null) {
                con.disconnect();
            }
        }
        return bufStr.toString();
    }

    @Override
    protected void onPostExecute(String bufStr) {
        Log.d("HttpPostTask", bufStr);
            Toast.makeText(context, bufStr, Toast.LENGTH_SHORT).show();

    }

    public void setLatitude(String data) {
        this.data = data;
    }

}
