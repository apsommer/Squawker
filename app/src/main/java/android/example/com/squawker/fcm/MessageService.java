package android.example.com.squawker.fcm;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.example.com.squawker.MainActivity;
import android.example.com.squawker.R;
import android.example.com.squawker.provider.SquawkContract;
import android.example.com.squawker.provider.SquawkProvider;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

// TODO (1) Make a new Service in the fcm package that extends from FirebaseMessagingService.

public class MessageService extends FirebaseMessagingService {

    // TODO (2) As part of the new Service - Override onMessageReceived. This method will
    // be triggered whenever a squawk is received. You can get the data from the squawk
    // message using getData(). When you send a test message, this data will include the
    // following key/value pairs:
    // test: true
    // author: Ex. "TestAccount"
    // authorKey: Ex. "key_test"
    // message: Ex. "Hello world"
    // date: Ex. 1484358455343

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        // There are two types of messages: data messages and notification messages. Data messages
        // are handled
        // here in onMessageReceived whether the app is in the foreground or background. Data
        // messages are the type
        // traditionally used with FCM. Notification messages are only received here in
        // onMessageReceived when the app
        // is in the foreground. When the app is in the background an automatically generated
        // notification is displayed.
        // When the user taps on the notification they are returned to the app. Messages
        // containing both notification
        // and data payloads are treated as notification messages. The Firebase console always
        // sends notification
        // messages. For more see: https://firebase.google.com/docs/cloud-messaging/concept-options\

        // The Squawk server provided by Udacity always sends just *data* messages,
        // meaning that onMessageReceived when
        // the app is both in the foreground AND the background

        // getFrom() method shows Firebase project? ID 624797599008
        Log.d("~~", "From: " + remoteMessage.getFrom());

        // RemoteMessage is a collection of key:value pairs
        Map<String, String> data = remoteMessage.getData();

        // TODO (3) As part of the new Service - If there is message data, get the data using
        // the keys and do two things with it :
        // 1. Display a notification with the first 30 character of the message
        // 2. Use the content provider to insert a new message into the local database
        // Hint: You shouldn't be doing content provider operations on the main thread.
        // If you don't know how to make notifications or interact with a content provider
        // look at the notes in the classroom for help.

        if (data.size() > 0) {
            sendNotification(data);
            insertSquawk(data);
        }

    }
    /**
     * Create and show a simple notification containing the received FCM message
     *
     * @param data Map which has the message data in it
     */
    void sendNotification(Map<String, String> data) {

        // create intent for main activity
        // the flag activity clear top means that if the app is active in the task stack,
        // then all other apps are destroyed and the app is brought to the foreground
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        // Create the pending intent to launch the activity
        // flag means this pending intent can be used only once
        PendingIntent pendingIntent = PendingIntent.getActivity
                (this, 0, intent, PendingIntent.FLAG_ONE_SHOT);

        String test = data.get("test");
        String author = data.get("author");
        String authorKey = data.get("authorKey");
        String message = data.get("message");
        String date = data.get("date");

        Log.e("~~", test + author + authorKey + message + date);

        // truncate message to 30 characters, if needed
        if (message.length() > 30) {
            message = message.substring(0, 30) + "\u2026";
        }

        // get system notification sound
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        // build the notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setContentText(message)
                .setSmallIcon(R.drawable.ic_duck)
                .setContentTitle(String.format(getString(R.string.notification_message), author))
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        // display notification
        Notification notification = builder.build();
        NotificationManager manager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(0, notification);

    }

    /**
     * Inserts a single squawk into the database;
     *
     * @param data Map which has the message data in it
     */
    void insertSquawk(final Map<String, String> data) {

        @SuppressLint("StaticFieldLeak")
        AsyncTask<Void, Void, Void> insertSquawkTask = new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... voids) {

                ContentValues newMessage = new ContentValues();

                newMessage.put(SquawkContract.COLUMN_AUTHOR, data.get(SquawkContract.COLUMN_AUTHOR));
                newMessage.put(SquawkContract.COLUMN_MESSAGE, data.get(SquawkContract.COLUMN_MESSAGE).trim());
                newMessage.put(SquawkContract.COLUMN_DATE, data.get(SquawkContract.COLUMN_DATE));
                newMessage.put(SquawkContract.COLUMN_AUTHOR_KEY, data.get(SquawkContract.COLUMN_AUTHOR));

                getContentResolver().insert(SquawkProvider.SquawkMessages.CONTENT_URI, newMessage);

                return null;
            }
        };

    }

}
