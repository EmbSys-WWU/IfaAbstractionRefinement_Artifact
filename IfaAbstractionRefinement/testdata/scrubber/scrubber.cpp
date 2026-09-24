#include <systemc.h>

// Secure case study that realises the situation of Figure 3d of the paper:
// the potentially violating path is not cut by a controlling node of any of
// its own nodes, but by a dependency of one of its dependency nodes.
//
// Two independent sources publish into the shared register val: the
// confidential one in the even rounds and the public one in the odd rounds.
// Neither write is guarded by a condition, so no node on the path
//   high_in -> val -> data = 2 * val -> low_out = data
// has a controlling node whose value would resolve anything. The sink,
// however, overwrites data with a constant in exactly those rounds in which
// val is confidential, which kills the perceived data dependency. That
// overwrite (data = 5) is not itself on the path; it only appears in the
// backwards slice of low_out = data.
//
// Resolving this therefore requires the third and rarest selection strategy of
// the path resolution heuristic: tracking all dependencies of a dependency
// node.
//
// Timeline (sink runs every 2 ms with alternating phase):
//   t = 2, 6, 10, ... : val = high_in     t = 3, 7, 11, ... : phase 0, scrubbed
//   t = 4, 8, 12, ... : val = low_in      t = 5, 9, 13, ... : phase 1, forwarded
SC_MODULE(scrubber) {

	int low_in;
	int high_in;

	int val;
	int data;

	int low_out;

	void high_source() {
		while (true) {
			wait(2, SC_MS);
			val = high_in;
			wait(2, SC_MS);
		}
	}

	void low_source() {
		wait(4, SC_MS);
		while (true) {
			val = low_in;
			wait(4, SC_MS);
		}
	}

	void sink() {
		int phase = 0;
		wait(3, SC_MS);
		while (true) {
			data = 2 * val;
			if (phase == 0) {
				data = 5;
			}
			low_out = data;
			if (phase == 0)
				phase = 1;
			else
				phase = 0;
			wait(2, SC_MS);
		}
	}

	SC_CTOR(scrubber) {
		SC_THREAD(high_source);
		SC_THREAD(low_source);
		SC_THREAD(sink);
	}

};

int sc_main(int argc, char* argv[]) {
	scrubber sc("sc");

	sc_start();
	return 0;
}
