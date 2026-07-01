#include <systemc.h>

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