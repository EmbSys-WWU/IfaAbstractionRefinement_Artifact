#include <systemc.h>

// Secure case study realising the situation of Figure 3c of the paper: the
// potentially violating path is cut by a controlling node that has no useful
// data dependencies of its own, so resolving it requires tracking *all*
// dependencies of that controlling node.
//
// The design is a 2x2 crossbar switch rather than a shared bus. A routing
// controller alternates the crossbar between two configurations. In the
// straight configuration it forwards the public stream and marks it for
// egress port a, in the crossed configuration it forwards the confidential
// stream and marks it for egress port b. The router itself carries no payload
// at all; it only decides *which* egress event it notifies. The egress
// processes are pure copies without any condition of their own.
//
// Consequently the confidential payload reaches the public egress process only
// via the event that triggers it, i.e. through a chain of control
// dependencies: low_out = xbar_data is controlled by a_event, a_event is
// notified under if (dest == 0), and dest is written under if (config == 0).
SC_MODULE(crossbar) {

	int low_in;
	int high_in;

	int config;      // 0 = straight (public stream), 1 = crossed (confidential)
	int xbar_data;   // payload currently traversing the crossbar
	int dest;        // egress port selected for that payload

	sc_event route_event;
	sc_event a_event;
	sc_event b_event;

	int low_out;
	int high_out;

	void controller() {
		config = 0;
		while (true) {
			// One payload per 4 ms; the routing chain below needs 2 ms, so a
			// payload has always been consumed before the next one is placed.
			wait(4, SC_MS);
			if (config == 0) {
				xbar_data = low_in;
				dest = 0;
			} else {
				xbar_data = high_in;
				dest = 1;
			}
			config = 1 - config;
			route_event.notify(1, SC_MS);
		}
	}

	void router() {
		if (dest == 0) {
			a_event.notify(1, SC_MS);
		} else {
			b_event.notify(1, SC_MS);
		}
	}

	void egress_a() {
		low_out = xbar_data;
	}

	void egress_b() {
		high_out = xbar_data;
	}

	SC_CTOR(crossbar) {
		SC_THREAD(controller);

		SC_METHOD(router);
		sensitive << route_event;

		SC_METHOD(egress_a);
		sensitive << a_event;

		SC_METHOD(egress_b);
		sensitive << b_event;
	}

};

int sc_main(int argc, char* argv[]) {
	crossbar xb("xb");

	sc_start();
	return 0;
}
