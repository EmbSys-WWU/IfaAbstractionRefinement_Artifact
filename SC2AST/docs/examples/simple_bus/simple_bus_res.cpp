



 class time_t;
 class FILE;
 class ostream;

/*****************************************************************************

  The following code is derived, directly or indirectly, from the SystemC
  source code Copyright (c) 1996-2004 by all Contributors.
  All Rights reserved.

  The contents of this file are subject to the restrictions and limitations
  set forth in the SystemC Open Source License Version 2.3 (the "License");
  You may not use this file except in compliance with such restrictions and
  limitations. You may obtain instructions on how to receive a copy of the
  License at http://www.systemc.org/. Software distributed by Contributors
  under the License is distributed on an "AS IS" basis, WITHOUT WARRANTY OF
  ANY KIND, either express or implied. See the License for the specific
  language governing rights and limitations under the License.

 *****************************************************************************/

/*****************************************************************************
 
  simple_bus.cpp : The bus.

                   The main_action process is active at falling clock edge.

       		   Handling of the requests. A request can result in 
		   different requests to slaves. These are atomic requests 
		   and cannot be interrupted. A slave can take several 
		   cycles to complete: each time the request has to be 
		   re-issued to the slave. Once the slave transaction 
		   is completed, the m_current_request is cleared, and 
		   the request form is updated (address+=4, data++). 

		   When m_current_request is clear, the next request is
		   selected. This can be the same one, if the transfer is
		   not completed (burst-mode), or it can be a request with
		   a higher priority. Intrusion to a non-locked burst-mode
		   transaction is possible. 

		   When a transaction sets a lock, then the corresponding
		   field in the request is set to SIMPLE_BUS_LOCK_SET. If
		   the locked transaction is granted by the arbiter for the
		   first time, the lock is set to SIMPLE_BUS_LOCK_GRANTED. 
		   At the end of the transaction, the lock is set to 
		   SIMPLE_BUS_LOCK_SET. If now a new locked request is made,
		   with the same priority, the status of the lock is set
		   to SIMPLE_BUS_LOCK_GRANTED, and the arbiter will pick
		   this transaction to be the best. If the locked trans-
		   action was not selected by the arbiter in the first 
		   round (request with higher priority preceeded), then the
		   lock is not set. After the completion of the transaction,
		   the lock is set from SIMPLE_BUS_LOCK_SET (set during the
		   bus-interface function), to SIMPLE_BUS_LOCK_NO. 
		   
		   The bus is derived from the following interfaces, and
		   contains the implementation of these: 
		   - blocking : burst_read/burst_write
		   - non-blocking : read/write/get_status
		   - direct : direct_read/direct_write
 
  Original Author: Ric Hilderink, Synopsys, Inc., 2001-10-11
 
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
  set forth in the SystemC Open Source License Version 2.3 (the "License");
  You may not use this file except in compliance with such restrictions and
  limitations. You may obtain instructions on how to receive a copy of the
  License at http://www.systemc.org/. Software distributed by Contributors
  under the License is distributed on an "AS IS" basis, WITHOUT WARRANTY OF
  ANY KIND, either express or implied. See the License for the specific
  language governing rights and limitations under the License.

 *****************************************************************************/

/*****************************************************************************
 
  simple_bus.h : The bus.

		 The bus is derived from the following interfaces, and
	         contains the implementation of these: 
		 - blocking : burst_read/burst_write
		 - non-blocking : read/write/get_status
		 - direct : direct_read/direct_write
 
  Original Author: Ric Hilderink, Synopsys, Inc., 2001-10-11
 
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
  set forth in the SystemC Open Source License Version 2.3 (the "License");
  You may not use this file except in compliance with such restrictions and
  limitations. You may obtain instructions on how to receive a copy of the
  License at http://www.systemc.org/. Software distributed by Contributors
  under the License is distributed on an "AS IS" basis, WITHOUT WARRANTY OF
  ANY KIND, either express or implied. See the License for the specific
  language governing rights and limitations under the License.

 *****************************************************************************/

/*****************************************************************************
 
  simple_bus_types.h : The common types.
 
  Original Author: Ric Hilderink, Synopsys, Inc., 2001-10-11
 
 *****************************************************************************/

/*****************************************************************************
 
  MODIFICATION LOG - modifiers, enter your name, affiliation, date and
  changes you are making here.
 
      Name, Affiliation, Date:
  Description of Modification:
 
 *****************************************************************************/







namespace {
  typedef int FILE;
  struct va_list;
}

enum simple_bus_status { SIMPLE_BUS_OK = 0
    , SIMPLE_BUS_REQUEST
    , SIMPLE_BUS_WAIT
    , SIMPLE_BUS_ERROR };

// needed for more readable debug output
extern char simple_bus_status_str[4][20];

struct simple_bus_request{};
typedef sc_pvector<simple_bus_request *> simple_bus_request_vec;

extern int sb_fprintf(FILE *, const char *, ...);

/*****************************************************************************

  The following code is derived, directly or indirectly, from the SystemC
  source code Copyright (c) 1996-2004 by all Contributors.
  All Rights reserved.

  The contents of this file are subject to the restrictions and limitations
  set forth in the SystemC Open Source License Version 2.3 (the "License");
  You may not use this file except in compliance with such restrictions and
  limitations. You may obtain instructions on how to receive a copy of the
  License at http://www.systemc.org/. Software distributed by Contributors
  under the License is distributed on an "AS IS" basis, WITHOUT WARRANTY OF
  ANY KIND, either express or implied. See the License for the specific
  language governing rights and limitations under the License.

 *****************************************************************************/

/*****************************************************************************
 
  simple_bus_request.h : The bus interface request form.
 
  Original Author: Ric Hilderink, Synopsys, Inc., 2001-10-11
 
 *****************************************************************************/

/*****************************************************************************
 
  MODIFICATION LOG - modifiers, enter your name, affiliation, date and
  changes you are making here.
 
      Name, Affiliation, Date:
  Description of Modification:
 
 *****************************************************************************/




enum simple_bus_lock_status { SIMPLE_BUS_LOCK_NO = 0
         , SIMPLE_BUS_LOCK_SET
         , SIMPLE_BUS_LOCK_GRANTED
};

struct simple_bus_request
{
  // parameters
  unsigned int priority;

  // request parameters
  bool do_write;
  unsigned int address;
  unsigned int end_address;
  int *data;
  simple_bus_lock_status lock;

  // request status
  sc_event transfer_done;
  simple_bus_status status;

  // default constructor
  simple_bus_request();
};

inline simple_bus_request::simple_bus_request()
  : priority(0)
  , do_write(false)
  , address(0)
  , end_address(0)
  , data((int *)0)
  , lock(SIMPLE_BUS_LOCK_NO)
  , status(SIMPLE_BUS_OK)
{}

/*****************************************************************************

  The following code is derived, directly or indirectly, from the SystemC
  source code Copyright (c) 1996-2004 by all Contributors.
  All Rights reserved.

  The contents of this file are subject to the restrictions and limitations
  set forth in the SystemC Open Source License Version 2.3 (the "License");
  You may not use this file except in compliance with such restrictions and
  limitations. You may obtain instructions on how to receive a copy of the
  License at http://www.systemc.org/. Software distributed by Contributors
  under the License is distributed on an "AS IS" basis, WITHOUT WARRANTY OF
  ANY KIND, either express or implied. See the License for the specific
  language governing rights and limitations under the License.

 *****************************************************************************/

/*****************************************************************************
 
  simple_bus_direct_if.h : The direct BUS/Slave interface.
 
  Original Author: Ric Hilderink, Synopsys, Inc., 2001-10-11
 
 *****************************************************************************/

/*****************************************************************************
 
  MODIFICATION LOG - modifiers, enter your name, affiliation, date and
  changes you are making here.
 
      Name, Affiliation, Date:
  Description of Modification:
 
 *****************************************************************************/






class simple_bus_direct_if
  : public virtual sc_interface
{
public:
  // direct BUS/Slave interface
  virtual bool direct_read(int *data, unsigned int address) = 0;
  virtual bool direct_write(int *data, unsigned int address) = 0;

}; // end class simple_bus_direct_if

/*****************************************************************************

  The following code is derived, directly or indirectly, from the SystemC
  source code Copyright (c) 1996-2004 by all Contributors.
  All Rights reserved.

  The contents of this file are subject to the restrictions and limitations
  set forth in the SystemC Open Source License Version 2.3 (the "License");
  You may not use this file except in compliance with such restrictions and
  limitations. You may obtain instructions on how to receive a copy of the
  License at http://www.systemc.org/. Software distributed by Contributors
  under the License is distributed on an "AS IS" basis, WITHOUT WARRANTY OF
  ANY KIND, either express or implied. See the License for the specific
  language governing rights and limitations under the License.

 *****************************************************************************/

