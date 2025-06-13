package de.bsommerfeld.antiac.click.impl;

import de.bsommerfeld.antiac.click.Click;
import de.bsommerfeld.antiac.click.Cps;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

public class DefaultCps implements Cps {

    private final LinkedList<Click> clicks = new LinkedList<>();

    @Override
    public void add(Click click) {
        clicks.add(click);
    }

    @Override
    public Click last() {
        return clicks.getLast();
    }

    @Override
    public int asInt() {
        return clicks.size();
    }

    @Override
    public boolean empty() {
        return clicks.isEmpty();
    }

    @Override
    public Collection<Click> all() {
    return Collections.unmodifiableCollection(clicks);
    }
}
