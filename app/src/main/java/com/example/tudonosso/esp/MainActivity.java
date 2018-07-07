package com.example.tudonosso.esp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import java.io.IOException;

public class MainActivity extends AppCompatActivity
{
    private UdpSocket myUdpSocket;
    private String broadcastMessage = "newclient";
    private String leaveMessage = "leaving";
    private boolean isEspConnected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        try
        {
            myUdpSocket = new UdpSocket(13186);
        }
        catch(IOException e)
        {
            Log.d("socket", "error creating udp socket, "+e.getMessage());
        }

        new Thread(new ThreadBroadcast()).start();
        new Thread(new ThreadSendAndReceive()).start();

    }

    @Override
    public void onDestroy()
    {
        new Thread(new ThreadClose()).start();
        super.onDestroy();
    }

    private class ThreadClose implements Runnable
    {
        @Override
        public void run()
        {
            if(!isEspConnected) return;

            try
            {
                myUdpSocket.sendBroadcast(leaveMessage);
                myUdpSocket.close();
                isEspConnected = false;
            }
            catch(Exception e){}

        }
    }

    private class ThreadBroadcast implements Runnable
    {
        @Override
        public void run()
        {
            if(isEspConnected) return;

            try {
                myUdpSocket.sendBroadcast(broadcastMessage);
                Log.d("socket", "message sent");
                String s;
                while((s = myUdpSocket.receive(true)).equals(broadcastMessage));
                Log.d("socket", "got "+s+" from esp");
                Log.d("socket", "esp address at "+myUdpSocket.getEspAddress());
                isEspConnected = true;
            }
            catch(IOException e)
            {
                Log.d("socket", "error sending broadcast "+e.getMessage());
            }
        }
    }

    private class ThreadSendAndReceive implements Runnable
    {
        @Override
        public void run()
        {
            while (!isEspConnected);
            while(true)
            {
                try{
                    myUdpSocket.send("send me something");
                    String s = myUdpSocket.receive(false);
                    Log.d("socket", "Received "+s);
                    Thread.sleep(1000);
                }catch (Exception e)
                {
                    Log.d("socket", "Exception when sending or receiving "+e.getMessage());
                }
            }

        }
    }
}
