#include <systemc.h>

// Secure case study in which exploration refinement is not an optimisation
// but a precondition for analysability. The inter-slot gaps are not selected
// by a chain of if-statements over a small enumeration (as in Transmitters)
// but computed arithmetically and then used directly as the argument of a
// wait-statement. With the trivial abstraction the delay of wait(gap, SC_MS)
// is unknown, so the symbolic execution cannot make the step at all and the
// analysis aborts; exploration refinement discovers that the gap registers
// have to be tracked and the exploration succeeds.
//
// All three tasks run the same gap schedule and are therefore phase-locked,
// and inside each slot the confidential transmitter, the public transmitter
// and the monitor use disjoint time offsets, so the design is secure.
SC_MODULE(rate_adapt) {
    sc_inout<int> bus;

    int high_in;
    int low_in;

    int high_out;
    int low_out;

    void low_task() {
        int gap = 1;
        while (true) {
            wait(gap, SC_MS);
            bus.write(low_in);
            if (gap < 8)
                gap = gap * 2;
            else
                gap = 1;
            wait(500, SC_US);
        }
    }

    void high_task() {
        int gap = 1;
        while (true) {
            wait(gap, SC_MS);
            wait(250, SC_US);
            bus.write(high_in);
            if (gap < 8)
                gap = gap * 2;
            else
                gap = 1;
            wait(250, SC_US);
        }
    }

    void monitor() {
        int gap = 1;
        while (true) {
            wait(gap, SC_MS);
            wait(125, SC_US);
            low_out = bus.read();
            wait(375, SC_US);
            high_out = bus.read();
            if (gap < 8)
                gap = gap * 2;
            else
                gap = 1;
        }
    }

    SC_CTOR(rate_adapt) {
        SC_THREAD(low_task);
        SC_THREAD(high_task);
        SC_THREAD(monitor);
    }
};

int sc_main(int argc, char* argv[]) {
    rate_adapt ra("ra");
    sc_signal<int> b;
    ra.bus(b);
    sc_start();
    return 0;
}
