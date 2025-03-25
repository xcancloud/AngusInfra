package cloud.xcan.angus.spec.utils;

import static cloud.xcan.angus.spec.experimental.BizConstant.DEFAULT_AGENT_PORT;
import static cloud.xcan.angus.spec.experimental.BizConstant.DEFAULT_EXCHANGE_SERVER_PORT;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isEmpty;
import static java.net.InetAddress.getLocalHost;
import static java.util.Objects.nonNull;

import cloud.xcan.angus.api.pojo.auth.SimpleHttpAuth;
import cloud.xcan.angus.spec.http.HttpUrlConnectionSender;
import cloud.xcan.angus.spec.http.HttpSender.Request;
import cloud.xcan.angus.spec.http.HttpSender.Response;
import java.io.IOException;
import java.io.InputStream;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class NetworkUtils {

  /**
   * IP address
   */
  public static final Pattern REGEX_IPV4 = Pattern
      .compile("^(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})$");

  public static String getHostName() {
    try {
      InetAddress localAddress = InetAddress.getLocalHost();
      return localAddress.getHostName();
    } catch (UnknownHostException e) {
      // NOOP
    }
    return null;
  }

  public static boolean ping(String ip) {
    return ping(ip, 3000);
  }

  public static boolean ping(String ip, int timeout) {
    boolean status = false;
    try {
      status = InetAddress.getByName(ip).isReachable(timeout);
    } catch (IOException e) {
      // NOOP
    }
    return status;
  }

  public static boolean isLocalPortInUse(int port) {
    try {
      // ServerSocket try to open a LOCAL port
      // new ServerSocket(port).close() will bind 0.0.0.0:3000 and always successful
      new ServerSocket(port, 50, getLocalHost()).close();
      // local port can be opened, it's available
      return false;
    } catch (IOException e) {
      // local port cannot be opened, it's in use
      return true;
    }
  }

  public static boolean isPortInUse(int port, InetAddress address) {
    try {
      // ServerSocket try to open a LOCAL port
      new ServerSocket(port, 50, address).close();
      // local port can be opened, it's available
      return false;
    } catch (IOException e) {
      // local port cannot be opened, it's in use
      return true;
    }
  }

  public static boolean isRemotePortInUse(String hostName, int portNumber) {
    try {
      // Socket try to open a REMOTE port
      new Socket(hostName, portNumber).close();
      // remote port can be opened, this is a listening port on remote machine
      // this port is in use on the remote machine !
      return true;
    } catch (Exception e) {
      // remote port is closed, nothing is running on
      return false;
    }
  }

  public static boolean isIpAddress(String ipAddress) {
    return REGEX_IPV4.matcher(ipAddress).matches();
  }

  public static String domainToIp(String domain) {
    try {
      if (isEmpty(domain) || isIpAddress(domain)) {
        return domain;
      }
      InetAddress address = InetAddress.getByName(domain);
      return address.getHostAddress();
    } catch (UnknownHostException e) {
      e.printStackTrace();
    }
    return domain;
  }

  public static String formatPossibleIpv6Address(String address) {
    if (address == null) {
      return null;
    } else if (!address.contains(":")) {
      return address;
    } else {
      return address.startsWith("[") && address.endsWith("]") ? address : "[" + address + "]";
    }
  }

  public static InetAddress parseIpv4Address(String addressString) throws IOException {
    String[] parts = addressString.split("\\.");
    if (parts.length != 4) {
      throw new IOException(String.format("Invalid IP address %s", addressString));
    } else {
      byte[] data = new byte[4];
      for (int i = 0; i < 4; ++i) {
        String part = parts[i];
        if (part.length() == 0 || part.charAt(0) == '0' && part.length() > 1) {
          throw new IOException(String.format("Invalid IP address %s", addressString));
        }
        data[i] = (byte) Integer.parseInt(part);
      }
      return InetAddress.getByAddress(data);
    }
  }

  public static InetAddress parseIpv6Address(String addressString) throws IOException {
    boolean startsWithColon = addressString.startsWith(":");
    if (startsWithColon && !addressString.startsWith("::")) {
      throw new IOException(String.format("Invalid IP address %s", addressString));
    } else {
      String[] parts = (startsWithColon ? addressString.substring(1) : addressString).split(":");
      byte[] data = new byte[16];
      int partOffset = 0;
      boolean seenEmpty = false;
      if (parts.length > 8) {
        throw new IOException(String.format("Invalid IP address %s", addressString));
      } else {
        for (int i = 0; i < parts.length; ++i) {
          String part = parts[i];
          if (part.length() > 4) {
            throw new IOException(String.format("Invalid IP address %s", addressString));
          }

          int off;
          if (part.isEmpty()) {
            if (seenEmpty) {
              throw new IOException(String.format("Invalid IP address %s", addressString));
            }

            seenEmpty = true;
            off = 8 - parts.length;
            if (off < 0) {
              throw new IOException(String.format("Invalid IP address %s", addressString));
            }

            partOffset = off * 2;
          } else {
            if (part.length() > 1 && part.charAt(0) == '0') {
              throw new IOException(String.format("Invalid IP address %s", addressString));
            }

            off = Integer.parseInt(part, 16);
            data[i * 2 + partOffset] = (byte) (off >> 8);
            data[i * 2 + partOffset + 1] = (byte) off;
          }
        }

        if (parts.length < 8 && !seenEmpty) {
          throw new IOException(String.format("Invalid IP address %s", addressString));
        } else {
          return InetAddress.getByAddress(data);
        }
      }
    }
  }

  public static String getValidIpv4(String... default0) {
    List<String> addresses = getValidIpv4s();
    return isEmpty(addresses) ? (nonNull(default0) ? default0[0] : "127.0.0.1")
        : addresses.get(addresses.size() - 1);
  }

  public static List<String> getValidIpv4s() {
    List<InetAddress> addresses = getValidIpv4Addresses();
    return isEmpty(addresses) ? null : addresses.stream().map(InetAddress::getHostAddress)
        .collect(Collectors.toList());
  }

  public static InetAddress getValidIpv4Address() {
    List<InetAddress> addresses = getValidIpv4Addresses();
    return isEmpty(addresses) ? null : addresses.get(0);
  }

  public static List<InetAddress> getValidIpv4Addresses() {
    List<InetAddress> addresses = new ArrayList<>();
    try {
      Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
      while (networkInterfaces.hasMoreElements()) {
        NetworkInterface networkInterface = networkInterfaces.nextElement();
        if (networkInterface.isUp() && !networkInterface.isLoopback()) {
          Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
          while (inetAddresses.hasMoreElements()) {
            InetAddress inetAddress = inetAddresses.nextElement();
            if (!inetAddress.isLoopbackAddress()
                && inetAddress.getHostAddress().indexOf(':') == -1) {
              // System.out.println("网卡名称: " + networkInterface.getDisplayName()); -> 网卡名称: en0
              // System.out.println("网卡IP地址: " + inetAddress.getHostAddress()); -> 网卡IP地址: 192.168.1.4
              addresses.add(inetAddress);
            }
          }
        }
      }
    } catch (SocketException e) {
      // e.printStackTrace();
    }
    return isEmpty(addresses) ? null : addresses;
  }

  public static NetworkInterface getLocalNetworkInterface() {
    Enumeration<NetworkInterface> interfaces;
    try {
      interfaces = NetworkInterface.getNetworkInterfaces();
    } catch (SocketException e) {
      throw new RuntimeException("NetworkInterface not found", e);
    }
    while (interfaces.hasMoreElements()) {
      NetworkInterface networkInterface = interfaces.nextElement();
      Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
      while (addresses.hasMoreElements()) {
        InetAddress address = addresses.nextElement();
        if (address.isLoopbackAddress()) {
          continue;
        }
        if (address.getHostAddress().contains(":")) {
          continue;
        }
        if (address.isSiteLocalAddress()) {
          return networkInterface;
        }
      }
    }
    throw new RuntimeException("NetworkInterface not found");
  }

  public static String toObfuscatedString(InetAddress address) {
    if (address == null) {
      return null;
    } else {
      String s = address.getHostAddress();
      return address instanceof Inet4Address
          ? s.substring(0, s.lastIndexOf(".") + 1)
          : s.substring(0, s.indexOf(":", s.indexOf(":") + 1) + 1);
    }
  }

  public static String urlToString(String url, List<SimpleHttpAuth> auths) throws Throwable {
    Response response = Request.build(url, new HttpUrlConnectionSender()).withAuths(auths).send();
    return response.body();
  }

  public static InputStream urlToInputStream(String url, List<SimpleHttpAuth> auths)
      throws Throwable {
    Response response = Request.build(url, new HttpUrlConnectionSender()).withAuths(auths).send();
    return response.bodyIS();
  }

  public static byte[] urlToBytes(String url, List<SimpleHttpAuth> auths)
      throws Throwable {
    Response response = Request.build(url, new HttpUrlConnectionSender()).withAuths(auths).send();
    try (InputStream is = response.bodyIS()) {
      return is.readAllBytes();
    }
  }

  public static int getDefaultAgentPort() {
    String agentPort = System.getProperty("agentPort");
    return isEmpty(agentPort) ? DEFAULT_AGENT_PORT : Integer.parseInt(agentPort);
  }

  public static int getDefaultExchangeServerPort() {
    String agentPort = System.getProperty("exchangeServerPort");
    return isEmpty(agentPort) ? DEFAULT_EXCHANGE_SERVER_PORT : Integer.parseInt(agentPort);
  }
}
