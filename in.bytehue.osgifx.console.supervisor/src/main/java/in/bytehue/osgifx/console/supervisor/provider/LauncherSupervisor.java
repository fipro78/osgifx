package in.bytehue.osgifx.console.supervisor.provider;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.osgi.service.component.annotations.Component;

import aQute.bnd.util.dto.DTO;
import in.bytehue.osgifx.console.agent.Agent;
import in.bytehue.osgifx.console.agent.dto.XEventDTO;
import in.bytehue.osgifx.console.agent.dto.XLogEntryDTO;
import in.bytehue.osgifx.console.supervisor.EventListener;
import in.bytehue.osgifx.console.supervisor.LogEntryListener;
import in.bytehue.osgifx.console.supervisor.Supervisor;

@Component
public final class LauncherSupervisor extends AgentSupervisor<Supervisor, Agent> implements Supervisor {

    private Appendable stdout;
    private Appendable stderr;
    private Thread     stdin;
    private int        shell = -100; // always invalid so we update it

    private final List<EventListener>    eventListeners    = new CopyOnWriteArrayList<>();
    private final List<LogEntryListener> logEntryListeners = new CopyOnWriteArrayList<>();

    static class Info extends DTO {
        public String sha;
        public long   lastModified;
    }

    @Override
    public boolean stdout(final String out) throws Exception {
        if (stdout != null) {
            stdout.append(out);
            return true;
        }
        return false;
    }

    @Override
    public boolean stderr(final String out) throws Exception {
        if (stderr != null) {
            stderr.append(out);
            return true;
        }
        return false;
    }

    public void setStdout(final Appendable out) throws Exception {
        stdout = out;
    }

    public void setStderr(final Appendable err) throws Exception {
        stderr = err;
    }

    public void setStdin(final InputStream in) throws Exception {
        final InputStreamReader isr = new InputStreamReader(in);
        stdin = new Thread("stdin") {
            @Override
            public void run() {
                final StringBuilder sb = new StringBuilder();

                while (!isInterrupted()) {
                    try {
                        if (isr.ready()) {
                            final int read = isr.read();
                            if (read < 0) {
                                return;
                            }
                            sb.append((char) read);

                        } else if (sb.length() == 0) {
                            sleep(100);
                        } else {
                            getAgent().stdin(sb.toString());
                            sb.setLength(0);
                        }
                    } catch (final Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        stdin.start();
    }

    public void setStreams(final Appendable out, final Appendable err) throws Exception {
        setStdout(out);
        setStderr(err);
        getAgent().redirect(shell);
    }

    public void connect(final String host, final int port) throws Exception {
        super.connect(Agent.class, this, host, port);
    }

    /**
     * The shell port to use.
     * <ul>
     * <li>&lt;0 – Attach to a local Gogo CommandSession
     * <li>0 – Use the standard console
     * <li>else – Open a stream to that port
     * </ul>
     *
     * @param shellPort
     */
    public void setShell(final int shellPort) {
        shell = shellPort;
    }

    public int getExitCode() {
        return exitCode;
    }

    public void abort() throws Exception {
        if (isOpen()) {
            getAgent().abort();
        }
    }

    public void redirect(final int shell) throws Exception {
        if (this.shell != shell && isOpen()) {
            getAgent().redirect(shell);
            this.shell = shell;
        }
    }

    @Override
    public synchronized void onOSGiEvent(final XEventDTO event) {
        eventListeners.forEach(listener -> listener.onEvent(event));
    }

    @Override
    public void logged(final XLogEntryDTO logEvent) {
        logEntryListeners.forEach(listener -> listener.logged(logEvent));
    }

    @Override
    public synchronized void addOSGiEventListener(final EventListener eventListener) {
        if (eventListeners.contains(eventListener)) {
            return;
        }
        eventListeners.add(eventListener);
    }

    @Override
    public synchronized void removeOSGiEventListener(final EventListener eventListener) {
        eventListeners.remove(eventListener);
    }

    @Override
    public void addOSGiLogListener(final LogEntryListener logEntryListener) {
        if (logEntryListeners.contains(logEntryListener)) {
            return;
        }
        logEntryListeners.add(logEntryListener);
    }

    @Override
    public void removeOSGiLogListener(final LogEntryListener logEntryListener) {
        logEntryListeners.remove(logEntryListener);
    }

    @Override
    public void connect(final String host, final int port, final int timeout) throws Exception {
        super.connect(Agent.class, this, host, port, timeout);
    }
}
