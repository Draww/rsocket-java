package io.reactivesocket.aeron.server;

import io.reactivesocket.ConnectionSetupHandler;
import io.reactivesocket.Frame;
import io.reactivesocket.LeaseGovernor;
import io.reactivesocket.ReactiveSocket;
import io.reactivesocket.aeron.internal.Loggable;
import io.reactivesocket.aeron.internal.MessageType;
import io.reactivesocket.rx.Observer;
import uk.co.real_logic.aeron.Aeron;
import uk.co.real_logic.aeron.FragmentAssembler;
import uk.co.real_logic.aeron.Image;
import uk.co.real_logic.aeron.Publication;
import uk.co.real_logic.aeron.Subscription;
import uk.co.real_logic.aeron.logbuffer.Header;
import uk.co.real_logic.agrona.BitUtil;
import uk.co.real_logic.agrona.DirectBuffer;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static io.reactivesocket.aeron.internal.Constants.CLIENT_STREAM_ID;
import static io.reactivesocket.aeron.internal.Constants.SERVER_IDLE_STRATEGY;
import static io.reactivesocket.aeron.internal.Constants.SERVER_STREAM_ID;

public class ReactiveSocketAeronServer implements AutoCloseable, Loggable {
    private static volatile Aeron aeron;

    private static volatile boolean running = true;

    private static volatile boolean pollingStarted = false;

    private static final CopyOnWriteArrayList<ReactiveSocketAeronServer> servers = new CopyOnWriteArrayList<>();

    private final int port;

    private static final ConcurrentHashMap<Integer, AeronServerDuplexConnection> connections = new ConcurrentHashMap<>();

    private static final ConcurrentHashMap<Integer, ReactiveSocket> sockets = new ConcurrentHashMap<>();

    private final Subscription subscription;

    private final ConnectionSetupHandler connectionSetupHandler;

    private final LeaseGovernor leaseGovernor;

    private final CountDownLatch shutdownLatch;

    private final FragmentAssembler fragmentAssembler;

    private ReactiveSocketAeronServer(String host, int port, ConnectionSetupHandler connectionSetupHandler, LeaseGovernor leaseGovernor) {

        this.port = port;
        this.connectionSetupHandler = connectionSetupHandler;
        this.leaseGovernor = leaseGovernor;
        this.shutdownLatch = new CountDownLatch(1);

        if (aeron == null) {
            synchronized (shutdownLatch) {
                if (aeron == null) {
                    final Aeron.Context ctx = new Aeron.Context();
                    ctx.newImageHandler(this::newImageHandler);
                    ctx.errorHandler(t -> error(t.getMessage(), t));

                    aeron = Aeron.connect(ctx);
                }
            }
        }

        final String serverChannel =  "udp://" + host + ":" + port;
        info("Start new ReactiveSocketAeronServer on channel {}", serverChannel);
        subscription = aeron.addSubscription(serverChannel, SERVER_STREAM_ID);

        fragmentAssembler = new FragmentAssembler(this::fragmentHandler);

        servers.add(this);

        poll();
    }

    public static ReactiveSocketAeronServer create(String host, int port, ConnectionSetupHandler connectionSetupHandler, LeaseGovernor leaseGovernor) {
        return new ReactiveSocketAeronServer(host, port, connectionSetupHandler, leaseGovernor);
    }

    public static ReactiveSocketAeronServer create(int port, ConnectionSetupHandler connectionSetupHandler, LeaseGovernor leaseGovernor) {
        return create("127.0.0.1", port, connectionSetupHandler, leaseGovernor);
    }

    public static ReactiveSocketAeronServer create(ConnectionSetupHandler connectionSetupHandler, LeaseGovernor leaseGovernor) {
        return create(39790, connectionSetupHandler, leaseGovernor);
    }

    public static ReactiveSocketAeronServer create(String host, int port, ConnectionSetupHandler connectionSetupHandler) {
        return new ReactiveSocketAeronServer(host, port, connectionSetupHandler, LeaseGovernor.NULL_LEASE_GOVERNOR);
    }

