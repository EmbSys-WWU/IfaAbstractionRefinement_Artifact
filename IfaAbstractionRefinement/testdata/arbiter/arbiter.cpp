#include <systemc.h>

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
