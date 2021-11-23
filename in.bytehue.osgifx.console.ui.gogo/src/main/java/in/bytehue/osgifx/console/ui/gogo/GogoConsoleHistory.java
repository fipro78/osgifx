package in.bytehue.osgifx.console.ui.gogo;

import java.util.List;

import org.osgi.service.component.annotations.Component;

import com.google.common.collect.Lists;

@Component(service = GogoConsoleHistory.class)
public final class GogoConsoleHistory {

    private final List<String> history = Lists.newArrayList();

    public synchronized void add(final String command) {
        if (history.size() == 20) {
            // evicting last element
            history.remove(history.size() - 1);
        }
        history.add(command);
    }

    public synchronized void clear() {
        history.clear();
    }

    public synchronized int size() {
        return history.size();
    }

    public synchronized String get(final int index) {
        if (history.isEmpty()) {
            return "";
        }
        return history.get(index);
    }

}
