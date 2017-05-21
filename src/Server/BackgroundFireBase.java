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
import java.util.concurrent.Phaser;
import java.util.concurrent.RunnableFuture;

/**
 * Created by nicholas on 15-May-17.
 */
public class BackgroundFireBase{

    public final Map<String,String> onlineUsers = new HashMap<>();
    public static int numReferences = 0;

//    private final DatabaseReference ref = FirebaseDatabase
//                                                .getInstance()
//                                                .getReference("/");

    private DatabaseReference ref;

    private static BackgroundFireBase instance = null;

    private BackgroundFireBase() {
        start();
    }

    public static BackgroundFireBase getInstance() {
        if(instance == null) {
            instance = new BackgroundFireBase();
        }
        numReferences ++;
        return instance;
//        return new BackgroundFireBase();
    }

    private void start() {
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
        ref = FirebaseDatabase
                .getInstance()
                .getReference("/");

//        onlineUsers = new HashMap<String,String>();

        ref = FirebaseDatabase
                        .getInstance()
                        .getReference("/");

        ref.addChildEventListener(new ChildEventListener() {

            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                System.out.println("Child Added");
                HashMap userHandle = (HashMap) dataSnapshot.getValue();
                if((boolean) userHandle.get("Online")){
                    System.out.println("Added newly added child");
                    onlineUsers.put(dataSnapshot.getKey(),(String) userHandle.get("IPAddress"));
                }else{
                    System.out.println("Removed added child");
                    onlineUsers.remove(dataSnapshot.getKey());
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                System.out.println("Online Changed");
                HashMap userHandle = (HashMap) dataSnapshot.getValue();
                if((boolean) userHandle.get("Online")){
                    System.out.println("Added changed Child");
                    onlineUsers.put(dataSnapshot.getKey(),(String) userHandle.get("IPAddress"));
                }else{
                    System.out.println("Removed changed child");
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

        Runnable test = new Runnable() {
            @Override
            public void run() {
                while (true){
                    System.out.println(Thread.currentThread().getName());
                    System.out.println(onlineUsers.toString());
                    try {
                        Thread.sleep(10000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };

        new Thread(test).start();

    }

    public String Authenticate(String user){

        final String[] AuthString = new String[1];
        Phaser phaser = new Phaser();
        phaser.register();// Registers this main thread
        phaser.register(); // Registers listener thread
        ref.child("/"+ user+"/Auth").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                AuthString[0] = dataSnapshot.getValue(String.class);
                System.out.println("Single Query Data Change");
                System.out.println(dataSnapshot);
                System.out.println(dataSnapshot.getValue(String.class));
                phaser.arriveAndDeregister();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("Cancelled");
            }
        });
        phaser.arriveAndAwaitAdvance();
        return AuthString[0];
    }

    // Index 0 : Public Key , Index 1 : Private Key
    public String[] QueryPubPriv(String user){
        final String[] PubPriv = new String[2];
        Phaser phaser = new Phaser();
        phaser.bulkRegister(3); // Registers main thread, listener for priv and listener for pub
        ref.child("/"+user+"/Pub").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                PubPriv[0] = dataSnapshot.getValue(String.class);
                phaser.arriveAndDeregister();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        ref.child("/"+user+"/Priv").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                PubPriv[1] = dataSnapshot.getValue(String.class);
                phaser.arriveAndDeregister();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        phaser.arriveAndAwaitAdvance();
        return PubPriv;
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

