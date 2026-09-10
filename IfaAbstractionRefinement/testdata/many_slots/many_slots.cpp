#include <systemc.h>

// Secure case study modelling a bus with several time slots, where the
// security level of the current slot is announced on a second bus.
//
// The producer cycles through four slots, writing two public and two
// confidential values in turn and publishing the level of each on type_bus.
// The consumer samples both buses and routes the value to low_out or high_out
// according to type_bus, so a confidential value is never copied to the public
// output.
//
// Timeline (producer writes every 4 ms, consumer reads every 4 ms, offset):
//   t =  2 : slot 0, low_in_a,  type 0     t =  5 : consumer reads slot 0
//   t =  6 : slot 1, low_in_b,  type 0     t =  9 : consumer reads slot 1
//   t = 10 : slot 2, high_in_a, type 1     t = 13 : consumer reads slot 2
//   t = 14 : slot 3, high_in_b, type 1     t = 17 : consumer reads slot 3
//
// Like Priorities, this needs no exploration refinement but reports a false
// positive without information flow refinement, and is resolved in the same way
// (Figure 3b): the consumer's branch is controlled by type_bus, whose data
// dependencies lead back to the rotating slot register. The discriminator here
// is a counter rather than a boolean, so more values have to be tracked.
//
// See leaky_slots for the insecure counterpart of this design.

SC_MODULE(many_slots) {
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
                type_bus.write(1);
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

    SC_CTOR(many_slots) {
        SC_THREAD(producer);
        SC_THREAD(consumer);
    }
};

int sc_main(int argc, char* argv[]) {
    many_slots ms("ms");
    sc_signal<int> d_bus, t_bus;
    ms.data_bus(d_bus);
    ms.type_bus(t_bus);
    sc_start();
    return 0;
}