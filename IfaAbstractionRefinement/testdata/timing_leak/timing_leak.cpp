#include <systemc.h>

// Insecure case study with a pure timing channel.
//
// No confidential value is ever written to the bus: the high writer always
// writes the constant 99. Only *when* it writes depends on high_in. Because
// the reader samples the bus at a fixed point in time, the value it observes
// (and copies to the public output) is low_in if the high writer was early and
// 99 if it was late. The leak therefore exists solely because of the timing
// behaviour and cannot be found by a timing-insensitive analysis of the data
// flow. A timing-sensitive analysis must report it, and no refinement may
// remove it.
//
// Timeline (period 10 ms):
//   high_in > 0 : write 99 @ 3 ms, low_in @ 4 ms -> reader @ 6 ms sees low_in
//   high_in <= 0: low_in @ 4 ms, write 99 @ 5 ms -> reader @ 6 ms sees 99
SC_MODULE(timing_leak) {
    sc_inout<int> bus;

    int high_in;
    int low_in;

    int low_out;

    void high_writer() {
        while (true) {
            wait(1, SC_MS);
            if (high_in > 0) {
                wait(2, SC_MS);
                bus.write(99);
                wait(7, SC_MS);
            } else {
                wait(4, SC_MS);
                bus.write(99);
                wait(5, SC_MS);
            }
        }
    }

    void low_writer() {
        wait(4, SC_MS);
        while (true) {
            bus.write(low_in);
            wait(10, SC_MS);
        }
    }

    void reader() {
        wait(6, SC_MS);
        while (true) {
            low_out = bus.read();
            wait(10, SC_MS);
        }
    }

    SC_CTOR(timing_leak) {
        SC_THREAD(high_writer);
        SC_THREAD(low_writer);
        SC_THREAD(reader);
    }
};

int sc_main(int argc, char* argv[]) {
    timing_leak tl("tl");
    sc_signal<int> b;
    tl.bus(b);
    sc_start();
    return 0;
}