/*****************************************************************************
 
  simple_bus_non_blocking_if.h : The non-blocking BUS interface.
 
  Original Author: Ric Hilderink, Synopsys, Inc., 2001-10-11
 
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
  set forth in the SystemC Open Source License Version 2.3 (the "License");
  You may not use this file except in compliance with such restrictions and
  limitations. You may obtain instructions on how to receive a copy of the
  License at http://www.systemc.org/. Software distributed by Contributors
  under the License is distributed on an "AS IS" basis, WITHOUT WARRANTY OF
  ANY KIND, either express or implied. See the License for the specific
  language governing rights and limitations under the License.

 *****************************************************************************/

/*****************************************************************************
 
  simple_bus_types.h : The common types.
 
  Original Author: Ric Hilderink, Synopsys, Inc., 2001-10-11
 
 *****************************************************************************/

/*****************************************************************************
 
  MODIFICATION LOG - modifiers, enter your name, affiliation, date and
  changes you are making here.
 
      Name, Affiliation, Date:
  Description of Modification:
 
 *****************************************************************************/

class simple_bus_non_blocking_if
  : public virtual sc_interface
{
public:
  // non-blocking BUS interface
  virtual void read(unsigned int unique_priority
      , int *data
      , unsigned int address
      , bool lock = false) = 0;
  virtual void write(unsigned int unique_priority
       , int *data
       , unsigned int address
       , bool lock = false) = 0;

  virtual simple_bus_status get_status(unsigned int unique_priority) = 0;

}; // end class simple_bus_non_blocking_if

/*****************************************************************************

  The following code is derived, directly or indirectly, from the SystemC
  source code Copyright (c) 1996-2004 by all Contributors.
  All Rights reserved.

  The contents of this file are subject to the restrictions and limitations
  set forth in the SystemC Open Source License Version 2.3 (the "License");
  You may not use this file except in compliance with such restrictions and
  limitations. You may obtain instructions on how to receive a copy of the
  License at http://www.systemc.org/. Software distributed by Contributors
  under the License is distributed on an "AS IS" basis, WITHOUT WARRANTY OF
  ANY KIND, either express or implied. See the License for the specific
  language governing rights and limitations under the License.

 *****************************************************************************/

/*****************************************************************************
 
  simple_bus_blocking_if.h : The blocking bus interface.
 
  Original Author: Ric Hilderink, Synopsys, Inc., 2001-10-11
 
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
  set forth in the SystemC Open Source License Version 2.3 (the "License");
  You may not use this file except in compliance with such restrictions and
  limitations. You may obtain instructions on how to receive a copy of the
  License at http://www.systemc.org/. Software distributed by Contributors
  under the License is distributed on an "AS IS" basis, WITHOUT WARRANTY OF
  ANY KIND, either express or implied. See the License for the specific
  language governing rights and limitations under the License.

 *****************************************************************************/

/*****************************************************************************
 
  simple_bus_types.h : The common types.
 
  Original Author: Ric Hilderink, Synopsys, Inc., 2001-10-11
 
 *****************************************************************************/

/*****************************************************************************
 
  MODIFICATION LOG - modifiers, enter your name, affiliation, date and
  changes you are making here.
 
      Name, Affiliation, Date:
  Description of Modification:
 
 *****************************************************************************/

class simple_bus_blocking_if
  : public virtual sc_interface
{
public:
  // blocking BUS interface
  virtual simple_bus_status burst_read(unsigned int unique_priority
           , int *data
           , unsigned int start_address
           , unsigned int length = 1
           , bool lock = false) = 0;
  virtual simple_bus_status burst_write(unsigned int unique_priority
     , int *data
     , unsigned int start_address
     , unsigned int length = 1
     , bool lock = false) = 0;

}; // end class simple_bus_blocking_if

/*****************************************************************************

  The following code is derived, directly or indirectly, from the SystemC
  source code Copyright (c) 1996-2004 by all Contributors.
  All Rights reserved.

  The contents of this file are subject to the restrictions and limitations
  set forth in the SystemC Open Source License Version 2.3 (the "License");
  You may not use this file except in compliance with such restrictions and
  limitations. You may obtain instructions on how to receive a copy of the
  License at http://www.systemc.org/. Software distributed by Contributors
  under the License is distributed on an "AS IS" basis, WITHOUT WARRANTY OF
  ANY KIND, either express or implied. See the License for the specific
  language governing rights and limitations under the License.

 *****************************************************************************/

/*****************************************************************************
 
  simple_bus_arbiter_if.h : The arbiter interface.
 
  Original Author: Ric Hilderink, Synopsys, Inc., 2001-10-11
 
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
  set forth in the SystemC Open Source License Version 2.3 (the "License");
  You may not use this file except in compliance with such restrictions and
  limitations. You may obtain instructions on how to receive a copy of the
  License at http://www.systemc.org/. Software distributed by Contributors
  under the License is distributed on an "AS IS" basis, WITHOUT WARRANTY OF
  ANY KIND, either express or implied. See the License for the specific
  language governing rights and limitations under the License.

 *****************************************************************************/

/*****************************************************************************
 
  simple_bus_types.h : The common types.
 
  Original Author: Ric Hilderink, Synopsys, Inc., 2001-10-11
 
 *****************************************************************************/

/*****************************************************************************
 
  MODIFICATION LOG - modifiers, enter your name, affiliation, date and
  changes you are making here.
 
      Name, Affiliation, Date:
  Description of Modification:
 
 *****************************************************************************/


class simple_bus_arbiter_if
  : public virtual sc_interface
{
public:
  virtual simple_bus_request *
    arbitrate(const simple_bus_request_vec &requests) = 0;

}; // end class simple_bus_arbiter_if

/*****************************************************************************

  The following code is derived, directly or indirectly, from the SystemC
  source code Copyright (c) 1996-2004 by all Contributors.
  All Rights reserved.

  The contents of this file are subject to the restrictions and limitations
  set forth in the SystemC Open Source License Version 2.3 (the "License");
  You may not use this file except in compliance with such restrictions and
  limitations. You may obtain instructions on how to receive a copy of the
  License at http://www.systemc.org/. Software distributed by Contributors
  under the License is distributed on an "AS IS" basis, WITHOUT WARRANTY OF
  ANY KIND, either express or implied. See the License for the specific
  language governing rights and limitations under the License.

 *****************************************************************************/

/*****************************************************************************
 
  simple_bus_slave_if.h : The Slave interface.
 
  Original Author: Ric Hilderink, Synopsys, Inc., 2001-10-11
 
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
  set forth in the SystemC Open Source License Version 2.3 (the "License");
  You may not use this file except in compliance with such restrictions and
  limitations. You may obtain instructions on how to receive a copy of the
  License at http://www.systemc.org/. Software distributed by Contributors
  under the License is distributed on an "AS IS" basis, WITHOUT WARRANTY OF
  ANY KIND, either express or implied. See the License for the specific
  language governing rights and limitations under the License.

 *****************************************************************************/

/*****************************************************************************
 
  simple_bus_types.h : The common types.
 
  Original Author: Ric Hilderink, Synopsys, Inc., 2001-10-11
 
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
  set forth in the SystemC Open Source License Version 2.3 (the "License");
  You may not use this file except in compliance with such restrictions and
  limitations. You may obtain instructions on how to receive a copy of the
  License at http://www.systemc.org/. Software distributed by Contributors
  under the License is distributed on an "AS IS" basis, WITHOUT WARRANTY OF
  ANY KIND, either express or implied. See the License for the specific
  language governing rights and limitations under the License.

 *****************************************************************************/

/*****************************************************************************
 
  simple_bus_direct_if.h : The direct BUS/Slave interface.
 
  Original Author: Ric Hilderink, Synopsys, Inc., 2001-10-11
 
 *****************************************************************************/

/*****************************************************************************
 
  MODIFICATION LOG - modifiers, enter your name, affiliation, date and
  changes you are making here.
 
      Name, Affiliation, Date:
  Description of Modification:
 
 *****************************************************************************/


class simple_bus_slave_if
  : public simple_bus_direct_if
{
public:
  // Slave interface
  virtual simple_bus_status read(int *data, unsigned int address) = 0;
  virtual simple_bus_status write(int *data, unsigned int address) = 0;

  virtual unsigned int start_address() const = 0;
  virtual unsigned int end_address() const = 0;

}; // end class simple_bus_slave_if


