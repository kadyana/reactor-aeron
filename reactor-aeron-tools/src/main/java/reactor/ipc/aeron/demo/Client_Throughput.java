package reactor.ipc.aeron.demo;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.ipc.aeron.client.AeronClient;

import java.nio.ByteBuffer;

/**
 * @author Anatoly Kadyshev
 */
public class Client_Throughput {

    private static final String HOST = "localhost";

    public static void main(String[] args) {
        AeronClient client = AeronClient.create("client", options -> {
            options.serverChannel("aeron:udp?endpoint=" + HOST + ":13000");
            options.clientChannel("aeron:udp?endpoint=" + HOST + ":12001");
        });

        ByteBuffer buffer = ByteBuffer.allocate(1024 * 3);

        client.newHandler((inbound, outbound) -> {
            outbound.send(Flux.create(sink -> {
                System.out.println("About to send");
                for (int i = 0; i < 1000 * 1024; i++) {
                    sink.next(buffer);
                }
                sink.complete();
                System.out.println("Send complete");
            })).then().subscribe(avoid -> {}, th ->
                    System.err.printf("Failed to send flux due to: %s\n", th));

            return Mono.never();
        }).block();

        System.out.println("main completed");
    }

}
