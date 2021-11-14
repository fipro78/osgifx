package in.bytehue.osgifx.console.supervisor;

import in.bytehue.osgifx.console.agent.Agent;
import in.bytehue.osgifx.console.agent.dto.XEventDTO;
import in.bytehue.osgifx.console.agent.dto.XLogEntryDTO;

/**
 * A Supervisor handles the initiating side of a session with a remote agent.
 * The methods defined in this interface are intended to be called by the remote
 * agent, not the initiator. I.e. this is not the interface the initiator will
 * use to control the session.
 */
public interface Supervisor {

    /** The topic where an event will be sent after the agent gets connected */
    String AGENT_CONNECTED_EVENT_TOPIC = "fx/console/agent/connected";

    /** The topic where an event will be sent after the agent gets disconnected */
    String AGENT_DISCONNECTED_EVENT_TOPIC = "fx/console/agent/disconnected";

    /** System property comprising the host and port of the connected agent */
    String CONNECTED_AGENT = "osgi.fx.connected.agent";

    /**
     * Redirected standard output
     *
     * @param out the text that was redirected
     * @return ignored (to make sync)
     */
    boolean stdout(String out) throws Exception;

    /**
     * Redirected standard error.
     *
     * @param out the text that was redirected
     * @return ignored (to make sync)
     */
    boolean stderr(String out) throws Exception;

    /**
     * Return the contents of the file that has the given SHA-1. The initiator
     * of the connection should in general register the files it refers to in
     * the communication to the agent. The agent then calls this method to
     * retrieve the contents if it does not have it in its local cache.
     *
     * @param sha the SHA-1
     * @return the contents of that file or null if no such file exists.
     */
    byte[] getFile(String sha) throws Exception;

    /**
     * Connects to the specific host and port using the provided timeout in connection
     *
     * @param host the host name
     * @param port the port address
     * @param timeout the timeout in milliseconds
     *
     * @throws Exception if any issue occurs during connection
     */
    void connect(String host, int port, int timeout) throws Exception;

    /**
     * Callback method for OSGi Events
     */
    void onOSGiEvent(XEventDTO event);

    /**
     * Callback method for logs
     */
    void logged(XLogEntryDTO event);

    /**
     * Registers the specified listener to listen to the OSGi events from the remote machine
     *
     * @param eventListener the event listener to register
     */
    void addOSGiEventListener(EventListener eventListener);

    /**
     * Deregisters previously registered listener
     *
     * @param eventListener the listener to deregister
     */
    void removeOSGiEventListener(EventListener eventListener);

    /**
     * Registers the specified listener to receive to the OSGi logs from the remote machine
     *
     * @param logEntryListener the log entry listener to register
     */
    void addOSGiLogListener(LogEntryListener logEntryListener);

    /**
     * Deregisters previously registered log entry listener
     *
     * @param logEntryListener the log consumer to deregister
     */
    void removeOSGiLogListener(LogEntryListener logEntryListener);

    /**
     * Returns the associated agent
     *
     * @return the agent
     */
    Agent getAgent();
}
