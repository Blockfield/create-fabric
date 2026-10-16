package com.simibubi.create.content.kinetics.chainConveyor;

import java.util.UUID;

/**
 * Blockfield anim-sync hook (scoped).
 *
 * <p>Reports chain-conveyor hanging membership acceptances to an installed listener: enter at
 * {@code handleTTLPacket}, exit at {@code handleStopRidingPacket}, TTL expiry inside {@code
 * tick()}. No listener is installed by default and the local/global Catnip animation clock is
 * untouched, so with no listener every call site behaves exactly as before. Blockfield installs
 * its journal feed on the server only; the client render path never sees this class.
 */
public final class BlockfieldHangingHook {
    /** Server-side membership listener; all calls happen on the server thread. */
    public interface Listener {
        void hangingEntered(UUID player);

        void hangingExited(UUID player, boolean expired);
    }

    private static volatile Listener listener;

    private BlockfieldHangingHook() {}

    public static void install(Listener hook) {
        listener = hook;
    }

    public static void uninstall() {
        listener = null;
    }

    static void entered(UUID player) {
        Listener hook = listener;
        if (hook != null) hook.hangingEntered(player);
    }

    static void exited(UUID player, boolean expired) {
        Listener hook = listener;
        if (hook != null) hook.hangingExited(player, expired);
    }
}
