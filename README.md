# CSE-Design-Competition

## Description:
This was for the 50.005 Computer Systems Engineering Design Competition at Singapore University of Technology and Design. The aim of this competition is to implement a secure file transfer, along with any additional features, such as Graphical User Interface (GUI) and more. In this competition, we have created a secure file sharing JavaFx application that allows online users to share files with other online users. We incorporated Material Design to our GUI to improve the looks and usability. We have also utilised Firebase to keep track of currently online users.  

### Collaborators:
Nicholas Yeow Teng Mun (1001490)

Yin Ji Sheng (1001670)

## How to Run the Application on Intellij IDEA:
On Intellij, run the Main.java file and the application should load. While loading, the Firebase will be kickstarted and this will take about 4 seconds. There will be a nullpointer Exception printed in the console initially but that is okay. That error was caused by kickstarting the firebase without a valid user input. After which, the Login Page will be displayed and user can proceed as per normal to send or receive files.

## Graphical User Interface (GUI):
This application's GUI was created using JavaFx, along with JFoenix's Material Design library. All of the pages are designed to be change dynamically with window sizes. Below are a few screenshots of the application:

![Sign Up Page](https://github.com/yinjisheng311/CSE-Design-Competition/blob/master/src/Screenshots/Screen%20Shot%202017-05-23%20at%2010.50.32%20PM.png "Sign Up Page")
![Login Page](https://github.com/yinjisheng311/CSE-Design-Competition/blob/master/src/Screenshots/Screen%20Shot%202017-05-22%20at%2010.35.53%20PM.png "Login Page")
![Home Page](https://github.com/yinjisheng311/CSE-Design-Competition/blob/master/src/Screenshots/Screen%20Shot%202017-05-22%20at%2010.39.38%20PM.png "Home Page")
![Send File Page](https://github.com/yinjisheng311/CSE-Design-Competition/blob/master/src/Screenshots/Screen%20Shot%202017-05-22%20at%2010.39.23%20PM.png "Send File Page")

## How to Create User:
1. At the Login Page, click on "Sign Up", and you will be brought to the Sign Up Page.
2. Enter your preferred username and password. Both must at least be 6 characters long. If it is successul, a popup will appear saying you have succeeded in creating a new user.
3. Upon clicking "OK" on the popup, you will be brought back to the Login Page (Clicking "Cancel" can also bring you back to the Login Page). There, you can sign up with your newly created user. 

## How to Send:
1. Login with a registered username and password. (Use "user" as username and "user" as password) If login is successful, you will be brought to the Home Page, where all the currently online users are displayed.
2. Click on the user that you intend to send the file to and you will be brought to the Send File Page.
3. Drag and drop the files that you want to send to this user. Currently, all file types are supported. The files that you have dropped will be displayed on the list view on the left. Click "send" when you have dropped all your files.
4. The intended receiver will receive a popup that asks if he or she wants to accept the files from you or not. If yes, then the files will be transferred to him or her.

## How to Receive:
1. Login with a registered username and password. (Use "user" as username and "user" as password) If login is successful, you will be brought to the Home Page, where all the currently online users are displayed.
2. Here at the Home Page, you will wait for a popup that asks you if you want to receive files from another user. If you click yes, the files will be downloaded.

## Specifications of the OLD Protocols:
This program is split into two main parts, ensuring authentication and confidentality. The first part of the protocol will authenticate both the server to the client, and vice-versa. This will make sure both of them are the intended sender and recipient. After authentication, the next objective would be to securely transmit a file from the client to the authenticated server. This is done via encryption, where the client will send an encrypted file to the authenticated server. Below is a sample Authentication Protocol that was implemented before this competition, as given in the handout previously. 

![Sample AP](https://github.com/imny94/CSE-Programming-Assignments/blob/master/CSE-Programming-Assignment-2/Screen%20Shot%202017-04-20%20at%2012.09.11%20PM.png "Sample Authentication Protocol")

However, this Protocol is not ideal, as there is the possibility of a playback attack being carried out on the Client. Additionally, the Identity of the client is not verified by the server, and the client in this protocol can easily spoof ones identity. These issues were fixed in our implementation of our Authentication Protocol prior to the making of this application.

Our Authentication Protocol (AP) and Confidentiality Protocol (CP) from before are outlined below. 

### Authentication Protocol (AP) 
Our previous Authentication Protocol utilises asymmetric key cryptography to authenticate the identity of both the server and the client. To prevent playback attacks, a nonce request is first sent from the client to the server, and then the server will encrypt the nonce message with its own private key before sending it back to the client. The client will then request for the server's signed certificate to decrypt the encrypted nonce reply from the server. If the decrypted reply from the server matches the nonce initially transmitted by the client to the server, the client can be assured of both the identity of the server, as affirmed by the trusted certification authority, and the absence of a playback attack. This procedure is then repeated by the server to authenticate the identity of the client as well. Below is a chart to describe the AP. 

![alt text](https://github.com/imny94/CSE-Programming-Assignments/blob/master/CSE-Programming-Assignment-2/APFigure.001.jpeg "Logo Title Text 1")


### Confidentiality Protocol (CP)
After the server and client have successfully authenticated each others identity, we implement two different types of confidentiality protocols and compare the performance between the different protocols. The two protocols will be defined as CP1 and CP2.

#### CP1
CP1 implements asymmetric key cryptography using RSA.

#### CP2
CP2 implements symmetric key cryptography using AES.

To compare the performance between the 2 different confidentiality protocols,their respective runtimes are computed and plotted in a graph to compare their speeds. The outcomes can be found at the last section of this page.  

## The NEW Protocol
- Use certificate to ensure authencity of app ... //TODO
- Utilise AES Key for user login ... //TODO

Confidentiality Protocol
- Use CP2 ... //TODO

### Things to note when running the program

Both the server and client must be connected to the same network with this current version. Port forwarding has yet to be implemented. ><

Note that the server will receive the encrypted file, decrypt it and save it on its own local computer.

### Acknowledgements:
A large part of this application has incorporated many components used from JFoenix's JavaFx Material Design library. Below is the github link to the library:
```
https://github.com/jfoenixadmin/JFoenix
```

