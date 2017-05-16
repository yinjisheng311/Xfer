package Server;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseCredentials;
import com.google.firebase.database.*;
import com.sun.org.apache.xpath.internal.SourceTree;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

/**
 * Created by nicholas on 15-May-17.
 */
public class TestFireBase {

    public static void main(String[] args) throws FileNotFoundException, InterruptedException {
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
                .getReference("");
        System.out.println(ref);
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Object document = dataSnapshot.getValue();
                System.out.println(document);
                System.out.println(dataSnapshot.getValue());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("Cancelled!");
            }
        });
        System.out.println("Completed");

        ref.setValue(new User("June 23, 1912", "Alan Turing"));

        System.out.println("ADDED STUFF");

        while(true){ // This is to make sure this thread never terminates, destroying listener threads
            Thread.sleep(Long.MAX_VALUE);
        }

//        Runnable tester = new Runnable() {
//            @Override
//            public void run() {
//                while(true){
//                    System.out.println("Hello");
//                    try {
//                        Thread.sleep(1000);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                }
//            }
//        };
//
//        Thread thread = new Thread(tester);
//        thread.start();
    }
}

class User{

    public String DOB;
    public String name;

    User(String DOB, String name){
        this.DOB = DOB;
        this.name = name;
    }
}