



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
 
  display.cpp -- Implementation of the display module.
 
  Original Author: Amit Rao, Synopsys, Inc.
 
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
 
  display.h -- This is the interface file for the display module.
 
  Original Author: Amit Rao, Synopsys, Inc.
 
 *****************************************************************************/

/*****************************************************************************

  MODIFICATION LOG - modifiers, enter your name, affiliation, date and
  changes you are making here.

      Name, Affiliation, Date:
  Description of Modification:

 *****************************************************************************/




struct display : sc_module {
    sc_in<double> in; // input port 1
    sc_in<bool> clk; // clock

    void print_result(); // method to display input port values

    //Constructor
    SC_CTOR( display ) {
 SC_METHOD( print_result ); // declare print_result as SC_METHOD and 
        dont_initialize();
 sensitive_pos << clk; // make it sensitive to positive clock edge
    }

};



//Definition of print_result method 
void display::print_result()
{
    printf("Result = %f\n", in.read());
} // end of print method

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
 
  numgen.cpp -- The implementation for the numgen module.
 
  Original Author: Amit Rao, Synopsys, Inc.
 
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
 
  numgen.h -- This is the interface file for the numgen module.
 
  Original Author: Amit Rao, Synopsys, Inc.
 
 *****************************************************************************/

/*****************************************************************************

  MODIFICATION LOG - modifiers, enter your name, affiliation, date and
  changes you are making here.

      Name, Affiliation, Date:
  Description of Modification:

 *****************************************************************************/




struct numgen : sc_module {
    sc_out<double> out1; //output 1
    sc_out<double> out2; //output 2
    sc_in<bool> clk; //clock

    // method to write values to the output ports
    void generate();

    //Constructor
    SC_CTOR( numgen ) {
 SC_METHOD( generate ); //Declare generate as SC_METHOD and
        dont_initialize();
        sensitive_pos << clk; //make it sensitive to positive clock edge

    }
};

// definition of the `generate' method
void numgen::generate()
{
  static double a = 134.56;
  static double b = 98.24;

  a -= 1.5;
  b -= 2.8;
  out1.write(a);
  out2.write(b);

} // end of `generate' method

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
 
  stage1.cpp -- This is the implementation file for the stage1 module.
 
  Original Author: Amit Rao, Synopsys, Inc.
 
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
 
  stage1.h -- This is the interface file for the stage1 module.
 
  Original Author: Amit Rao, Synopsys, Inc.
 
 *****************************************************************************/

/*****************************************************************************

  MODIFICATION LOG - modifiers, enter your name, affiliation, date and
  changes you are making here.

      Name, Affiliation, Date:
  Description of Modification:

 *****************************************************************************/




struct stage1 : sc_module {
    sc_in<double> in1; //input 1
    sc_in<double> in2; //input 2
    sc_out<double> sum; //output 1
    sc_out<double> diff; //output 2
    sc_in<bool> clk; //clock

    void addsub(); //method implementing functionality

    //Counstructor
    SC_CTOR( stage1 ) {
        SC_METHOD( addsub ); //Declare addsub as SC_METHOD and  
        dont_initialize();
        sensitive_pos << clk; //make it sensitive to positive clock edge
    }

};

//Definition of addsub method
void stage1::addsub()
{
  double a;
  double b;

  a = in1.read();
  b = in2.read();
  sum.write(a+b);
  diff.write(a-b);

} // end of addsub method

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
 
  stage2.cpp -- This is the implementation file for the stage2 module.
 
  Original Author: Amit Rao, Synopsys, Inc.
 
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
 
  stage2.h -- This is the interface file for the stage2 module.
 
  Original Author: Amit Rao, Synopsys, Inc.
 
 *****************************************************************************/

/*****************************************************************************

  MODIFICATION LOG - modifiers, enter your name, affiliation, date and
  changes you are making here.

      Name, Affiliation, Date:
  Description of Modification:

 *****************************************************************************/




struct stage2 : sc_module {
    sc_in<double> sum; //input port 1
    sc_in<double> diff; //input port 2
    sc_out<double> prod; //output port 1
    sc_out<double> quot; //output port 2
    sc_in<bool> clk; //clock

    void multdiv(); //method providing functionality

    //Constructor
    SC_CTOR( stage2 ) {
 SC_METHOD( multdiv ); //Declare multdiv as SC_METHOD and
        dont_initialize();
        sensitive_pos << clk; //make it sensitive to positive clock edge. 
    }

};

//definition of multdiv method
void stage2::multdiv()
{
  double a;
  double b;

  a = sum.read();
  b = diff.read();
  if( b == 0 )
      b = 5.0;

  prod.write(a*b);
  quot.write(a/b);

} // end of multdiv

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
 
  stage3.cpp -- This is the implementation file for the stage3 module.
 
  Original Author: Amit Rao, Synopsys, Inc.
 
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
 
  stage3.h -- This is the interface file for the stage3 module.
 
  Original Author: Amit Rao, Synopsys, Inc.
 
 *****************************************************************************/

/*****************************************************************************

  MODIFICATION LOG - modifiers, enter your name, affiliation, date and
  changes you are making here.

      Name, Affiliation, Date:
  Description of Modification:

 *****************************************************************************/




struct stage3: sc_module {
    sc_in<double> prod; //input port 1
    sc_in<double> quot; //input port 2
    sc_out<double> powr; //output port 1 
    sc_in<bool> clk; //clock

    void power(); //method implementing functionality

    //Constructor
    SC_CTOR( stage3 ){
        SC_METHOD( power ); //declare power as SC_METHOD and 
        dont_initialize();
        sensitive_pos << clk; //make it sensitive to positive clock edge 
    }

};

//Definition of power method
void stage3::power()
{
  double a;
  double b;
  double c;

  a = prod.read();
  b = quot.read();
  c = (a>0 && b>0)? pow(a, b) : 0.;
  powr.write(c);

} // end of power method