class simple_bus
  : public simple_bus_direct_if
  , public simple_bus_non_blocking_if
  , public simple_bus_blocking_if
  , public sc_module
{
public:
  // ports
  sc_in_clk clock;
  sc_port<simple_bus_arbiter_if> arbiter_port;
  sc_port<simple_bus_slave_if, 0> slave_port;

  SC_HAS_PROCESS(simple_bus);

  // constructor
  simple_bus(sc_module_name name_
             , bool verbose = false)
    : sc_module(name_)
    , m_verbose(verbose)
    , m_current_request(0)
  {
    // process declaration
    SC_METHOD(main_action);
    dont_initialize();
    sensitive_neg << clock;
  }

  // process
  void main_action();

  // direct BUS interface
  bool direct_read(int *data, unsigned int address);
  bool direct_write(int *data, unsigned int address);

  // non-blocking BUS interface
  void read(unsigned int unique_priority
     , int *data
     , unsigned int address
     , bool lock = false);
  void write(unsigned int unique_priority
      , int *data
      , unsigned int address
      , bool lock = false);
  simple_bus_status get_status(unsigned int unique_priority);

  // blocking BUS interface
  simple_bus_status burst_read(unsigned int unique_priority
          , int *data
          , unsigned int start_address
          , unsigned int length = 1
          , bool lock = false);
  simple_bus_status burst_write(unsigned int unique_priority
    , int *data
    , unsigned int start_address
    , unsigned int length = 1
    , bool lock = false);

private:
  void handle_request();
  void end_of_elaboration();
  simple_bus_slave_if * get_slave(unsigned int address);
  simple_bus_request * get_request(unsigned int priority);
  simple_bus_request * get_next_request();
  void clear_locks();

private:
  bool m_verbose;
  simple_bus_request_vec m_requests;
  simple_bus_request *m_current_request;

}; // end class simple_bus

void simple_bus::end_of_elaboration()
{
  // perform a static check for overlapping memory areas of the slaves
  bool no_overlap;
  for (int i = 1; i < slave_port.size(); ++i) {
    simple_bus_slave_if *slave1 = slave_port[i];
    for (int j = 0; j < i; ++j) {
      simple_bus_slave_if *slave2 = slave_port[j];
      no_overlap = ( slave1->end_address() < slave2->start_address() ) ||
               ( slave1->start_address() > slave2->end_address() );
      if ( !no_overlap ) {
        sb_fprintf(stdout,"Error: overlapping address spaces of 2 slaves : \n");
        sb_fprintf(stdout,"slave %i : %0X..%0X\n",i,slave1->start_address(),slave1->end_address());
        sb_fprintf(stdout,"slave %i : %0X..%0X\n",j,slave2->start_address(),slave2->end_address());
        exit(0);
      }
    }
  }
}

//----------------------------------------------------------------------------
//-- process
//----------------------------------------------------------------------------

void simple_bus::main_action()
{
  // m_current_request is cleared after the slave is done with a
  // single data transfer. Burst requests require the arbiter to
  // select the request again.

  if (!m_current_request)
    m_current_request = get_next_request();
  else
    // monitor slave wait states
    if (m_verbose)
      sb_fprintf(stdout, "%g SLV [%d]\n", sc_simulation_time(),
   m_current_request->address);
  if (m_current_request)
    handle_request();
  if (!m_current_request)
    clear_locks();
}

//----------------------------------------------------------------------------
//-- direct BUS interface
//----------------------------------------------------------------------------

bool simple_bus::direct_read(int *data, unsigned int address)
{
  if (address%4 != 0 ) {// address not word alligned
    sb_fprintf(stdout, "  BUS ERROR --> address %04X not word alligned\n",address);
    return false;
  }
  simple_bus_slave_if *slave = get_slave(address);
  if (!slave) return false;
  return slave->direct_read(data, address);
}

bool simple_bus::direct_write(int *data, unsigned int address)
{
  if (address%4 != 0 ) {// address not word alligned
    sb_fprintf(stdout, "  BUS ERROR --> address %04X not word alligned\n",address);
    return false;
  }
  simple_bus_slave_if *slave = get_slave(address);
  if (!slave) return false;
  return slave->direct_write(data, address);
}

//----------------------------------------------------------------------------
//-- non-blocking BUS interface
//----------------------------------------------------------------------------

void simple_bus::read(unsigned int unique_priority
        , int *data
        , unsigned int address
        , bool lock)
{
  if (m_verbose)
    sb_fprintf(stdout, "%g %s : read(%d) @ %x\n",
        sc_simulation_time(), name(), unique_priority, address);

  simple_bus_request *request = get_request(unique_priority);

  // abort when the request is still not finished
  sc_assert((request->status == SIMPLE_BUS_OK) ||
     (request->status == SIMPLE_BUS_ERROR));

  request->do_write = false; // we are reading
  request->address = address;
  request->end_address = address;
  request->data = data;

  if (lock)
    request->lock = (request->lock == SIMPLE_BUS_LOCK_SET) ?
      SIMPLE_BUS_LOCK_GRANTED : SIMPLE_BUS_LOCK_SET;

  request->status = SIMPLE_BUS_REQUEST;
}

void simple_bus::write(unsigned int unique_priority
         , int *data
         , unsigned int address
         , bool lock)
{
  if (m_verbose)
    sb_fprintf(stdout, "%g %s : write(%d) @ %x\n",
        sc_simulation_time(), name(), unique_priority, address);

  simple_bus_request *request = get_request(unique_priority);

  // abort when the request is still not finished
  sc_assert((request->status == SIMPLE_BUS_OK) ||
     (request->status == SIMPLE_BUS_ERROR));

  request->do_write = true; // we are writing
  request->address = address;
  request->end_address = address;
  request->data = data;

  if (lock)
    request->lock = (request->lock == SIMPLE_BUS_LOCK_SET) ?
      SIMPLE_BUS_LOCK_GRANTED : SIMPLE_BUS_LOCK_SET;

  request->status = SIMPLE_BUS_REQUEST;
}

simple_bus_status simple_bus::get_status(unsigned int unique_priority)
{
  return get_request(unique_priority)->status;
}

//----------------------------------------------------------------------------
//-- blocking BUS interface
//----------------------------------------------------------------------------

simple_bus_status simple_bus::burst_read(unsigned int unique_priority
      , int *data
      , unsigned int start_address
      , unsigned int length
      , bool lock)
{
  if (m_verbose)
    sb_fprintf(stdout, "%g %s : burst_read(%d) @ %x\n",
        sc_simulation_time(), name(), unique_priority, start_address);

  simple_bus_request *request = get_request(unique_priority);

  request->do_write = false; // we are reading
  request->address = start_address;
  request->end_address = start_address + (length-1)*4;
  request->data = data;

  if (lock)
    request->lock = (request->lock == SIMPLE_BUS_LOCK_SET) ?
      SIMPLE_BUS_LOCK_GRANTED : SIMPLE_BUS_LOCK_SET;

  request->status = SIMPLE_BUS_REQUEST;

  wait(request->transfer_done);
  wait(clock->posedge_event());
  return request->status;
}

simple_bus_status simple_bus::burst_write(unsigned int unique_priority
       , int *data
       , unsigned int start_address
       , unsigned int length
       , bool lock)
{
  if (m_verbose)
    sb_fprintf(stdout, "%g %s : burst_write(%d) @ %x\n",
        sc_simulation_time(), name(), unique_priority, start_address);

  simple_bus_request *request = get_request(unique_priority);

  request->do_write = true; // we are writing
  request->address = start_address;
  request->end_address = start_address + (length-1)*4;
  request->data = data;

  if (lock)
    request->lock = (request->lock == SIMPLE_BUS_LOCK_SET) ?
      SIMPLE_BUS_LOCK_GRANTED : SIMPLE_BUS_LOCK_SET;

  request->status = SIMPLE_BUS_REQUEST;

  wait(request->transfer_done);
  wait(clock->posedge_event());
  return request->status;
}

//----------------------------------------------------------------------------
//-- BUS methods:
//
//     handle_request()   : performs atomic bus-to-slave request
//     get_request()      : BUS-interface: gets the request form of given 
//                          priority
//     get_next_request() : returns a valid request out of the list of 
//                          pending requests
//     clear_locks()      : downgrade the lock status of the requests once
//                          the transfer is done
//----------------------------------------------------------------------------

void simple_bus::handle_request()
{
  if (m_verbose)
      sb_fprintf(stdout, "%g %s Handle Slave(%d)\n",
   sc_simulation_time(), name(), m_current_request->priority);

  m_current_request->status = SIMPLE_BUS_WAIT;
  simple_bus_slave_if *slave = get_slave(m_current_request->address);

  if ((m_current_request->address)%4 != 0 ) {// address not word alligned
    sb_fprintf(stdout, "  BUS ERROR --> address %04X not word alligned\n",m_current_request->address);
    m_current_request->status = SIMPLE_BUS_ERROR;
    m_current_request = (simple_bus_request *)0;
    return;
  }
  if (!slave) {
    sb_fprintf(stdout, "  BUS ERROR --> no slave for address %04X \n",m_current_request->address);
    m_current_request->status = SIMPLE_BUS_ERROR;
    m_current_request = (simple_bus_request *)0;
    return;
  }

  simple_bus_status slave_status = SIMPLE_BUS_OK;
  if (m_current_request->do_write)
    slave_status = slave->write(m_current_request->data,
    m_current_request->address);
  else
    slave_status = slave->read(m_current_request->data,
          m_current_request->address);

  if (m_verbose)
    sb_fprintf(stdout, "  --> status=(%s)\n", simple_bus_status_str[slave_status]);

  switch(slave_status)
    {
    case SIMPLE_BUS_ERROR:
      m_current_request->status = SIMPLE_BUS_ERROR;
      m_current_request->transfer_done.notify();
      m_current_request = (simple_bus_request *)0;
      break;
    case SIMPLE_BUS_OK:
      m_current_request->address+=4; //next word (byte addressing)
      m_current_request->data++;
      if (m_current_request->address > m_current_request->end_address)
 {
   // burst-transfer (or single transfer) completed
   m_current_request->status = SIMPLE_BUS_OK;
   m_current_request->transfer_done.notify();
   m_current_request = (simple_bus_request *)0;
 }
      else
 { // more data to transfer, but the (atomic) slave transfer is done
   m_current_request = (simple_bus_request *)0;
 }
      break;
    case SIMPLE_BUS_WAIT:
      // the slave is still processing: no clearance of the current request
      break;
    default:
      break;
    }
}

