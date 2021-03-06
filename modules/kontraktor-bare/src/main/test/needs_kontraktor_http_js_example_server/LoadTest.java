package needs_kontraktor_http_js_example_server;

import org.nustaq.kontraktor.barebone.Callback;
import org.nustaq.kontraktor.barebone.ConnectionListener;
import org.nustaq.kontraktor.barebone.RemoteActor;
import org.nustaq.kontraktor.barebone.RemoteActorConnection;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by ruedi on 11/06/15.
 */
public class LoadTest {

    static AtomicInteger connections = new AtomicInteger();

    int count = 0;
    AtomicLong lastBcast = new AtomicLong(System.currentTimeMillis());
    volatile RemoteActor session;
    volatile RemoteActorConnection act;

    public void run() {
        act = new RemoteActorConnection(
           new ConnectionListener() {
               @Override
               public void connectionClosed(String s) {
                   System.out.println("connection closed");
               }
           },
           false
        );

        act.connect("http://localhost:8080/api", true).then(new Callback<RemoteActor>() {
            @Override
            public void receive(RemoteActor facade, Object error) {
            System.out.println("CLIENTS:"+connections.incrementAndGet());

            facade.ask("login", "user", "password").then(new Callback() {
                @Override
                public void receive(Object res, Object error) {
                session = (RemoteActor) res;
                session.tell("subscribe", new Callback() {
                    @Override
                    public void receive(Object result, Object error) {
                    count++;
                    lastBcast.set(System.currentTimeMillis());
                    }
                });
                }
            });
            }
        });

    }

    public static void main(String[] args) throws InterruptedException {
        final ArrayList<LoadTest> tests = new ArrayList<>();
        for ( int i = 0; i < 10; i++ ) {
            for ( int n = 0; n < 40; n++ ) {
                LoadTest loadTest = new LoadTest();
                synchronized (tests) {
                    tests.add(loadTest);
                }
                loadTest.run();
            }
            Thread.sleep(6000);
        }
        while( true ) {
            synchronized (tests) {
                for (int i = 0; i < tests.size(); i++) {
                    LoadTest loadTest = tests.get(i);
                    long l = System.currentTimeMillis() - loadTest.lastBcast.get();
                    if ( l > 3000 ) {
                        System.out.println("client "+loadTest.session+" is late:"+l);
                        if ( l > 20_000 ) {
                            loadTest.act.close();
                            tests.remove(i);
                            i--;
                            System.out.println("clients remaining "+tests.size());
                        }
                    }
                }
            }
            Thread.sleep(2000);
        }
    }
}
