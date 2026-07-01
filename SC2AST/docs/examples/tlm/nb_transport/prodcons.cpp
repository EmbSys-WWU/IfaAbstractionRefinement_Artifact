//#define SC_INCLUDE_DYNAMIC_PROCESSES

// Number of generated inputes, change the command array when changing this value
#define INPUTSIZE 5
// size of the data we want to send
#define INTSIZE 32

// enables some extra outputs, set to 0 for disabling
#define DEBUG 0

// needed as phase-arguments
#define INIT_REQ_PHASE 0
#define END_REQ_PHASE 1
#define INIT_RES_PHASE 2
#define END_RES_PHASE 3


#include "systemc.h"


struct data
{
	//seems as if this has to be data, errors if not
	typedef data tlm_payload_type;
	//unused in our case but still necessary
	typedef int tlm_phase_type;

	//containing the submitted value
	int val;

	//dummy methods for being able to compile
  bool has_mm() { return false; }
	void set_mm(bool mm) { return; }
	void set_auto_extension(void* x) { return; }
	//these seem to be used for a kind of pooling for payloads
	//we don't use it here
	void acquire() { return; }
	void release() { return; }
	int get_ref_count() { return 1; }
	
};

#include "tlm.h"
#include "tlm_h/tlm_sockets/tlm_initiator_socket.h"
#include "tlm_h/tlm_sockets/tlm_target_socket.h"
#include "tlm_utils/peq_with_cb_and_phase.h"

//Module generating several read/write accesses to the memory
struct Producer : sc_module, tlm::tlm_bw_transport_if<data>
{

	//for using own datatypes we have to instantiate the initiator_socket with a Module, the size of the datatype we want to send over the socket, and the datatype we want to send.
  tlm::tlm_initiator_socket< INTSIZE, data, 1 > socket;


	data d;

  	SC_CTOR(Producer)
		: socket("socket")  // Construct and name socket
		, m_peq("m_peq", this, &Producer::peq_cb)
  	{
    	SC_THREAD(initiator_main);
		//binding the interface (in our case implemented by the Producer itself) to the socket
		socket.bind(*this);
  	}

  	void initiator_main()
  	{
	  sc_time transport_delay = sc_core::sc_time(5, SC_NS);
		sc_time loop_delay = sc_time(20, SC_NS);

    	for (int i = 0; i < INPUTSIZE; i++)
    	{

			d.val = i;

			if (DEBUG) cout << "[" << sc_time_stamp() << "](Producer_t): " << "Starting communication." << endl;
			int phase = INIT_REQ_PHASE;
			cout << "[" << sc_time_stamp() << "](Producer_t): " << "Sending request to the consumer, using nb_transport_fw, data is " << d.val << endl;
			tlm::tlm_sync_enum se = socket->nb_transport_fw( d, phase, transport_delay );
			cout << "[" << sc_time_stamp() << "](Producer_t): " << "nb_transport_fw returned, data is now " << d.val << endl;

			if (DEBUG) cout << "[" << sc_time_stamp() << "](Producer_t): " << "Communication finished. Target returned " << se << " and phase " << phase << "." << endl;

			// Realize the delay annotated onto the transport call
			wait(loop_delay);

			cout << "[" << sc_time_stamp() << "](Producer_t): " << "Finished iteration " << i+1 << "." << endl;			
    	}
  	}

	//interface methods needed for tlm_bw_transport_if
	//this method is called by the target when it either has finished its work or needs something from the initiator
          virtual void invalidate_direct_mem_ptr(sc_dt::uint64 start_range, sc_dt::uint64 end_range) {}
  tlm::tlm_sync_enum nb_transport_bw(data& trans, int& phase, sc_time& t) 
  //tlm::tlm_sync_enum  nb_transport_bw(data& trans, int& phase, sc_time& t) 
	  {
	   cout << "[" << sc_time_stamp() << "](Producer_bw): " << "Received request with data " << trans.val << ", phase " << phase << " and delay " << t << ". Storing it in the Payload event queue and returning TLM_ACCEPTED." << endl;
		
		m_peq.notify(trans, phase, t);
		return tlm::TLM_ACCEPTED;	
	}

