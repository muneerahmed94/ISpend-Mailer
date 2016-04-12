package com.example.muneer.ispendmailer;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class MainActivity extends AppCompatActivity {

    ProgressDialog progressDialog;
    String json_string, JSON_STRING;

    Button buttonSendEmail;
    JSONObject jsonObject;
    JSONArray jsonArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        buttonSendEmail = (Button) findViewById(R.id.buttonSendEmail);

        buttonSendEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new SendEmailAsync().execute();
            }
        });

        progressDialog = new ProgressDialog(MainActivity.this);
        progressDialog.setCancelable(false);
        progressDialog.setTitle("Retrieving offers for customers...");
        progressDialog.setMessage("Please wait...");
    }

    class SendEmailAsync extends AsyncTask<Void, Void, String>
    {
        String json_url;

        @Override
        protected void onPreExecute() {
            json_url = "https://ispend-jntuhceh.rhcloud.com/koti/index.php";
            progressDialog.show();
        }

        @Override
        protected String doInBackground(Void... params) {
            try {
                URL url = new URL(json_url);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                InputStream inputStream = httpURLConnection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder stringBuilder = new StringBuilder();
                while((JSON_STRING = bufferedReader.readLine()) != null)
                {
                    stringBuilder.append(JSON_STRING + "\n");
                }
                bufferedReader.close();
                inputStream.close();
                httpURLConnection.disconnect();
                return stringBuilder.toString().trim();
            }
            catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            progressDialog.dismiss();
            json_string = result;
            Toast.makeText(MainActivity.this, "Offers retrieved successfully", Toast.LENGTH_SHORT).show();
            if(json_string == null)
                Toast.makeText(MainActivity.this, "Unable to retrieve offers", Toast.LENGTH_SHORT).show();
            else
                sendEmails();
        }
    }

    public void sendEmails()
    {
        try {
            jsonObject = new JSONObject(json_string);
            jsonArray = jsonObject.getJSONArray("server_response");

            int count = 0;
            String email, body;
            while(count < jsonArray.length())
            {
                JSONObject jo = jsonArray.getJSONObject(count);
                email = jo.getString("Email");
                body = jo.getString("Body");


                String from = "emailsender827@gmail.com";
                String username = "emailsender827";
                String password = "emailsender1994";

                String to = email;
                String subject = "Exciting offers for you";
                String message = body;

                EmailSender ISpendMailer = new EmailSender(com.example.muneer.ispendmailer.MainActivity.this);
                ISpendMailer.send(from, username, password, to, subject, message);

                count++;
            }
        }catch (JSONException e) {
            e.printStackTrace();
        }
    }


    public class EmailSender {

        ProgressDialog progressDialog;
        Context context;

        EmailSender(Context context) {
            this.context = context;
            progressDialog = new ProgressDialog(context);
            progressDialog.setCancelable(false);
            progressDialog.setTitle("Sending offers...");
            progressDialog.setMessage("Please wait...");
        }

        public void send(String from, String username, String password, String to, String subject, String msgBody) {
            new EmailSenderAsync(context, from, username, password, to, subject, msgBody).execute();
        }

        public class EmailSenderAsync extends AsyncTask<Void, Void, Boolean> {
            Context context;
            String from, username, password, to, subject, msgBody;

            public EmailSenderAsync(Context context, String from, String username, String password, String to, String subject, String msgBody) {
                this.context = context;
                this.from = from;
                this.msgBody = msgBody;
                this.password = password;
                this.subject = subject;
                this.to = to;
                this.username = username;
            }

            @Override
            protected void onPreExecute() {
                progressDialog.show();
            }

            @Override
            protected Boolean doInBackground(Void... params) {

                String host = "smtp.gmail.com";

                Properties props = new Properties();
                props.put("mail.smtp.auth", "true");
                props.put("mail.smtp.starttls.enable", "true");
                props.put("mail.smtp.host", host);
                props.put("mail.smtp.port", "587");

                Session session = Session.getInstance(props,
                        new javax.mail.Authenticator() {
                            protected PasswordAuthentication getPasswordAuthentication() {
                                return new PasswordAuthentication(username, password);
                            }
                        });

                try {
                    Message message = new MimeMessage(session);
                    message.setFrom(new InternetAddress(from));
                    message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
                    message.setSubject(subject);
                    message.setText(msgBody);
                    Transport.send(message);
                } catch (MessagingException e) {
                    return false;
                }
                catch(Exception e) {
                    return false;
                }

                return true;
            }

            @Override
            protected void onPostExecute(Boolean isEmailSent) {
                progressDialog.dismiss();
                if(isEmailSent) {
                    Toast.makeText(context, "Offers sent to " + to, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, "Offers not Sent to " + to, Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}
