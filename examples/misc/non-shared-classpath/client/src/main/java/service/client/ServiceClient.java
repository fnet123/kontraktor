package service.client;

import org.nustaq.kontraktor.Actor;

import static service.common.MyProtocol.*;

import org.nustaq.kontraktor.remoting.tcp.TCPConnectable;
import service.common.MyServiceInterface;

/**
 * Created by ruedi on 19/05/15.
 *
 * Client is not made an actor for simplicity
 * (and to demonstrate actor-style is not required in orderto make use of kontraktor)
 *
 */
public class ServiceClient {

    public void run() {
        MyServiceInterface service = (MyServiceInterface)
            new TCPConnectable()
                .actorClass(MyServiceInterface.class)
                .host("localhost").port(6789)
                .connect( (connector, error) -> {
                    System.out.println("Disconnected from service .. exiting");
                    System.exit(0);
                }).await();

        service.addPerson( new Person("Hickory","Heinz",13,Sex.MALE) );
        service.addPerson( new Person("Blob","Jim",13,Sex.MALE));
        service.addPerson( new Person("Gerstenbroich-Huckerbühl","Mareike",17,Sex.MALE) );

        service.listPersons(null,null,13, (p,err) -> {
            if ( ! Actor.isErrorOrComplete(err) )
                System.out.println("found: "+p);
            else {
                System.out.println("query finished");
                System.exit(0);
            }
        });
    }

    public static void main( String a[] ) {
        new ServiceClient().run();
    }

}