	 void peq_cb(data& trans, const int& phase)
	 {
		sc_time delay;
		int phase1;

		if (phase == END_REQ_PHASE) {
			cout << "[" << sc_time_stamp() << "](Producer_cb): " << "Receiving END_REQ_PHASE from the Producer via nb_transport_bw. Waiting for INIT_RES." << endl;
		}
		else if (phase == INIT_RES_PHASE) {
			cout << "[" << sc_time_stamp() << "](Producer_cb): " << "Receiving INIT_RES_PHASE and result " << trans.val << " from the Producer. Preparing the response." << endl;
			m_peq.notify(trans, END_RES_PHASE, sc_time(3, SC_NS)); //call this method again in 5 NS
		}
		else if (phase == END_RES_PHASE) {
			delay = sc_time(1, SC_NS);
			phase1 = END_RES_PHASE;
			cout << "[" << sc_time_stamp() << "](Consumer_cb): " << "Sending END_RES_PHASE to the consumer." << endl;
			
			socket->nb_transport_fw(trans, phase1, delay);
		}
	 }
	
	 //initializing the PEQ
         tlm_utils::peq_with_cb_and_phase< Producer, data > m_peq;
};


// Target module
struct Consumer : sc_module, tlm::tlm_fw_transport_if<data>
{
    	tlm::tlm_target_socket< INTSIZE, data, 1 > socket;

	SC_CTOR(Consumer)
		: socket("socket")
		, m_peq("m_peq", this, &Consumer::peq_cb)			//register callback with PEQ (Payload event queue, see TLM LRM 9.3.1)
	{
		//binding the interface (in our case implemented by the Consumer itself) to the socket
	  		socket.bind(*this);
	}

  	// TLM-2 blocking transport method

	// the work that should be done when a nb_transport is called from an initiator
  //	virtual tlm::tlm_sync_enum  
virtual void nb_transport_fw(data& trans, int& phase, sc_time& t)
	 {
	   cout << "[" << sc_time_stamp() << "](Consumer_fw): " << "Received request with data " << trans.val << ", phase " << phase << " and delay " << t << ". Storing it in the Payload event queue and returning TLM_ACCEPTED." << endl;		
	     trans.val = trans.val + 10;
	    cout << "[" << sc_time_stamp() << "](Consumer_fw): data um 10 erhoeht, data ist jetzt: " << trans.val << endl;
		m_peq.notify(trans, phase, t);
		return tlm::TLM_ACCEPTED;		
        }

	void peq_cb(data& trans, const int& phase)
	{
		sc_time delay;
		int phase1;

		if (phase == INIT_REQ_PHASE) {
			delay = sc_time(2, SC_NS);
			phase1 = END_REQ_PHASE;
			cout << "[" << sc_time_stamp() << "](Consumer_cb): " << "Sending END_REQ_PHASE to the Producer." << endl;
			socket->nb_transport_bw(trans, phase1, delay);

			cout << "[" << sc_time_stamp() << "](Consumer_cb): " << "Waiting 5 NS to begin the response." << endl;
			m_peq.notify(trans, INIT_RES_PHASE, sc_time(5, SC_NS)); //call this method again in 5 NS
		}
		else if (phase == INIT_RES_PHASE) {
			delay = sc_time(2, SC_NS);
			phase1 = INIT_RES_PHASE;
			cout << "[" << sc_time_stamp() << "](Consumer_cb): " << "Sending INIT_RES_PHASE to the Producer using nb_transport_bw." << endl;
			//	trans.val *= trans.val;			
			socket->nb_transport_bw(trans, phase1, delay);
		}
		else if (phase == END_RES_PHASE) {
		  cout << "[" << sc_time_stamp() << "](Consumer_cb): " << "Receiving END_RES_PHASE from the Producer. Transport of data " << trans.val << " finished" << endl;
		}
	}

	// //dummy implementation
        virtual void b_transport( data& d, sc_time& delay ) {}
    	virtual bool get_direct_mem_ptr(data& trans, tlm::tlm_dmi& dmi_data) { return false; }
	virtual unsigned int transport_dbg(data& trans) { return 0; }

	// //initializing the PEQ
  	tlm_utils::peq_with_cb_and_phase< Consumer, data > m_peq;
  
};


int sc_main(int argc, char* argv[])
{
  	Producer prod("p");
	Consumer cons("c");

	prod.socket.bind( cons.socket);	
	
	sc_start();
  	return 0;
}