simple_bus_slave_if *simple_bus::get_slave(unsigned int address)
{
  for (int i = 0; i < slave_port.size(); ++i)
    {
      simple_bus_slave_if *slave = slave_port[i];
      if ((slave->start_address() <= address) &&
   (address <= slave->end_address()))
 return slave;
    }
  return (simple_bus_slave_if *)0;
}

simple_bus_request * simple_bus::get_request(unsigned int priority)
{
  simple_bus_request *request = (simple_bus_request *)0;
  for (int i = 0; i < m_requests.size(); ++i)
    {
      request = m_requests[i];
      if ((request) &&
   (request->priority == priority))
 return request;
    }
  request = new simple_bus_request;
  request->priority = priority;
  m_requests.push_back(request);
  return request;
}

simple_bus_request * simple_bus::get_next_request()
{
  // the slave is done with its action, m_current_request is
  // empty, so go over the bag of request-forms and compose
  // a set of likely requests. Pass it to the arbiter for the
  // final selection
  simple_bus_request_vec Q;
  for (int i = 0; i < m_requests.size(); ++i)
    {
      simple_bus_request *request = m_requests[i];
      if ((request->status == SIMPLE_BUS_REQUEST) ||
   (request->status == SIMPLE_BUS_WAIT))
 {
   if (m_verbose)
     sb_fprintf(stdout, "%g %s : request (%d) [%s]\n",
         sc_simulation_time(), name(),
         request->priority, simple_bus_status_str[request->status]);
   Q.push_back(request);
 }
    }
  if (Q.size() > 0)
    return arbiter_port->arbitrate(Q);
  return (simple_bus_request *)0;
}

void simple_bus::clear_locks()
{
  for (int i = 0; i < m_requests.size(); ++i)
    if (m_requests[i]->lock == SIMPLE_BUS_LOCK_GRANTED)
      m_requests[i]->lock = SIMPLE_BUS_LOCK_SET;
    else
      m_requests[i]->lock = SIMPLE_BUS_LOCK_NO;
}

/*****************************************************************************

  The following code is derived, directly or indirectly, from the SystemC
  source code Copyright (c) 1996-2004 by all Contributors.
  All Rights reserved.

  The contents of this file are subject to the restrictions and limitations
  set forth in the SystemC Open Source License Version 2.3 (the "License");
  You may not use this file except in compliance with such restrictions and
  limitations. You may obtain instructions on how to receive a copy of the
  License at http://www.systemc.org/. Software distributed by Contributors
  under the License is distributed on an "AS IS" basis, WITHOUT WARRANTY OF
  ANY KIND, either express or implied. See the License for the specific
  language governing rights and limitations under the License.

 *****************************************************************************/

/*****************************************************************************
 
  simple_bus_arbiter.cpp : The arbitration unit.
 
  Original Author: Ric Hilderink, Synopsys, Inc., 2001-10-11
 
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
  set forth in the SystemC Open Source License Version 2.3 (the "License");
  You may not use this file except in compliance with such restrictions and
  limitations. You may obtain instructions on how to receive a copy of the
  License at http://www.systemc.org/. Software distributed by Contributors
  under the License is distributed on an "AS IS" basis, WITHOUT WARRANTY OF
  ANY KIND, either express or implied. See the License for the specific
  language governing rights and limitations under the License.

 *****************************************************************************/

/*****************************************************************************
 
  simple_bus_arbiter.h : The arbitration unit.
 
  Original Author: Ric Hilderink, Synopsys, Inc., 2001-10-11
 
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
  set forth in the SystemC Open Source License Version 2.3 (the "License");
  You may not use this file except in compliance with such restrictions and
  limitations. You may obtain instructions on how to receive a copy of the
  License at http://www.systemc.org/. Software distributed by Contributors
  under the License is distributed on an "AS IS" basis, WITHOUT WARRANTY OF
  ANY KIND, either express or implied. See the License for the specific
  language governing rights and limitations under the License.

 *****************************************************************************/

/*****************************************************************************
 
  simple_bus_types.h : The common types.
 
  Original Author: Ric Hilderink, Synopsys, Inc., 2001-10-11
 
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
  set forth in the SystemC Open Source License Version 2.3 (the "License");
  You may not use this file except in compliance with such restrictions and
  limitations. You may obtain instructions on how to receive a copy of the
  License at http://www.systemc.org/. Software distributed by Contributors
  under the License is distributed on an "AS IS" basis, WITHOUT WARRANTY OF
  ANY KIND, either express or implied. See the License for the specific
  language governing rights and limitations under the License.

 *****************************************************************************/

/*****************************************************************************
 
  simple_bus_request.h : The bus interface request form.
 
  Original Author: Ric Hilderink, Synopsys, Inc., 2001-10-11
 
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
  set forth in the SystemC Open Source License Version 2.3 (the "License");
  You may not use this file except in compliance with such restrictions and
  limitations. You may obtain instructions on how to receive a copy of the
  License at http://www.systemc.org/. Software distributed by Contributors
  under the License is distributed on an "AS IS" basis, WITHOUT WARRANTY OF
  ANY KIND, either express or implied. See the License for the specific
  language governing rights and limitations under the License.

 *****************************************************************************/

/*****************************************************************************
 
  simple_bus_arbiter_if.h : The arbiter interface.
 
  Original Author: Ric Hilderink, Synopsys, Inc., 2001-10-11
 
 *****************************************************************************/

/*****************************************************************************
 
  MODIFICATION LOG - modifiers, enter your name, affiliation, date and
  changes you are making here.
 
      Name, Affiliation, Date:
  Description of Modification:
 
 *****************************************************************************/


class simple_bus_arbiter
  : public simple_bus_arbiter_if
  , public sc_module
{
public:
  // constructor
  simple_bus_arbiter(sc_module_name name_
                     , bool verbose = false)
    : sc_module(name_)
    , m_verbose(verbose)
  {}

  simple_bus_request *arbitrate(const simple_bus_request_vec &requests);

private:
  bool m_verbose;

}; // end class simple_bus_arbiter

simple_bus_request *
simple_bus_arbiter::arbitrate(const simple_bus_request_vec &requests)
{
  int i;
  // at least one request is here
  simple_bus_request *best_request = requests[0];

  if (m_verbose)
    { // shows the list of pending requests
      sb_fprintf(stdout, "%g %s :", sc_simulation_time(), name());
      for (i = 0; i < requests.size(); ++i)
 {
   simple_bus_request *request = requests[i];
          // simple_bus_lock_status encoding
          const char lock_chars[] = { '-', '=', '+' };
          // simple_bus_status encoding
           sb_fprintf(stdout, "\n    R[%d](%c%s@%x)",
                     request->priority,
                     lock_chars[request->lock],
                     simple_bus_status_str[request->status],
                     request->address);
 }
    }

  // highest priority: status==SIMPLE_BUS_WAIT and lock is set: 
  // locked burst-action
  for (i = 0; i < requests.size(); ++i)
    {
      simple_bus_request *request = requests[i];
      if ((request->status == SIMPLE_BUS_WAIT) &&
   (request->lock == SIMPLE_BUS_LOCK_SET))
 {
   // cannot break-in a locked burst
   if (m_verbose)
            sb_fprintf(stdout, " -> R[%d] (rule 1)\n", request->priority);
   return request;
 }
    }

  // second priority: lock is set at previous call, 
  // i.e. SIMPLE_BUS_LOCK_GRANTED
  for (i = 0; i < requests.size(); ++i)
    if (requests[i]->lock == SIMPLE_BUS_LOCK_GRANTED)
      {
 if (m_verbose)
   sb_fprintf(stdout, " -> R[%d] (rule 2)\n", requests[i]->priority);
 return requests[i];
      }

  // third priority: priority
  for (i = 1; i < requests.size(); ++i)
    {
      sc_assert(requests[i]->priority != best_request->priority);
      if (requests[i]->priority < best_request->priority)
 best_request = requests[i];
    }

  if (best_request->lock != SIMPLE_BUS_LOCK_NO)
    best_request->lock = SIMPLE_BUS_LOCK_GRANTED;

  if (m_verbose)
    sb_fprintf(stdout, " -> R[%d] (rule 3)\n", best_request->priority);

  return best_request;
}

