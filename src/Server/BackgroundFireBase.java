package Server;

import com.google.api.client.util.ArrayMap;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseCredentials;
import com.google.firebase.database.*;
import com.google.firebase.tasks.OnSuccessListener;
import com.sun.org.apache.xpath.internal.SourceTree;
import com.sun.org.apache.xpath.internal.operations.Bool;

import javax.xml.crypto.Data;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;

/**
 * Created by nicholas on 15-May-17.
 */
public class BackgroundFireBase extends Thread {

    public Map<String,String> onlineUsers;

    BackgroundFireBase() {
        // Fetch the service account key JSON file contents
        FileInputStream serviceAccount = null;
        try {
            serviceAccount = new FileInputStream("src//Server//cse-design-competition-firebase-adminsdk-nj8bz-0f00554d8d.json");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

// Initialize the app with a service account, granting admin privileges
        FirebaseOptions options = new FirebaseOptions.Builder()
                .setCredential(FirebaseCredentials.fromCertificate(serviceAccount))
                .setDatabaseUrl("https://cse-design-competition.firebaseio.com/")
                .build();
        FirebaseApp.initializeApp(options);

// As an admin, the app has access to read and write all data, regardless of Security Rules
        DatabaseReference ref = FirebaseDatabase
                .getInstance()
                .getReference("/");
        System.out.println(ref);

        onlineUsers = new HashMap<String,String>();

        ref.addChildEventListener(new ChildEventListener() {

            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                System.out.println("Child Added");
                HashMap userHandle = (HashMap) dataSnapshot.getValue();
                if((boolean) userHandle.get("online")){
                    onlineUsers.put(dataSnapshot.getKey(),(String) userHandle.get("IPAddress"));
                }else{
                    onlineUsers.remove(dataSnapshot.getKey());
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                System.out.println("Online Changed");
                HashMap userHandle = (HashMap) dataSnapshot.getValue();
                if((boolean) userHandle.get("online")){
                    onlineUsers.put(dataSnapshot.getKey(),(String) userHandle.get("IPAddress"));
                }else{
                    onlineUsers.remove(dataSnapshot.getKey());
                }
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                System.out.println("Online Removed");
                onlineUsers.remove(dataSnapshot.getKey());
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                System.out.println("Online Moved");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("Cancelled!");
            }
        });
    }
}

class User{

    public boolean Online;
    public String IPAddress;
    public String Auth;

    User(boolean Online, String IPAddress, String Auth) throws UnknownHostException {
        this.Online = Online;
        this.IPAddress = IPAddress;
        this.Auth = Auth;
    }
}

