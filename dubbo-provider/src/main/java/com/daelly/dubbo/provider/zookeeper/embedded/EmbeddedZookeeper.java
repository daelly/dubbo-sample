package com.daelly.dubbo.provider.zookeeper.embedded;

import org.apache.zookeeper.server.ServerConfig;
import org.apache.zookeeper.server.ZooKeeperServerMain;
import org.apache.zookeeper.server.quorum.QuorumPeerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.SmartLifecycle;
import org.springframework.util.ErrorHandler;
import org.springframework.util.SocketUtils;

import java.io.File;
import java.lang.reflect.Method;
import java.util.Properties;
import java.util.UUID;

public class EmbeddedZookeeper implements SmartLifecycle {

    private static final Logger logger = LoggerFactory.getLogger(EmbeddedZookeeper.class);

    private final int clientPort;

    private boolean autoStartup = true;

    private int phase = 0;

    private volatile Thread zkServerThread;

    private volatile ZooKeeperServerMain zkServer;

    private ErrorHandler errorHandler;

    private boolean daemon = true;

    public EmbeddedZookeeper() {
        clientPort = SocketUtils.findAvailableTcpPort();
    }

    public EmbeddedZookeeper(int clientPort) {
        this.clientPort = clientPort;
    }

    public EmbeddedZookeeper(int clientPort, boolean daemon) {
        this.clientPort = clientPort;
        this.daemon = daemon;
    }

    public int getClientPort() {
        return this.clientPort;
    }


    @Override
    public boolean isAutoStartup() {
        return this.autoStartup;
    }

    @Override
    public void stop(Runnable callback) {
        stop();
        callback.run();
    }

    @Override
    public int getPhase() {
        return this.phase;
    }

    @Override
    public synchronized void start() {
        if (zkServerThread == null) {
            zkServerThread = new Thread(new ServerRunnable(), "Zookeeper Server Starter");
            zkServerThread.setDaemon(daemon);
            zkServerThread.start();
        }
    }

    @Override
    public synchronized void stop() {
        if (zkServerThread != null) {
            try {
                Method shutdown = ZooKeeperServerMain.class.getDeclaredMethod("shutdown");
                shutdown.setAccessible(true);
                shutdown.invoke(zkServer);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            try {
                zkServerThread.join(5000);
                zkServerThread = null;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.warn("Interrupted while waiting for embedded Zookeeper to exit");
                zkServerThread = null;
            }
        }
    }

    @Override
    public boolean isRunning() {
        return zkServerThread != null;
    }

    private class ServerRunnable implements Runnable {

        @Override
        public void run() {
            try {
                Properties properties = new Properties();
                File file = new File(System.getProperty("java.io.tmpdir") + File.separator + UUID.randomUUID());
                file.deleteOnExit();
                properties.setProperty("dataDir", file.getAbsolutePath());
                properties.setProperty("clientPort", String.valueOf(clientPort));

                QuorumPeerConfig quorumPeerConfig = new QuorumPeerConfig();
                quorumPeerConfig.parseProperties(properties);

                zkServer = new ZooKeeperServerMain();
                ServerConfig config = new ServerConfig();
                config.readFrom(quorumPeerConfig);

                zkServer.runFromConfig(config);
            } catch (Exception e) {
                if (errorHandler != null) {
                    errorHandler.handleError(e);
                } else {
                    logger.error("Exception running embedded Zookeeper", e);
                }
            }
        }
    }
}