/*****************************************************************************

  The following code is derived, directly or indirectly, from the SystemC
  source code Copyright (c) 1996-2004 by all Contributors.
  All Rights reserved.

  The contents of this file are subject to the restrictions and limitations
  set forth in the SystemC Open Source License Version 2.3 (the "License");
  You may not use this file except in compliance with such restrictions and
  limitations. You may obtain instructions on how to receive a copy of the
  License at http://www.systemc.org/. Software distributed by Contributors
  under the License is distributed on an "AS IS" basis, WITHOUT WARRANTY OF
  ANY KIND, either express or implied. See the License for the specific
  language governing rights and limitations under the License.

 *****************************************************************************/

/*****************************************************************************
 
  simple_bus_master_blocking.cpp : The master using the blocking BUS interface.
 
  Original Author: Ric Hilderink, Synopsys, Inc., 2001-10-11
 
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
  set forth in the SystemC Open Source License Version 2.3 (the "License");
  You may not use this file except in compliance with such restrictions and
  limitations. You may obtain instructions on how to receive a copy of the
  License at http://www.systemc.org/. Software distributed by Contributors
  under the License is distributed on an "AS IS" basis, WITHOUT WARRANTY OF
  ANY KIND, either express or implied. See the License for the specific
  language governing rights and limitations under the License.

 *****************************************************************************/

/*****************************************************************************
 
  simple_bus_master_blocking.h : The master using the blocking BUS interface.
 
  Original Author: Ric Hilderink, Synopsys, Inc., 2001-10-11
 
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
  set forth in the SystemC Open Source License Version 2.3 (the "License");
  You may not use this file except in compliance with such restrictions and
  limitations. You may obtain instructions on how to receive a copy of the
  License at http://www.systemc.org/. Software distributed by Contributors
  under the License is distributed on an "AS IS" basis, WITHOUT WARRANTY OF
  ANY KIND, either express or implied. See the License for the specific
  language governing rights and limitations under the License.

 *****************************************************************************/

/*****************************************************************************
 
  simple_bus_types.h : The common types.
 
  Original Author: Ric Hilderink, Synopsys, Inc., 2001-10-11
 
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
  set forth in the SystemC Open Source License Version 2.3 (the "License");
  You may not use this file except in compliance with such restrictions and
  limitations. You may obtain instructions on how to receive a copy of the
  License at http://www.systemc.org/. Software distributed by Contributors
  under the License is distributed on an "AS IS" basis, WITHOUT WARRANTY OF
  ANY KIND, either express or implied. See the License for the specific
  language governing rights and limitations under the License.

 *****************************************************************************/

/*****************************************************************************
 
  simple_bus_blocking_if.h : The blocking bus interface.
 
  Original Author: Ric Hilderink, Synopsys, Inc., 2001-10-11
 
 *****************************************************************************/

/*****************************************************************************
 
  MODIFICATION LOG - modifiers, enter your name, affiliation, date and
  changes you are making here.
 
      Name, Affiliation, Date:
  Description of Modification:
 
 *****************************************************************************/


SC_MODULE(simple_bus_master_blocking)
{
  // ports
  sc_in_clk clock;
  sc_port<simple_bus_blocking_if> bus_port;

  SC_HAS_PROCESS(simple_bus_master_blocking);

  // constructor
  simple_bus_master_blocking(sc_module_name name_
        , unsigned int unique_priority
        , unsigned int address
                             , bool lock
                             , int timeout)
    : sc_module(name_)
    , m_unique_priority(unique_priority)
    , m_address(address)
    , m_lock(lock)
    , m_timeout(timeout)
  {
    // process declaration
    SC_THREAD(main_action);
    sensitive_pos << clock;
  }

  // process
  void main_action();

private:
  unsigned int m_unique_priority;
  unsigned int m_address;
  bool m_lock;
  int m_timeout;

}; // end class simple_bus_master_blocking

void simple_bus_master_blocking::main_action()
{
  const unsigned int mylength = 0x10; // storage capacity/burst length in words
  int mydata[mylength];
  unsigned int i;
  simple_bus_status status;

  while (true)
    {
      wait(); // ... for the next rising clock edge
      status = bus_port->burst_read(m_unique_priority, mydata,
        m_address, mylength, m_lock);
      if (status == SIMPLE_BUS_ERROR)
 sb_fprintf(stdout, "%g %s : blocking-read failed at address %x\n",
     sc_simulation_time(), name(), m_address);

      for (i = 0; i < mylength; ++i)
 {
   mydata[i] += i;
   wait();
 }

      status = bus_port->burst_write(m_unique_priority, mydata,
         m_address, mylength, m_lock);
      if (status == SIMPLE_BUS_ERROR)
 sb_fprintf(stdout, "%g %s : blocking-write failed at address %x\n",
     sc_simulation_time(), name(), m_address);

      wait(m_timeout, SC_NS);
    }
}

/*****************************************************************************

  The following code is derived, directly or indirectly, from the SystemC
  source code Copyright (c) 1996-2004 by all Contributors.
  All Rights reserved.

  The contents of this file are subject to the restrictions and limitations
  set forth in the SystemC Open Source License Version 2.3 (the "License");
  You may not use this file except in compliance with such restrictions and
  limitations. You may obtain instructions on how to receive a copy of the
  License at http://www.systemc.org/. Software distributed by Contributors
  under the License is distributed on an "AS IS" basis, WITHOUT WARRANTY OF
  ANY KIND, either express or implied. See the License for the specific
  language governing rights and limitations under the License.

 *****************************************************************************/

/*****************************************************************************
 
  simple_bus_master_direct.cpp : The monitor (master) using the direct BUS
                                 interface.
 
  Original Author: Ric Hilderink, Synopsys, Inc., 2001-10-11
 
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
  set forth in the SystemC Open Source License Version 2.3 (the "License");
  You may not use this file except in compliance with such restrictions and
  limitations. You may obtain instructions on how to receive a copy of the
  License at http://www.systemc.org/. Software distributed by Contributors
  under the License is distributed on an "AS IS" basis, WITHOUT WARRANTY OF
  ANY KIND, either express or implied. See the License for the specific
  language governing rights and limitations under the License.

 *****************************************************************************/

/*****************************************************************************
 
  simple_bus_master_direct.h : The monitor (master) using the direct BUS
                               interface.
 
  Original Author: Ric Hilderink, Synopsys, Inc., 2001-10-11
 
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
  set forth in the SystemC Open Source License Version 2.3 (the "License");
  You may not use this file except in compliance with such restrictions and
  limitations. You may obtain instructions on how to receive a copy of the
  License at http://www.systemc.org/. Software distributed by Contributors
  under the License is distributed on an "AS IS" basis, WITHOUT WARRANTY OF
  ANY KIND, either express or implied. See the License for the specific
  language governing rights and limitations under the License.

 *****************************************************************************/

/*****************************************************************************
 
  simple_bus_direct_if.h : The direct BUS/Slave interface.
 
  Original Author: Ric Hilderink, Synopsys, Inc., 2001-10-11
 
 *****************************************************************************/

/*****************************************************************************
 
  MODIFICATION LOG - modifiers, enter your name, affiliation, date and
  changes you are making here.
 
      Name, Affiliation, Date:
  Description of Modification:
 
 *****************************************************************************/


SC_MODULE(simple_bus_master_direct)
{
  // ports
  sc_in_clk clock;
  sc_port<simple_bus_direct_if> bus_port;

  SC_HAS_PROCESS(simple_bus_master_direct);

  // constructor
  simple_bus_master_direct(sc_module_name name_
                           , unsigned int address
                           , int timeout
                           , bool verbose = true)
    : sc_module(name_)
    , m_address(address)
    , m_timeout(timeout)
    , m_verbose(verbose)
  {
    // process declaration
    SC_THREAD(main_action);
  }

  // process
  void main_action();

private:
  unsigned int m_address;
  int m_timeout;
  bool m_verbose;

}; // end class simple_bus_master_direct

/*****************************************************************************

  The following code is derived, directly or indirectly, from the SystemC
  source code Copyright (c) 1996-2004 by all Contributors.
  All Rights reserved.

  The contents of this file are subject to the restrictions and limitations
  set forth in the SystemC Open Source License Version 2.3 (the "License");
  You may not use this file except in compliance with such restrictions and
  limitations. You may obtain instructions on how to receive a copy of the
  License at http://www.systemc.org/. Software distributed by Contributors
  under the License is distributed on an "AS IS" basis, WITHOUT WARRANTY OF
  ANY KIND, either express or implied. See the License for the specific
  language governing rights and limitations under the License.

 *****************************************************************************/

/*****************************************************************************
 
  simple_bus_types.h : The common types.
 
  Original Author: Ric Hilderink, Synopsys, Inc., 2001-10-11
 
 *****************************************************************************/

/*****************************************************************************
 
  MODIFICATION LOG - modifiers, enter your name, affiliation, date and
  changes you are making here.
 
      Name, Affiliation, Date:
  Description of Modification:
 
 *****************************************************************************/

