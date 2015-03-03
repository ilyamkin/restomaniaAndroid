package com.restomania.restomania;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.dd.processbutton.iml.ActionProcessButton;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.json.JSONObject;

import java.io.IOException;

/**
 * Created by Freemahn on 01.03.2015.
 */
public class RegisterActivity extends Activity implements View.OnClickListener {
    EditText textName, textLogin, textPass;
    private ActionProcessButton btnRegister;
    private static String TAG = "REGISTER";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        textName = (EditText) findViewById(R.id.textName);
        textLogin = (EditText) findViewById(R.id.textLogin);
        textPass = (EditText) findViewById(R.id.textPassword);
        btnRegister = (ActionProcessButton) findViewById(R.id.btnRegister);
        btnRegister.setMode(ActionProcessButton.Mode.ENDLESS);
        btnRegister.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        attemptRegister();

    }

    public void attemptRegister() {
        btnRegister.setProgress(1);
        if (mAuthTask != null) {
            return;
        }

        String name = textName.getText().toString();
        String login = textLogin.getText().toString();
        String pass = textPass.getText().toString();
        String hash = "";
        if (name.equals("") || login.equals("") || pass.equals("")) {
            Toast.makeText(this, "Заполните все поля", Toast.LENGTH_LONG).show();
            return;
        }
        hash = PasswordHash.createHash(login, pass);
        Log.d(TAG, "args:" + name + " " + login + " " + pass + " " + hash);
        mAuthTask = new UserRegisterTask(login, hash, name);
        mAuthTask.execute((Void) null);
    }


    public class UserRegisterTask extends AsyncTask<Void, Void, Boolean> {

        private final String mLogin;
        private final String mHash;
        private final String mName;
        public final MediaType JSON
                = MediaType.parse("application/json; charset=utf-8");

        UserRegisterTask(String login, String hash, String name) {
            mLogin = login;
            mHash = hash;
            mName = name;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            btnRegister.setProgress(1);
        }

        OkHttpClient client = new OkHttpClient();

        //true if success
        @Override
        protected Boolean doInBackground(Void... strings) {
            String url = "http://104.131.184.188:8080/restoserver/signUp";
            String answer = null;
            try {
                Thread.sleep(1000);
                answer = post(url, bowlingJson());
                Log.d(TAG, "Response " + answer);
                JSONObject ans = new JSONObject(answer);
                if (ans.has("error")) {
                    return false;
                }
            } catch (Exception e) {
                Log.e(TAG, e.toString());
                return false;
            }
            return true;

        }

        String post(String url, String json) throws IOException {
            RequestBody body = RequestBody.create(JSON, json);
            Request request = new Request.Builder()
                    .url(url)
                    .post(body)
                    .build();
            Response response = client.newCall(request).execute();
            return response.body().string();
        }

        String bowlingJson() {
            return "{'login':'" + mLogin + "',"
                    + "'hash':'" + mHash + "',"
                    + "'name':" + mName +
                    "'}";
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            if (success) {
                btnRegister.setProgress(100);
                Intent intent = new Intent();
                intent.putExtra("login", mLogin);
                intent.putExtra("hash", mHash);
                Log.d(TAG, "returning to account...args " + mLogin + ":" + mHash);
                setResult(RESULT_OK, intent);
                finish();
            } else {
                //Toast.makeText(getApplicationContext(), "Что то пошло не так", Toast.LENGTH_LONG).show();
                btnRegister.setProgress(-1);
            }


        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;

        }
    }

    private UserRegisterTask mAuthTask = null;
}
