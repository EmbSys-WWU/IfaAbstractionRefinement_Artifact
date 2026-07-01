#include "systemc.h"
#include "sc_barrier.h"
using sc_dp::scx_barrier;

SC_MODULE(X)
{
	SC_CTOR(X)
	{
		sc_thread_handle last_thread;

		SC_THREAD(a);
		SC_THREAD(b);
		SC_THREAD(c);

		m_barrier.initialize(3);
	}
	void a()
	{
		wait(5.0, SC_NS);
		m_barrier.wait();
		printf("%f - a\n", sc_simulation_time());
	}
	void b()
	{
		wait(11.0, SC_NS);
		m_barrier.wait();
		printf("%f - b\n", sc_simulation_time());
	}
	void c()
	{
		m_barrier.wait();
		printf("%f - c\n", sc_simulation_time());
	}
	scx_barrier   m_barrier;
};

int sc_main( int argc, char* argv[] )
{
	sc_clock clock;
	X x("x");

	sc_start(1000);

	cout << "Program completed" << endl;
	return 0;
}

