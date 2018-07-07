package com.example.tudonosso.esp;

import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import java.io.IOException;

public class MainActivity extends AppCompatActivity
{
    private UdpSocket myUdpSocket;
    private String broadcastMessage = "newclient";
    private String leaveMessage = "leaving";
    private boolean isEspConnected = false;
    private TextView corrente;
    private TextView tensao;
    private TextView potencia;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        updateUIComponents();
    }

    private void updateUIComponents()
    {
        corrente = (TextView)findViewById(R.id.tCorrente);
        tensao = (TextView)findViewById(R.id.tTensao);
        potencia = (TextView)findViewById(R.id.tPotencia);
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
    private void updateUI(final TextView t, final String s)
    {
        runOnUiThread(new Runnable() {
            public void run() {
                t.setText(s);
            }
        });
    }


    private void parseAndUpdateUI(String s)
    {
        String[] sp = s.split(";");
        updateUI(potencia, sp[0]+" W");
        updateUI(tensao, sp[1]+" V");
        updateUI(corrente, sp[2]+" A");

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
                    parseAndUpdateUI(s);
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
