#include <systemc.h>

// Secure case study in which values of different trust levels are carried over
// one shared bus and handled with different timing behaviour.
//
// The dispatcher derives a trust level from trust_level_in, places the matching
// value on the bus, and wakes either the high- or the low-priority handler
// depending on priority_in. Each handler samples trust_level into a local flag
// and routes the bus value to critical_output or routine_output accordingly.
// The priority therefore selects *which* handler runs (and hence the timing),
// while the trust level selects *where* the value ends up; the two are
// independent, and every value reaches an output of its own level.
//
// The example needs no exploration refinement, but reports a false positive
// without information flow refinement: with trust_level unknown, both branches
// of both handlers look reachable for every bus value, so untrusted_in appears
// to flow to critical_output. It is the plain instance of Figure 3b of the
// paper -- the violating path is cut by a controlling node (if (trusted)) whose
// own data dependencies (trust_level) are enough to resolve it.
//
// The policy is a "separate" policy: each input must reach the output of its
// own level, and neither may reach the other's.

SC_MODULE(priorities) {
    sc_inout<int> data_bus;

    int trusted_in;
    int untrusted_in;
    int priority_in;    // 0 = low-priority, 1 = high-priority
    int trust_level_in; // <16 -> untrusted, >=16 -> trusted

    int critical_output;
    int routine_output;
    int status_log;

    sc_event high_prio_ev, low_prio_ev;
    int trust_level;

    void task_dispatcher() {
        while (true) {
            wait(1, SC_MS);

            if (trust_level_in < 16)
				trust_level = 0;
			else 
				trust_level = 1;

            if (trust_level == 0)
                data_bus.write(untrusted_in);
            else
                data_bus.write(trusted_in);

            if (priority_in == 0)
                low_prio_ev.notify(SC_ZERO_TIME);
            else
                high_prio_ev.notify(SC_ZERO_TIME);

            status_log = priority_in;
            wait(4, SC_MS);
        }
    }

    void high_priority_handler() {
        while (true) {
            wait(high_prio_ev);
			bool trusted = trust_level == 1;
			wait(1, SC_MS);
            if (trusted)
                critical_output = data_bus.read();
            else
                routine_output = data_bus.read();
        }
    }

    void low_priority_handler() {
        while (true) {
            wait(low_prio_ev);
			bool trusted = trust_level == 1;
			wait(3, SC_MS);
            if (trusted)
                critical_output = data_bus.read();
            else
                routine_output = data_bus.read();
        }
    }

    SC_CTOR(priorities) {
        SC_THREAD(task_dispatcher);
        SC_THREAD(high_priority_handler);
        SC_THREAD(low_priority_handler);
    }
};

int sc_main(int argc, char* argv[]) {
    priorities prio("prio");
    sc_signal<int> bus;
    prio.data_bus(bus);
    sc_start();
    return 0;
}