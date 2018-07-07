package com.example.tudonosso.esp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;

public class UdpSocket
{
    private int myPort;
    private DatagramSocket myDatagramSocket;
    private DatagramSocket myBroadcastSocket = null;
    private byte[] myBuffer = new byte[4096];
    private InetAddress myBroadcast = InetAddress.getByName("255.255.255.255");
    private DatagramPacket dp = new DatagramPacket(myBuffer, myBuffer.length);
    private InetAddress espAddress;
    private boolean isEspConnected = false;

    UdpSocket(int port) throws IOException
    {
        myPort = port;

        myDatagramSocket = new DatagramSocket(this.myPort);
    }

    String receive(boolean isBroadcast) throws IOException
    {
        myDatagramSocket.receive(dp);
        if(isBroadcast) espAddress=  dp.getAddress();

        return new String(myBuffer, 0, dp.getLength());
    }

    void send(String message) throws IOException
    {
        DatagramPacket dp;
        dp = new DatagramPacket(message.getBytes(), message.length(), espAddress, this.myPort);
        myDatagramSocket.send(dp);
    }

    String getEspAddress()
    {
        return espAddress.getHostAddress();
    }

    void sendBroadcast(String message) throws IOException
    {
        myBroadcastSocket = new DatagramSocket();
        DatagramPacket dp;
        dp = new DatagramPacket(message.getBytes(), message.length(), myBroadcast, this.myPort);
        myBroadcastSocket.setBroadcast(true);
        myBroadcastSocket.send(dp);
    }

    public void close()
    {
        this.myBroadcastSocket.close();
        this.myDatagramSocket.close();
    }
}
