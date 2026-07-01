



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

  receiver.cpp - This is the implementation file for the asynchronous process
                 "receiver".

  Original Author: Rashmi Goswami, Synopsys, Inc.

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

  pkt.h - This file defines a SystemC structure "pkt".

  Original Author: Rashmi Goswami, Synopsys, Inc.

 *****************************************************************************/

/*****************************************************************************

  MODIFICATION LOG - modifiers, enter your name, affiliation, date and
  changes you are making here.

      Name, Affiliation, Date:
  Description of Modification:

 *****************************************************************************/






struct pkt {
       sc_int<8> data;
       sc_int<4> id;
       bool dest0;
       bool dest1;
       bool dest2;
       bool dest3;

       inline bool operator == (const pkt& rhs) const
   {
     return (rhs.data == data && rhs.id == id && rhs.dest0 == dest0 && rhs.dest1 == dest1 && rhs.dest2 == dest2 && rhs.dest3 == dest3);
   }

};

inline
ostream&
operator << ( ostream& os, const pkt& a )
{
    os << "streaming of struct pkt not implemented";
    return os;
}

inline
void
sc_trace( sc_trace_file* tf, const pkt& a, const sc_string& name )
{
  sc_trace( tf, a.data, name + ".data" );
  sc_trace( tf, a.id, name + ".id" );
  sc_trace( tf, a.dest0, name + ".dest0" );
  sc_trace( tf, a.dest1, name + ".dest1" );
  sc_trace( tf, a.dest2, name + ".dest2" );
  sc_trace( tf, a.dest3, name + ".dest3" );
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

  receiver.h - This is the interface file for the asynchronous process
               "receiver".

  Original Author: Rashmi Goswami, Synopsys, Inc.

 *****************************************************************************/

/*****************************************************************************

  MODIFICATION LOG - modifiers, enter your name, affiliation, date and
  changes you are making here.

      Name, Affiliation, Date:
  Description of Modification:

 *****************************************************************************/



struct receiver: sc_module {
  sc_in<pkt> pkt_in;
  sc_in<sc_int<4> > sink_id;
  int first;
  SC_CTOR(receiver) {
      SC_METHOD(entry);
      dont_initialize();
      sensitive(pkt_in);
      first = 1;
    }
 void entry();
};


void receiver:: entry()
{ pkt temp_val;
  // Ignore the first packet arriving on start-on
  if (first == 1) {first = 0;}
  else {
 temp_val = pkt_in.read();
 cout << "                                  .........................." << endl;
 cout << "                                  New Packet Received" << endl;
 cout << "                                  Receiver ID: " << (int)sink_id.read() + 1 << endl;
 cout << "                                  Packet Value: " << (int)temp_val.data << endl;
 cout << "                                  Sender ID: " << (int)temp_val.id + 1 << endl;
 cout << "                                  .........................." << endl;
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

  fifo.cpp - This is the functionality file for the SystemC structure "fifo".

  Original Author: Rashmi Goswami, Synopsys, Inc.

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

  pkt.h - This file defines a SystemC structure "pkt".

  Original Author: Rashmi Goswami, Synopsys, Inc.

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

  fifo.h - This is the header file for the SystemC structure "fifo".

  Original Author: Rashmi Goswami, Synopsys, Inc.

 *****************************************************************************/

/*****************************************************************************

  MODIFICATION LOG - modifiers, enter your name, affiliation, date and
  changes you are making here.

      Name, Affiliation, Date:
  Description of Modification:

 *****************************************************************************/




struct fifo {

   pkt regs[4];
   bool full;
   bool empty;
   sc_uint<3> pntr;

   // constructor

   fifo()
    {
      full = false;
      empty = true;
      pntr = 0;
    };

  // methods

   void pkt_in(const pkt& data_pkt);

   pkt pkt_out();
};

    void fifo::pkt_in(const pkt& data_pkt)
    {
      regs[pntr++] = data_pkt; empty = false;
      if (pntr == 4) full = true;
    }

    pkt fifo::pkt_out()
    {
       pkt temp;
       temp = regs[0];
       if (--pntr == 0) empty = true;
       else
 {
            regs[0] = regs[1];
     regs[1] = regs[2];
     regs[2] = regs[3];
        }
      return(temp);
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

  sender.cpp - This is the implementation file for the synchronous process
               "sender".

  Original Author: Rashmi Goswami, Synopsys, Inc.

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

  pkt.h - This file defines a SystemC structure "pkt".

  Original Author: Rashmi Goswami, Synopsys, Inc.

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

  sender.h - This is the interface file for the synchronous process "sender".

  Original Author: Rashmi Goswami, Synopsys, Inc.

 *****************************************************************************/

/*****************************************************************************

  MODIFICATION LOG - modifiers, enter your name, affiliation, date and
  changes you are making here.

      Name, Affiliation, Date:
  Description of Modification:

 *****************************************************************************/



struct sender: sc_module {
  sc_out<pkt> pkt_out;
  sc_in<sc_int<4> > source_id;
  sc_in_clk CLK;

  SC_CTOR(sender)
    {
      SC_CTHREAD(entry, CLK.pos());
     }
  void entry();
};



void sender:: entry()
{
   pkt pkt_data;
   sc_uint<4> dest;

   srand((unsigned)time(NULL));
   wait(8);
   while(true)
     {
       /////generate an 8-bit random value for data//////////
       pkt_data.data = rand()%255;

       ////stamp the sender's id
       pkt_data.id = source_id.read();


       /////send it to 1 or more(<=4) destinations//////////////
       dest = rand()%15 + 1;
       pkt_data.dest0 = dest[0];
       pkt_data.dest1 = dest[1];
       pkt_data.dest2 = dest[2];
       pkt_data.dest3 = dest[3];

       pkt_out.write(pkt_data);

        cout << ".........................." << endl;
 cout << "New Packet Sent" << endl;
 cout << "Destination Addresses: ";
        if (dest[0]) cout << 1 << " " ;
        if (dest[1]) cout << 2 << " ";
        if (dest[2]) cout << 3 << " ";
 if (dest[3]) cout << 4 << " ";
        cout << endl;
 cout << "Packet Value: " << (int)pkt_data.data << endl;
 cout << "Sender ID: " << (int)source_id.read() + 1 << endl;
 cout << ".........................." << endl;

       wait();

       /////wait for 1 to 3 clock periods/////////////////////
       wait(1+(rand()%3));
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

  switch.cpp - This is the implementation file for the asynchronous process
               "switch".

  Original Author: Rashmi Goswami, Synopsys, Inc.

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

  pkt.h - This file defines a SystemC structure "pkt".

  Original Author: Rashmi Goswami, Synopsys, Inc.

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

  fifo.h - This is the header file for the SystemC structure "fifo".

  Original Author: Rashmi Goswami, Synopsys, Inc.

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

  switch_reg.h - This file describes a SystemC structure "switch_reg".

  Original Author: Rashmi Goswami, Synopsys, Inc.

 *****************************************************************************/

/*****************************************************************************

  MODIFICATION LOG - modifiers, enter your name, affiliation, date and
  changes you are making here.

      Name, Affiliation, Date:
  Description of Modification:

 *****************************************************************************/




struct switch_reg {
   pkt val;
   bool free;

// constructor
   switch_reg()
    {
      free = true;
    }
};

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

  switch.h - This is the interface file for the asynchronous process "switch".

  Original Author: Rashmi Goswami, Synopsys, Inc.

 *****************************************************************************/

/*****************************************************************************

  MODIFICATION LOG - modifiers, enter your name, affiliation, date and
  changes you are making here.

      Name, Affiliation, Date:
  Description of Modification:

 *****************************************************************************/



struct mcast_pkt_switch : sc_module {

    sc_in<bool> switch_cntrl;
    sc_in<pkt> in0;
    sc_in<pkt> in1;
    sc_in<pkt> in2;
    sc_in<pkt> in3;

    sc_out<pkt> out0;
    sc_out<pkt> out1;
    sc_out<pkt> out2;
    sc_out<pkt> out3;

    SC_CTOR(mcast_pkt_switch)
     {
      SC_THREAD(entry);
      sensitive << in0;
      sensitive << in1;
      sensitive << in2;
      sensitive << in3;
      sensitive_pos << switch_cntrl;
    }

  void entry();

};


void mcast_pkt_switch :: entry()
{
  wait();

  // declarations
  switch_reg R0;
  switch_reg R1;
  switch_reg R2;
  switch_reg R3;
  switch_reg temp;

  int sim_count;
  int pkt_count;
  int drop_count;

  fifo q0_in;
  fifo q1_in;
  fifo q2_in;
  fifo q3_in;

  fifo q0_out;
  fifo q1_out;
  fifo q2_out;
  fifo q3_out;

  // FILE *result;

  // initialization
  pkt_count = 0;
  drop_count = 0;
  sim_count = 0;

  q0_in.pntr = 0;
  q1_in.pntr = 0;
  q2_in.pntr = 0;
  q3_in.pntr = 0;

  q0_out.pntr = 0;
  q1_out.pntr = 0;
  q2_out.pntr = 0;
  q3_out.pntr = 0;

  q0_in.full = false;
  q1_in.full = false;
  q2_in.full = false;
  q3_in.full = false;

  q0_in.empty = true;
  q1_in.empty = true;
  q2_in.empty = true;
  q3_in.empty = true;

  q0_out.full = false;
  q1_out.full = false;
  q2_out.full = false;
  q3_out.full = false;

  q0_out.empty = true;
  q1_out.empty = true;
  q2_out.empty = true;
  q3_out.empty = true;

  R0.free = true;
  R1.free = true;
  R2.free = true;
  R3.free = true;

  // result = fopen("result","w");

  cout << endl;
  cout << "-------------------------------------------------------------------------------" << endl;
  cout << endl << "             4x4 Multicast Helix Packet Switch Simulation" << endl;
  cout << "-------------------------------------------------------------------------------" << endl;
  cout << "  This is the simulation of a 4x4 non-blocking multicast helix packet switch.  " << endl;
  cout << "  The switch uses a self-routing ring of shift registers to transfer cells     " << endl;
  cout << "  from one port to another in a pipelined fashion, resolving output contention " << endl;
  cout << "  and handling multicast switch efficiently." << endl << endl;

  cout << "  Press \"Return\" key to start the simulation..." << endl << endl;

  getchar();

  wait();
  // functionality
  while( sim_count++ < 500 )
    {
       wait();

       /////read input packets//////////////////////////////////     
      if (in0.event())
       {
  pkt_count++;
  if (q0_in.full == true) drop_count++;
         else q0_in.pkt_in(in0.read());
       };

       if (in1.event())
       {
  pkt_count++;
  if (q1_in.full == true) drop_count++;
         else q1_in.pkt_in(in1.read());
       };

       if (in2.event())
       {
  pkt_count++;
  if (q2_in.full == true) drop_count++;
         else q2_in.pkt_in(in2.read());
       };

       if (in3.event())
       {
  pkt_count++;
  if (q3_in.full == true) drop_count++;
         else q3_in.pkt_in(in3.read());
       };

      /////move the packets from fifo to shift register ring/////

      if((!q0_in.empty) && R0.free)
 {
          R0.val = q0_in.pkt_out();
   R0.free = false;
 }

      if((!q1_in.empty) && R1.free)
 {
          R1.val = q1_in.pkt_out();
   R1.free = false;
 }
      if((!q2_in.empty) && R2.free)
 {
          R2.val = q2_in.pkt_out();
   R2.free = false;
 }
      if((!q3_in.empty) && R3.free)
 {
          R3.val = q3_in.pkt_out();
   R3.free = false;
 }

      if((bool)switch_cntrl && switch_cntrl.event())
 {
            /////shift the channel registers /////////////////////////
            temp = R0;
            R0 = R1;
     R1 = R2;
     R2 = R3;
     R3 = temp;

     /////write the register values to output fifos////////////
     if ((!R0.free) && (R0.val.dest0) && (!q0_out.full))
       {
  q0_out.pkt_in(R0.val);
  R0.val.dest0 = false;
  if (!(R0.val.dest0|R0.val.dest1|R0.val.dest2|R0.val.dest3)) R0.free = true;
       }

     if ((!R1.free) && (R1.val.dest1) && (!q1_out.full))
       {
  q1_out.pkt_in(R1.val);
  R1.val.dest1 = false;
  if (!(R1.val.dest1|R1.val.dest1|R1.val.dest2|R1.val.dest3)) R1.free = true;
       }
     if ((!R2.free) && (R2.val.dest2) && (!q2_out.full))
       {
  q2_out.pkt_in(R2.val);
  R2.val.dest2 = false;
  if (!(R2.val.dest2|R2.val.dest1|R2.val.dest2|R2.val.dest3)) R2.free = true;
       }
     if ((!R3.free) && (R3.val.dest3) && (!q3_out.full))
       {
  q3_out.pkt_in(R3.val);
  R3.val.dest3 = false;
  if (!(R3.val.dest3|R3.val.dest1|R3.val.dest2|R3.val.dest3)) R3.free = true;
       }

     /////write the packets out//////////////////////////////////    
     if (!q0_out.empty) out0.write(q0_out.pkt_out());
     if (!q1_out.empty) out1.write(q1_out.pkt_out());
     if (!q2_out.empty) out2.write(q2_out.pkt_out());
     if (!q3_out.empty) out3.write(q3_out.pkt_out());
 }
    }

  sc_stop();

  cout << endl << endl << "-------------------------------------------------------------------------------" << endl;
  cout << "End of switch operation..." << endl;
  cout << "Total number of packets received: " << pkt_count << endl;
  cout << "Total number of packets dropped: " << drop_count << endl;
  cout << "Percentage packets dropped:  " << drop_count*100/pkt_count << endl;
  cout << "-------------------------------------------------------------------------------" << endl;

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

  switch_clk.cpp - This is the implementation file for the synchronous process
                   "switch_clk".

  Original Author: Rashmi Goswami, Synopsys, Inc.

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

  switch_clk.h - This is the interface file for the synchronous process 
                 "switch_clk".

  Original Author: Rashmi Goswami, Synopsys, Inc.

 *****************************************************************************/

/*****************************************************************************

  MODIFICATION LOG - modifiers, enter your name, affiliation, date and
  changes you are making here.

      Name, Affiliation, Date:
  Description of Modification:

 *****************************************************************************/




struct switch_clk: sc_module {
      sc_out<bool> switch_cntrl;
      sc_in_clk CLK;

      SC_CTOR(switch_clk)
        {
           SC_METHOD(entry);
           dont_initialize();
           sensitive_pos(CLK);
         }
     void entry();
};

void switch_clk::entry()
 {
  static bool var_switch_cntrl = false;
    {
       switch_cntrl = var_switch_cntrl;
       var_switch_cntrl = !var_switch_cntrl;
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

  main.cpp - This file instantiates all processes and ties them together
             with signals.

  Original Author: Rashmi Goswami, Synopsys, Inc.

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

  pkt.h - This file defines a SystemC structure "pkt".

  Original Author: Rashmi Goswami, Synopsys, Inc.

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

  switch_clk.h - This is the interface file for the synchronous process 
                 "switch_clk".

  Original Author: Rashmi Goswami, Synopsys, Inc.

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

  sender.h - This is the interface file for the synchronous process "sender".

  Original Author: Rashmi Goswami, Synopsys, Inc.

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

  receiver.h - This is the interface file for the asynchronous process
               "receiver".

  Original Author: Rashmi Goswami, Synopsys, Inc.

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

  switch.h - This is the interface file for the asynchronous process "switch".

  Original Author: Rashmi Goswami, Synopsys, Inc.

 *****************************************************************************/

/*****************************************************************************

  MODIFICATION LOG - modifiers, enter your name, affiliation, date and
  changes you are making here.

      Name, Affiliation, Date:
  Description of Modification:

 *****************************************************************************/


int
sc_main(int argc, char *argv[])
{
  sc_signal<pkt> pkt_in0;
  sc_signal<pkt> pkt_in1;
  sc_signal<pkt> pkt_in2;
  sc_signal<pkt> pkt_in3;
  sc_signal<pkt> pkt_out0;
  sc_signal<pkt> pkt_out1;
  sc_signal<pkt> pkt_out2;
  sc_signal<pkt> pkt_out3;

  sc_signal<sc_int<4> > id0, id1, id2, id3;

  sc_signal<bool> switch_cntrl;

  sc_clock clock1("CLOCK1", 75, 0.5, 0.0);
  sc_clock clock2("CLOCK2", 30, 0.5, 10.0);

  // Module instiatiations follow
  // Note that modules can be connected by hooking up ports 
  // to signals by name or by using a positional notation

  sender sender0("SENDER0");
  // hooking up signals to ports by name
  sender0.pkt_out(pkt_in0);
  sender0.source_id(id0);
  sender0.CLK(clock1);

  sender sender1("SENDER1");
  // hooking up signals to ports by position
  sender1 << pkt_in1 << id1 << clock1;

  sender sender2("SENDER2");
  // hooking up signals to ports by name
  sender2.pkt_out(pkt_in2);
  sender2.source_id(id2);
  sender2.CLK(clock1);

  sender sender3("SENDER3");
  // hooking up signals to ports by position
  sender3 << pkt_in3 << id3 << clock1;

  switch_clk switch_clk1("SWITCH_CLK");
  // hooking up signals to ports by name
  switch_clk1.switch_cntrl(switch_cntrl);
  switch_clk1.CLK(clock2);

  mcast_pkt_switch switch1("SWITCH");
  // hooking up signals to ports by name
  switch1.switch_cntrl(switch_cntrl);
  switch1.in0(pkt_in0);
  switch1.in1(pkt_in1);
  switch1.in2(pkt_in2);
  switch1.in3(pkt_in3);
  switch1.out0(pkt_out0);
  switch1.out1(pkt_out1);
  switch1.out2(pkt_out2);
  switch1.out3(pkt_out3);

  receiver receiver0("RECEIVER0");
  // hooking up signals to ports by name  
  receiver0.pkt_in(pkt_out0);
  receiver0.sink_id(id0);

  receiver receiver1("RECEIVER1");
  // hooking up signals to ports by position
  receiver1 << pkt_out1 << id1;

  receiver receiver2("RECEIVER2");
  // hooking up signals to ports by name
  receiver2.pkt_in(pkt_out2);
  receiver2.sink_id(id2);

  receiver receiver3("RECEIVER3");
  // hooking up signals to ports by position
  receiver3 << pkt_out3 << id3;

  sc_initialize();


  id0.write(0);
  id1.write(1);
  id2.write(2);
  id3.write(3);
  sc_clock::start(-1);
  return 0;

}
