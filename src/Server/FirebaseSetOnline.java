package Server;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseCredentials;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import javax.xml.crypto.Data;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by nicholas on 22-May-17.
 */
public class FirebaseSetOnline {

//    private final String user;
//
//    public FirebaseSetOnline(String user){
//        this.user = user;
//    }
//
//    public void setOnline() throws UnknownHostException {
//        // Fetch the service account key JSON file contents
//        FileInputStream serviceAccount = null;
//        try {
//            serviceAccount = new FileInputStream("src//Server//cse-design-competition-firebase-adminsdk-nj8bz-0f00554d8d.json");
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }
//
//        // Initialize the app with a service account, granting admin privileges
//        FirebaseOptions options = new FirebaseOptions.Builder()
//                .setCredential(FirebaseCredentials.fromCertificate(serviceAccount))
//                .setDatabaseUrl("https://cse-design-competition.firebaseio.com/")
//                .build();
//        FirebaseApp.initializeApp(options);
//
//// As an admin, the app has access to read and write all data, regardless of Security Rules
//        DatabaseReference ref = FirebaseDatabase
//                .getInstance()
//                .getReference("/");
//
//        DatabaseReference userRef = ref.child(user);
//        Map<String, Object> userUpdates = new HashMap<String, Object>();
//        userUpdates.put("IPAddress", InetAddress.getLocalHost());
//        userUpdates.put("Online",true);
//
//        ref.child(user).updateChildren(userUpdates);
//    }
}
