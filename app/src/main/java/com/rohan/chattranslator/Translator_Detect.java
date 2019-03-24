package com.rohan.chattranslator;

import android.os.AsyncTask;
import android.widget.TextView;

import com.google.cloud.translate.Detection;
import com.google.cloud.translate.Translate;
import com.google.cloud.translate.TranslateOptions;
import com.google.cloud.translate.Translation;

import java.util.LinkedList;
import java.util.List;

class Translator_Detect extends AsyncTask<String, Void, String> {

    private static final String API_KEY = "AIzaSyAqMZ4Lm6rFU9anDSvLjCN5O4frooEWCDQ";

    protected String doInBackground(String... params) {

        TranslateOptions options = TranslateOptions.newBuilder()
                .setApiKey(API_KEY)
                .build();
        Translate translateT = options.getService();

        Detection detection = translateT.detect(params[0]);
        String otherAbbrev = detection.getLanguage();
        System.out.printf("Other Language Detected: %s, My Language: %s", otherAbbrev, MainActivity.myAbbrev);
        System.out.printf("params : %s", params[0]);

        if(otherAbbrev.equals(MainActivity.myAbbrev)) {
            MainActivity.finishTranslate = params[0];
            return params[0];
        } else {
            final Translation translation = translateT.translate(params[0], Translate.TranslateOption.targetLanguage(MainActivity.myAbbrev), Translate.TranslateOption.sourceLanguage(otherAbbrev), Translate.TranslateOption.model("nmt"));
            MainActivity.finishTranslate = translation.getTranslatedText();
            return translation.getTranslatedText();
        }

        //System.out.printf("Sentence: %s", finishedText);
        //return finishedText;
    }
}
