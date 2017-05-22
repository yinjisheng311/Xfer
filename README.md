# CSE-Design-Competition

## Description:
This was for the 50.005 Computer Systems Engineering Design Competition at Singapore University of Technology and Design. The aim of this competition is to implement a secure file transfer, along with any additional features, such as Graphical User Interface (GUI) and more. In this competition, we have created a secure file sharing JavaFx application that allows online users to share files with other online users. We incorporated Material Design to our GUI to improve the looks and usability. We have also utilised Firebase to keep track of currently online users.  

### Collaborators:
Nicholas Yeow Teng Mun (1001490)

Yin Ji Sheng (1001670)

## Graphical User Interface (GUI):
This application's GUI was created using JavaFx, along with JFoenix's Material Design library. Below are a few screenshots of the application:
![Login Page](https://github.com/yinjisheng311/CSE-Design-Competition/blob/master/src/Screenshots/Screen%20Shot%202017-05-22%20at%2010.35.53%20PM.png "Login Page")
![Home Page](https://github.com/yinjisheng311/CSE-Design-Competition/blob/master/src/Screenshots/Screen%20Shot%202017-05-22%20at%2010.39.38%20PM.png "Home Page")
![Send File Page](https://github.com/yinjisheng311/CSE-Design-Competition/blob/master/src/Screenshots/Screen%20Shot%202017-05-22%20at%2010.39.23%20PM.png "Send File Page")

All of the pages are designed to be change dynamically with window sizes.

## Specifications of the Protocols:
This program is split into two main parts, ensuring authentication and confidentality. The first part of the protocol will authenticate both the server to the client, and vice-versa. This will make sure both of them are the intended sender and recipient. After authentication, the next objective would be to securely transmit a file from the client to the authenticated server. This is done via encryption, where the client will send an encrypted file to the authenticated server. Below is a sample Authentication Protocol to be implemented, as given in the handout. 

![Sample AP](https://github.com/imny94/CSE-Programming-Assignments/blob/master/CSE-Programming-Assignment-2/Screen%20Shot%202017-04-20%20at%2012.09.11%20PM.png "Sample Authentication Protocol")

However, this Protocol is not ideal, as there is the possibility of a playback attack being carried out on the Client. Additionally, the Identity of the client is not verified by the server, and the client in this protocol can easily spoof ones identity. These issues are fixed in our implementation of our Authentication Protocol.

Our Authentication Protocol (AP) and Confidentiality Protocol (CP) are outlined below. 

### Authentication Protocol (AP) 
Our Authentication Protocol utilises asymmetric key cryptography to authenticate the identity of both the server and the client. To prevent playback attacks, a nonce request is first sent from the client to the server, and then the server will encrypt the nonce message with its own private key before sending it back to the client. The client will then request for the server's signed certificate to decrypt the encrypted nonce reply from the server. If the decrypted reply from the server matches the nonce initially transmitted by the client to the server, the client can be assured of both the identity of the server, as affirmed by the trusted certification authority, and the absence of a playback attack. This procedure is then repeated by the server to authenticate the identity of the client as well. Below is a chart to describe the AP. 

![alt text](https://github.com/imny94/CSE-Programming-Assignments/blob/master/CSE-Programming-Assignment-2/APFigure.001.jpeg "Logo Title Text 1")


### Confidentiality Protocol (CP)
After the server and client have successfully authenticated each others identity, we implement two different types of confidentiality protocols and compare the performance between the different protocols. The two protocols will be defined as CP1 and CP2.

#### CP1
CP1 implements asymmetric key cryptography using RSA.

#### CP2
CP2 implements symmetric key cryptography using AES.

To compare the performance between the 2 different confidentiality protocols,their respective runtimes are computed and plotted in a graph to compare their speeds. The outcomes can be found at the last section of this page.  

## How to Compile the program on Intellij IDEA:
To compile the program on Intellij IDEA, one should have two different machines with an active Internet connection. Before running the program, one must configure the I.P address to which the client will attempt to connect to the server. This parameters are parsed to the client and server programs via the command line. This can either be done in Eclipse or via the Command line. 

### Client Side configuration

The input to the run configurations should be in the following order: 
1) hostName 
2) portNumber 
3) path to files to be transferred

```
<HostName> <PortNumber> <PathsFilesToTransferred,SeparatedBySpaces>
```

Below is an example of how the run configuration should look like when running the client from Eclipse:
```
"10.12.21.29" "7777" "smallFile.txt" "medianFile.txt" "largeFile.txt" ...
```

### Server Side Configuration

The input to the run configurations should be in the following order: 
1) PortNumber to listen on 
2) Path to Servers private Key
3) Path to Servers Signed Certificate

```
<PortNumber> <PathToPrivateKey> <PathToSignedCertificate>
```

Below is an example of how the run configuration should look like when running the client from Eclipse:
```
"7777" "ServerPrivateKey.der" "ServerSignedCertifcate.crt"
```

### Things to note when running the program

To run the CP1 codes first, one should run the CP1Server.java first on one computer before the other computer can run the CP1Client.java. If the client computer fails to connect to the correct server within 8080ms, the program will throw an timeout error. Both programs should run automatically until the end without errors. 

The steps above hold for files implementing both CP1 and CP2.

Note that the server will receive the encrypted file, decrypt it and save it on its own local computer.

### Acknowledgements:
A large part of this application has incorporated many components used from JFoenix's JavaFx Material Design library. Below is the github link to the library:
```
https://github.com/jfoenixadmin/JFoenix
```

