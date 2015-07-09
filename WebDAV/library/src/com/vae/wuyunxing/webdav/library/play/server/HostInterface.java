package com.vae.wuyunxing.webdav.library.play.server;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Vector;

public class HostInterface {

	private static final String TAG = HostInterface.class.getSimpleName();

	public static boolean USE_LOOP_BACK_ADDR = false;
	public static boolean USE_ONLY_IPV4_ADDR = false;
	public static boolean USE_ONLY_IPV6_ADDR = false;

	private final static int IPV4_BIT_MASK  = 0x0001;
	private final static int IPV6_BIT_MASK  = 0x0010;
	private final static int LOCAL_BIT_MASK = 0x0100;

	private static boolean isUsableAddress(InetAddress addr) {
		if (!USE_LOOP_BACK_ADDR) {
			if (addr.isLoopbackAddress() || addr.isLinkLocalAddress()) {
				return false;
			}
		}

		if (USE_ONLY_IPV4_ADDR) {
			if (addr instanceof Inet6Address) {
				return false;
			}
		}
		if (USE_ONLY_IPV6_ADDR) {
			if (addr instanceof Inet4Address) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Obtain the numbers of InetAddress of host.
	 */
	public static int getNHostAddresses() {
		int nHostAddrs = 0;
		try {
			Enumeration nis = NetworkInterface.getNetworkInterfaces();
			while (nis.hasMoreElements()) {
				NetworkInterface ni = (NetworkInterface) nis.nextElement();
				Enumeration<InetAddress> addrs = ni.getInetAddresses();
				while (addrs.hasMoreElements()) {
					InetAddress addr = addrs.nextElement();

					if (!isUsableAddress(addr)) {
						continue;
					}
					nHostAddrs++;
				}
			}
		} catch (Exception e) {
			Log.e(TAG, e.getMessage());
		}
		return nHostAddrs;
	}

	public static InetAddress[] getInetAddress(int ipfilter, String[] interfaces) {
		Enumeration nis;
		if (interfaces != null) {
			Vector<NetworkInterface> iflist = new Vector<NetworkInterface>();
			for (String interf : interfaces) {
				NetworkInterface ni;
				try {
					ni = NetworkInterface.getByName(interf);
				} catch (SocketException e) {
					continue;
				}
				if (ni != null) {
					iflist.add(ni);
				}

			}
			nis = iflist.elements();
		} else {
			try {
				nis = NetworkInterface.getNetworkInterfaces();
			} catch (SocketException e) {
				return null;
			}
		}
		ArrayList<InetAddress> addresses = new ArrayList<InetAddress>();
		while (nis.hasMoreElements()) {
			NetworkInterface ni = (NetworkInterface) nis.nextElement();
			Enumeration addrs = ni.getInetAddresses();
			while (addrs.hasMoreElements()) {
				InetAddress addr = (InetAddress) addrs.nextElement();
				if (((ipfilter & LOCAL_BIT_MASK) == 0) && addr.isLoopbackAddress()) {
					continue;
				}

				if (((ipfilter & IPV4_BIT_MASK) != 0) && addr instanceof Inet4Address) {
					addresses.add(addr);
				} else if (((ipfilter & IPV6_BIT_MASK) != 0) && addr instanceof Inet6Address) {
					addresses.add(addr);
				}
			}
		}
		return addresses.toArray(new InetAddress[addresses.size()]);
	}

	public static String getHostAddress(int n) {
		int hostAddrCnt = 0;
		try {
			Enumeration<NetworkInterface> nis = NetworkInterface.getNetworkInterfaces();
			while (nis.hasMoreElements()) {
				NetworkInterface ni = nis.nextElement();
				Enumeration<InetAddress> addrs = ni.getInetAddresses();
				while (addrs.hasMoreElements()) {
					InetAddress addr = addrs.nextElement();
					if (!isUsableAddress(addr)) {
						continue;
					}
					if (hostAddrCnt < n) {
						hostAddrCnt++;
						continue;
					}
					return addr.getHostAddress();
				}
			}
		} catch (Exception ignored) {
		}
		return "";
	}

	public static String getLocalIpAddress(Context context) {
		WifiManager wifimanager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		WifiInfo wifiinfo = wifimanager.getConnectionInfo();
		try {
			int i = wifiinfo.getIpAddress();
			return (i & 0xff) + "." + ((i >> 8) & 0xff) + "." + ((i >> 16) & 0xff) + "." + ((i >> 24) & 0xff);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}
}
