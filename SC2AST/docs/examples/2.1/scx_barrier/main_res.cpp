



 class time_t;
 class FILE;
 class ostream;

/*****************************************************************************

  The following code is derived, directly or indirectly, from the SystemC
  source code Copyright (c) 1996-2005 by all Contributors.
  All Rights reserved.

  The contents of this file are subject to the restrictions and limitations
  set forth in the SystemC Open Source License Version 2.4 (the "License");
  You may not use this file except in compliance with such restrictions and
  limitations. You may obtain instructions on how to receive a copy of the
  License at http://www.systemc.org/. Software distributed by Contributors
  under the License is distributed on an "AS IS" basis, WITHOUT WARRANTY OF
  ANY KIND, either express or implied. See the License for the specific
  language governing rights and limitations under the License.

 *****************************************************************************/

/*****************************************************************************

  scx_barrier.cpp -- This example demonstrates the proposed scx_barrier class
             that provides execution synchronization between threads. Each
             thread in the "thread pool" of an scx_barrier object instance
             waits on the scx_barrier object instance. When all the threads
             in the thread pool are waiting, they will all be dispatched. 

  Original Author: Andy Goodrich, Forte Design Systems, Inc.

 *****************************************************************************/

/*****************************************************************************

  MODIFICATION LOG - modifiers, enter your name, affiliation, date and
  changes you are making here.

      Name, Affiliation, Date:
  Description of Modification:

 *****************************************************************************/



/*****************************************************************************

  The following code is derived, directly or indirectly, from the SystemC
  source code Copyright (c) 1996-2004 by all Contributors.
  All Rights reserved.

  The contents of this file are subject to the restrictions and limitations
  set forth in the SystemC Open Source License Version 2.4 (the "License");
  You may not use this file except in compliance with such restrictions and
  limitations. You may obtain instructions on how to receive a copy of the
  License at http://www.systemc.org/. Software distributed by Contributors
  under the License is distributed on an "AS IS" basis, WITHOUT WARRANTY OF
  ANY KIND, either express or implied. See the License for the specific
  language governing rights and limitations under the License.

 *****************************************************************************/

/*****************************************************************************

  sc_barrier.h -- Barrier Process Synchronization Definition

  Original Author: Andy Goodrich, Forte Design Systems, 5 May 2003

 *****************************************************************************/

/*****************************************************************************

  MODIFICATION LOG - modifiers, enter your name, affiliation, date and
  changes you are making here.

      Name, Affiliation, Date:
  Description of Modification:

 *****************************************************************************/




namespace sc_dp {

// -----------------------------------------------------------------------------
// CLASS scx_barrier
//
// This class provides a way of synchronising a set of processes. Each process
// calls the wait() method and one all processes have called that method they
// will all be released for execution.
// -----------------------------------------------------------------------------

class scx_barrier {
  public:
    void initialize( int thread_n )
    {
        m_thread_n = thread_n;
    }

    void wait()
    {
        m_thread_n--;
        if ( m_thread_n )
        {
            ::wait(m_barrier_event);
        }
        else
        {
            m_barrier_event.notify_delayed();
            ::wait(m_barrier_event);
        }
    }

  protected:
    sc_event m_barrier_event; // Event to wait on.
    int m_thread_n; // # of threads left to wait.
};

} // namespace sc_dp
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
 scx_barrier m_barrier;
};

int sc_main( int argc, char* argv[] )
{
 sc_clock clock;
 X x("x");

 sc_start(1000);

 cout << "Program completed" << endl;
 return 0;
}
