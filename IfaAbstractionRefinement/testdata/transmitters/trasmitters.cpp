#include <systemc.h>

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