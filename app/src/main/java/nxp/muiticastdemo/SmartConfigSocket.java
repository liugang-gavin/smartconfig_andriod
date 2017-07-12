package nxp.muiticastdemo;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

/**
 * Created by b41466 on 17-7-7.
 */

public class SmartConfigSocket{
    /*socket for multicast*/
    private MulticastSocket mSocket;
    private DatagramPacket mDataPacket;
    private SocketThread mSocketThread;

    public SmartConfigSocket() {
        try {
            mSocket = new MulticastSocket();
            mSocket.setTimeToLive(4);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void startSendPacket(String ssid, String pass){
        mSocketThread = new SocketThread(ssid, pass);
        mSocketThread.start();
    }

    public void stopSendPacket(){
        mSocketThread.exit();
        mSocketThread = null;
    }

    class SocketThread extends Thread{
        private volatile boolean mPause = false;
        private String mSsid, mPasswd;
        SocketThread(String ssid, String pass){
            mSsid = ssid;
            mPasswd = pass;
        }

        public void run(){
            mPause = false;
            while (!mPause) {
                try {
                    byte[] data = "Data-no-use(NXP-Smart-link)".getBytes();
                    for (int i = 0; i < 20; i ++) {
                        InetAddress address = InetAddress.getByName("224.78.88.80");
                        mDataPacket = new DatagramPacket(data, data.length, address, 15000);
                        mSocket.send(mDataPacket);
                    }

                    for (int i = 0; i < 5; i ++) {
                        String ip = "224.94.255." + Integer.toString(mPasswd.length());
                        InetAddress address = InetAddress.getByName(ip);
                        mDataPacket = new DatagramPacket(data, data.length, address, 15000);
                        mSocket.send(mDataPacket);
                    }

                    for (int i = 0; i < mPasswd.length(); i ++) {
                        String ip = "224.94." + Integer.toString(i) + "." + Integer.toString(mPasswd.charAt(i));
                        System.out.println(ip);
                        InetAddress address = InetAddress.getByName(ip);
                        mDataPacket = new DatagramPacket(data, data.length, address, 15000);
                        mSocket.send(mDataPacket);
                    }
                    sleep(10);
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
