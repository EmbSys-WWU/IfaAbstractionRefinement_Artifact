



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
 
  stimulus.cpp -- 
 
  Original Author: Rocco Jonack, Synopsys, Inc.
 
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
 
  stimulus.h -- 
 
  Original Author: Rocco Jonack, Synopsys, Inc.
 
 *****************************************************************************/

/*****************************************************************************
 
  MODIFICATION LOG - modifiers, enter your name, affiliation, date and
  changes you are making here.
 
      Name, Affiliation, Date:
  Description of Modification:
    
 *****************************************************************************/



SC_MODULE(stimulus) {

  sc_out<bool> reset;
  sc_out<bool> input_valid;
  sc_out<int> sample;
  sc_in<bool> CLK;

  sc_int<8> send_value1;
  unsigned cycle;

  SC_CTOR(stimulus)
  {
      SC_METHOD(entry);
      dont_initialize();
      sensitive_pos(CLK);
      send_value1 = 0;
      cycle = 0;
  }
  void entry();
};

void stimulus::entry() {

  cycle++;
  // sending some reset values
  if (cycle<4) {
    reset.write(true);
    input_valid.write(false);
  } else {
    reset.write(false);
    input_valid.write( false );
    // sending normal mode values
    if (cycle%10==0) {
      input_valid.write(true);
      sample.write( (int)send_value1 );
      cout << "Stimuli : " << (int)send_value1 << " at time "
    /* << sc_time_stamp() << endl; */
    << sc_simulation_time() << endl;
      send_value1++;
    };
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
 
  display.cpp -- 
 
  Original Author: Rocco Jonack, Synopsys, Inc.

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
 
  display.h -- 
 
  Original Author: Rocco Jonack, Synopsys, Inc.

 *****************************************************************************/

/*****************************************************************************
 
  MODIFICATION LOG - modifiers, enter your name, affiliation, date and
  changes you are making here.
 
      Name, Affiliation, Date:
  Description of Modification:
    
 *****************************************************************************/




SC_MODULE(display) {

  sc_in<bool> output_data_ready;
  sc_in<int> result;

  int i, tmp1;

  SC_CTOR(display)
    {
      SC_METHOD(entry);
      dont_initialize();
      sensitive_pos(output_data_ready);
      i = 0;
    }

  void entry();
};

void display::entry(){

  //  Reading Data when valid if high
  tmp1 = result.read();
  cout << "Display : " << tmp1 << " "
       /* << " at time " << sc_time_stamp() << endl; */
       << " at time " << sc_simulation_time() << endl;
  i++;
  if(i == 24) {
    cout << "Simulation of " << i << " items finished"
  /* << " at time " << sc_time_stamp() << endl; */
  << " at time " << sc_simulation_time() << endl;
    sc_stop();
  };
}
// EOF

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
 
  fir_fsm.cpp -- 
 
  Original Author: Rocco Jonack, Synopsys, Inc.
 
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
 
  fir_fsm.h -- 
 
  Original Author: Rocco Jonack, Synopsys, Inc.
 
 *****************************************************************************/

/*****************************************************************************
 
  MODIFICATION LOG - modifiers, enter your name, affiliation, date and
  changes you are making here.
 
      Name, Affiliation, Date:
  Description of Modification:
    
 *****************************************************************************/




SC_MODULE(fir_fsm) {

  sc_in<bool> clock;
  sc_in<bool> reset;
  sc_in<bool> in_valid;
  sc_out<unsigned> state_out;

  // defining the states of the ste machine
  enum {reset_s, first_s, second_s, third_s, output_s} state;

  SC_CTOR(fir_fsm)
    {
      SC_METHOD(entry);
      dont_initialize();
      sensitive_pos(clock);
    };
  void entry();
};

void fir_fsm::entry() {

  sc_uint<3> state_tmp;

  // reset behavior
  if(reset.read()==true) {
    state = reset_s;
  } else {



    // main state machine 
    switch(state) {
    case reset_s:
      state = first_s;
      state_tmp = 0;
      state_out.write((unsigned)state_tmp);
      break;
    case first_s:
      if(in_valid.read()==true) {
 state = second_s;
      };
      state_tmp = 1;
      state_out.write((unsigned)state_tmp);
      break;
    case second_s:
      state = third_s;
      state_tmp = 2;
      state_out.write((unsigned)state_tmp);
      break;
    case third_s:
      state = output_s;
      state_tmp = 3;
      state_out.write((unsigned)state_tmp);
      break;
    default:
      state = first_s;
      state_tmp = 4;
      state_out.write((unsigned)state_tmp);
      break;
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
 
  fir_data.cpp -- 
 
  Original Author: Rocco Jonack, Synopsys, Inc.
 
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
 
  fir_data.h -- 
 
  Original Author: Rocco Jonack, Synopsys, Inc.
 
 *****************************************************************************/

/*****************************************************************************
 
  MODIFICATION LOG - modifiers, enter your name, affiliation, date and
  changes you are making here.
 
      Name, Affiliation, Date:
  Description of Modification:
 
 *****************************************************************************/




SC_MODULE(fir_data) {

  sc_in<bool> reset;
  sc_in<unsigned> state_out;
  sc_in<int> sample;
  sc_out<int> result;
  sc_out<bool> output_data_ready;

  sc_int<19> acc;
  sc_int<8> shift[16];
  sc_int<9> coefs[16];

  SC_CTOR(fir_data)
    {
      SC_METHOD(entry);
      dont_initialize();
      sensitive(reset);
      sensitive(state_out);
      sensitive(sample);

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
 
  fir_const.h -- 
 
  Original Author: Rocco Jonack, Synopsys, Inc.
 
 *****************************************************************************/

/*****************************************************************************
 
  MODIFICATION LOG - modifiers, enter your name, affiliation, date and
  changes you are making here.
 
      Name, Affiliation, Date:
  Description of Modification:
 
 *****************************************************************************/

coefs[0] = -6;
coefs[1] = -4;
coefs[2] = 13;
coefs[3] = 16;
coefs[4] = -18;
coefs[5] = -41;
coefs[6] = 23;
coefs[7] = 154;
coefs[8] = 222;
coefs[9] = 154;
coefs[10] = 23;
coefs[11] = -41;
coefs[12] = -18;
coefs[13] = 16;
coefs[14] = 13;
coefs[15] = -4;
    };
  void entry();
};

void fir_data::entry()
{
  int state;
  sc_int<8> sample_tmp;

  // reset functionality
  if(reset.read()==true) {
    sample_tmp = 0;
    acc = 0;
    for (int i=0; i<=15; i++)
      shift[i] = 0;
  }

  // default settings
  result.write(0);
  output_data_ready.write(false);
  state = state_out.read();
  // cycle behavior could be as well a case statement
  switch (state) {
  case 1 :
    sample_tmp = sample.read();
    acc = sample_tmp*coefs[0];
    acc += shift[14]* coefs[15];
    acc += shift[13]*coefs[14];
    acc += shift[12]*coefs[13];
    acc += shift[11]*coefs[12];
    break;
  case 2 :
    acc += shift[10]*coefs[11];
    acc += shift[9]*coefs[10];
    acc += shift[8]*coefs[9];
    acc += shift[7]*coefs[8];
    break;
  case 3 :
    acc += shift[6]*coefs[7];
    acc += shift[5]*coefs[6];
    acc += shift[4]*coefs[5];
    acc += shift[3]*coefs[4];
    break;
  case 4 :
    acc += shift[2]*coefs[3];
    acc += shift[1]*coefs[2];
    acc += shift[0]*coefs[1];
    for(int i=14; i>=0; i--) {
      shift[i+1] = shift[i];
    };
    shift[0] = sample.read();
    result.write((int)acc);
    output_data_ready.write(true);
    break;
  default :
    cout << "Information : Reset state" << endl;
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
 
  main_rtl.cpp -- 
 
  Original Author: Rocco Jonack, Synopsys, Inc.
 
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
 
  stimulus.h -- 
 
  Original Author: Rocco Jonack, Synopsys, Inc.
 
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
 
  display.h -- 
 
  Original Author: Rocco Jonack, Synopsys, Inc.

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
 
  fir_top.h --
 
  Original Author: Rocco Jonack, Synopsys, Inc.
 
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
 
  fir_fsm.h -- 
 
  Original Author: Rocco Jonack, Synopsys, Inc.
 
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
 
  fir_data.h -- 
 
  Original Author: Rocco Jonack, Synopsys, Inc.
 
 *****************************************************************************/

/*****************************************************************************
 
  MODIFICATION LOG - modifiers, enter your name, affiliation, date and
  changes you are making here.
 
      Name, Affiliation, Date:
  Description of Modification:
 
 *****************************************************************************/

SC_MODULE(fir_top) {

  sc_in<bool> CLK;
  sc_in<bool> RESET;
  sc_in<bool> IN_VALID;
  sc_in<int> SAMPLE;
  sc_out<bool> OUTPUT_DATA_READY;
  sc_out<int> RESULT;

  sc_signal<unsigned> state_out;

  fir_fsm *fir_fsm1;
  fir_data *fir_data1;

  SC_CTOR(fir_top) {

      fir_fsm1 = new fir_fsm("FirFSM");
      fir_fsm1->clock(CLK);
      fir_fsm1->reset(RESET);
      fir_fsm1->in_valid(IN_VALID);
      fir_fsm1->state_out(state_out);

      fir_data1 = new fir_data("FirData");
      fir_data1 -> reset(RESET);
      fir_data1 -> state_out(state_out);
      fir_data1 -> sample(SAMPLE);
      fir_data1 -> result(RESULT);
      fir_data1 -> output_data_ready(OUTPUT_DATA_READY);

    }
};

int sc_main (int argc , char *argv[]) {
  sc_clock clock;
  sc_signal<bool> reset;
  sc_signal<bool> input_valid;
  sc_signal<int> sample;
  sc_signal<bool> output_data_ready;
  sc_signal<int> result;

  stimulus stimulus1("stimulus_block");
  stimulus1.reset(reset);
  stimulus1.input_valid(input_valid);
  stimulus1.sample(sample);
  stimulus1.CLK(clock.signal());

  fir_top fir_top1 ( "process_body");
  fir_top1.RESET(reset);
  fir_top1.IN_VALID(input_valid);
  fir_top1.SAMPLE(sample);
  fir_top1.OUTPUT_DATA_READY(output_data_ready);
  fir_top1.RESULT(result);
  fir_top1.CLK(clock.signal());

  display display1 ( "display");
  display1.output_data_ready(output_data_ready);
  display1.result(result);

  sc_start(clock, -1);
  return 0;
}
