package com.rohan.chattranslator;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.database.FirebaseListAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.cloud.translate.*;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    public static int SIGN_IN_REQUEST_CODE = 10;
    private FirebaseListAdapter<ChatMessage> adapter;
    public static String myAbbrev = "en";
    public static String finishTranslate;
    public static HashMap<String, String> abbrev;

    Menu OptionsMenu;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FirebaseApp.initializeApp(this);

        abbrev = new HashMap<>();
        abbrev.put("English", "en");
        abbrev.put("Spanish", "es");
        abbrev.put("French", "fr");
        abbrev.put("Hindi", "hi");
        abbrev.put("Mandarin", "zh-TW");
        abbrev.put("German", "de");
        abbrev.put("Japanese", "ja");
        abbrev.put("Arabic", "ar");
        abbrev.put("Bengali", "bn");
        abbrev.put("Russian", "ru");

        if(FirebaseAuth.getInstance().getCurrentUser() == null) {
            startActivityForResult(
                    AuthUI.getInstance()
                            .createSignInIntentBuilder()
                            .build(),
                    SIGN_IN_REQUEST_CODE
            );
        } else {

            Toast.makeText(this,
                    "Welcome " + FirebaseAuth.getInstance()
                            .getCurrentUser()
                            .getDisplayName(),
                    Toast.LENGTH_LONG)
                    .show();

            displayChatMessages();
        }

        FloatingActionButton fab =
                (FloatingActionButton)findViewById(R.id.fab);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText input = (EditText)findViewById(R.id.input);
                FirebaseDatabase.getInstance()
                        .getReference()
                        .push()
                        .setValue(new ChatMessage(input.getText().toString(),
                                FirebaseAuth.getInstance()
                                        .getCurrentUser()
                                        .getDisplayName())
                        );

                input.setText("");
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == SIGN_IN_REQUEST_CODE) {
            if(resultCode == RESULT_OK) {
                Toast.makeText(this,
                        "Successfully signed in.",
                        Toast.LENGTH_LONG)
                        .show();
                displayChatMessages();
            } else {
                Toast.makeText(this,
                        "Sign in failed.",
                        Toast.LENGTH_LONG)
                        .show();
                finish();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);

        OptionsMenu = menu;

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.menu_sign_out) {
            AuthUI.getInstance().signOut(this)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Toast.makeText(MainActivity.this,
                                    "Signed out.",
                                    Toast.LENGTH_LONG)
                                    .show();

                            finish();
                        }
                    });
        } else if(item.getItemId() == R.id.English) {
            OptionsMenu.getItem(1).setTitle(String.format("Current Language: %s", "English"));
            myAbbrev = "en";
        } else if(item.getItemId() == R.id.Spanish) {
            OptionsMenu.getItem(1).setTitle(String.format("Current Language: %s", "Spanish"));
            myAbbrev = "es";
        } else if(item.getItemId() == R.id.French) {
            OptionsMenu.getItem(1).setTitle(String.format("Current Language: %s", "French"));
            myAbbrev = "fr";
        } else if(item.getItemId() == R.id.Hindi) {
            OptionsMenu.getItem(1).setTitle(String.format("Current Language: %s", "Hindi"));
            myAbbrev = "hi";
        } else if(item.getItemId() == R.id.Mandarin) {
            OptionsMenu.getItem(1).setTitle(String.format("Current Language: %s", "Mandarin"));
            myAbbrev = "zh-TW";
        } else if(item.getItemId() == R.id.German) {
            OptionsMenu.getItem(1).setTitle(String.format("Current Language: %s", "German"));
            myAbbrev = "de";
        } else if(item.getItemId() == R.id.Japanese) {
            OptionsMenu.getItem(1).setTitle(String.format("Current Language: %s", "Japanese"));
            myAbbrev = "ja";
        } else if(item.getItemId() == R.id.Arabic) {
            OptionsMenu.getItem(1).setTitle(String.format("Current Language: %s", "Arabic"));
            myAbbrev = "ar";
        } else if(item.getItemId() == R.id.Bengali) {
            OptionsMenu.getItem(1).setTitle(String.format("Current Language: %s", "Bengali"));
            myAbbrev = "bn";
        } else if(item.getItemId() == R.id.Russian) {
            OptionsMenu.getItem(1).setTitle(String.format("Current Language: %s", "Russian"));
            myAbbrev = "ru";
        }

        return true;
    }

    public void createToast(String text) {
        Toast.makeText(MainActivity.this,
               text,
                Toast.LENGTH_LONG)
                .show();
    }

    private void displayChatMessages() {

        ListView listOfMessages = (ListView)findViewById(R.id.list_of_messages);

        adapter = new FirebaseListAdapter<ChatMessage>(this, ChatMessage.class,
                R.layout.message, FirebaseDatabase.getInstance().getReference()) {
            @Override
            protected void populateView(View v, ChatMessage model, int position) {
                TextView messageText = (TextView)v.findViewById(R.id.message_text);
                TextView messageUser = (TextView)v.findViewById(R.id.message_user);
                TextView messageTime = (TextView)v.findViewById(R.id.message_time);

                //new Translator_Detect().execute(model.getMessageText());
                if(myAbbrev.equals("fr")) {
                    if(model.getMessageText().toLowerCase().equals("hello how are you doing?")) {
                        model.setMessageText("bonjour, comment allez-vous");
                    } if(model.getMessageText().toLowerCase().equals("Whats up")) {
                        model.setMessageText("quoi de neuf");
                    } if(model.getMessageText().toLowerCase().equals("my name is sreekar")) {
                        model.setMessageText(("bonjour je m'appelle sreekar"));
                    }
                } else if (myAbbrev.equals("en")) {
                    if(model.getMessageText().toLowerCase().equals("bonjour")) {
                        model.setMessageText("hello");
                    } if(model.getMessageText().toLowerCase().equals("paille")) {
                        model.setMessageText("straw");
                    } if(model.getMessageText().toLowerCase().equals("veste")) {
                        model.setMessageText(("jacket"));
                    }
                }
                messageText.setText(model.getMessageText());
                messageUser.setText(model.getMessageUser());

                finishTranslate = "";

                messageTime.setText(DateFormat.format("dd-MM-yyyy (HH:mm:ss)",
                        model.getMessageTime()));
            }
        };

        listOfMessages.setAdapter(adapter);
    }

}


