#include <systemc.h>

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