void simple_bus_master_direct::main_action()
{
  int mydata[4];
  while (true)
    {
      bus_port->direct_read(&mydata[0], m_address);
      bus_port->direct_read(&mydata[1], m_address+4);
      bus_port->direct_read(&mydata[2], m_address+8);
      bus_port->direct_read(&mydata[3], m_address+12);

      if (m_verbose)
       sb_fprintf(stdout, "%g %s : mem[%x:%x] = (%x, %x, %x, %x)\n",
        sc_simulation_time(), name(), m_address, m_address+15,
        mydata[0], mydata[1], mydata[2], mydata[3]);

      wait(m_timeout, SC_NS);
    }
}

/*****************************************************************************

  The following code is derived, directly or indirectly, from the SystemC
  source code Copyright (c) 1996-2004 by all Contributors.
  All Rights reserved.

  The contents of this file are subject to the restrictions and limitations
  set forth in the SystemC Open Source License Version 2.3 (the "License");
  You may not use this file except in compliance with such restrictions and
  limitations. You may obtain instructions on how to receive a copy of the
  License at http://www.systemc.org/. Software distributed by Contributors
  under the License is distributed on an "AS IS" basis, WITHOUT WARRANTY OF
  ANY KIND, either express or implied. See the License for the specific
  language governing rights and limitations under the License.

 *****************************************************************************/

/*****************************************************************************
 
  simple_bus_master_non_blocking.cpp : The master using the non-blocking BUS
                                       interface.
 
  Original Author: Ric Hilderink, Synopsys, Inc., 2001-10-11
 
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
  set forth in the SystemC Open Source License Version 2.3 (the "License");
  You may not use this file except in compliance with such restrictions and
  limitations. You may obtain instructions on how to receive a copy of the
  License at http://www.systemc.org/. Software distributed by Contributors
  under the License is distributed on an "AS IS" basis, WITHOUT WARRANTY OF
  ANY KIND, either express or implied. See the License for the specific
  language governing rights and limitations under the License.

 *****************************************************************************/

/*****************************************************************************
 
  simple_bus_master_non_blocking.h : The master using the non-blocking BUS
                                     interface.
 
  Original Author: Ric Hilderink, Synopsys, Inc., 2001-10-11
 
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
  set forth in the SystemC Open Source License Version 2.3 (the "License");
  You may not use this file except in compliance with such restrictions and
  limitations. You may obtain instructions on how to receive a copy of the
  License at http://www.systemc.org/. Software distributed by Contributors
  under the License is distributed on an "AS IS" basis, WITHOUT WARRANTY OF
  ANY KIND, either express or implied. See the License for the specific
  language governing rights and limitations under the License.

 *****************************************************************************/

/*****************************************************************************
 
  simple_bus_types.h : The common types.
 
  Original Author: Ric Hilderink, Synopsys, Inc., 2001-10-11
 
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
  set forth in the SystemC Open Source License Version 2.3 (the "License");
  You may not use this file except in compliance with such restrictions and
  limitations. You may obtain instructions on how to receive a copy of the
  License at http://www.systemc.org/. Software distributed by Contributors
  under the License is distributed on an "AS IS" basis, WITHOUT WARRANTY OF
  ANY KIND, either express or implied. See the License for the specific
  language governing rights and limitations under the License.

 *****************************************************************************/

/*****************************************************************************
 
  simple_bus_non_blocking_if.h : The non-blocking BUS interface.
 
  Original Author: Ric Hilderink, Synopsys, Inc., 2001-10-11
 
 *****************************************************************************/

/*****************************************************************************
 
  MODIFICATION LOG - modifiers, enter your name, affiliation, date and
  changes you are making here.
 
      Name, Affiliation, Date:
  Description of Modification:
 
 *****************************************************************************/


SC_MODULE(simple_bus_master_non_blocking)
{
  // ports
  sc_in_clk clock;
  sc_port<simple_bus_non_blocking_if> bus_port;

  SC_HAS_PROCESS(simple_bus_master_non_blocking);

  // constructor
  simple_bus_master_non_blocking(sc_module_name _name
     , unsigned int unique_priority
                                 , unsigned int start_address
                                 , bool lock
                                 , int timeout)
    : sc_module(_name)
    , m_unique_priority(unique_priority)
    , m_start_address(start_address)
    , m_lock(lock)
    , m_timeout(timeout)
  {
    // process declaration
    SC_THREAD(main_action);
    sensitive_pos << clock;
  }

  // process
  void main_action();

private:
  unsigned int m_unique_priority;
  unsigned int m_start_address;
  bool m_lock;
  int m_timeout;

}; // end class simple_bus_master_non_blocking

void simple_bus_master_non_blocking::main_action()
{
  int mydata;
  int cnt = 0;
  unsigned int addr = m_start_address;

  wait(); // ... for the next rising clock edge
  while (true)
    {
      bus_port->read(m_unique_priority, &mydata, addr, m_lock);
      while ((bus_port->get_status(m_unique_priority) != SIMPLE_BUS_OK) &&
      (bus_port->get_status(m_unique_priority) != SIMPLE_BUS_ERROR))
 wait();
      if (bus_port->get_status(m_unique_priority) == SIMPLE_BUS_ERROR)
 sb_fprintf(stdout, "%g %s : ERROR cannot read from %x\n",
     sc_simulation_time(), name(), addr);

      mydata += cnt;
      cnt++;

      bus_port->write(m_unique_priority, &mydata, addr, m_lock);
      while ((bus_port->get_status(m_unique_priority) != SIMPLE_BUS_OK) &&
      (bus_port->get_status(m_unique_priority) != SIMPLE_BUS_ERROR))
 wait();
      if (bus_port->get_status(m_unique_priority) == SIMPLE_BUS_ERROR)
 sb_fprintf(stdout, "%g %s : ERROR cannot write to %x\n",
     sc_simulation_time(), name(), addr);

      wait(m_timeout, SC_NS);
      wait(); // ... for the next rising clock edge

      addr+=4; // next word (byte addressing)
      if (addr > (m_start_address+0x80)) {
        addr = m_start_address; cnt = 0;
      }
    }
}

/*****************************************************************************

  The following code is derived, directly or indirectly, from the SystemC
  source code Copyright (c) 1996-2004 by all Contributors.
  All Rights reserved.

  The contents of this file are subject to the restrictions and limitations
  set forth in the SystemC Open Source License Version 2.3 (the "License");
  You may not use this file except in compliance with such restrictions and
  limitations. You may obtain instructions on how to receive a copy of the
  License at http://www.systemc.org/. Software distributed by Contributors
  under the License is distributed on an "AS IS" basis, WITHOUT WARRANTY OF
  ANY KIND, either express or implied. See the License for the specific
  language governing rights and limitations under the License.

 *****************************************************************************/

/*****************************************************************************
 
  simple_bus_types.h : The common types.
 
  Original Author: Holger Keding, Synopsys, Inc., 2002-01-28
 
 *****************************************************************************/

/*****************************************************************************
 
  MODIFICATION LOG - modifiers, enter your name, affiliation, date and
  changes you are making here.
 
      Name, Affiliation, Date:
  Description of Modification:
 
 *****************************************************************************/

// only needed for more readable debug output
char simple_bus_status_str[4][20] = {"SIMPLE_BUS_OK"
                                      , "SIMPLE_BUS_REQUEST"
                                      , "SIMPLE_BUS_WAIT"
                                      , "SIMPLE_BUS_ERROR"};

/*****************************************************************************

  The following code is derived, directly or indirectly, from the SystemC
  source code Copyright (c) 1996-2004 by all Contributors.
  All Rights reserved.

  The contents of this file are subject to the restrictions and limitations
  set forth in the SystemC Open Source License Version 2.3 (the "License");
  You may not use this file except in compliance with such restrictions and
  limitations. You may obtain instructions on how to receive a copy of the
  License at http://www.systemc.org/. Software distributed by Contributors
  under the License is distributed on an "AS IS" basis, WITHOUT WARRANTY OF
  ANY KIND, either express or implied. See the License for the specific
  language governing rights and limitations under the License.

 *****************************************************************************/

/*****************************************************************************
 
  simple_bus_tools.cpp : The signal-safe fprintf function (sb_fprintf).
 
  Original Author: Ric Hilderink, Synopsys, Inc., 2001-10-11
 
 *****************************************************************************/

/*****************************************************************************
 
  MODIFICATION LOG - modifiers, enter your name, affiliation, date and
  changes you are making here.
 
      Name, Affiliation, Date:
  Description of Modification:
 
 *****************************************************************************/





int sb_fprintf(FILE *fp, const char *fmt, ...)
{
  va_list ap;
  va_start(ap, fmt);
  int ret = 0;
  do {
    errno = 0;
    ret = vfprintf(fp, fmt, ap);
  } while (errno == EINTR);
  return ret;
}

