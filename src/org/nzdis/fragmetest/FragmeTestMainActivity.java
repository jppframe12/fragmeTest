package org.nzdis.fragmetest;

import android.net.wifi.WifiManager;

import android.net.wifi.WifiManager.MulticastLock;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;

import org.nzdis.fragme.ControlCenter;
import org.jgroups.Address;
import org.jgroups.ChannelClosedException;
import org.jgroups.ChannelException;
import org.jgroups.ChannelNotConnectedException;
import org.jgroups.JChannel;
import org.jgroups.MembershipListener;
import org.jgroups.Message;
import org.jgroups.MessageListener;
import org.jgroups.View;
import org.jgroups.blocks.PullPushAdapter;
import org.jgroups.stack.IpAddress;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.Vector;
import java.util.Observer;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class FragmeTestMainActivity extends Activity implements MessageListener,
		MembershipListener {

	// private HelloTest ht;
	private int peerNumber;
	private TextView textview;
	private String localIpAddress;
	private EditText textedit;
	private JChannel channel = null;
	private PullPushAdapter adapter;
	private Address lip;
	private TextView view;
	private MulticastLock mcLock;
	private boolean running=true;
	private static String props = "UDP(mcast_addr=224.0.0.0;mcast_port=7500;ip_ttl=32;"
			+ "mcast_send_buf_size=150000;mcast_recv_buf_size=80000):"
			
			+ "PING(timeout=2000;num_initial_members=3):"
			+ "FD_SOCK:"
			+ "VERIFY_SUSPECT(timeout=1000):"
			+ "pbcast.NAKACK(gc_lag=50;retransmit_timeout=300,600,1200,2400,4800):"
			+ "UNICAST(timeout=5000):"
			+ "pbcast.STABLE(desired_avg_gossip=20000):"
			+ "FRAG(frag_size=8096;down_thread=false;up_thread=false):"
			+ "CAUSAL:"
			+ "pbcast.GMS(join_timeout=5000;join_retry_timeout=2000;"
			+ "shun=false;print_local_addr=true)";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_fragme_test_main);

		textview = (TextView) findViewById(R.id.firstTextView);
		textedit = (EditText) findViewById(R.id.editText1);
		view = (TextView) findViewById(R.id.secondTextView);

		if (channel == null) {
			initializeJGroups();
		}

		// Vector shareObjects= ControlCenter.getAllObjects();

		// if (shareObjects.size() == 0) {

		// this.ht = (HelloTest) ControlCenter
		// .createNewObject(HelloTest.class);
		// Log.e("hello", "Create new hellotest:");

		// Log.e("hello","first player");

		// } else {
		// this.ht = (HelloTest) shareObjects.get(0);
		// Log.e("hello", "Joining the existing game:");

		// Log.e("hello", "second player");

		// }

	}

	private void initializeJGroups() {
		//System.setProperty("java.net.preferIPv4Stack", "true");
		//System.setProperty("java.net.preferIPv6Stack", "true");
		//System.setProperty("java.net.preferIPv6Addresses", "true");
		//System.setProperty("JGroups.bind_addr", "wlan0");
		System.setProperty("JGroups.bind_addr", "IPV4");
		//System.setProperty("JGroups.bind_addr", "127.0.0.1");
		
		// check wifi available and unlock multicast
		WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		if (wifi != null) {
			mcLock = wifi.createMulticastLock("mylock");
			mcLock.acquire();
			Log.e("hello", "unlock multicastlock");

			// get all network interface
			try {
				for (Enumeration<NetworkInterface> en = NetworkInterface
						.getNetworkInterfaces(); en.hasMoreElements();) {
					NetworkInterface intf = en.nextElement();
					System.out.println("network interface:"+intf.getDisplayName());
					for (Enumeration<InetAddress> enumIpAddr = intf
							.getInetAddresses(); enumIpAddr.hasMoreElements();) {
						InetAddress inetAddress = enumIpAddr.nextElement();
						System.out.println("interface address:"+inetAddress.getHostAddress());
						if (!inetAddress.isLoopbackAddress()) {
							System.out.println("local all interface address:"
									+ inetAddress.getHostAddress().toString());
							if (!inetAddress.getHostAddress().toString()
									.contains(":")) {
								localIpAddress = inetAddress.getHostAddress()
										.toString();
								System.out.println("local ip4 address:"
										+ inetAddress.getHostAddress()
												.toString() + "=="
										+ localIpAddress + " name == "
										+ inetAddress.getHostName());
							}
						}
					}
				}
			}
			catch (Exception ex) {
			}

			// set up connection with jgroups
			try {
				channel = new JChannel(props);
				channel.setOpt(channel.LOCAL, new Boolean(false));
				channel.connect("hello");
				PullPushAdapter adatper = new PullPushAdapter(channel, this,
						this);

				// get IP address from the Channel Address and get its IP
				// address using IpAddress.

				lip = channel.getLocalAddress();
				String ipstr = lip.toString().substring(0,
						lip.toString().indexOf(":"));
				String port = lip.toString().substring(
						lip.toString().indexOf(":") + 1,
						lip.toString().length());
				
				sendMessage();

			} catch (ChannelException ce) {
				System.out.println(ce.getMessage());

			}

			// set up connection with Fragme
			
			  /*ControlCenter.setUpConnections("Hello", localIpAddress); Address
			  myaddr = ControlCenter.getMyAddress();
			  System.out.println("connection at: peer address:" +
			  myaddr.toString() + "==" + localIpAddress);
			  Log.e("establish connection", "group hello");
			  textedit.setText("connection at: peer address:" +
			  myaddr.toString() + "==" + localIpAddress);
			  
			  peerNumber = ControlCenter.getNoOfPeers();
			  System.out.println("peernumber: "+peerNumber);
			  
			  if (peerNumber == 0) { 
				  Log.e("hello", "first member");
			  //Toast.makeText(this, "first member", Toast.LENGTH_LONG).show();
			  textview.setText("first memeber"); 
			  } else {
				  Log.e("hello", "join the group"); 
			  Toast.makeText(this, "join the group",
			  Toast.LENGTH_LONG).show(); 
			  textview.setText("Join the group");
			  
			  }*/
			 

		} else {
			Log.e("hello", "no wifi");
			textview.setText("no wifi");
		}
	}

	private void sendMessage() {
		
		new Thread(new Runnable(){
			
			public void run() {
				while(running){
				// TODO Auto-generated method stub
					if (channel != null) {
						try {
							channel.send(new Message(null, null, "Hello world"));
							System.out.println("Send message hello world.");// +"to:"+revaddr.getIpAddress().toString()+
							// ":"+revaddr.getPort()+"   from: "+sendaddr.getIpAddress().toString()+":"+sendaddr.getPort());
							//textview.setText("Send message Hello World, from local address: "
									//+ channel.getLocalAddress());
						} catch (ChannelNotConnectedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (ChannelClosedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
	
					}
					try {
						Thread.sleep(5000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					//p++;
				}
			}
			
		}).start();
		
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (channel != null) {
			running=false;
			channel.disconnect();
			channel.close();
			if (mcLock.isHeld()) {
				mcLock.release();
			}
		}

		textview.setText("activity destroyed");
		Toast.makeText(this, "activity destroyed", Toast.LENGTH_LONG).show();

	}

	public void viewAccepted(View new_view) {
		// TODO Auto-generated method stub
		System.out.println("received new view from " + new_view);
		view.setText("Received new view: " + new_view.getMembers().toString()
				+ "\n");

	}

	public void suspect(Address suspected_mbr) {
		// TODO Auto-generated method stub

	}

	public void block() {
		// TODO Auto-generated method stub

	}

	public void receive(Message msg) {
		// TODO Auto-generated method stub
		System.out.println("Received msg from " + msg.getSrc() + ": "
				+ msg.getObject());
		textedit.setText("Received msg from " + msg.getSrc() + ": "
				+ msg.getObject().toString());

	}

	public byte[] getState() {
		// TODO Auto-generated method stub
		return null;
	}

	public void setState(byte[] state) {
		// TODO Auto-generated method stub

	}

}


