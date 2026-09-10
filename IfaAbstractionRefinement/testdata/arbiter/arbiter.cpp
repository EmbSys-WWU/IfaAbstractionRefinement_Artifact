#include <systemc.h>

// Secure case study in which the shared bus is allocated dynamically rather
// than by a fixed schedule.
//
// The reader claims the bus for whichever party arbiter_in selects, records the
// current owner in flag, and hands the transfer on to a processing stage after
// 1 ms. That stage transforms the bus value and then wakes the writer belonging
// to the recorded owner, which copies the value to its own output. A request
// for neither party is simply skipped.
//
// Timeline of one transfer:
//   t = 3 : reader claims the bus and sets flag
//   t = 4 : process transforms the bus value and notifies the owner's event
//   t = 5 : the matching writer copies the bus to its output
//   t = 6 : the reader claims the bus again
//
// This is the one case study in which exploration refinement alone already
// removes the spurious flow, without any information flow refinement. The
// reason is that flag -- the register that discriminates confidential from
// public transfers -- also decides *which event is notified*, and therefore
// which process becomes runnable. Leaving it unknown changes the scheduler
// state at the end of the atomic block, which is exactly the criterion of the
// splitters heuristic, so flag is tracked as a side effect of reducing the
// state space. The additional states this introduces are needed to prove
// security anyway.

SC_MODULE(arbiter) {

	int arbiter_in;
	int low_in;
	int high_in;
	
	int flag;
	int bus;
	sc_event process_event;
	sc_event low_event;
	sc_event high_event;
	
	int low_out;
	int high_out;
	
	void reader() {
		while (true) {
			wait (3, SC_MS);
			flag = 0;
			if (arbiter_in == 1) {
				bus = low_in;
				flag = 1;
			} else if (arbiter_in == 2) {
				bus = high_in;
				flag = 2;
			} else {
				continue;
			}
			process_event.notify(1, SC_MS);
		}
	}
	
	void process() {
		bus = (bus + 1) * 2;
		while (bus < 0) {
			bus += 42;
		}
		bus %= 31;
		
		if (flag == 1) {
			low_event.notify(1, SC_MS);
		} else if (flag == 2) {
			high_event.notify(1, SC_MS);
		}
	}
	
	void low_writer() {
		low_out = bus;
	}
	
	void high_writer() {
		high_out = bus;
	}
	
	SC_CTOR(arbiter) {
		SC_THREAD(reader);
		
		SC_METHOD(process);
		sensitive << process_event;
		
		SC_METHOD(low_writer);
		sensitive << low_event;
		
		SC_METHOD(high_writer);
		sensitive << high_event;
	}

};

int sc_main(int argc, char* argv[]) {
	arbiter arb("arbiter");
	
	sc_start();
	return 0;
}
