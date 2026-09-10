#include <systemc.h>

// Secure case study with two transmitters of different security levels sharing
// one bus under an exponential backoff schedule. This is the example that
// motivates exploration refinement: it is the one design in the set that is
// intractable without it.
//
// All three processes run the same backoff schedule (1, 2, 4, ... 64 ms,
// then restarting), so they stay phase-locked, and within each slot they use
// disjoint time offsets. The monitor therefore always samples the public value
// before the confidential transmitter writes, and the confidential value only
// afterwards, so high_in never reaches low_out.
//
// Offsets within one slot, relative to the end of the backoff delay:
//   +  0 us : low_transmitter writes low_in
//   +125 us : monitor reads low_out
//   +250 us : high_transmitter writes high_in
//   +500 us : monitor reads high_out
//
// With the trivial abstraction the backoff counters are unknown, so each of the
// seven branches of handle_backoff is taken to be possible in every round and
// in every process independently. The relative phases of the three processes
// then become unknown as well, and the state space explodes combinatorially --
// millions of states, and an OutOfMemoryError when the dependence graph is
// built. Tracking the three backoff registers collapses this to 170 states,
// which both exploration heuristics discover on their own.
//
// See leaky_transmitters for the insecure counterpart of this design, in which
// the monitor samples the confidential slot and the flow is real.

#define MAX_BACKOFF  6

SC_MODULE(backoff_transmitter) {
    sc_inout<int> bus;

    int high_in;  
    int low_in;   

    int high_out;   
    int low_out;

    void low_transmitter() {
        int backoff = 0;
        while (true) {
			handle_backoff(backoff);
            bus.write(low_in);
            if (backoff < MAX_BACKOFF)
				backoff = backoff + 1;
			else
				backoff = 0;
			wait(500, SC_US);
        }
    }

    void high_transmitter() {
        int backoff = 0;
        while (true) {
			handle_backoff(backoff);
			wait(250, SC_US);
            bus.write(high_in);
            if (backoff < MAX_BACKOFF)
				backoff = backoff + 1;
			else
				backoff = 0;
			wait(250, SC_US);
        }
    }
	
	void handle_backoff(int bo) {
		if      (bo == 0) wait(1,  SC_MS);
		else if (bo == 1) wait(2,  SC_MS);
		else if (bo == 2) wait(4,  SC_MS);
		else if (bo == 3) wait(8,  SC_MS);
		else if (bo == 4) wait(16,  SC_MS);
		else if (bo == 5) wait(32,  SC_MS);
		else              wait(64, SC_MS);
	}

    void monitor() {
        int backoff = 0;
        while (true) {
			handle_backoff(backoff);
			wait(125, SC_US);
            low_out = bus.read();
			wait(375, SC_US);
            high_out = bus.read();
            if (backoff < MAX_BACKOFF)
				backoff = backoff + 1;
			else
				backoff = 0;
        }
    }

    SC_CTOR(backoff_transmitter) {
        SC_THREAD(low_transmitter);
        SC_THREAD(high_transmitter);
        SC_THREAD(monitor);
    }
};

int sc_main(int argc, char* argv[]) {
    backoff_transmitter bt("bt");
    sc_signal<int> bus;
    bt.bus(bus);
    sc_start();
    return 0;
}