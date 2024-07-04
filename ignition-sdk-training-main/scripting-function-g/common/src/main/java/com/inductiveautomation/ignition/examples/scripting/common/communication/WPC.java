package com.inductiveautomation.ignition.examples.scripting.common.communication;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.*;
import java.util.concurrent.*;


/**********************************************************************************************************************************************************************************************************
 * This class makes it possible to communicate with an external process running locally in same Windows OS.                                                                                               *
 * The communication is established through two named pipes, one for writing and other one for reading                                                                                                    *
 * The named pipes are not created here, rather this class only use them to issue requests.                                                                                                               *
 * The {@code WindowsPipeCommunicator.sendRequest(String req, boolean timeOn, int maxTimeInSec)} sends a request and wait for the response.                                                               *
 * If timerOn is true then the method waits until the maximum waiting time is up and returns an error message if no pipes available, otherwise (!timerOn) the method waits until pipes are available.     *
 * This method is synchronized                                                                                                                                                                            *
 * @author Mehdi Haidari                                                                                                                                                                                  *
 *                                                                                                                                                                             *
 * ********************************************************************************************************************************************************************************************************/
public class WPC {


    /**********************************************************************************************************************************************************
     * @param req which is a String, indicates the desired data                                                                                               *
     * @param timerOn is a boolean indicating whether this method should return after maximum waiting time is up                                              *
     * @param maxTimeInSec is an integer indicating the maximum waiting time for pipe to be available                                                         *
     * @return String response to the requestedData, if the maximum waiting time has been reached and timer was on this method return a time-out message      *
     * @throws IllegalArgumentException if req == null || req.isEmpty || req.isBlank                                                                          *
     * ********************************************************************************************************************************************************/
    public static synchronized String sendRequestByPipe(String req, boolean timerOn, int maxTimeInSec) {
        // the input should be valid
        if (req == null ||req.isBlank()) {
            throw new IllegalArgumentException("invalid input!");
        }
        //boolean to be used if maximum waiting time is up
        boolean[] timeOut = new boolean[1];
        // create single executor managing time-out task
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        Future<?> oneShotAction = new CompletableFuture<>();
        if(timerOn){
            oneShotAction = executor.schedule(()-> {
                timeOut[0] = true;
                executor.shutdown();
            }, maxTimeInSec, TimeUnit.SECONDS);
        }

        while (true){
            // check if max time is up
            if(timeOut[0]){
                return "waiting time is up! please run pipe server and try again!";
            }
            // check if pipes are available
            if(pipeExists("writePipe") && pipeExists("readPipe")){
                oneShotAction.cancel(true); // cancel the timer functionality
                executor.shutdown(); // shut down the timer thread
                break;
            }
        }
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get("\\\\.\\pipe\\readPipe"))) { // writing stream which writes to the reading-end of the server
            String cleanedRequest = req.replaceAll("\n", " ").replaceAll("\r", " ");

            writer.write(cleanedRequest); // write the request
            writer.newLine(); // indicate end of the line
            writer.flush(); // flush the pipe
            writer.close(); // close, not needed any more
            try( BufferedReader reader = Files.newBufferedReader(Paths.get("\\\\.\\pipe\\writePipe"))) { // reading stream which listens/reads from the writing-end of the server
                String response = reader.readLine();

                reader.close(); // close the reading end, not needed any more
                return response; // return the response for the actual thread
            }catch (IOException e){
                printError("failed to request data (while trying to get the response)", e.getMessage());
                return "failed to request the data";
            }
        } catch (IOException e) {
            printError("failed to request data", e.getMessage());
            return "failed to request the data";
        }
    }

    /**********************************************************************************
     * @param pipeName String, name of the pipe to check if exists                    *
     * @return  boolean, true if pipe exists in pipe directory otherwise false        *
     * ********************************************************************************/
    public static boolean pipeExists(String pipeName){
        boolean exists = false;
        FileSystem fs = FileSystems.getDefault();
        Path directory = fs.getPath("\\\\.\\pipe"); // Path to map where all pipes exist in Windows
        try (DirectoryStream<Path> paths = Files.newDirectoryStream(directory)) {
            for (Path p : paths) {
                if (p.toString().contains(pipeName)) {
                    exists = true;
                    break;
                }
            }
        } catch (IOException e) {
            printError("failed to check if pipe exists", e.getMessage());
            return false;
        }
        return exists;
    }


    // error printer used internally
    private  static void printError(String message, String err){
        System.out.println(
                "Err! "
                + message +"\n"
                + err
        );
    }
}



