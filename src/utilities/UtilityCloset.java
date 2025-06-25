package utilities;

import java.io.IOException;
import java.net.DatagramSocket;

public class UtilityCloset {

        // This class provides utility methods for other classes
 
        public static boolean freePort(int port){

        // This method checks whether a port is available

        try(DatagramSocket socket = new DatagramSocket(port)){
            socket.close();
            return true;
        } catch (IOException e){
            // port is already in use

            System.out.println(port + " is already in use.");

            return false;
        }
    }
}
