



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

  fft.cpp - This is the implementation file for the synchronous process "fft".

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

  fft.h - This is the interface file for the synchronous process "fft".

  Original Author: Rashmi Goswami, Synopsys, Inc.

 *****************************************************************************/

/*****************************************************************************

  MODIFICATION LOG - modifiers, enter your name, affiliation, date and
  changes you are making here.

      Name, Affiliation, Date:
  Description of Modification:

 *****************************************************************************/



struct fft: sc_module {
  sc_in<float> in_real;
  sc_in<float> in_imag;
  sc_in<bool> data_valid;
  sc_in<bool> data_ack;
  sc_out<float> out_real;
  sc_out<float> out_imag;
  sc_out<bool> data_req;
  sc_out<bool> data_ready;
  sc_in_clk CLK;


  SC_CTOR(fft)
    {
      SC_CTHREAD(entry, CLK.pos());
    }

 void entry();
};


void fft::entry()
{ float sample[16][2];
  unsigned int index;

  while(true)
  { data_req.write(false);
    data_ready.write(false);
    index = 0;
    //Reading in the Samples
      cout << endl << "Reading in the samples..." << endl;
      while( index < 16 )
      {
       data_req.write(true);
       wait_until(data_valid.delayed() == true);
       sample[index][0] = in_real.read();
       sample[index][1] = in_imag.read();
       index++;
       data_req.write(false);
       wait();
      }
      index = 0;


      //////////////////////////////////////////////////////////////////////////
      ///  Computation - 1D Complex DFT In-Place DIF Computation Algorithm  ////
      //////////////////////////////////////////////////////////////////////////

      //Size of FFT, N = 2**M    
       unsigned int N, M, len ;
       float theta;
       float W[7][2], w_real, w_imag, w_rec_real, w_rec_imag, w_temp;

       //Initialize
       M = 4; N = 16;
       len = N/2;
       theta = 8.0*atan(1.0)/N;

       cout << "Computing..." << endl;

       //Calculate the W-values recursively
        w_real = cos(theta);
        w_imag = -sin(theta);

        w_rec_real = 1;
        w_rec_imag = 0;

        index = 0;
        while(index < len-1)
        {
           w_temp = w_rec_real*w_real - w_rec_imag*w_imag;
           w_rec_imag = w_rec_real*w_imag + w_rec_imag*w_real;
           w_rec_real = w_temp;
           W[index][0] = w_rec_real;
           W[index][1] = w_rec_imag;
           index++;
        }


       float tmp_real, tmp_imag, tmp_real2, tmp_imag2;
       unsigned int stage, i, j,index2, windex, incr;

      //Begin Computation 
       stage = 0;

       len = N;
       incr = 1;

       while (stage < M)
       {
        len = len/2;

        //First Iteration :  With No Multiplies
          i = 0;


          while(i < N)
          {
             index = i; index2 = index + len;

             tmp_real = sample[index][0] + sample[index2][0];
             tmp_imag = sample[index][1] + sample[index2][1];

             sample[index2][0] = sample[index][0] - sample[index2][0];
             sample[index2][1] = sample[index][1] - sample[index2][1];

             sample[index][0] = tmp_real;
             sample[index][1] = tmp_imag;


             i = i + 2*len;

          }


        //Remaining Iterations: Use Stored W
         j = 1; windex = incr - 1;
         while (j < len) // This loop executes N/2 times at first stage, .. once at last stage.
         {
            i = j;
            while (i < N)
            {
              index = i;
              index2 = index + len;

              tmp_real = sample[index][0] + sample[index2][0];
              tmp_imag = sample[index][1] + sample[index2][1];
              tmp_real2 = sample[index][0] - sample[index2][0];
              tmp_imag2 = sample[index][1] - sample[index2][1];

              sample[index2][0] = tmp_real2*W[windex][0] - tmp_imag2*W[windex][1];
              sample[index2][1] = tmp_real2*W[windex][1] + tmp_imag2*W[windex][0];

              sample[index][0] = tmp_real;
              sample[index][1] = tmp_imag;

              i = i + 2*len;

            }
            windex = windex + incr;
            j++;
         }
          stage++;
          incr = 2*incr;
       }

     //////////////////////////////////////////////////////////////////////////

     //Writing out the normalized transform values in bit reversed order
      sc_uint<4> bits_i;
      sc_uint<4> bits_index;
      bits_i = 0;
      i = 0;

      cout << "Writing the transform values..." << endl;
      while( i < 16)
      {
       bits_i = i;
       bits_index[3]= bits_i[0];
       bits_index[2]= bits_i[1];
       bits_index[1]= bits_i[2];
       bits_index[0]= bits_i[3];
       index = bits_index;
       out_real.write(sample[index][0]);
       out_imag.write(sample[index][1]);
       data_ready.write(true);
       wait_until(data_ack.delayed() == true);
       data_ready.write(false);
       i++;
       wait();
      }
      index = 0;
      cout << "Done..." << endl;
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

  sink.cpp - This is the implementation file for the synchronous process
             "sink".

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

  sink.h - This is the interface file for the synchronous process "sink".

  Original Author: Rashmi Goswami, Synopsys, Inc.

 *****************************************************************************/

/*****************************************************************************

  MODIFICATION LOG - modifiers, enter your name, affiliation, date and
  changes you are making here.

      Name, Affiliation, Date:
  Description of Modification:

 *****************************************************************************/




struct sink: sc_module {
 sc_in<bool> data_ready;
 sc_out<bool> data_ack;
 sc_in<float> in_real;
 sc_in<float> in_imag;
 sc_in_clk CLK;

 FILE* fp_real;
 FILE* fp_imag;

 SC_CTOR(sink)
  {
     SC_CTHREAD(entry, CLK.pos());
  }

  void entry();

  ~sink()
  {
    fclose( fp_real );
    fclose( fp_imag );
  }
};

void sink::entry()
{
 fp_real = fopen("out_real","w");
 fp_imag = fopen("out_imag","w");

 data_ack.write(false);

 while(true)
 {
   wait_until(data_ready.delayed() == true);
   fprintf(fp_real,"%e  \n",in_real.read());
   fprintf(fp_imag,"%e  \n",in_imag.read());
   data_ack.write(true);
   wait_until(data_ready.delayed() == false);
   data_ack.write(false);
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

  source.cpp - This is the implementation file for the synchronous process
               "source" .

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

  source.h - This is the interface file for the synchronous process source.

  Original Author: Rashmi Goswami, Synopsys, Inc.

 *****************************************************************************/

/*****************************************************************************

  MODIFICATION LOG - modifiers, enter your name, affiliation, date and
  changes you are making here.

      Name, Affiliation, Date:
  Description of Modification:

 *****************************************************************************/



struct source: sc_module {
  sc_in<bool> data_req;
  sc_out<float> out_real;
  sc_out<float> out_imag;
  sc_out<bool> data_valid;
  sc_in_clk CLK;

  SC_CTOR(source)
    {
      SC_CTHREAD(entry, CLK.pos());
    }

 void entry();
};
void source::entry()
{ FILE *fp_real, *fp_imag;

  float tmp_val;

  fp_real = fopen("in_real", "r");
  fp_imag = fopen("in_imag", "r");

  data_valid.write(false);

  while(true)
  {
    wait_until(data_req.delayed() == true);
    if (fscanf(fp_real,"%f \n",&tmp_val) == EOF)
      { cout << "End of Input Stream: Simulation Stops" << endl;
        sc_stop();
        break;
 };
    out_real.write(tmp_val);
    if (fscanf(fp_imag,"%f \n",&tmp_val) == EOF)
      { cout << "End of Input Stream: Simulation Stops" << endl;
        sc_stop();
 break;
 };
    out_imag.write(tmp_val);
    data_valid.write(true);
    wait_until(data_req.delayed() == false);
    data_valid.write(false);
    wait();
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

  fft.h - This is the interface file for the synchronous process "fft".

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

  source.h - This is the interface file for the synchronous process source.

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

  sink.h - This is the interface file for the synchronous process "sink".

  Original Author: Rashmi Goswami, Synopsys, Inc.

 *****************************************************************************/

/*****************************************************************************

  MODIFICATION LOG - modifiers, enter your name, affiliation, date and
  changes you are making here.

      Name, Affiliation, Date:
  Description of Modification:

 *****************************************************************************/

int sc_main(int ac, char* av[])
{
 sc_signal<float> in_real;
 sc_signal<float> in_imag;
 sc_signal<bool> data_valid;
 sc_signal<bool> data_ack;
 sc_signal<float> out_real;
 sc_signal<float> out_imag;
 sc_signal<bool> data_req;
 sc_signal<bool> data_ready;

 sc_clock clock("CLOCK", 10, 0.5, 0.0);

 fft FFT1("FFTPROCESS");
 FFT1.in_real(in_real);
 FFT1.in_imag(in_imag);
 FFT1.data_valid(data_valid);
 FFT1.data_ack(data_ack);
 FFT1.out_real(out_real);
 FFT1.out_imag(out_imag);
 FFT1.data_req(data_req);
 FFT1.data_ready(data_ready);
 FFT1.CLK(clock);

 source SOURCE1("SOURCEPROCESS");
 SOURCE1.data_req(data_req);
 SOURCE1.out_real(in_real);
 SOURCE1.out_imag(in_imag);
 SOURCE1.data_valid(data_valid);
 SOURCE1.CLK(clock);

 sink SINK1("SINKPROCESS");
 SINK1.data_ready(data_ready);
 SINK1.data_ack(data_ack);
 SINK1.in_real(out_real);
 SINK1.in_imag(out_imag);
 SINK1.CLK(clock);

 sc_start(clock,-1);
 return 0;

}
