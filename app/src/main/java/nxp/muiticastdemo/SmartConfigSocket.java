package nxp.muiticastdemo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

/**
 * Created by b41466 on 17-7-7.
 */

public class SmartConfigSocket extends BroadcastReceiver{
    /*socket for multicast*/
    private MulticastSocket mSendSocket;
    private MulticastSocket mRecvSocket;
    private ReciveThread mRecvThread;
    private SendThread mSendThread;
    private Context mContext;

    public SmartConfigSocket(Context context) {
        mContext = context;
        try {
            mSendSocket = new MulticastSocket();
            mSendSocket.setTimeToLive(4);

            mRecvSocket = new MulticastSocket(14000);
            mRecvSocket.setReuseAddress(true);
            mRecvSocket.setSoTimeout(0);

            context.registerReceiver(this,
                    new IntentFilter("android.intent.action.NEWDEVICE"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onReceive(Context context, Intent intent) {
        if("android.intent.action.NEWDEVICE".equals(intent.getAction())){
            System.out.println(intent.getExtras().getString("key"));
        }
    }

    public void startSendPacket(String ssid, String pass){
        mSendThread = new SendThread(ssid, pass);
        mRecvThread = new ReciveThread();
        mSendThread.start();
        mRecvThread.start();
    }

    public void stopSendPacket(){
        if (mSendThread != null)
            mSendThread.exit();
        mSendThread = null;

        if (mRecvThread != null)
            mRecvThread.exit();
        mRecvThread = null;
    }

    class ReciveThread extends Thread {
        private volatile boolean mPause = false;

        public void run() {
            mPause = false;
            try {
                while (!mPause) {
                    byte[] data = new byte[1024];
                    DatagramPacket packet = new DatagramPacket(data, data.length);
                    mRecvSocket.receive(packet);
                    String str = new String(packet.getData(), 0, packet.getLength());
                    System.out.println("Get package：" + str);

                    if (str.contains("SUCCESS:ls1012ardb")) {
                        Intent intent = new Intent();
                        intent.setAction("android.intent.action.NEWDEVICE");
                        intent.putExtra("key", str.replace("SUCCESS:", ""));
                        mContext.sendBroadcast(intent);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        public void exit() {
            mPause = true;
        }
    }

    class SendThread extends Thread {
        private volatile boolean mPause = false;
        private String mSsid, mPasswd;
        SendThread(String ssid, String pass){
            mSsid = ssid;
            mPasswd = pass;
        }

        public void run(){
            DatagramPacket mDataPacket;
            InetAddress address;
            mPause = false;

            while (!mPause) {
                try {
                    byte[] data = "Data-no-use(NXP-Smart-link)".getBytes();
                    for (int i = 0; i < 20; i ++) {
                        address = InetAddress.getByName("224.78.88.80");
                        mDataPacket = new DatagramPacket(data, data.length, address, 15000);
                        mSendSocket.send(mDataPacket);
                    }

                    for (int i = 0; i < 5; i ++) {
                        String ip = "224.94.255." + Integer.toString(mPasswd.length());
                        address = InetAddress.getByName(ip);
                        mDataPacket = new DatagramPacket(data, data.length, address, 15000);
                        mSendSocket.send(mDataPacket);
                    }

                    for (int i = 0; i < mPasswd.length(); i ++) {
                        String ip = "224.94." + Integer.toString(i) + "." + Integer.toString(mPasswd.charAt(i));
                        address = InetAddress.getByName(ip);
                        mDataPacket = new DatagramPacket(data, data.length, address, 15000);
                        mSendSocket.send(mDataPacket);
                    }
                    sleep(1000);
                } catch (InterruptedException e) {
                    break;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        public void exit() {
            mPause = true;
        }

    }
}
