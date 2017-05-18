package Server;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseCredentials;
import com.google.firebase.database.*;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by nicholas on 15-May-17.
 */
public class TestFireBase {

    public static void main(String[] args) throws FileNotFoundException, InterruptedException, UnknownHostException {
        // Fetch the service account key JSON file contents
        FileInputStream serviceAccount = new FileInputStream("src//Server//cse-design-competition-firebase-adminsdk-nj8bz-0f00554d8d.json");

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

        Map<String,String> onlineUsers = new HashMap<String,String>();

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
                HashMap userHandle = (HashMap) dataSnapshot.getValue();
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

        ref.child("Nic").setValue(new User(true, InetAddress.getLocalHost().toString().split("/")[1] , "UH6L+Yjwovt02rMYY+s/vU9U4Eb423P/WgeT7nku69s="));

        Runnable test = new Runnable() {
            @Override
            public void run() {
                try {
                    ref.child("Ji").setValue(new User(true , InetAddress.getLocalHost().toString().split("/")[1], "UH6L+Yjwovt02rMYY+s/vU9U4Eb423P/WgeT7nku69s="));
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                }

                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                DatabaseReference userRef = ref.child("Ji");
                Map<String, Object> userUpdates = new HashMap<String, Object>();
                userUpdates.put("IPAddress", "123.456.32.8");
                userUpdates.put("online",true);

                userRef.updateChildren(userUpdates);
            }
        };

        Thread.sleep(5000);

        Thread tester = new Thread(test);
        tester.start();

        Runnable anotherTest = new Runnable() {
            @Override
            public void run() {
                while(true){
                    System.out.println(onlineUsers.toString());
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };

        new Thread(anotherTest).start();

        while(true){ // This is to make sure this thread never terminates, destroying listener threads
            Thread.sleep(Long.MAX_VALUE);
        }
    }
}


