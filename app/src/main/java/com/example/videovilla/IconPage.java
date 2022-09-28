package com.example.videovilla;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Random;

public class IconPage extends AppCompatActivity {

    TextView name,num1,num2,operand;
    Button signOut,start;
    GoogleSignInOptions gso;
    GoogleSignInClient gsc;
    TextToSpeech tts;

    int qtn=1, results=0;
    int ans,response;
    String ans1,response1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_icon_page);

        name = findViewById(R.id.name);
        signOut = findViewById(R.id.signOut);
        start = findViewById(R.id.start);
        num1 = findViewById(R.id.num1);
        num2 = findViewById(R.id.num2);
        operand = findViewById(R.id.operand);
        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build();
        gsc = GoogleSignIn.getClient(IconPage.this,gso);

        // Display name of the user from signed in account
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(IconPage.this);
        if(account!=null){
            String name1 = account.getDisplayName();
            name.setText(name1);
        }

        signOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                gsc.signOut().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(Task<Void> task) {
                        finish();
                        Intent intent = new Intent(IconPage.this,MainActivity.class);
                        startActivity(intent);
                    }
                });
            }
        });
    }

        public void askQuestions(View view){

                start.setText("Speak the answer!");

                // Generate random operation
                Random rand = new Random();
                String number1 = String.valueOf(rand.nextInt(10) + 1);
                num1.setText(number1);
                String number2 = String.valueOf(rand.nextInt(10) + 1);
                num2.setText(number2);
                String[] operands = new String[]{"+","-","*"};
                String op = operands[rand.nextInt(operands.length)];
                operand.setText(op);

                // question from text to speech
                String say="";
                int ans = 0;
                if(op.equals("+")){
                   say = "Plus";
                    ans = Integer.parseInt(number1) + Integer.parseInt(number2);
                }
                else if(op.equals("-")){
                    say = "Minus";
                    ans = Integer.parseInt(number1) - Integer.parseInt(number2);
                }
                else if(op.equals("*")){
                    say = "Into";
                    ans = Integer.parseInt(number1) * Integer.parseInt(number2);
                }
                ans1 = String.valueOf(ans);
                String text = number1+say+number2;
                tts = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
                    @Override
                    public void onInit(int i) {
                        if (i == TextToSpeech.SUCCESS) {
                            tts.setLanguage(Locale.getDefault());
                            tts.setSpeechRate(1.0f);
                            tts.speak(text, TextToSpeech.QUEUE_ADD, null);
                        }
                    }
                });

                // user's speech into text
                Runnable r = new Runnable() {
                    @SuppressLint("QueryPermissionsNeeded")
                    @Override
                    public void run() {
                        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
                        if (intent.resolveActivity(getPackageManager()) != null) {
                            startActivityForResult(intent, 2000);
                        }
                        else {
                            Toast.makeText(IconPage.this, "Device is not supporting speech!", Toast.LENGTH_SHORT).show();
                        }
                    }
                };

                Handler h = new Handler();
                h.postDelayed(r, 1500);
        }

    // after getting result from speech to text intent
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // if the requestCode mentioned in intent matches 1000
        switch(requestCode){
            case 2000:
                if(resultCode==RESULT_OK && data!=null){
                    // get the speech into text as an arraylist and set it in text_from_speech
                    ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    response1 = String.valueOf(result.get(0));

                    String command;
                    if(ans1.equals(response1)){
                        command = "Correct";
                        results++;
                    }
                    else{
                        command = "Incorrect";
                    }

                    // Say if ans is correct or incorrect
                    tts = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
                        @Override
                        public void onInit(int i) {
                            if(i==TextToSpeech.SUCCESS){
                                tts.setLanguage(Locale.getDefault());
                                tts.setSpeechRate(1.0f);
                                tts.speak(command,TextToSpeech.QUEUE_ADD,null);

                                //next question
                                Runnable q = new Runnable() {
                                    @SuppressLint("QueryPermissionsNeeded")
                                    @Override
                                    public void run() {
                                        if(qtn!=11) {
                                            start.performClick();
                                        }
                                    }
                                };

                                Handler h = new Handler();
                                h.postDelayed(q, 1000);

                                qtn++;

                                if (qtn==11) {
                                    String r = "Your Score is "+String.valueOf(results)+ " out of 10";
                                    Toast.makeText(IconPage.this,r, Toast.LENGTH_LONG).show();
                                    tts = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
                                        @Override
                                        public void onInit(int i) {
                                            if (i == TextToSpeech.SUCCESS) {
                                                tts.setLanguage(Locale.getDefault());
                                                tts.setSpeechRate(1.0f);
                                                tts.speak(r, TextToSpeech.QUEUE_ADD, null);

                                                finish();
                                                Intent intent = new Intent(IconPage.this,IconPage.class);
                                                startActivity(intent);
                                            }
                                        }
                                    });
                                }
                            }
                        }
                    });
                }
                break;
        }
    }
}