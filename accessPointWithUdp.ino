
#include <ESP8266WiFi.h>
#include <WiFiUDP.h>

/*Variables*/
bool accessPointCreated = false;
WiFiUDP Udp;
IPAddress clientIp;
const unsigned int localUdpPort = 13186;
const IPAddress broadcastIp(192,168,4,255);
char incomingPacket[255];
String myLocalIpString;
int r;
String s = "nothing\n";

/*Headers*/
void creteUdpPort();
int findClients();
void sendUdpPacket(IPAddress, unsigned int);
void getMyLocalIp();
bool configureAccessPoint();
int readPacket();
void receiveSmartphoneData();
void receiveSerialData();
void sendStringUdp();

/*State mahine*/
#define CONFIGURE_ACCESS_POINT    0
#define SET_UP_UDP_PORT    1
#define GET_MY_LOCAL_IP 2
#define FIND_CLIENT     3
#define SEND_MESSAGE_TO_CLIENT 4
#define SEND_DATA 5

static int state = CONFIGURE_ACCESS_POINT;

void setup() {
  Serial.begin(115200);
  Serial.println();
  Serial.setTimeout(2000); 
  WiFi.disconnect();
}

void loop() {
  switch (state)
  {
    case CONFIGURE_ACCESS_POINT:
          if(configureAccessPoint()) state = SET_UP_UDP_PORT;
          break;
          
    case SET_UP_UDP_PORT:
          createUdpPort();
          state = GET_MY_LOCAL_IP;
          break;
          
    case GET_MY_LOCAL_IP:
          getMyLocalIp();
          state = FIND_CLIENT;
          break;
          
    case FIND_CLIENT: 
          r = findClients();
          delay(1000);
          if(r == 0){
            state = SEND_MESSAGE_TO_CLIENT;
            break;
          }
          if(r == 1) Serial.println("Got my own message");
          break;         
          
    case SEND_MESSAGE_TO_CLIENT:
          sendUdpPacket(clientIp, localUdpPort);
          state = SEND_DATA;
          break;
          
    case SEND_DATA:
          receiveSmartphoneData();
          receiveSerialData();
          sendUdpPacket(clientIp, localUdpPort);
          break;     
    default:
      break;
  }
}


bool configureAccessPoint()
{
  return WiFi.softAP("ESPsoftAP_01", "password");
}

void createUdpPort()
{
  Udp.begin(localUdpPort);
  delay(5000);   
}

int findClients()
{
  //receives packet
  int packetSize = Udp.parsePacket();
  if (packetSize)
  {
    //check if its my own packet
    if(Udp.remoteIP().toString().equals(myLocalIpString)) return 1;

    //get it
    int sizePacket = readPacket();

    if(strcmp(incomingPacket, "newclient") == 0){
      clientIp = Udp.remoteIP();
      return 0;
    }    
  }

  //no packet
  return 2;
}

void getMyLocalIp()
{
  myLocalIpString = WiFi.localIP().toString();
}

void sendUdpPacket(IPAddress ipToSend, unsigned int port)
{
  Udp.beginPacket(ipToSend, port);
  Udp.write(s.c_str());
  Udp.endPacket();  
}

int readPacket()
{
  int len = Udp.read(incomingPacket, 255);
  if (len > 0) incomingPacket[len] = 0;
  return len;
}

void receiveSerialData()
{
  while (Serial.available() <= 0);
  s = Serial.readString();
}

void receiveSmartphoneData()
{
  //receives packet
  int packetSize = Udp.parsePacket();
  if (packetSize)
  {
    readPacket();  
    if(strcmp(incomingPacket, "leaving") == 0)
    {
      state = FIND_CLIENT;      
    }
  }
}
