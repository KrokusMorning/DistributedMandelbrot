package client.view;

import client.model.Mandelbroter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * Used to store and handle user input.
 */
public class Input {

    private final String SEPARATOR = " ";
    private String[] userInput;
    private Mandelbroter mandelbroter;
    private List<String> servers;

    public Input(String userInput) {
        this.userInput = userInput.split(SEPARATOR);
        servers = new ArrayList<>();
    }

    /**
     * Creates a new mandelbrotDispatcher from user input.
     * @return boolean, true if correct user input false otherwise.
     */
    public boolean parseInput() {
        String[] params = userInput;
        if(params.length < 9)
            return false;
        this.mandelbroter = new Mandelbroter(Double.parseDouble(params[0]), Double.parseDouble(params[1]), Double.parseDouble(params[2]), Double.parseDouble(params[3]),
                Integer.parseInt(params[4]), Integer.parseInt(params[5]), Integer.parseInt(params[6]), Integer.parseInt(params[7]));
        servers.addAll(Arrays.asList(params).subList(8, params.length));
        return true;
    }

    public Mandelbroter getMandelbroter() {
        return mandelbroter;
    }

    public void setMandelbroter(Mandelbroter mandelbroter) {
        this.mandelbroter = mandelbroter;
    }

    public List getServers() {
        return servers;
    }

    public void setServers(List servers) {
        this.servers = servers;
    }
}