    public static ReactiveSocketAeronServer create(int port, ConnectionSetupHandler connectionSetupHandler) {
        return create("127.0.0.1", port, connectionSetupHandler, LeaseGovernor.NULL_LEASE_GOVERNOR);
    }

    public static ReactiveSocketAeronServer create(ConnectionSetupHandler connectionSetupHandler) {
        return create(39790, connectionSetupHandler, LeaseGovernor.NULL_LEASE_GOVERNOR);
    }

    synchronized void poll() {
        if (pollingStarted) {
            return;
        }

        Thread dutyThread = new Thread(() -> {
            while (running) {
                int poll = 0;
                for (ReactiveSocketAeronServer server : servers) {
                    try {
                        poll += server.subscription.poll(server.fragmentAssembler, Integer.MAX_VALUE);
                    } catch (Throwable t) {
                        t.printStackTrace();
                    }
                }
                SERVER_IDLE_STRATEGY.idle(poll);

            }
            shutdownLatch.countDown();
        });
        dutyThread.setName("reactive-socket-aeron-server");
        dutyThread.setDaemon(true);
        dutyThread.start();
        pollingStarted = true;
    }

    void fragmentHandler(DirectBuffer buffer, int offset, int length, Header header) {
            final int sessionId = header.sessionId();

            short messageTypeInt = buffer.getShort(offset + BitUtil.SIZE_OF_SHORT);
            MessageType type = MessageType.from(messageTypeInt);

            if (MessageType.FRAME == type) {
                AeronServerDuplexConnection connection = connections.get(sessionId);
                if (connection != null) {
                    List<? extends Observer<Frame>> subscribers = connection.getSubscriber();
                    final Frame frame = Frame.from(buffer, BitUtil.SIZE_OF_INT + offset, length);
                    subscribers.forEach(s -> {
                        try {
                            s.onNext(frame);
                        } catch (Throwable t) {
                            s.onError(t);
                        }
                    });
                }
            } else if (MessageType.ESTABLISH_CONNECTION_REQUEST == type) {
                final long start = System.nanoTime();
                AeronServerDuplexConnection connection = null;
                debug("Looking a connection to ack establish connection for session id => {}", sessionId);
                while (connection == null) {
                    final long current = System.nanoTime();

                    if (current - start > TimeUnit.SECONDS.toNanos(30)) {
                        throw new RuntimeException("unable to find connection to ack establish connection for session id => " + sessionId);
                    }

                    connection = connections.get(sessionId);
                }
                debug("Found a connection to ack establish connection for session id => {}", sessionId);
                connection.ackEstablishConnection(sessionId);
            }

    }

    void newImageHandler(Image image, String channel, int streamId, int sessionId, long joiningPosition, String sourceIdentity) {
        if (SERVER_STREAM_ID == streamId) {
            debug("Handling new image for session id => {} and stream id => {}", streamId, sessionId);
            final AeronServerDuplexConnection connection = connections.computeIfAbsent(sessionId, (_s) -> {
                final String responseChannel = "udp://" + sourceIdentity.substring(0, sourceIdentity.indexOf(':')) + ":" + port;
                Publication publication = aeron.addPublication(responseChannel, CLIENT_STREAM_ID);
                debug("Creating new connection for responseChannel => {}, streamId => {}, and sessionId => {}", responseChannel, streamId, sessionId);
                return new AeronServerDuplexConnection(publication);
            });
            debug("Accepting ReactiveSocket connection");
            ReactiveSocket socket = ReactiveSocket.fromServerConnection(
                connection,
                connectionSetupHandler,
                leaseGovernor,
                error -> error(error.getMessage(), error));

            sockets.put(sessionId, socket);

            socket.startAndWait();
        } else {
            debug("Unsupported stream id {}", streamId);
        }
    }

    @Override
    public void close() throws Exception {
        /*
        running = false;

        shutdownLatch.await(30, TimeUnit.SECONDS);

        aeron.close();

        for (AeronServerDuplexConnection connection : connections.values()) {
            connection.close();
        }

        for (ReactiveSocket reactiveSocket : sockets.values()) {
            reactiveSocket.close();
        } */
    }

}
