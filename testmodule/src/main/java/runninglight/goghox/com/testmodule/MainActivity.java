package runninglight.goghox.com.testmodule;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private String TAG;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        /*Net net = Net.getInstance();
        net.get("http://192.168.1.125:23014/timeschedule", new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.i(TAG, "onFailure: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String body = response.body().source().readUtf8();
                Log.i(TAG, "onResponse: " + body);

            }
        });
        OkHttpClient client = new OkHttpClient();

        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType, "{\n\t\"name\": \"Admin\",\n\t\"password\": \"test1233\"\n}");
        Request request = new Request.Builder()
                .url("http://localhost:8080/auth")
                .get()
                .addHeader("content-type", "application/json")
                .build();

        try {
            Response response = client.newCall(request).execute();
            Log.i(TAG, "onCreate: " + response.body().toString());
        } catch (IOException e) {
            e.printStackTrace();
        }*/
    }
}
