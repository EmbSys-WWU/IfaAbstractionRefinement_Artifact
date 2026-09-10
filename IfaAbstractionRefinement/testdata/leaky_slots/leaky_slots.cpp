#include <systemc.h>

// Insecure variant of the "many slots" case study.
// The producer mislabels the last slot: the confidential value high_in_b is
// announced on the type bus as a public value, so the consumer copies it to
// low_out. This is a genuine (non-spurious) information flow that no
// abstraction refinement may remove.
SC_MODULE(leaky_slots) {
    sc_inout<int> data_bus;
    sc_inout<int> type_bus;

    int low_in_a;
    int low_in_b;
    int high_in_a;
    int high_in_b;

    int low_out;
    int high_out;

    int slot;

    void producer() {
        slot = 0;
        while (true) {
            wait(2, SC_MS);

            if (slot == 0) {
                data_bus.write(low_in_a);
                type_bus.write(0);
            } else if (slot == 1) {
                data_bus.write(low_in_b);
                type_bus.write(0);
            } else if (slot == 2) {
                data_bus.write(high_in_a);
                type_bus.write(1);
            } else {
                data_bus.write(high_in_b);
                type_bus.write(0);   // leak: high value announced as public
            }
            slot = (slot + 1) % 4;
            wait(2, SC_MS);
        }
    }

    void consumer() {
        wait(1, SC_MS);
        while (true) {
            int d = data_bus.read();

            if (type_bus.read() == 0)
                low_out = d;
            else
                high_out = d;
            wait(4, SC_MS);
        }
    }

    SC_CTOR(leaky_slots) {
        SC_THREAD(producer);
        SC_THREAD(consumer);
    }
};

int sc_main(int argc, char* argv[]) {
    leaky_slots ms("ms");
    sc_signal<int> d_bus, t_bus;
    ms.data_bus(d_bus);
    ms.type_bus(t_bus);
    sc_start();
    return 0;
}
