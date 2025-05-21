package cloud.xcan.angus.remote.client;

import feign.Client;
import feign.Contract;
import feign.Feign;
import feign.RequestInterceptor;
import feign.codec.Decoder;
import feign.codec.Encoder;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class FeignRemoteFactory {

  private final Client client;
  private final Encoder encoder;
  private final Decoder decoder;
  private final Contract contract;
  private final RequestInterceptor requestInterceptor;

  private final Map<String, DynamicFeignClient> clients = new ConcurrentHashMap<>();

  public FeignRemoteFactory(Client client, Encoder encoder, Decoder decoder, Contract contract,
      RequestInterceptor requestInterceptor) {
    this.client = client;
    this.encoder = encoder;
    this.decoder = decoder;
    this.contract = contract;
    this.requestInterceptor = requestInterceptor;
  }

  public DynamicFeignClient dynamicClient(String url) {
    if (clients.containsKey(url)) {
      return clients.get(url);
    }
    DynamicFeignClient dynamicFeignClient = Feign.builder().client(client)
        .encoder(encoder).decoder(decoder).contract(contract)
        .requestInterceptor(requestInterceptor)
        .target(DynamicFeignClient.class, url);
    clients.put(url, dynamicFeignClient);
    return dynamicFeignClient;
  }
}