/*****************************************************************************

  The following code is derived, directly or indirectly, from the SystemC
  source code Copyright (c) 1996-2004 by all Contributors.
  All Rights reserved.

  The contents of this file are subject to the restrictions and limitations
  set forth in the SystemC Open Source License Version 2.3 (the "License");
  You may not use this file except in compliance with such restrictions and
  limitations. You may obtain instructions on how to receive a copy of the
  License at http://www.systemc.org/. Software distributed by Contributors
  under the License is distributed on an "AS IS" basis, WITHOUT WARRANTY OF
  ANY KIND, either express or implied. See the License for the specific
  language governing rights and limitations under the License.

 *****************************************************************************/

/*****************************************************************************
 
  simple_bus_main.cpp : sc_main
 
  Original Author: Ric Hilderink, Synopsys, Inc., 2001-10-11
 
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
  set forth in the SystemC Open Source License Version 2.3 (the "License");
  You may not use this file except in compliance with such restrictions and
  limitations. You may obtain instructions on how to receive a copy of the
  License at http://www.systemc.org/. Software distributed by Contributors
  under the License is distributed on an "AS IS" basis, WITHOUT WARRANTY OF
  ANY KIND, either express or implied. See the License for the specific
  language governing rights and limitations under the License.

 *****************************************************************************/

/*****************************************************************************
 
  simple_bus_test.h : The test bench.
 
  Original Author: Ric Hilderink, Synopsys, Inc., 2001-10-11
 
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
  set forth in the SystemC Open Source License Version 2.3 (the "License");
  You may not use this file except in compliance with such restrictions and
  limitations. You may obtain instructions on how to receive a copy of the
  License at http://www.systemc.org/. Software distributed by Contributors
  under the License is distributed on an "AS IS" basis, WITHOUT WARRANTY OF
  ANY KIND, either express or implied. See the License for the specific
  language governing rights and limitations under the License.

 *****************************************************************************/

/*****************************************************************************
 
  simple_bus_master_blocking.h : The master using the blocking BUS interface.
 
  Original Author: Ric Hilderink, Synopsys, Inc., 2001-10-11
 
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
  set forth in the SystemC Open Source License Version 2.3 (the "License");
  You may not use this file except in compliance with such restrictions and
  limitations. You may obtain instructions on how to receive a copy of the
  License at http://www.systemc.org/. Software distributed by Contributors
  under the License is distributed on an "AS IS" basis, WITHOUT WARRANTY OF
  ANY KIND, either express or implied. See the License for the specific
  language governing rights and limitations under the License.

 *****************************************************************************/

/*****************************************************************************
 
  simple_bus_master_non_blocking.h : The master using the non-blocking BUS
                                     interface.
 
  Original Author: Ric Hilderink, Synopsys, Inc., 2001-10-11
 
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
  set forth in the SystemC Open Source License Version 2.3 (the "License");
  You may not use this file except in compliance with such restrictions and
  limitations. You may obtain instructions on how to receive a copy of the
  License at http://www.systemc.org/. Software distributed by Contributors
  under the License is distributed on an "AS IS" basis, WITHOUT WARRANTY OF
  ANY KIND, either express or implied. See the License for the specific
  language governing rights and limitations under the License.

 *****************************************************************************/

/*****************************************************************************
 
  simple_bus_master_direct.h : The monitor (master) using the direct BUS
                               interface.
 
  Original Author: Ric Hilderink, Synopsys, Inc., 2001-10-11
 
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
  set forth in the SystemC Open Source License Version 2.3 (the "License");
  You may not use this file except in compliance with such restrictions and
  limitations. You may obtain instructions on how to receive a copy of the
  License at http://www.systemc.org/. Software distributed by Contributors
  under the License is distributed on an "AS IS" basis, WITHOUT WARRANTY OF
  ANY KIND, either express or implied. See the License for the specific
  language governing rights and limitations under the License.

 *****************************************************************************/

/*****************************************************************************
 
  simple_bus_slow_mem.h : Slave : The memory (slave) with wait states.
 
  Original Author: Ric Hilderink, Synopsys, Inc., 2001-10-11
 
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
  set forth in the SystemC Open Source License Version 2.3 (the "License");
  You may not use this file except in compliance with such restrictions and
  limitations. You may obtain instructions on how to receive a copy of the
  License at http://www.systemc.org/. Software distributed by Contributors
  under the License is distributed on an "AS IS" basis, WITHOUT WARRANTY OF
  ANY KIND, either express or implied. See the License for the specific
  language governing rights and limitations under the License.

 *****************************************************************************/

/*****************************************************************************
 
  simple_bus_types.h : The common types.
 
  Original Author: Ric Hilderink, Synopsys, Inc., 2001-10-11
 
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
  set forth in the SystemC Open Source License Version 2.3 (the "License");
  You may not use this file except in compliance with such restrictions and
  limitations. You may obtain instructions on how to receive a copy of the
  License at http://www.systemc.org/. Software distributed by Contributors
  under the License is distributed on an "AS IS" basis, WITHOUT WARRANTY OF
  ANY KIND, either express or implied. See the License for the specific
  language governing rights and limitations under the License.

 *****************************************************************************/

/*****************************************************************************
 
  simple_bus_slave_if.h : The Slave interface.
 
  Original Author: Ric Hilderink, Synopsys, Inc., 2001-10-11
 
 *****************************************************************************/

/*****************************************************************************
 
  MODIFICATION LOG - modifiers, enter your name, affiliation, date and
  changes you are making here.
 
      Name, Affiliation, Date:
  Description of Modification:
 
 *****************************************************************************/


class simple_bus_slow_mem
  : public simple_bus_slave_if
  , public sc_module
{
public:
  // ports
  sc_in_clk clock;

  SC_HAS_PROCESS(simple_bus_slow_mem);

  // constructor
  simple_bus_slow_mem(sc_module_name name_
        , unsigned int start_address
        , unsigned int end_address
        , unsigned int nr_wait_states)
    : sc_module(name_)
    , m_start_address(start_address)
    , m_end_address(end_address)
    , m_nr_wait_states(nr_wait_states)
    , m_wait_count(-1)
  {
    // process declaration
    SC_METHOD(wait_loop);
    dont_initialize();
    sensitive_pos << clock;

    sc_assert(m_start_address <= m_end_address);
    sc_assert((m_end_address-m_start_address+1)%4 == 0);
    unsigned int size = (m_end_address-m_start_address+1)/4;
    MEM = new int [size];
    for (unsigned int i = 0; i < size; ++i)
      MEM[i] = 0;
  }

  // destructor
  ~simple_bus_slow_mem();

  // process
  void wait_loop();

  // direct Slave Interface
  bool direct_read(int *data, unsigned int address);
  bool direct_write(int *data, unsigned int address);

  // Slave Interface
  simple_bus_status read(int *data, unsigned int address);
  simple_bus_status write(int *data, unsigned int address);

  unsigned int start_address() const;
  unsigned int end_address() const;

private:
  int *MEM;
  unsigned int m_start_address;
  unsigned int m_end_address;
  unsigned int m_nr_wait_states;
  int m_wait_count;

}; // end class simple_bus_slow_mem

inline simple_bus_slow_mem::~simple_bus_slow_mem()
{
  if (MEM) delete [] MEM;
  MEM = (int *)0;
}

inline void simple_bus_slow_mem::wait_loop()
{
  if (m_wait_count >= 0) m_wait_count--;
}

inline bool simple_bus_slow_mem::direct_read(int *data, unsigned int address)
{
  *data = MEM[(address - m_start_address)/4];
  return true;
}

inline bool simple_bus_slow_mem::direct_write(int *data, unsigned int address)
{
  MEM[(address - m_start_address)/4] = *data;
  return true;
}

inline simple_bus_status simple_bus_slow_mem::read(int *data
         , unsigned int address)
{
  // accept a new call if m_wait_count < 0)
  if (m_wait_count < 0)
    {
      m_wait_count = m_nr_wait_states;
      return SIMPLE_BUS_WAIT;
    }
  if (m_wait_count == 0)
    {
      *data = MEM[(address - m_start_address)/4];
      return SIMPLE_BUS_OK;
    }
  return SIMPLE_BUS_WAIT;
}

inline simple_bus_status simple_bus_slow_mem::write(int *data
          , unsigned int address)
{
  // accept a new call if m_wait_count < 0)
  if (m_wait_count < 0)
    {
      m_wait_count = m_nr_wait_states;
      return SIMPLE_BUS_WAIT;
    }
  if (m_wait_count == 0)
    {
      MEM[(address - m_start_address)/4] = *data;
      return SIMPLE_BUS_OK;
    }
  return SIMPLE_BUS_WAIT;
}


inline unsigned int simple_bus_slow_mem::start_address() const
{
  return m_start_address;
}

inline unsigned int simple_bus_slow_mem::end_address() const
{
  return m_end_address;
}

/*****************************************************************************

  The following code is derived, directly or indirectly, from the SystemC
  source code Copyright (c) 1996-2004 by all Contributors.
  All Rights reserved.

  The contents of this file are subject to the restrictions and limitations
  set forth in the SystemC Open Source License Version 2.3 (the "License");
  You may not use this file except in compliance with such restrictions and
  limitations. You may obtain instructions on how to receive a copy of the
  License at http://www.systemc.org/. Software distributed by Contributors
  under the License is distributed on an "AS IS" basis, WITHOUT WARRANTY OF
  ANY KIND, either express or implied. See the License for the specific
  language governing rights and limitations under the License.

 *****************************************************************************/

/*****************************************************************************
 
  simple_bus.h : The bus.

		 The bus is derived from the following interfaces, and
	         contains the implementation of these: 
		 - blocking : burst_read/burst_write
		 - non-blocking : read/write/get_status
		 - direct : direct_read/direct_write
 
  Original Author: Ric Hilderink, Synopsys, Inc., 2001-10-11
 
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
  set forth in the SystemC Open Source License Version 2.3 (the "License");
  You may not use this file except in compliance with such restrictions and
  limitations. You may obtain instructions on how to receive a copy of the
  License at http://www.systemc.org/. Software distributed by Contributors
  under the License is distributed on an "AS IS" basis, WITHOUT WARRANTY OF
  ANY KIND, either express or implied. See the License for the specific
  language governing rights and limitations under the License.

 *****************************************************************************/

/*****************************************************************************
 
  simple_bus_fast_mem.h : The memory (slave) without wait states.
 
  Original Author: Ric Hilderink, Synopsys, Inc., 2001-10-11
 
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
  set forth in the SystemC Open Source License Version 2.3 (the "License");
  You may not use this file except in compliance with such restrictions and
  limitations. You may obtain instructions on how to receive a copy of the
  License at http://www.systemc.org/. Software distributed by Contributors
  under the License is distributed on an "AS IS" basis, WITHOUT WARRANTY OF
  ANY KIND, either express or implied. See the License for the specific
  language governing rights and limitations under the License.

 *****************************************************************************/

/*****************************************************************************
 
  simple_bus_types.h : The common types.
 
  Original Author: Ric Hilderink, Synopsys, Inc., 2001-10-11
 
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
  set forth in the SystemC Open Source License Version 2.3 (the "License");
  You may not use this file except in compliance with such restrictions and
  limitations. You may obtain instructions on how to receive a copy of the
  License at http://www.systemc.org/. Software distributed by Contributors
  under the License is distributed on an "AS IS" basis, WITHOUT WARRANTY OF
  ANY KIND, either express or implied. See the License for the specific
  language governing rights and limitations under the License.

 *****************************************************************************/

/*****************************************************************************
 
  simple_bus_slave_if.h : The Slave interface.
 
  Original Author: Ric Hilderink, Synopsys, Inc., 2001-10-11
 
 *****************************************************************************/

/*****************************************************************************
 
  MODIFICATION LOG - modifiers, enter your name, affiliation, date and
  changes you are making here.
 
      Name, Affiliation, Date:
  Description of Modification:
 
 *****************************************************************************/


class simple_bus_fast_mem
  : public simple_bus_slave_if
  , public sc_module
{
public:
  // constructor
  simple_bus_fast_mem(sc_module_name name_
        , unsigned int start_address
        , unsigned int end_address)
    : sc_module(name_)
    , m_start_address(start_address)
    , m_end_address(end_address)
  {
    sc_assert(m_start_address <= m_end_address);
    sc_assert((m_end_address-m_start_address+1)%4 == 0);
    unsigned int size = (m_end_address-m_start_address+1)/4;
    MEM = new int [size];
    for (unsigned int i = 0; i < size; ++i)
      MEM[i] = 0;
  }

  // destructor
  ~simple_bus_fast_mem();

  // direct Slave Interface
  bool direct_read(int *data, unsigned int address);
  bool direct_write(int *data, unsigned int address);

  // Slave Interface
  simple_bus_status read(int *data, unsigned int address);
  simple_bus_status write(int *data, unsigned int address);

  unsigned int start_address() const;
  unsigned int end_address() const;

private:
  int * MEM;
  unsigned int m_start_address;
  unsigned int m_end_address;

}; // end class simple_bus_fast_mem

inline bool simple_bus_fast_mem::direct_read(int *data, unsigned int address)
{
  return (read(data, address) == SIMPLE_BUS_OK);
}

inline bool simple_bus_fast_mem::direct_write(int *data, unsigned int address)
{
  return (write(data, address) == SIMPLE_BUS_OK);
}

inline simple_bus_status simple_bus_fast_mem::read(int *data
         , unsigned int address)
{
  *data = MEM[(address - m_start_address)/4];
  return SIMPLE_BUS_OK;
}

inline simple_bus_status simple_bus_fast_mem::write(int *data
          , unsigned int address)
{
  MEM[(address - m_start_address)/4] = *data;
  return SIMPLE_BUS_OK;
}

inline simple_bus_fast_mem::~simple_bus_fast_mem()
{
  if (MEM) delete [] MEM;
  MEM = (int *)0;
}

inline unsigned int simple_bus_fast_mem::start_address() const
{
  return m_start_address;
}

inline unsigned int simple_bus_fast_mem::end_address() const
{
  return m_end_address;
}

/*****************************************************************************

  The following code is derived, directly or indirectly, from the SystemC
  source code Copyright (c) 1996-2004 by all Contributors.
  All Rights reserved.

  The contents of this file are subject to the restrictions and limitations
  set forth in the SystemC Open Source License Version 2.3 (the "License");
  You may not use this file except in compliance with such restrictions and
  limitations. You may obtain instructions on how to receive a copy of the
  License at http://www.systemc.org/. Software distributed by Contributors
  under the License is distributed on an "AS IS" basis, WITHOUT WARRANTY OF
  ANY KIND, either express or implied. See the License for the specific
  language governing rights and limitations under the License.

 *****************************************************************************/

/*****************************************************************************
 
  simple_bus_arbiter.h : The arbitration unit.
 
  Original Author: Ric Hilderink, Synopsys, Inc., 2001-10-11
 
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
  set forth in the SystemC Open Source License Version 2.3 (the "License");
  You may not use this file except in compliance with such restrictions and
  limitations. You may obtain instructions on how to receive a copy of the
  License at http://www.systemc.org/. Software distributed by Contributors
  under the License is distributed on an "AS IS" basis, WITHOUT WARRANTY OF
  ANY KIND, either express or implied. See the License for the specific
  language governing rights and limitations under the License.

 *****************************************************************************/

/*****************************************************************************
 
  simple_bus_test.h : The test bench.
 
  Original Author: Ric Hilderink, Synopsys, Inc., 2001-10-11
 
 *****************************************************************************/

/*****************************************************************************
 
  MODIFICATION LOG - modifiers, enter your name, affiliation, date and
  changes you are making here.
 
      Name, Affiliation, Date:
  Description of Modification:
 
 *****************************************************************************/

SC_MODULE(simple_bus_test)
{
  // channels
  sc_clock C1;

  // module instances
  simple_bus_master_blocking *master_b;
  simple_bus_master_non_blocking *master_nb;
  simple_bus_master_direct *master_d;
  simple_bus_slow_mem *mem_slow;
  simple_bus *bus;
  simple_bus_fast_mem *mem_fast;
  simple_bus_arbiter *arbiter;

  // constructor
  SC_CTOR(simple_bus_test)
    : C1("C1")
  {
    // create instances
    master_b = new simple_bus_master_blocking("master_b", 4, 0x4c, false, 300);
    master_nb = new simple_bus_master_non_blocking("master_nb", 3, 0x38, false, 20);
    master_d = new simple_bus_master_direct("master_d", 0x78, 100);
    mem_fast = new simple_bus_fast_mem("mem_fast", 0x00, 0x7f);
    mem_slow = new simple_bus_slow_mem("mem_slow", 0x80, 0xff, 1);
    // bus = new simple_bus("bus",true); // verbose output
    bus = new simple_bus("bus");
    // arbiter = new simple_bus_arbiter("arbiter",true); // verbose output
    arbiter = new simple_bus_arbiter("arbiter");

    // connect instances
    master_d->clock(C1);
    bus->clock(C1);
    master_b->clock(C1);
    master_nb->clock(C1);
    mem_slow->clock(C1);
    master_d->bus_port(*bus);
    master_b->bus_port(*bus);
    master_nb->bus_port(*bus);
    bus->arbiter_port(*arbiter);
    bus->slave_port(*mem_slow);
    bus->slave_port(*mem_fast);
  }

  // destructor
  ~simple_bus_test()
  {
    if (master_b) {delete master_b; master_b = 0;}
    if (master_nb) {delete master_nb; master_nb = 0;}
    if (master_d) {delete master_d; master_d = 0;}
    if (mem_slow) {delete mem_slow; mem_slow = 0;}
    if (bus) {delete bus; bus = 0;}
    if (mem_fast) {delete mem_fast; mem_fast = 0;}
    if (arbiter) {delete arbiter; arbiter = 0;}
  }

}; // end class simple_bus_test

int sc_main(int, char **)
{
  simple_bus_test top("top");

  sc_start(10000);

  return 0;
}
