



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
 
  bios.cpp -- System Bios.
 
  Original Author: Martin Wang, Synopsys, Inc.
 
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
 
  bios.h -- System Bios Unit.
 
  Original Author: Martin Wang, Synopsys, Inc.
 
 *****************************************************************************/

/*****************************************************************************
 
  MODIFICATION LOG - modifiers, enter your name, affiliation, date and
  changes you are making here.
 
      Name, Affiliation, Date:
  Description of Modification:
 
 *****************************************************************************/






struct bios : sc_module {
   sc_in<unsigned > datain; // modified instruction
   sc_in<bool> cs; // chip select
   sc_in<bool> we; // write enable for SMC
   sc_in<unsigned > addr; // physical address

   sc_out<unsigned > dataout; // ram data out
   sc_out<bool> bios_valid; // out valid
   sc_out<bool> stall_fetch; // stall fetch if output not valid
 sc_in_clk CLK;

  // Parameter
  unsigned *imemory; // BIOS program data memory
  unsigned *itagmemory; // program tag memory (NOT USED)
  int wait_cycles; // Cycle # it takes to access memory

  void init_param(int given_delay_cycles) {
    wait_cycles = given_delay_cycles;
  }

  //Constructor 
  SC_CTOR(bios) {
        SC_CTHREAD(entry, CLK.pos());

 // initialize instruction imemory from external file
 FILE *fp = fopen("bios","r");
 int size=0;
 int mem_word;
   imemory = new unsigned[4000];
   itagmemory = new unsigned[4000];
 printf("** ALERT ** BIOS: initialize BIOS\n");
 for (size = 0; size < 4000; size++) { // initialize bad data
  imemory[size] = 0xffffffff;
  itagmemory[size] = 0xffffffff;
 }
 size = 0;
 while (fscanf(fp,"%x\n", &mem_word) != EOF) {
  imemory[size] = mem_word;
  itagmemory[size] = size;
  size++;
 }
  }

  // Process functionality in member function below
  void entry();
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
 
  directive.h -- Debug directive Unit.
 
  Original Author: Martin Wang, Synopsys, Inc.
 
 *****************************************************************************/

/*****************************************************************************
 
  MODIFICATION LOG - modifiers, enter your name, affiliation, date and
  changes you are making here.
 
      Name, Affiliation, Date:
  Description of Modification:
 
 *****************************************************************************/




//#define DEBUG true


void bios::entry()
{
 unsigned address;

 while (true) {
     wait_until(cs.delayed() == true);
     address = addr.read();
 if (address < 5) { // in BOOTING STAGE
      if (we.read() == true) { // Write operation
         wait(wait_cycles-1);
         imemory[address] = datain.read();
      }
      else { // Read operation
         if (wait_cycles > 2)
    wait(wait_cycles-2); // Introduce delay needed
           dataout.write(imemory[address]);

    if (false) {
     printf("------------------------\n");
     printf("BIOS: fetching mem[%d]\n", address);
     printf("BIOS: (%0x)", imemory[address]);
      cout.setf(ios::dec,ios::basefield);
        cout << " at CSIM " << sc_time_stamp() << endl;
     printf("------------------------\n");
    }

    bios_valid.write(true);
          wait();
    bios_valid.write(false);
    wait();
      }
 } else {
    bios_valid.write(false);
    wait();
 }
 }
} // end of entry function

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
 
  dcache.cpp -- Data Cache Unit.
 
  Original Author: Martin Wang, Synopsys, Inc.
 
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
 
  dcache.h -- Data Cache Unit.
 
  Original Author: Martin Wang, Synopsys, Inc.
 
 *****************************************************************************/

/*****************************************************************************
 
  MODIFICATION LOG - modifiers, enter your name, affiliation, date and
  changes you are making here.
 
      Name, Affiliation, Date:
  Description of Modification:
 
 *****************************************************************************/




struct dcache : sc_module {
   sc_in<signed> datain; // input data
   sc_in<unsigned> statein; // input state bit MESI(=3210)
   sc_in<bool> cs; // chip select
   sc_in<bool> we; // write enable 
   sc_in<unsigned > addr; // address
   sc_in<unsigned> dest; // write back to which register
   sc_out<unsigned> destout; // write back to which register
   sc_out<signed> dataout; // dataram data out
   sc_out<bool> out_valid; // output valid
   sc_out<unsigned> stateout; // state output
        sc_in_clk CLK;


  // Parameter
  unsigned *dmemory; // data memory
  unsigned *dsmemory; // data state memory
  unsigned *dtagmemory; // tag memory
  int wait_cycles; // cycles # it takes to access dmemory

  void init_param(int given_delay_cycles) {
    wait_cycles = given_delay_cycles;
  }

  //Constructor 
  SC_CTOR(dcache) {
        SC_CTHREAD(entry, CLK.pos());

 // initialize instruction dmemory from external file
 FILE *fp = fopen("dcache","r");
 int size=0;
 int i=0;
 int mem_word;
   dmemory = new unsigned[4000];
   dsmemory = new unsigned[4000];
   dtagmemory = new unsigned[4000];
 printf("** ALERT ** DCU: initialize Data Cache\n");
 while (fscanf(fp,"%x", &mem_word) != EOF) {
  dmemory[size] = mem_word;
  dsmemory[size] = 0;
  dtagmemory[size] = size;
  size++;
 }
 for (i=size; i<4000; i++) {
  dtagmemory[i] = 0xdeadbeef;
  dmemory[i] = 0xdeadbeef;
  dsmemory[i] = 0;
 }
  }

  // Process functionality in member function below
  void entry();
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
 
  directive.h -- Debug directive Unit.
 
  Original Author: Martin Wang, Synopsys, Inc.
 
 *****************************************************************************/

/*****************************************************************************
 
  MODIFICATION LOG - modifiers, enter your name, affiliation, date and
  changes you are making here.
 
      Name, Affiliation, Date:
  Description of Modification:
 
 *****************************************************************************/


void dcache::entry()
{
  unsigned int address; // address to DataCache
  unsigned int reg_tmp=0;
  unsigned int dest_tmp = 0;


  while (true) {
     wait_until(cs.delayed() == true);
      dest_tmp = dest.read();
        reg_tmp = dest.read();
     address = addr.read();
     if (we.read() == true) { // Write operation
        wait();
        out_valid.write(false);
        dmemory[address] = datain.read();
        dsmemory[address] = statein.read();
        dtagmemory[address] = addr.read();
  cout << "\t\t\t\t\t\t\t-------------------------------" << endl;
  printf("\t\t\t\t\t\t\tDCU :St %x->mem[%d]", dmemory[address], address);
  cout << " at CSIM " << sc_time_stamp() << endl;
  cout << "\t\t\t\t\t\t\t-------------------------------" << endl;
  wait();
     }
     else { // Read operation
  wait();
         dataout.write(dmemory[address]);
  stateout.write(dsmemory[address]);
  destout.write(dest_tmp);
         out_valid.write(true);
  cout << "\t\t\t\t\t\t\t-------------------------------" << endl;
  printf("\t\t\t\t\t\t\tDCU :ld %d<-mem[%d]->R%d\n", dmemory[address], address, dest_tmp);
  printf("\t\t\t\t\t\t\tDCU :Tag = 0x%x", dtagmemory[address]);
  if (dsmemory[address] == 3) {
   printf(" (M)");
  } else if (dsmemory[address] == 2) {
   printf(" (E)");
  } else if (dsmemory[address] == 1) {
   printf(" (S)");
  } else if (dsmemory[address] == 0) {
   printf(" (I)");
  } else
   printf(" (X)");
  cout << " at CSIM " << sc_time_stamp() << endl;
  cout << "\t\t\t\t\t\t\t-------------------------------" << endl;
         wait();
         out_valid.write(false);
         wait();
     }
  }
} // end of entry function

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
 
  decode.cpp  -- Instruction Decode Unit.
 
  Original Author: Martin Wang, Synopsys, Inc.
 
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
 
  decode.h -- Instruction Decode Unit.
 
  Original Author: Martin Wang, Synopsys, Inc.
 
 *****************************************************************************/

/*****************************************************************************
 
  MODIFICATION LOG - modifiers, enter your name, affiliation, date and
  changes you are making here.
 
      Name, Affiliation, Date:
  Description of Modification:
 
 *****************************************************************************/




struct decode : sc_module {
   sc_in<bool> resetin; // input reset
   sc_in<unsigned> instruction; // fetched instruction
   sc_in<unsigned> pred_instruction; // fetched instruction
   sc_in<bool> instruction_valid; // input valid
   sc_in<bool> pred_inst_valid; // input valid
   sc_in<bool> destreg_write; // register write enable
   sc_in<unsigned> destreg_write_src; // which register to write?      
   sc_in<signed> alu_dataout; // data from ALU     
   sc_in<signed> dram_dataout; // data from Dcache
   sc_in<bool> dram_rd_valid; // Dcache read data valid
   sc_in<unsigned> dram_write_src; // Dcache data write to which reg
   sc_in<signed> fpu_dout; // data from FPU
   sc_in<bool> fpu_valid; // FPU data valid
   sc_in<unsigned> fpu_destout; // write to which register
   sc_in<bool> clear_branch; // clear outstanding branch
   sc_in<bool> display_done; // display to monitor done
   sc_in<unsigned > pc; // program counter from IFU
   sc_in<bool> pred_on; // branch prediction is on
   sc_out<unsigned > br_instruction_address; // branch invoke instruction
   sc_out<bool> next_pc; // next pc ++ ?
   sc_out<bool> branch_valid; // branch valid signal
   sc_out<unsigned > branch_target_address; // branch target address
   sc_out<bool> mem_access; // memory access valid 
   sc_out<unsigned > mem_address; // memory physical address
   sc_out<int> alu_op; // ALU/FPU/MMU Opcode
   sc_out<bool> mem_write; // memory write enable
   sc_out<unsigned> alu_src; // destination register number
   sc_out<bool> reg_write; // not implemented
   sc_out<signed int> src_A; // operand A
   sc_out<signed int> src_B; // operand B
   sc_out<bool> forward_A; // data forwarding to operand A
   sc_out<bool> forward_B; // data forwarding to operand B
   sc_out<bool> stall_fetch; // stall fetch due to branch
   sc_out<bool> decode_valid; // decoder output valid
   sc_out<bool> float_valid; // enable FPU
   sc_out<bool> mmx_valid; // enable MMU
   sc_out<bool> pid_valid; // load process ID
   sc_out<signed> pid_data; // process ID value
        sc_in_clk CLK;


        signed int cpu_reg[32]; //CPU register
        signed int vcpu_reg[32]; //virtual CPU register
        bool cpu_reg_lock[32]; //lock architectural state register
 unsigned int pc_reg; //pc register
 unsigned int jalpc_reg; //jump back register

  //Constructor 
  SC_CTOR(decode) {
      SC_CTHREAD(entry, CLK.pos());
        FILE *fp = fopen("register","r");
        int size=0;
        int mem_word;
 printf("** ALERT ** ID: initialize Architectural Registers\n");
        while (fscanf(fp,"%x", &mem_word) != EOF) {
                cpu_reg[size] = mem_word;
  size++;
 }
 pc_reg = 0;
 jalpc_reg = 0;
 for (int j =0; j<32; j++) vcpu_reg[j] = 0;
 for (int k =0; k<32; k++) cpu_reg_lock[k] = 0;
  }

  // Process functionality in member function below
   void entry();
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
 
  directive.h -- Debug directive Unit.
 
  Original Author: Martin Wang, Synopsys, Inc.
 
 *****************************************************************************/

/*****************************************************************************
 
  MODIFICATION LOG - modifiers, enter your name, affiliation, date and
  changes you are making here.
 
      Name, Affiliation, Date:
  Description of Modification:
 
 *****************************************************************************/


void decode::entry()
{
  unsigned int instr_tmp = 0;
  unsigned int opcode_tmp = 0;
  unsigned int regA_tmp = 0;
  unsigned int regB_tmp = 0;
  unsigned int regC_tmp = 0;
  unsigned int imm_tmp = 0;
  unsigned int offset_tmp = 0;
  signed int label_tmp = 0;
  unsigned int longlabel_tmp = 0;
  unsigned int lastreg_tmp = 0;
  signed int srcA_tmp = 0;
  signed int srcB_tmp = 0;
  signed int srcC_tmp = 0;
  int i;
  bool branch_direction_tmp = 0;


  branch_valid.write(false);
  decode_valid.write(false);
  float_valid.write(false);
  mmx_valid.write(false);
  wait(2);

  while (true) {
 if (destreg_write.read() == true) {
  cpu_reg[destreg_write_src.read()] = alu_dataout.read();
                cout << "\t\t\t-------------------------------" << endl;
  printf("\t\t\tID: R%d=0x%x(%d) fr ALU", destreg_write_src.read(), alu_dataout.read(),alu_dataout.read());
  cout.setf(ios::dec,ios::basefield);
     cout << " at CSIM " << sc_time_stamp() << endl;
                cout << "\t\t\t-------------------------------" << endl;
 }
 if (dram_rd_valid.read() == true) {
  cpu_reg[dram_write_src.read()] = dram_dataout.read();
                cout << "\t\t\t-------------------------------" << endl;
  printf("\t\t\tID: R%d=0x%x(%d) fr MemLd", dram_write_src.read(), dram_dataout.read(), dram_dataout.read());
  cout.setf(ios::dec,ios::basefield);
     cout << " at CSIM " << sc_time_stamp() << endl;
                cout << "\t\t\t-------------------------------" << endl;
 }
 if (fpu_valid.read() == true) {
  cpu_reg[fpu_destout.read()] = fpu_dout.read();
                cout << "\t\t\t-------------------------------" << endl;
  printf("\t\t\tID: R%d=0x%x fr MMX", fpu_destout.read(), fpu_dout.read());
  cout.setf(ios::dec,ios::basefield);
     cout << " at CSIM " << sc_time_stamp() << endl;
                cout << "\t\t\t-------------------------------" << endl;
 }
 if ((instruction_valid.read() == true)) {
  pc_reg = pc.read();
  if (clear_branch.read() == true) {
                 cout << "\t\t\t-------------------------------" << endl;
   printf("\t\t\tID: clear branch");
   cout.setf(ios::dec,ios::basefield);
      cout << " at CSIM " << sc_time_stamp() << endl;
                 cout << "\t\t\t-------------------------------" << endl;
   branch_valid.write(false);
  }
     instr_tmp = instruction.read();
  opcode_tmp = (instr_tmp & 0xff000000) >> 24;
  regC_tmp = (instr_tmp & 0x00f00000) >> 20;
  regA_tmp = (instr_tmp & 0x000f0000) >> 16;
  regB_tmp = (instr_tmp & 0x0000f000) >> 12;
  imm_tmp = (instr_tmp & 0x0000ffff);
  offset_tmp = (instr_tmp & 0x00000fff);
  label_tmp = (instr_tmp & 0x0000ffff);
  longlabel_tmp = (instr_tmp & 0x00ffffff);
  branch_direction_tmp = (instr_tmp & 0x00008000) >> 15;
  if (branch_direction_tmp) { // handle backward branch
   label_tmp = - (0xffff - label_tmp + 1) ;
  }
  //printf("opcode = %d regC = %d regA = %d regB = %d\n",opcode_tmp, regC_tmp, regA_tmp, regB_tmp);
  srcA_tmp = cpu_reg[regA_tmp];
  srcB_tmp = cpu_reg[regB_tmp];
  srcC_tmp = cpu_reg[regC_tmp];
  wait();
                lastreg_tmp = regC_tmp;
                cout << "\t\t\t-------------------------------" << endl;
/******************************************************************************
		if (regA_tmp == lastreg_tmp){
			forward_A.write(true);
			forward_B.write(false);
		}  else  if (regB_tmp == lastreg_tmp){
				forward_A.write(false);
				forward_B.write(true);
		} else {
			forward_A.write(false);
			forward_B.write(false);
		}
*********************************************************************************/

  switch(opcode_tmp) {
   case 0x0: // halt
     printf("\n\n\t\t\t*******************************\n");
     printf("\t\t\tID: REGISTERS DUMP");
     cout << " at CSIM " << sc_time_stamp() << endl;
     printf("\t\t\t*******************************\n");
     printf("REG :==================================================================\n");
     for(i =0; i<32; i++){
      printf("  R%2d(%08x)  ",i, cpu_reg[i]);
      if ((i==3) || (i== 11) || (i==19) || (i== 27) ||(i==7) || (i==15) || (i==23) || (i==31)){
       printf("\n");
      }
     }
     printf("=======================================================================\n\n");
     wait();
     wait();
     break;
   case 0x01: // add R1, R2, R3
     printf("\t\t\tID: R%d= R%d(=%d)+R%d(=%d)",
      regC_tmp, regA_tmp, srcA_tmp, regB_tmp, srcB_tmp);
     cout << " \n\t\t\t  : at CSIM " << sc_time_stamp() << endl;
                   cout << "\t\t\t-------------------------------" << endl;
     src_A.write(srcA_tmp);
     src_B.write(srcB_tmp);
     alu_src.write(regC_tmp);
     alu_op.write(3);
     decode_valid.write(true);
     wait();
     decode_valid.write(false);
     wait();
     break;
   case 0x02: // addi R1, R2, #value
     printf("\t\t\tID: R%d= R%d(=%d)+%d",
      regC_tmp, regA_tmp, srcA_tmp, imm_tmp);
     cout << " \n\t\t\t  : at CSIM " << sc_time_stamp() << endl;
                   cout << "\t\t\t-------------------------------" << endl;
     src_A.write(srcA_tmp);
     src_B.write(imm_tmp);
     alu_src.write(regC_tmp);
     alu_op.write(3);
     decode_valid.write(true);
     wait();
     decode_valid.write(false);
     wait();
     break;
   case 0x03: // addc R1, R2, R3 + Carrybit
     printf("\t\t\tID: R%d=  R%d(=%d)+R%d(=%d)+C",
      regC_tmp, regA_tmp, srcA_tmp, regB_tmp, srcB_tmp);
     cout << " \n\t\t\t  : at CSIM " << sc_time_stamp() << endl;
                   cout << "\t\t\t-------------------------------" << endl;
     src_A.write(srcA_tmp);
     src_B.write(srcB_tmp);
     alu_src.write(regC_tmp);
     alu_op.write(1);
     decode_valid.write(true);
     wait();
     decode_valid.write(false);
     wait();
     break;
   case 0x04: // sub R1, R2, R3 
     printf("\t\t\tID: R%d=R%d(=%d)-R%d(=%d)",
      regC_tmp, regA_tmp, srcA_tmp, regB_tmp, srcB_tmp);
     cout << " \n\t\t\t  : at CSIM " << sc_time_stamp() << endl;
                   cout << "\t\t\t-------------------------------" << endl;
                                        src_A.write(srcA_tmp);
                                        src_B.write(srcB_tmp);
                                        alu_src.write(regC_tmp);
                                        alu_op.write(4);
                                        decode_valid.write(true);
                                        wait();
                                        decode_valid.write(false);
                                        wait();
     break;
   case 0x05: // subi R1, R2,  #value
     printf("\t\t\tID: R%d=R%d(=%d)-%d",
      regC_tmp, regA_tmp, srcA_tmp, imm_tmp);
     cout << " \n\t\t\t  : at CSIM " << sc_time_stamp() << endl;
                   cout << "\t\t\t-------------------------------" << endl;
                                        src_A.write(srcA_tmp);
                                        src_B.write(imm_tmp);
                                        alu_src.write(regC_tmp);
                                        alu_op.write(4);
                                        decode_valid.write(true);
                                        wait();
                                        decode_valid.write(false);
                                        wait();
     break;
   case 0x06: // subc R1, R2, R3 - Carrybit
                                        printf("\t\t\tID: R%d=R%d(=%d)-R%d(=%d)-C",
      regC_tmp, regA_tmp, srcA_tmp, regB_tmp, srcB_tmp);
     cout << " \n\t\t\t  : at CSIM " << sc_time_stamp() << endl;
                   cout << "\t\t\t-------------------------------" << endl;
                                        src_A.write(srcA_tmp);
                                        src_B.write(srcB_tmp);
                                        alu_src.write(regC_tmp);
                                        alu_op.write(2);
                                        decode_valid.write(true);
                                        wait();
                                        decode_valid.write(false);
                                        wait();
     break;
   case 0x07: // mul R1, R2, R3 
     printf("\t\t\tID: R%d=R%d(=%d)*R%d(=%d)",
      regC_tmp, regA_tmp, srcA_tmp, regB_tmp, srcB_tmp);
     cout << " \n\t\t\t  : at CSIM " << sc_time_stamp() << endl;
                   cout << "\t\t\t-------------------------------" << endl;
                                        src_A.write(srcA_tmp);
                                        src_B.write(srcB_tmp);
                                        alu_src.write(regC_tmp);
                                        alu_op.write(5);
                                        decode_valid.write(true);
                                        wait();
                                        decode_valid.write(false);
                                        wait();
     break;
   case 0x08: // div R1, R2, R3 
     printf("\t\t\tID: R%d=R%d(=%d)/R%d(=%d)",
      regC_tmp, regA_tmp, srcA_tmp, regB_tmp, srcB_tmp);
     cout << " \n\t\t\t  : at CSIM " << sc_time_stamp() << endl;
                   cout << "\t\t\t-------------------------------" << endl;
                                        src_A.write(srcA_tmp);
                                        src_B.write(srcB_tmp);
                                        alu_src.write(regC_tmp);
                                        alu_op.write(6);
                                        decode_valid.write(true);
                                        wait();
                                        decode_valid.write(false);
                                        wait();
     break;
   case 0x09: // nand R1, R2, R3 
     printf("\t\t\tID: R%d=R%d(=%x) nand R%d(=%x)",
      regC_tmp, regA_tmp, srcA_tmp, regB_tmp, srcB_tmp);
     cout << " \n\t\t\t  : at CSIM " << sc_time_stamp() << endl;
                   cout << "\t\t\t-------------------------------" << endl;
                                        src_A.write(srcA_tmp);
                                        src_B.write(srcB_tmp);
                                        alu_src.write(regC_tmp);
                                        alu_op.write(7);
                                        decode_valid.write(true);
                                        wait();
                                        decode_valid.write(false);
                                        wait();
     break;
   case 0x0a: // and R1, R2, R3 
     printf("\t\t\tID: R%d=R%d(=%x) and R%d(=%x)",
      regC_tmp, regA_tmp, srcA_tmp, regB_tmp, srcB_tmp);
     cout << " \n\t\t\t  : at CSIM " << sc_time_stamp() << endl;
                   cout << "\t\t\t-------------------------------" << endl;
                                        src_A.write(srcA_tmp);
                                        src_B.write(srcB_tmp);
                                        alu_src.write(regC_tmp);
                                        alu_op.write(8);
                                        decode_valid.write(true);
                                        wait();
                                        decode_valid.write(false);
                                        wait();
     break;
   case 0x0b: // or R1, R2, R3 
                                        printf("\t\t\tID: R%d=R%d(=%x) or R%d(=%x)",
                                                regC_tmp, regA_tmp, srcA_tmp, regB_tmp, srcB_tmp);
     cout << " \n\t\t\t  : at CSIM " << sc_time_stamp() << endl;
                   cout << "\t\t\t-------------------------------" << endl;
                                        src_A.write(srcA_tmp);
                                        src_B.write(srcB_tmp);
                                        alu_src.write(regC_tmp);
                                        alu_op.write(9);
                                        decode_valid.write(true);
                                        wait();
                                        decode_valid.write(false);
                                        wait();
                                        break;
   case 0x0c: // xor R1, R2, R3 
                                        printf("\t\t\tID: R%d=R%d(=%x) xor R%d(=%x)",
                                                regC_tmp, regA_tmp, srcA_tmp, regB_tmp, srcB_tmp);
     cout << " \n\t\t\t  : at CSIM " << sc_time_stamp() << endl;
                   cout << "\t\t\t-------------------------------" << endl;
                                        src_A.write(srcA_tmp);
                                        src_B.write(srcB_tmp);
                                        alu_src.write(regC_tmp);
                                        alu_op.write(10);
                                        decode_valid.write(true);
                                        wait();
                                        decode_valid.write(false);
                                        wait();
                                        break;
                        case 0x0d: // NOT R1, R2 
                                        printf("\t\t\tID: R%d= NOT R%d(=%x)",
                                                regC_tmp, regA_tmp, srcA_tmp);
                                        cout << " \n\t\t\t  : at CSIM " << sc_time_stamp() << endl;
                                        cout << "\t\t\t-------------------------------" << endl;
                                        src_A.write(srcA_tmp);
                                        src_B.write(0);
                                        alu_src.write(regC_tmp);
                                        alu_op.write(11);
                                        decode_valid.write(true);
                                        wait();
                                        decode_valid.write(false);
                                        wait();
                                        break;
                        case 0x0e: // modulo R1 = R2 mod R3 
                                        printf("\t\t\tID: R%d= R%d(=%x) mod R%d(=%x)",
      regC_tmp, regA_tmp, srcA_tmp, regB_tmp, srcB_tmp);
                                        cout << " \n\t\t\t  : at CSIM " << sc_time_stamp() << endl;
                                        cout << "\t\t\t-------------------------------" << endl;
                                        src_A.write(srcA_tmp);
                                        src_B.write(srcB_tmp);
                                        alu_src.write(regC_tmp);
                                        alu_op.write(14);
                                        decode_valid.write(true);
                                        wait();
                                        decode_valid.write(false);
                                        wait();
                                        break;
   case 0x4d: // lw R1, R2, offset
     printf("\t\t\tID: R%d<=mem[R%d=(%d)+%d]",
      regC_tmp, regA_tmp, srcA_tmp, offset_tmp);
     cout << " \n\t\t\t  : at CSIM " << sc_time_stamp() << endl;
                   cout << "\t\t\t-------------------------------" << endl;
     mem_access.write(true);
     mem_write.write(false);
     alu_src.write(regC_tmp);
     offset_tmp = offset_tmp + srcA_tmp;
     mem_address.write(offset_tmp);
     wait();
     mem_access.write(false);
     wait();
     break;
   case 0x4e: // sw R1, R2, offset
     printf("\t\t\tID: R%d=>mem[R%d(=%d) + %d]",
      regC_tmp, regA_tmp, srcA_tmp, offset_tmp);
     cout << " \n\t\t\t  : at CSIM " << sc_time_stamp() << endl;
                   cout << "\t\t\t-------------------------------" << endl;
     mem_access.write(true);
     mem_write.write(true);
     offset_tmp = offset_tmp + srcA_tmp;
     mem_address.write(offset_tmp);
     wait();
     mem_access.write(false);
     mem_write.write(false);
     wait();
     break;
   case 0x0f: // mov R1, R2
     printf("\t\t\tID: R%d=R%d(=%d)",
      regC_tmp, regA_tmp, srcA_tmp);
     cout << " \n\t\t\t  : at CSIM " << sc_time_stamp() << endl;
                   cout << "\t\t\t-------------------------------" << endl;
     src_A.write(srcA_tmp);
     src_B.write(0);
     alu_src.write(regC_tmp);
     alu_op.write(3);
     decode_valid.write(true);
     wait();
     decode_valid.write(false);
     wait();
     break;
   case 0x10: // beq R1, R2, label
     src_A.write(0);
     src_B.write(0);
     alu_src.write(0);
     alu_op.write(3);
     decode_valid.write(true);
     if (srcC_tmp == srcA_tmp) {
      branch_target_address.write(pc_reg + label_tmp);
      br_instruction_address.write(instr_tmp);
      branch_valid.write(true);
      printf("\t\t\tID: beq R%d(=%d), R%d(=%d), pc+=(%d).\n",
       regC_tmp, srcC_tmp, regA_tmp, srcA_tmp, label_tmp);
     } else {
      printf("\t\t\tID: beq R%d(=%d) != R%d(=%d),pc++.\n",
       regC_tmp, srcC_tmp, regA_tmp, srcA_tmp);
     }
     cout << "\t\t\tID: at CSIM " << sc_time_stamp() << endl;
                   cout << "\t\t\t-------------------------------" << endl;
     wait();
     branch_target_address.write(pc_reg + 1);
     decode_valid.write(false);
     wait();
     break;
   case 0x11: // bne R1, R2, label
                                        src_A.write(0);
                                        src_B.write(0);
                                        alu_src.write(0);
                                        alu_op.write(3);
                                        decode_valid.write(true);
                                        if (srcC_tmp != srcA_tmp) {
      branch_target_address.write(pc_reg + label_tmp);
      br_instruction_address.write(instr_tmp);
                                                branch_valid.write(true);
                                                printf("\t\t\tID: bne R%d(=%d), R%d(=%d), pc+=(%d).\n",
                                                        regC_tmp, srcC_tmp, regA_tmp, srcA_tmp, label_tmp);
                                        } else {
                                                printf("\t\t\tID: bne R%d(=%d) = R%d(=%d),pc++.\n",
                                                        regC_tmp, srcC_tmp, regA_tmp, srcA_tmp);
                                        }
     cout << "\t\t\tID: at CSIM " << sc_time_stamp() << endl;
                   cout << "\t\t\t-------------------------------" << endl;
                                        wait();
     branch_target_address.write(pc_reg + 1);
                                        decode_valid.write(false);
                                        wait();
                                        break;
   case 0x12: // bgt R1, R2, label
                                        src_A.write(0);
                                        src_B.write(0);
                                        alu_src.write(0);
                                        alu_op.write(3);
                                        decode_valid.write(true);
                                        if (srcC_tmp > srcA_tmp) {
      branch_target_address.write(pc_reg + label_tmp);
      br_instruction_address.write(instr_tmp);
                                                branch_valid.write(true);
                                                printf("\t\t\tID: bgt R%d(=%d)>R%d(=%d), pc+=(%d).\n",
                                                        regC_tmp, srcC_tmp, regA_tmp, srcA_tmp, label_tmp);
                                        } else {
                                                printf("\t\t\tID: bgt R%d(=%d) <= R%d(=%d),pc++.\n",
                                                        regC_tmp, srcC_tmp, regA_tmp, srcA_tmp);
                                        }
     cout << "\t\t\tID: at CSIM " << sc_time_stamp() << endl;
                   cout << "\t\t\t-------------------------------" << endl;
                                        wait();
     branch_target_address.write(pc_reg + 1);
                                        decode_valid.write(false);
                                        wait();
                                        break;
   case 0x13: // bge R1, R2, label
                                        src_A.write(0);
                                        src_B.write(0);
                                        alu_src.write(0);
                                        alu_op.write(3);
                                        decode_valid.write(true);
                                        if (srcC_tmp >= srcA_tmp) {
      branch_target_address.write(pc_reg + label_tmp);
      br_instruction_address.write(instr_tmp);
                                                branch_valid.write(true);
                                                printf("\t\t\tID: bge R%d(=%d)>=R%d(=%d), pc+=(%d).\n",
                                                        regC_tmp, srcC_tmp, regA_tmp, srcA_tmp, label_tmp);
                                        } else {
                                                printf("\t\t\tID: bge R%d(=%d) < R%d(=%d),pc++.\n",
                                                        regC_tmp, srcC_tmp, regA_tmp, srcA_tmp);
                                        }
     cout << "\t\t\tID: at CSIM " << sc_time_stamp() << endl;
                   cout << "\t\t\t-------------------------------" << endl;
                                        wait();
     branch_target_address.write(pc_reg + 1);
                                        decode_valid.write(false);
                                        wait();
                                        break;
   case 0x14: // blt R1, R2, label
                                        src_A.write(0);
                                        src_B.write(0);
                                        alu_src.write(0);
                                        alu_op.write(3);
                                        decode_valid.write(true);
                                        if (srcC_tmp < srcA_tmp) {
      branch_target_address.write(pc_reg + label_tmp);
      br_instruction_address.write(instr_tmp);
                                                branch_valid.write(true);
                                                printf("\t\t\tID: blt R%d(=%d)<R%d(=%d), pc+=(%d).\n",
                                                        regC_tmp, srcC_tmp, regA_tmp, srcA_tmp, label_tmp);
                                        } else {
                                                printf("\t\t\tID: blt R%d(=%d) >= R%d(=%d), pc++.\n",
                                                        regC_tmp, srcC_tmp, regA_tmp, srcA_tmp);
                                        }
     cout << "\t\t\tID: at CSIM " << sc_time_stamp() << endl;
                   cout << "\t\t\t-------------------------------" << endl;
                                        wait();
     branch_target_address.write(pc_reg + 1);
                                        decode_valid.write(false);
                                        wait();
                                        break;
   case 0x15: // ble R1, R2, label
                                        src_A.write(0);
                                        src_B.write(0);
                                        alu_src.write(0);
                                        alu_op.write(3);
                                        decode_valid.write(true);
                                        if (srcC_tmp <= srcA_tmp) {
      branch_target_address.write(pc_reg + label_tmp);
      br_instruction_address.write(instr_tmp);
                                                branch_valid.write(true);
                                                printf("\t\t\tID: ble R%d(=%d)<=R%d(=%d), pc+=(%d).\n",
                                                        regC_tmp, srcC_tmp, regA_tmp, srcA_tmp, label_tmp);
                                        } else {
                                                printf("\t\t\tID: ble R%d(=%d)>R%d(=%d), pc++.\n",
                                                        regC_tmp, srcC_tmp, regA_tmp, srcA_tmp);
                                        }
     cout << "\t\t\tID: at CSIM " << sc_time_stamp() << endl;
                   cout << "\t\t\t-------------------------------" << endl;
                                        wait();
     branch_target_address.write(pc_reg + 1);
                                        decode_valid.write(false);
                                        wait();
                                        break;
   case 0x16: // j label
                                        src_A.write(0);
                                        src_B.write(0);
                                        alu_src.write(0);
                                        alu_op.write(3);
                                        decode_valid.write(true);
                                        branch_target_address.write(longlabel_tmp);
     br_instruction_address.write(instr_tmp);
                                        branch_valid.write(true);
                                        printf("\t\t\tID: pc jump to => (%d).", longlabel_tmp);
     cout << " \n\t\t\t  : at CSIM " << sc_time_stamp() << endl;
                   cout << "\t\t\t-------------------------------" << endl;
                                        wait();
     branch_target_address.write(pc_reg + 1);
                                        decode_valid.write(false);
                                        wait();
                                        break;
   case 0x17: // jal label for procedure call
     printf("\t\t\tID: j pc(%d) and sp <- pc", longlabel_tmp);
     cout << " at CSIM " << sc_time_stamp() << endl;
                   cout << "\t\t\t-------------------------------" << endl;
     wait();
     wait();
     break;
   case 0x18: // jr
     printf("\t\t\tID: jr and pc <- sp\n");
     cout << " at CSIM " << sc_time_stamp() << endl;
                   cout << "\t\t\t-------------------------------" << endl;
     wait();
     wait();
     break;
                        case 0x29: // fadd R1, R2, R3
                                        printf("\t\t\tID: FR%d=FR%d(=%x)+FR%d(=%x)",
                                                regC_tmp, regA_tmp, srcA_tmp, regB_tmp, srcB_tmp);
     cout << " \n\t\t\t  : at CSIM " << sc_time_stamp() << endl;
                   cout << "\t\t\t-------------------------------" << endl;
                                        src_A.write(srcA_tmp);
                                        src_B.write(srcB_tmp);
                                        alu_src.write(regC_tmp);
                                        alu_op.write(3);
                                        float_valid.write(true);
                                        wait();
                                        float_valid.write(false);
                                        wait();
                                        break;
                        case 0x2a: // fsub R1, R2, R3
                                        printf("\t\t\tID: FR%d=FR%d(=%x)-FR%d(=%x)",
                                                regC_tmp, regA_tmp, srcA_tmp, regB_tmp, srcB_tmp);
     cout << " \n\t\t\t  : at CSIM " << sc_time_stamp() << endl;
                   cout << "\t\t\t-------------------------------" << endl;
                                        src_A.write(srcA_tmp);
                                        src_B.write(srcB_tmp);
                                        alu_src.write(regC_tmp);
                                        alu_op.write(4);
                                        float_valid.write(true);
                                        wait();
                                        float_valid.write(false);
                                        wait();
                                        break;
                        case 0x2b: // fmul R1, R2, R3
                                        printf("\t\t\tID: FR%d=FR%d(=%x)*FR%d(=%x)",
                                                regC_tmp, regA_tmp, srcA_tmp, regB_tmp, srcB_tmp);
     cout << " \n\t\t\t  : at CSIM " << sc_time_stamp() << endl;
                   cout << "\t\t\t-------------------------------" << endl;
                                        src_A.write(srcA_tmp);
                                        src_B.write(srcB_tmp);
                                        alu_src.write(regC_tmp);
                                        alu_op.write(5);
                                        float_valid.write(true);
                                        wait();
                                        float_valid.write(false);
                                        wait();
                                        break;
                        case 0x2c: // fdiv R1, R2, R3
                                        printf("\t\t\tID: FR%d=FR%d(=%x)/FR%d(=%x)",
                                                regC_tmp, regA_tmp, srcA_tmp, regB_tmp, srcB_tmp);
     cout << " \n\t\t\t  : at CSIM " << sc_time_stamp() << endl;
                   cout << "\t\t\t-------------------------------" << endl;
                                        src_A.write(srcA_tmp);
                                        src_B.write(srcB_tmp);
                                        alu_src.write(regC_tmp);
                                        alu_op.write(6);
                                        float_valid.write(true);
                                        wait();
                                        float_valid.write(false);
                                        wait();
                                        break;
                        case 0x31: // mmxadd R1, R2, R3
                                        printf("\t\t\tID: MR%d=MR%d(=%x)+MR%d(=%x)",
                                                regC_tmp, regA_tmp, srcA_tmp, regB_tmp, srcB_tmp);
     cout << " \n\t\t\t  : at CSIM " << sc_time_stamp() << endl;
                   cout << "\t\t\t-------------------------------" << endl;
                                        src_A.write(srcA_tmp);
                                        src_B.write(srcB_tmp);
                                        alu_src.write(regC_tmp);
                                        alu_op.write(3);
                                        mmx_valid.write(true);
                                        wait();
                                        mmx_valid.write(false);
                                        wait();
                                        break;
                        case 0x32: // mmxadds R1, R2, R3
                                        printf("\t\t\tID: MR%d=MR%d(=%x)+MR%d(=%x) Sat",
                                                regC_tmp, regA_tmp, srcA_tmp, regB_tmp, srcB_tmp);
     cout << " \n\t\t\t  : at CSIM " << sc_time_stamp() << endl;
                   cout << "\t\t\t-------------------------------" << endl;
                                        src_A.write(srcA_tmp);
                                        src_B.write(srcB_tmp);
                                        alu_src.write(regC_tmp);
                                        alu_op.write(4);
                                        mmx_valid.write(true);
                                        wait();
                                        mmx_valid.write(false);
                                        wait();
                                        break;
                        case 0x33: // mmxsub R1, R2, R3
                                        printf("\t\t\tID: MR%d=MR%d(=%x)-MR%d(=%x)",
                                                regC_tmp, regA_tmp, srcA_tmp, regB_tmp, srcB_tmp);
     cout << " \n\t\t\t  : at CSIM " << sc_time_stamp() << endl;
                   cout << "\t\t\t-------------------------------" << endl;
                                        src_A.write(srcA_tmp);
                                        src_B.write(srcB_tmp);
                                        alu_src.write(regC_tmp);
                                        alu_op.write(5);
                                        mmx_valid.write(true);
                                        wait();
                                        mmx_valid.write(false);
                                        wait();
                                        break;
                        case 0x34: // mmxsubs R1, R2, R3
                                        printf("\t\t\tID: MR%d=MR%d(=%x)-MR%d(=%x) Sat",
                                                regC_tmp, regA_tmp, srcA_tmp, regB_tmp, srcB_tmp);
     cout << " \n\t\t\t  : at CSIM " << sc_time_stamp() << endl;
                   cout << "\t\t\t-------------------------------" << endl;
                                        src_A.write(srcA_tmp);
                                        src_B.write(srcB_tmp);
                                        alu_src.write(regC_tmp);
                                        alu_op.write(6);
                                        mmx_valid.write(true);
                                        wait();
                                        mmx_valid.write(false);
                                        wait();
                                        break;
                        case 0x35: // pmadd R1, R2, R3
                                        printf("\t\t\tID: MR%d=MR%d(=%x) ,MR%d(=%x) PMADD",
                                                regC_tmp, regA_tmp, srcA_tmp, regB_tmp, srcB_tmp);
     cout << " \n\t\t\t  : at CSIM " << sc_time_stamp() << endl;
                   cout << "\t\t\t-------------------------------" << endl;
                                        src_A.write(srcA_tmp);
                                        src_B.write(srcB_tmp);
                                        alu_src.write(regC_tmp);
                                        alu_op.write(7);
                                        mmx_valid.write(true);
                                        wait();
                                        mmx_valid.write(false);
                                        wait();
                                        break;
                        case 0x36: // pack R1, R2, R3
                                        printf("\t\t\tID: MR%d=MR%d(=%x) pack MR%d(=%x)",
                                                regC_tmp, regA_tmp, srcA_tmp, regB_tmp, srcB_tmp);
     cout << " \n\t\t\t  : at CSIM " << sc_time_stamp() << endl;
                   cout << "\t\t\t-------------------------------" << endl;
                                        src_A.write(srcA_tmp);
                                        src_B.write(srcB_tmp);
                                        alu_src.write(regC_tmp);
                                        alu_op.write(8);
                                        mmx_valid.write(true);
                                        wait();
                                        mmx_valid.write(false);
                                        wait();
                                        break;
                        case 0x37: // mmxck R1, R2, R3 MMX Chroma Keying
                                        printf("\t\t\tID: MR%d=MR%d(=%x) mmxck MR%d(=%x)",
                                                regC_tmp, regA_tmp, srcA_tmp, regB_tmp, srcB_tmp);
                                        cout << " \n\t\t\t  : at CSIM " << sc_time_stamp() << endl;
                   cout << "\t\t\t-------------------------------" << endl;
                                        src_A.write(srcA_tmp);
                                        src_B.write(srcB_tmp);
                                        alu_src.write(regC_tmp);
                                        alu_op.write(9);
                                        mmx_valid.write(true);
                                        wait();
                                        mmx_valid.write(false);
                                        wait();
                                        break;
                        case 0xe0: // flush register
                                        printf("\t\t\tID: flush all registers");
                                        cout << " \n\t\t\t  : at CSIM " << sc_time_stamp() << endl;
                   cout << "\t\t\t-------------------------------" << endl;
                                        src_A.write(0);
                                        src_B.write(0);
                                        alu_src.write(0);
                                        alu_op.write(0);
     for (i = 0; i< 32; i++) {
      cpu_reg[i] = 0;
     }
                                        wait();
                                        wait();
                                        break;
                        case 0xf0: // ldpid process_number
                                        printf("\t\t\tID: ld pid =%d", longlabel_tmp);
                                        cout << " \n\t\t\t  : at CSIM " << sc_time_stamp() << endl;
                   cout << "\t\t\t-------------------------------" << endl;
                                        src_A.write(0);
                                        src_B.write(0);
                                        alu_src.write(0);
                                        alu_op.write(0);
     pid_valid.write(true);
     decode_valid.write(false);
     float_valid.write(false);
     mmx_valid.write(false);
     pid_data.write(longlabel_tmp);
                                        wait();
     pid_valid.write(false);
                                        wait();
                                        break;
   case 0xf1: // movi R1, #value
     printf("\t\t\tID: R%d=%d",
      regC_tmp, imm_tmp);
     cout << " at CSIM " << sc_time_stamp() << endl;
                   cout << "\t\t\t-------------------------------" << endl;
     src_A.write(imm_tmp);
     src_B.write(0);
     alu_src.write(regC_tmp);
     alu_op.write(3);
     decode_valid.write(true);
     wait();
     decode_valid.write(false);
     wait();
     break;
                        case 0xff: // QUIT
     printf("\t\t\tID: - SHUTDOWN - ");
     cout << "at CSIM " << sc_time_stamp() << endl;
     decode_valid.write(false);
     float_valid.write(false);
     mmx_valid.write(false);
     wait();
     printf("\t\t\tID: - PLEASE WAIT ...... - \n");
                   cout << "\t\t\t-------------------------------" << endl;
     sc_stop();
     printf("\n\n\n////////////////////////////////////////////////////////////////////////////////\n");
                                        wait();
     wait();
     break;
   default :
     printf("\t\t\tID: INVALID OPCODE");
     cout << " \n\t\t\t  : at CSIM " << sc_time_stamp() << endl;
     wait();
     break;
  }
  next_pc.write(true);
  wait();
 } else {
  next_pc.write(true);
  wait();
 }


  }
} // end of entry function

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
 
  exec.cpp -- Integer Execution Unit.
 
  Original Author: Martin Wang, Synopsys, Inc.
 
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
 
  exec.h -- Integer Execution Unit.
 
  Original Author: Martin Wang, Synopsys, Inc.
 
 *****************************************************************************/

/*****************************************************************************
 
  MODIFICATION LOG - modifiers, enter your name, affiliation, date and
  changes you are making here.
 
      Name, Affiliation, Date:
  Description of Modification:
 
 *****************************************************************************/



struct exec : sc_module {
  sc_in<bool> reset; // reset not used.
  sc_in<bool> in_valid; // input valid
  sc_in<int> opcode; // opcode from ID
  sc_in<bool> negate; // not implemented
  sc_in<int> add1; // not implemented
  sc_in<bool> shift_sel; // not implemented
  sc_in<signed int> dina; // operand A
  sc_in<signed int> dinb; // operand B
  sc_in<bool> forward_A; // data forwarding A valid
  sc_in<bool> forward_B; // data forwarding B valid
  sc_in<unsigned> dest; // destination register number
  sc_out<bool> C; // Carry bit 
  sc_out<bool> V; // Overflow bit
  sc_out<bool> Z; // Zero bit
  sc_out<signed int> dout; // output data
  sc_out<bool> out_valid; // output valid
  sc_out<unsigned> destout; // write to which registers?
  sc_in_clk CLK;


  SC_CTOR(exec) {
      SC_CTHREAD(entry, CLK.pos());
  }

  void entry();
};


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
 
  directive.h -- Debug directive Unit.
 
  Original Author: Martin Wang, Synopsys, Inc.
 
 *****************************************************************************/

/*****************************************************************************
 
  MODIFICATION LOG - modifiers, enter your name, affiliation, date and
  changes you are making here.
 
      Name, Affiliation, Date:
  Description of Modification:
 
 *****************************************************************************/


void exec::entry(){

  int opcode_tmp = 0;
  int add1_tmp = 0;
  signed int dina_tmp = 0;
  signed int dinb_tmp = 0;
  signed int dout_tmp = 0;
  unsigned int dest_tmp = 0;

  //
  // main loop
  //
  // 

  // initialization of output

  wait(3);
  while(1) {
    if (in_valid.read() == true) {
   dina_tmp = dina.read();
   dinb_tmp = dinb.read();
   opcode_tmp = opcode.read();
   dest_tmp = dest.read();

      // output MUX
      switch (opcode_tmp) {
          case 0: // Stall 
                 dout_tmp = dout_tmp;
                 wait();
                 break;
  case 1: // add with carry
       dout_tmp = dina_tmp + dinb_tmp + add1_tmp;
   wait();
   break;
  case 2: // sub with carry
       dout_tmp = dina_tmp - dinb_tmp - add1_tmp;
   wait();
   break;
         case 3: // add without carry
                  dout_tmp = dina_tmp + dinb_tmp;
                 wait();
                 break;
         case 4: // sub without carry
                  dout_tmp = dina_tmp - dinb_tmp;
                 wait();
                 break;
         case 5: // multiply assume 2 clock cycle multiplication
                 dout_tmp = dina_tmp * dinb_tmp;
                 //wait();	so that BC have something to do
                 wait();
                 break;
         case 6: // divide assume 2 clock cycle multiplication
   if (dinb_tmp == 0) {
    printf("Division Exception - Divide by zero \n");
   } else {
                  dout_tmp = dina_tmp / dinb_tmp;
   }
                 // wait();	so that BC have something to do
                 wait();
                 break;
         case 7: // bitwise NAND
                 dout_tmp = ~(dina_tmp & dinb_tmp);
                 wait();
                 break;
         case 8: // bitwise AND 
                 dout_tmp = dina_tmp & dinb_tmp;
                 wait();
   break;
         case 9: // bitwise OR
                 dout_tmp = dina_tmp | dinb_tmp;
                 wait();
   break;
         case 10: // bitwise XOR
                  dout_tmp = dina_tmp ^ dinb_tmp;
                 wait();
   break;
         case 11: // bitwise complement
                 dout_tmp = ~ dina_tmp;
                 wait();
   break;
         case 12: // left shift
                 dout_tmp = dina_tmp << dinb_tmp;
                 wait();
   break;
         case 13: // right shift
                 dout_tmp = dina_tmp >> dinb_tmp;
                 wait();
   break;
  case 14: // modulo
       dout_tmp = dina_tmp % dinb_tmp;
   wait();
   break;
  default:
   printf("ALU:      Bad Opcode %d.\n",opcode_tmp);
   break;
      }


      dout.write(dout_tmp);
      out_valid.write(true);
  destout.write(dest_tmp);

  if (dout_tmp == 0) {
   Z.write(true);
  } else {
   Z.write(false);
  }
  if (dout_tmp > 2^32) {
   V.write(true);
  }else {
   V.write(false);
  }
  printf("\t\t\t\t\t\t\t-------------------------------\n");
      cout << "\t\t\t\t\t\t\tALU :" << " op= " << opcode_tmp
   << " A= " << dina_tmp << " B= " << dinb_tmp << endl;
      cout << "\t\t\t\t\t\t\tALU :" << " R= " << dout_tmp << "-> R" << dest_tmp;
              cout << " at CSIM " << sc_time_stamp() << endl;
  printf("\t\t\t\t\t\t\t-------------------------------\n");
      wait();
      out_valid.write(false);
      wait();

 } else {
  wait();
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
 
  fetch.cpp -- Instruction Fetch Unit.
 
  Original Author: Martin Wang, Synopsys, Inc.
 
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
 
  fetch.h -- Instruction Fetch Unit.
 
  Original Author: Martin Wang, Synopsys, Inc.
 
 *****************************************************************************/

/*****************************************************************************
 
  MODIFICATION LOG - modifiers, enter your name, affiliation, date and
  changes you are making here.
 
      Name, Affiliation, Date:
  Description of Modification:
 
 *****************************************************************************/




struct fetch : sc_module {
  sc_in<unsigned > ramdata; // instruction from RAM    
  sc_in<unsigned > branch_address; // branch target address   
  sc_in<bool> next_pc; // pc ++
  sc_in<bool> branch_valid; // branch_valid
  sc_in<bool> stall_fetch; // STALL_FETCH
  sc_in<bool> interrupt; // interrrupt 
  sc_in<unsigned> int_vectno; // interrupt vector number
  sc_in<bool> bios_valid; // BIOS input valid
  sc_in<bool> icache_valid; // Icache input valid
  sc_in<bool> pred_fetch; // branch prediction fetch
  sc_in<unsigned > pred_branch_address; // branch target address   
  sc_in<bool> pred_branch_valid; // branch prediction fetch
  sc_out<bool> ram_cs; // RAM chip select
  sc_out<bool> ram_we; // RAM write enable for SMC
  sc_out<unsigned > address; // address send to RAM
  sc_out<unsigned > smc_instruction; // for self-modifying code 
  sc_out<unsigned> instruction; // instruction send to ID
  sc_out<bool> instruction_valid; // inst valid
  sc_out<unsigned > program_counter; // program counter	
  sc_out<bool> interrupt_ack; // interrupt acknowledge
  sc_out<bool> branch_clear; // clear outstanding branch
  sc_out<bool> pred_fetch_valid; // branch prediction fetch
  sc_out<bool> reset; // reset
  sc_in_clk CLK;

  // Parameter
  int memory_latency; // just a dummy for syntax

  void init_param(int given_delay_cycles) {
    memory_latency = given_delay_cycles;
  }

  //Constructor 
  SC_CTOR(fetch) {
        SC_CTHREAD(entry, CLK.pos());
  }

  // Process functionality in member function below
  void entry();
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
 
  directive.h -- Debug directive Unit.
 
  Original Author: Martin Wang, Synopsys, Inc.
 
 *****************************************************************************/

/*****************************************************************************
 
  MODIFICATION LOG - modifiers, enter your name, affiliation, date and
  changes you are making here.
 
      Name, Affiliation, Date:
  Description of Modification:
 
 *****************************************************************************/


void fetch::entry()
{

   unsigned addr_tmp=0;
   unsigned datao_tmp=0;
   unsigned datai_tmp=0;
   unsigned lock_tmp = 0;

   addr_tmp = 1;
   datao_tmp = 0xdeadbeef;

   // Now booting from default values
   reset.write(true);
   ram_cs.write(true);
   ram_we.write(false);
   address.write(addr_tmp);
   wait(memory_latency); // For data to appear

//   wait_until((bios_valid.delayed() == true) || (icache_valid.delayed() == true));
   if (stall_fetch.read() == true) {
    datai_tmp = 0;
   } else {
    datai_tmp = ramdata.read();
   }
   cout.setf(ios::hex,ios::basefield);
   cout << "-----------------------" << endl;
   cout << "IFU :" << " mem=0x" << datai_tmp << endl;
   cout << "IFU : pc= " << addr_tmp ;
   cout.setf(ios::dec,ios::basefield);
   cout << " at CSIM " << sc_time_stamp() << endl;
   cout << "-----------------------" << endl;

   instruction_valid.write(true);
   instruction.write(datai_tmp);
   program_counter.write(addr_tmp);
   ram_cs.write(false);
   wait();
   instruction_valid.write(false);
   addr_tmp++;
   wait();


  while (true) {
 if (addr_tmp == 5) {
  reset.write(false);
 }
 if (interrupt.read() == true) {

      ram_cs.write(true);
  addr_tmp = int_vectno.read();
      ram_we.write(false);
      wait(memory_latency);
      datai_tmp = ramdata.read();

  printf("IF ALERT: **INTERRUPT**\n");
     cout.setf(ios::hex,ios::basefield);
     cout << "------------------------" << endl;
     cout << "IFU :" << " mem=0x" << datai_tmp << endl;
     cout << "IFU : pc= " << addr_tmp ;
     cout.setf(ios::dec,ios::basefield);
     cout << " at CSIM " << sc_time_stamp() << endl;
     cout << "------------------------" << endl;

  instruction_valid.write(true);
  instruction.write(datai_tmp);
      ram_cs.write(false);
  interrupt_ack.write(true);
  if (next_pc.read() == true) { addr_tmp++; }
  wait();
                instruction_valid.write(false);
  interrupt_ack.write(false);
  wait();

 }

 if (branch_valid.read() == true) {
  printf("IFU ALERT: **BRANCH**\n");
  lock_tmp ++;
      ram_cs.write(true);
  addr_tmp = branch_address.read();
      ram_we.write(false);
      wait(memory_latency);
//		wait_until((bios_valid.delayed() == true) || (icache_valid.delayed() == true));
      datai_tmp = ramdata.read();

     cout.setf(ios::hex,ios::basefield);
     cout << "------------------------" << endl;
     cout << "IFU :" << " mem=0x" << datai_tmp << endl;
     cout << "IFU : pc= " << addr_tmp ;
     cout.setf(ios::dec,ios::basefield);
     cout << " at CSIM " << sc_time_stamp() << endl;
     cout << "------------------------" << endl;

  instruction_valid.write(true);
  instruction.write(datai_tmp);
      ram_cs.write(false);
  if (next_pc.read() == true) { addr_tmp++; }
  wait();
                instruction_valid.write(false);
  wait();
 } else {
  lock_tmp = 0;
      ram_cs.write(true);
      address.write(addr_tmp);
      ram_we.write(false);
      wait(memory_latency); // For data to appear
//		wait_until((bios_valid.delayed() == true) || (icache_valid.delayed() == true));
      datai_tmp = ramdata.read();

     cout.setf(ios::hex,ios::basefield);
     cout << "------------------------" << endl;
     cout << "IFU :" << " mem=0x" << datai_tmp << endl;
     cout << "IFU : pc= " << addr_tmp ;
     cout.setf(ios::dec,ios::basefield);
     cout << " at CSIM " << sc_time_stamp() << endl;
     cout << "------------------------" << endl;

                instruction_valid.write(true);
                instruction.write(datai_tmp);
     program_counter.write(addr_tmp);
  branch_clear.write(false);
      ram_cs.write(false);
  if (next_pc.read() == true) { addr_tmp++; }
                wait();
                instruction_valid.write(false);
      wait();
        }
 if (lock_tmp == 1) {
  branch_clear.write(true);
  wait();
 }

/* Unless you wanted to write to your instruction cache.  Usually Instruction cache is read only.
    	// Write memory location first
    	chip_select.write(true);
    	write_enable.write(true);
    	address.write(addr);
    	instruction_write.write(datao);
    	printf("fetch: Data Written = %x at address %x\n", datao, addr);
    	wait(memory_latency); // To make all the outputs appear at the interface

    	// some process functionality not shown here during which chip
    	// chip select is deasserted and bus is tristated
    	chip_select.write(false);
    	instruction_write.write(0);
    	wait();

*/

  }
} // end of entry function

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
 
  floating.cpp -- Floating Point Execution Unit.
 
  Original Author: Martin Wang, Synopsys, Inc.
 
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
 
  floating.h -- Floating Execution Unit.
 
  Original Author: Martin Wang, Synopsys, Inc.
 
 *****************************************************************************/

/*****************************************************************************
 
  MODIFICATION LOG - modifiers, enter your name, affiliation, date and
  changes you are making here.
 
      Name, Affiliation, Date:
  Description of Modification:
 
 *****************************************************************************/




struct floating : sc_module {
  sc_in<bool> in_valid; // input valid bit
  sc_in<int> opcode; // opcode
  sc_in<signed int> floata; // operand A
  sc_in<signed int> floatb; // operand B
  sc_in<unsigned> dest; // write to which register
  sc_out<signed int> fdout; // FPU output
  sc_out<bool> fout_valid; // output valid
  sc_out<unsigned> fdestout; // write to which register
  sc_in_clk CLK;


  SC_CTOR(floating) {
      SC_CTHREAD(entry, CLK.pos());
  }


  void entry();
};

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
 
  directive.h -- Debug directive Unit.
 
  Original Author: Martin Wang, Synopsys, Inc.
 
 *****************************************************************************/

/*****************************************************************************
 
  MODIFICATION LOG - modifiers, enter your name, affiliation, date and
  changes you are making here.
 
      Name, Affiliation, Date:
  Description of Modification:
 
 *****************************************************************************/

void floating::entry(){

  int opcode_tmp = 0;
  signed int dout_tmp = 0;
  unsigned int dest_tmp = 0;

  unsigned int fpua_sign_tmp;
  unsigned int fpua_exponent_tmp;
  unsigned int fpua_significand_tmp;
  unsigned int fpub_sign_tmp;
  unsigned int fpub_exponent_tmp;
  unsigned int fpub_significand_tmp;
  const char * opcode_encode="";
  unsigned int fpua_tmp;
  unsigned int fpub_tmp;


  int exponent_diff_tmp = 0;
  int exponent_tmp = 0;
  unsigned int significant_result = 0;
  unsigned int overflow_sign_tmp = 0;
  unsigned int result_exp_tmp = 0;
  unsigned int result_sign_tmp = 0;

  //
  // main loop
  //
  // 
  // current implementation only worked if both operand have same sign bits
  // overflow is ignored.

  // initialization of output

  wait(3);
  while(true) {
        wait_until(in_valid.delayed() == true);
 dest_tmp = dest.read();
  opcode_tmp = opcode.read();

        fpua_tmp = floata.read();
        fpub_tmp = floatb.read();

        fpua_sign_tmp = (fpua_tmp & 0x80000000) >> 31 ;
        fpub_sign_tmp = (fpub_tmp & 0x80000000) >> 31 ;

        fpua_exponent_tmp = (fpua_tmp & 0x7f800000) >> 23 ;
        fpub_exponent_tmp = (fpub_tmp & 0x7f800000) >> 23 ;

        fpua_significand_tmp = (fpua_tmp & 0x007fffff) ;
        fpub_significand_tmp = (fpub_tmp & 0x007fffff) ;

 exponent_diff_tmp = int(fpua_exponent_tmp) - int(fpub_exponent_tmp);


 if (true) {
   printf("\t\t\t\t\t\t\t-------------------------------\n");
   printf("\t\t\t\t\t\t\tFPU: A s=0x%x, exp=0x%x, mant=0x%x\n",fpua_sign_tmp, fpua_exponent_tmp, fpua_significand_tmp);
   printf("\t\t\t\t\t\t\t   : B s=0x%x, exp=0x%x, mant=0x%x\n",fpub_sign_tmp, fpub_exponent_tmp, fpub_significand_tmp);
  printf("\t\t\t\t\t\t\t   : exponent difference = %d\n",exponent_diff_tmp);
   printf("\t\t\t\t\t\t\t-------------------------------\n");
 }

 if (exponent_diff_tmp > 0) {
  exponent_tmp = fpua_exponent_tmp;
  //printf("shift significant B to Right\n");
         fpub_significand_tmp = fpub_significand_tmp >> exponent_diff_tmp ;
  fpub_exponent_tmp = fpua_exponent_tmp;
 } else {
  exponent_tmp = fpub_exponent_tmp;
  //printf("shift significant A to Right\n");
         fpua_significand_tmp = fpua_significand_tmp >> exponent_diff_tmp ;
  fpua_exponent_tmp = fpub_exponent_tmp;
 }
 wait();

 if (true) {
   printf("\t\t\t\t\t\t\t-------------------------------\n");
   printf("\t\t\t\t\t\t\tFPU: After Alignment\n");
   printf("\t\t\t\t\t\t\tFPU: A s=0x%x, exp=0x%x, mant=0x%x\n",fpua_sign_tmp, fpua_exponent_tmp, fpua_significand_tmp);
   printf("\t\t\t\t\t\t\t   : B s=0x%x, exp=0x%x, mant=0x%x\n",fpub_sign_tmp, fpub_exponent_tmp, fpub_significand_tmp);
   printf("\t\t\t\t\t\t\t-------------------------------\n");
 }

 // output MUX
 switch (opcode_tmp) {
 case 0: // Stall
  opcode_encode = "STALL";
  dout_tmp = dout_tmp;
  wait();
  break;
 case 3: // add
  opcode_encode = "FADD";
  significant_result = int(fpua_significand_tmp) + int(fpub_significand_tmp);
  wait();
  break;
 case 4: // sub
  opcode_encode = "FSUB";
  significant_result = int(fpua_significand_tmp) - int(fpub_significand_tmp);
  wait();
  break;
 case 5: // mul
  opcode_encode = "FMUL";
  significant_result = int(fpua_significand_tmp) * int(fpub_significand_tmp);
  fpub_exponent_tmp *= 2; // exponent is doubled in value
  wait();
  break;
 case 6: // div
  opcode_encode = "FDIV";
  significant_result = int(fpua_significand_tmp) / int(fpub_significand_tmp);
  wait();
  break;
 default:
   printf("\t\t\t\t\t\t\t-------------------------------\n");
  printf("\t\t\t\t\t\t\tFPU: Bad Opcode %d.\n",opcode_tmp);
   printf("\t\t\t\t\t\t\t-------------------------------\n");
  wait();
  break;
 }

 overflow_sign_tmp = (significant_result & 0xff800000) >> 23;
 dout_tmp = (significant_result << overflow_sign_tmp) & 0x007fffff ;
 result_exp_tmp = fpub_exponent_tmp + overflow_sign_tmp;
 dout_tmp = dout_tmp | ((result_exp_tmp << 23) & 0x7f800000) ;
 result_sign_tmp = fpua_sign_tmp;
 dout_tmp = dout_tmp | ((result_sign_tmp << 31) & 0x80000000) ;
 printf("\t\t\t\t\t\t\t-------------------------------\n");
 printf("\t\t\t\t\t\t\tFPU: Result   Sign=0x%x.\n",result_sign_tmp);
 printf("\t\t\t\t\t\t\t   : Result   Mantissa=0x%x.\n",significant_result);
 printf("\t\t\t\t\t\t\t   : Result   Exponent=0x%x.\n",result_exp_tmp);
 printf("\t\t\t\t\t\t\t   : Overflow Mantissa=0x%x.\n",overflow_sign_tmp);
 cout << "\t\t\t\t\t\t\t   : " << opcode_encode;
 printf(" FPU Output =0x%x.\n",dout_tmp);
 cout <<"\t\t\t\t\t\t\t   : at CSIM " << sc_time_stamp() << endl;
 printf("\t\t\t\t\t\t\t-------------------------------\n");
 fdout.write(dout_tmp);
 fout_valid.write(true);
 fdestout.write(dest_tmp);
 wait();
 fout_valid.write(false);
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
 
  icache.cpp -- Instruction Cache Unit.
 
  Original Author: Martin Wang, Synopsys, Inc.
 
 *****************************************************************************/

/*****************************************************************************
 
  MODIFICATION LOG - modifiers, enter your name, affiliation, date and
  changes you are making here.
 
      Name, Affiliation, Date: Gene Bushuyev, Synopsys, Inc.
  Description of Modification: - bug - read/write outside of allocated memory
 
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
 
  icache.h -- Instruction Cache Unit.
 
  Original Author: Martin Wang, Synopsys, Inc.
 
 *****************************************************************************/

/*****************************************************************************
 
  MODIFICATION LOG - modifiers, enter your name, affiliation, date and
  changes you are making here.
 
      Name, Affiliation, Date:
  Description of Modification:
 
 *****************************************************************************/







struct icache : sc_module {
   sc_in<unsigned > datain; // modified instruction
   sc_in<bool> cs; // chip select
   sc_in<bool> we; // write enable for SMC
   sc_in<unsigned > addr; // address
   sc_in<bool> ld_valid; // load valid    
   sc_in<signed> ld_data; // load data value
   sc_out<unsigned > dataout; // ram data out
   sc_out<bool> icache_valid; // output valid
   sc_out<bool> stall_fetch; // stall fetch if busy
   sc_in_clk CLK;

  // Parameter
  unsigned *icmemory; // icache data memory
  unsigned *ictagmemory; // icache tag memory
  signed int pid; // process ID

  int wait_cycles; // Number of cycles it takes to access imemory

  void init_param(int given_delay_cycles) {
    wait_cycles = given_delay_cycles;
  }

  //Constructor
  SC_CTOR(icache) {
        SC_CTHREAD(entry, CLK.pos());

 // initialize instruction icmemory from external file
 pid = 0;
 FILE *fp = fopen("icache","r");
 int size=0;
 int mem_word;
   icmemory = new unsigned[500];
   ictagmemory = new unsigned[500];
 for (size = 0; size < 500; size++) { // initialize bad data
  icmemory[size] = 0xeeeeeeee;
  ictagmemory[size] = 0xeeeeeeee;
 }
 size = 0;
 while (fscanf(fp,"%x", &mem_word) != EOF) {
  icmemory[size] = mem_word;
  ictagmemory[size] = size;
  size++;
 }
  }

  // Process functionality in member function below
  void entry();
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
 
  directive.h -- Debug directive Unit.
 
  Original Author: Martin Wang, Synopsys, Inc.
 
 *****************************************************************************/

/*****************************************************************************
 
  MODIFICATION LOG - modifiers, enter your name, affiliation, date and
  changes you are making here.
 
      Name, Affiliation, Date:
  Description of Modification:
 
 *****************************************************************************/


void icache::entry()
{
    unsigned int address;

 while (true) {
     wait_until(cs.delayed() == true);
     address = addr.read();
 if (address == 5) {
   printf("ICU ALERT: *********************************************************************\n");
   printf("         : *****************************AFTER RESET*****************************\n");
   printf("ICU ALERT: *********************************************************************\n");
 }
 if (address >= 5) {
  if (ld_valid.read() == true) {
   pid = ld_data.read();
   printf("------------------------\n");
   printf("ICU: PID = [%d]", pid);
                        cout << " at CSIM " << sc_time_stamp() << endl;
   printf("------------------------\n");
         wait();
         wait();
  }
      if (we.read() == true) { // Write operation
         wait();
   if (address < 500 && address >= 0)
                          icmemory[address] = datain.read();
                        else
                          printf("ICU ALERT: **MEMORY OUT OF RANGE**\n");
         wait();
      }
      else { // Read operation
   wait(); // Introduce delay needed
   if (address >= 500 || address < 0) {
    dataout.write(0xffffffff);
    printf("ICU ALERT: **MEMORY OUT OF RANGE**\n");
   }
                        else
            dataout.write(icmemory[address]);

   icache_valid.write(true);

   if (false) {
    printf("------------------------\n");
    printf("ICU: fetching mem[%d]\n", address);
                                if (address < 500 && address >= 0)
      printf("ICU: (%0x)", icmemory[address]);
                         cout.setf(ios::dec,ios::basefield);
                         cout << " at CSIM " << sc_time_stamp() << endl;
    printf("------------------------\n");
   }

         wait();
   icache_valid.write(false);
         wait();
      }
 }
 }
} // end of entry function

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
 
  mmxu.cpp -- MMX-Like Execution Unit.
 
  Original Author: Martin Wang, Synopsys, Inc.
 
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
 
  mmxu.h -- MMX-Like Execution Unit.
 
  Original Author: Martin Wang, Synopsys, Inc.
 
 *****************************************************************************/

/*****************************************************************************
 
  MODIFICATION LOG - modifiers, enter your name, affiliation, date and
  changes you are making here.
 
      Name, Affiliation, Date:
  Description of Modification:
 
 *****************************************************************************/




struct mmxu : sc_module {
  sc_in<bool> mmx_valid; // MMX unit enable
  sc_in<int> opcode; // opcode
  sc_in<signed int> mmxa; // operand A
  sc_in<signed int> mmxb; // operand B
  sc_in<unsigned> dest; // Destination register number
  sc_out<signed int> mmxdout; // MMX output
  sc_out<bool> mmxout_valid; // MMX output valid
  sc_out<unsigned> mmxdestout; // destination number
  sc_in_clk CLK;


  // can make it asynchronous process to speed up simulation
  SC_CTOR(mmxu) {
      SC_CTHREAD(entry, CLK.pos());
  }

  void entry();
};


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
 
  directive.h -- Debug directive Unit.
 
  Original Author: Martin Wang, Synopsys, Inc.
 
 *****************************************************************************/

/*****************************************************************************
 
  MODIFICATION LOG - modifiers, enter your name, affiliation, date and
  changes you are making here.
 
      Name, Affiliation, Date:
  Description of Modification:
 
 *****************************************************************************/






void mmxu::entry(){

  int opcode_tmp = 0;
  unsigned int dout_tmp = 0;
  unsigned int dest_tmp = 0;
  const char * opcode_encode;

  unsigned int mmxa_tmp = 0;
  unsigned int mmxb_tmp = 0;


  unsigned int mmxa0_tmp = 0;
  unsigned int mmxa1_tmp = 0;
  unsigned int mmxa2_tmp = 0;
  unsigned int mmxa3_tmp = 0;
  unsigned int mmxb0_tmp = 0;
  unsigned int mmxb1_tmp = 0;
  unsigned int mmxb2_tmp = 0;
  unsigned int mmxb3_tmp = 0;
  unsigned int mmxc0_tmp = 0;
  unsigned int mmxc1_tmp = 0;
  unsigned int mmxc2_tmp = 0;
  unsigned int mmxc3_tmp = 0;
  unsigned int mmxcU_tmp = 0;
  unsigned int mmxcL_tmp = 0;

  //
  // main loop
  //
  // 

  // initialization of output

  wait(3);
  while(1) {
    if (mmx_valid.read() == true) {
   mmxa_tmp = mmxa.read();
   mmxb_tmp = mmxb.read();

                 mmxa0_tmp = (mmxa_tmp & 0x000000ff) ;
                 mmxb0_tmp = (mmxb_tmp & 0x000000ff) ;
                 mmxa1_tmp = (mmxa_tmp & 0x0000ff00) >> 8 ;
                 mmxb1_tmp = (mmxb_tmp & 0x0000ff00) >> 8 ;
                 mmxa2_tmp = (mmxa_tmp & 0x00ff0000) >> 16 ;
                 mmxb2_tmp = (mmxb_tmp & 0x00ff0000) >> 16 ;
                 mmxa3_tmp = (mmxa_tmp & 0xff000000) >> 24 ;
                 mmxb3_tmp = (mmxb_tmp & 0xff000000) >> 24 ;
   opcode_tmp = opcode.read();
   dest_tmp = dest.read();

      // output MUX
      switch (opcode_tmp) {
         case 0: // Stall 
   opcode_encode = "STALL";
                 dout_tmp = dout_tmp;
                 wait();
                 break;
         case 3: // add 
   opcode_encode = "PADD";
   mmxc3_tmp = mmxa3_tmp + mmxb3_tmp;
   mmxc2_tmp = mmxa2_tmp + mmxb2_tmp;
   mmxc1_tmp = mmxa1_tmp + mmxb1_tmp;
   mmxc0_tmp = mmxa0_tmp + mmxb0_tmp;
   mmxc3_tmp = (mmxc3_tmp << 24) & 0xff000000;
   mmxc2_tmp = (mmxc2_tmp << 16) & 0x00ff0000;
   mmxc1_tmp = (mmxc1_tmp << 8) & 0x0000ff00;
   dout_tmp = mmxc0_tmp | mmxc1_tmp | mmxc2_tmp | mmxc3_tmp;
                 wait();
                 break;
                case 4: // add with saturation
   opcode_encode = "PADDS";
                        mmxc3_tmp = mmxa3_tmp + mmxb3_tmp;
                        mmxc2_tmp = mmxa2_tmp + mmxb2_tmp;
                        mmxc1_tmp = mmxa1_tmp + mmxb1_tmp;
                        mmxc0_tmp = mmxa0_tmp + mmxb0_tmp;
   if (mmxc3_tmp >= 256) mmxc3_tmp = 0xff;
   if (mmxc2_tmp >= 256) mmxc2_tmp = 0xff;
   if (mmxc1_tmp >= 256) mmxc1_tmp = 0xff;
   if (mmxc0_tmp >= 256) mmxc0_tmp = 0xff;
                        mmxc3_tmp = (mmxc3_tmp << 24) & 0xff000000;
                        mmxc2_tmp = (mmxc2_tmp << 16) & 0x00ff0000;
                        mmxc1_tmp = (mmxc1_tmp << 8) & 0x0000ff00;
                        dout_tmp = mmxc0_tmp | mmxc1_tmp | mmxc2_tmp | mmxc3_tmp;
                        wait();
                        break;

                case 5: // sub
                        opcode_encode = "PSUB";
                        mmxc3_tmp = mmxa3_tmp - mmxb3_tmp;
                        mmxc2_tmp = mmxa2_tmp - mmxb2_tmp;
                        mmxc1_tmp = mmxa1_tmp - mmxb1_tmp;
                        mmxc0_tmp = mmxa0_tmp - mmxb0_tmp;
                        mmxc3_tmp = (mmxc3_tmp << 24) & 0xff000000;
                        mmxc2_tmp = (mmxc2_tmp << 16) & 0x00ff0000;
                        mmxc1_tmp = (mmxc1_tmp << 8) & 0x0000ff00;
                        dout_tmp = mmxc0_tmp | mmxc1_tmp | mmxc2_tmp | mmxc3_tmp;
                        wait();
                        break;
                case 6: // sub with saturation
                        opcode_encode = "PSUBS";
                        mmxc3_tmp = mmxa3_tmp - mmxb3_tmp;
                        mmxc2_tmp = mmxa2_tmp - mmxb2_tmp;
                        mmxc1_tmp = mmxa1_tmp - mmxb1_tmp;
                        mmxc0_tmp = mmxa0_tmp - mmxb0_tmp;
                        if (mmxb3_tmp > mmxa3_tmp) mmxc3_tmp = 0x00;
                        if (mmxb2_tmp > mmxa2_tmp) mmxc2_tmp = 0x00;
                        if (mmxb1_tmp > mmxa1_tmp) mmxc1_tmp = 0x00;
                        if (mmxb0_tmp > mmxa0_tmp) mmxc0_tmp = 0x00;
                        mmxc3_tmp = (mmxc3_tmp << 24) & 0xff000000;
                        mmxc2_tmp = (mmxc2_tmp << 16) & 0x00ff0000;
                        mmxc1_tmp = (mmxc1_tmp << 8) & 0x0000ff00;
                        dout_tmp = mmxc0_tmp | mmxc1_tmp | mmxc2_tmp | mmxc3_tmp;
                        wait();
                        break;
                case 7: // packed multiply add with saturation
    // a3*b3+a2*b2 , a1*b1+a0*b0
                        opcode_encode = "PMADD";
                        mmxc3_tmp = mmxa3_tmp * mmxb3_tmp;
                        mmxc2_tmp = mmxa2_tmp * mmxb2_tmp;
                        mmxc1_tmp = mmxa1_tmp * mmxb1_tmp;
                        mmxc0_tmp = mmxa0_tmp * mmxb0_tmp;
   if (mmxc3_tmp >= 256) mmxc3_tmp = 0xff;
   if (mmxc2_tmp >= 256) mmxc2_tmp = 0xff;
   if (mmxc1_tmp >= 256) mmxc1_tmp = 0xff;
   if (mmxc0_tmp >= 256) mmxc0_tmp = 0xff;
   mmxcU_tmp = mmxc3_tmp + mmxc2_tmp;
   mmxcL_tmp = mmxc1_tmp + mmxc0_tmp;
   if (mmxcU_tmp >= 256) mmxcU_tmp = 0xff;
   if (mmxcL_tmp >= 256) mmxcL_tmp = 0xff;
                        mmxcU_tmp = (mmxcU_tmp << 16) ;
                        dout_tmp = mmxcU_tmp | mmxcL_tmp;
                        wait();
                        break;

                case 8: // packed b1 (16bit) b0(16bit) and a1(16 bit) a0(16 bit)
                                // to B1(8bit) B0(8bit) A1(8bit) A0(8bit)
                        opcode_encode = "PACK";
   mmxc3_tmp = mmxb2_tmp << 24;
   mmxc2_tmp = mmxb0_tmp << 16;
   mmxc1_tmp = mmxa2_tmp << 8;
   mmxc0_tmp = mmxa0_tmp ;
                        dout_tmp = mmxc3_tmp | mmxc2_tmp | mmxc1_tmp | mmxc0_tmp;
                        wait();
                        break;

                case 9: // mmx chroma keying
                                // A =green != green green !=green
    // B =green    green green   green
                                //Res=0xff  00   ff    00
                        opcode_encode = "MMXCK";
   if (mmxa3_tmp == mmxb3_tmp)
    mmxc3_tmp = 0xff;
   else
    mmxc3_tmp = 0x00;
   if (mmxa2_tmp == mmxb2_tmp)
    mmxc2_tmp = 0xff;
   else
    mmxc2_tmp = 0x00;
   if (mmxa1_tmp == mmxb1_tmp)
    mmxc1_tmp = 0xff;
   else
    mmxc1_tmp = 0x00;
   if (mmxa0_tmp == mmxb0_tmp)
    mmxc0_tmp = 0xff;
   else
    mmxc0_tmp = 0x00;
                        mmxc3_tmp = mmxc3_tmp << 24;
                        mmxc2_tmp = mmxc2_tmp << 16;
                        mmxc1_tmp = mmxc1_tmp << 8;
                        mmxc0_tmp = mmxc0_tmp ;
                        dout_tmp = mmxc3_tmp | mmxc2_tmp | mmxc1_tmp | mmxc0_tmp;
                        wait();
                        break;

  default:
   opcode_encode = "INVALID";
   printf("MMX:      Bad Opcode %d.\n",opcode_tmp);
   wait();
   break;
      }


      mmxdout.write(dout_tmp);
      mmxout_valid.write(true);
  mmxdestout.write(dest_tmp);

  printf("\t\t\t\t\t\t\t-------------------------------\n");
  cout.setf(ios::hex,ios::basefield);
      cout << "\t\t\t\t\t\t\tMMX :" << " op= " << opcode_encode
   << " A=0x " << mmxa_tmp << " B=0x " << mmxb_tmp << endl;
      cout << "\t\t\t\t\t\t\tMMX :" << " C=0x " << dout_tmp << "-> R" << dest_tmp ;
  cout.setf(ios::dec,ios::basefield);
              cout << " at CSIM " << sc_time_stamp() << endl;
  printf("\t\t\t\t\t\t\t-------------------------------\n");
      wait();
      mmxout_valid.write(false);
      wait();

 } else {
  wait();
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
 
  paging.cpp -- Instruction Paging Unit.
 
  Original Author: Martin Wang, Synopsys, Inc.
 
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
 
  paging.h -- Instruction Paging Unit.
 
  Original Author: Martin Wang, Synopsys, Inc.
 
 *****************************************************************************/

/*****************************************************************************
 
  MODIFICATION LOG - modifiers, enter your name, affiliation, date and
  changes you are making here.
 
      Name, Affiliation, Date:
  Description of Modification:
 
 *****************************************************************************/




struct paging : sc_module {
        sc_in<unsigned > paging_din; // input data       
        sc_in<bool> paging_csin; // chip select        
        sc_in<bool> paging_wein; // write enable       
        sc_in<unsigned > logical_address; // logical address  
        sc_in<unsigned > icache_din; // data from BIOS/icache
        sc_in<bool> icache_validin; // data valid bit 
        sc_in<bool> icache_stall; // stall IFU if busy

        sc_out<unsigned > paging_dout; // output data
        sc_out<bool> paging_csout; // output cs to cache/BIOS
        sc_out<bool> paging_weout; // write enable to cache/BIOS
        sc_out<unsigned > physical_address; // physical address 
        sc_out<unsigned > dataout; // dataout from memory
        sc_out<bool> data_valid; // data valid
        sc_out<bool> stall_ifu; // stall IFU if busy
 sc_in_clk CLK;

        signed int pid_reg; //CPU process ID register      

  SC_CTOR(paging) {
      SC_CTHREAD(entry, CLK.pos());
      pid_reg = 0;
  }

  void entry();
};



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
 
  directive.h -- Debug directive Unit.
 
  Original Author: Martin Wang, Synopsys, Inc.
 
 *****************************************************************************/

/*****************************************************************************
 
  MODIFICATION LOG - modifiers, enter your name, affiliation, date and
  changes you are making here.
 
      Name, Affiliation, Date:
  Description of Modification:
 
 *****************************************************************************/

void paging::entry()
{
 int address=0;
 int address_conversion_factor = 0;
 int dataout_tmp =0;

 while (true) {
     wait_until(paging_csin.delayed() == true);
 address = logical_address.read();
        address_conversion_factor = paging_din.read();

 if (address >= 5) {
      if (paging_wein.read() == true) { // Write operation
   paging_dout.write(paging_din.read());
   paging_csout.write(true);
   paging_weout.write(true);
   physical_address.write(logical_address.read());
         wait();
   paging_csout.write(false);
   paging_weout.write(false);

      }
      else { // Read operation
   paging_csout.write(true);
   paging_weout.write(false);
   physical_address.write(logical_address.read());
   wait();
   wait_until(icache_validin.delayed() == true);
   dataout_tmp = icache_din.read();

   if (false){
       cout << "-----------------------" << endl;
    printf( "PAGE : mem=%x\n",dataout_tmp);
       cout << "PAGE : " ;
       cout << " at CSIM " << sc_time_stamp() << endl;
       cout << "-----------------------" << endl;
   }

   dataout.write(icache_din.read());
   data_valid.write(true);
   paging_csout.write(false);
   wait();
   data_valid.write(false);
   wait();
      }
 }
 }
} // end of entry function

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
 
  pic.cpp -- Programmable Interrupt Unit.
 
  Original Author: Martin Wang, Synopsys, Inc.
 
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
 
  pic.h -- Programmable Interrupt Unit.
 
  Original Author: Martin Wang, Synopsys, Inc.
 
 *****************************************************************************/

/*****************************************************************************
 
  MODIFICATION LOG - modifiers, enter your name, affiliation, date and
  changes you are making here.
 
      Name, Affiliation, Date:
  Description of Modification:
 
 *****************************************************************************/




struct pic : sc_module {
   sc_in<bool> ireq0; // interrupt request 0   	
   sc_in<bool> ireq1; // interrupt request 1
   sc_in<bool> ireq2; // interrupt request 2
   sc_in<bool> ireq3; // interrupt request 3
   sc_in<bool> cs; // chip select
   sc_in<bool> rd_wr; // read or write
   sc_in<bool> intack_cpu; // interrupt acknowledge from CPU
   sc_out<bool> intreq; // interrupt request to CPU		
   sc_out<bool> intack; // interrupt acknowledge to devices		
   sc_out<unsigned> vectno; // vector number		


  //Constructor 
  SC_CTOR(pic) {
      SC_METHOD(entry);
      dont_initialize();
      sensitive << ireq0 << ireq1 << ireq2 << ireq3 ;
  }


  // Process functionality in member function below
  void entry();
};

void pic::entry(){

 if (ireq0.read() == true) {
  intreq.write(true);
  vectno.write(0);
 } else if (ireq1.read() == true) {
   intreq.write(true);
   vectno.write(1);
  } else if (ireq2.read() == true) {
    intreq.write(true);
    vectno.write(2);
   } else if (ireq3.read() == true) {
      intreq.write(true);
      vectno.write(2);
    } else {
    }
 if ((intack_cpu.read() == true) && (cs.read() == true)) {
   intreq.write(false);
 }
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
 
  main -- This is a simple CPU modeling using SystemC.
          Architecure defined by Martin Wang.
          You can initialize register by modifying file
          named register, and so is bios, and dacache.
 
  Original Author: Martin Wang, Synopsys, Inc.
 
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
 
  directive.h -- Debug directive Unit.
 
  Original Author: Martin Wang, Synopsys, Inc.
 
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
 
  bios.h -- System Bios Unit.
 
  Original Author: Martin Wang, Synopsys, Inc.
 
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
 
  paging.h -- Instruction Paging Unit.
 
  Original Author: Martin Wang, Synopsys, Inc.
 
 *****************************************************************************/

/*****************************************************************************
 
  MODIFICATION LOG - modifiers, enter your name, affiliation, date and
  changes you are making here.
 
      Name, Affiliation, Date:
  Description of Modification:
 
 *****************************************************************************/
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
 
  icache.h -- Instruction Cache Unit.
 
  Original Author: Martin Wang, Synopsys, Inc.
 
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
 
  fetch.h -- Instruction Fetch Unit.
 
  Original Author: Martin Wang, Synopsys, Inc.
 
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
 
  decode.h -- Instruction Decode Unit.
 
  Original Author: Martin Wang, Synopsys, Inc.
 
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
 
  exec.h -- Integer Execution Unit.
 
  Original Author: Martin Wang, Synopsys, Inc.
 
 *****************************************************************************/

/*****************************************************************************
 
  MODIFICATION LOG - modifiers, enter your name, affiliation, date and
  changes you are making here.
 
      Name, Affiliation, Date:
  Description of Modification:
 
 *****************************************************************************/
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
 
  mmxu.h -- MMX-Like Execution Unit.
 
  Original Author: Martin Wang, Synopsys, Inc.
 
 *****************************************************************************/

/*****************************************************************************
 
  MODIFICATION LOG - modifiers, enter your name, affiliation, date and
  changes you are making here.
 
      Name, Affiliation, Date:
  Description of Modification:
 
 *****************************************************************************/
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
 
  floating.h -- Floating Execution Unit.
 
  Original Author: Martin Wang, Synopsys, Inc.
 
 *****************************************************************************/

/*****************************************************************************
 
  MODIFICATION LOG - modifiers, enter your name, affiliation, date and
  changes you are making here.
 
      Name, Affiliation, Date:
  Description of Modification:
 
 *****************************************************************************/
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
 
  dcache.h -- Data Cache Unit.
 
  Original Author: Martin Wang, Synopsys, Inc.
 
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
 
  pic.h -- Programmable Interrupt Unit.
 
  Original Author: Martin Wang, Synopsys, Inc.
 
 *****************************************************************************/

/*****************************************************************************
 
  MODIFICATION LOG - modifiers, enter your name, affiliation, date and
  changes you are making here.
 
      Name, Affiliation, Date:
  Description of Modification:
 
 *****************************************************************************/



//#include <sys/times.h>


int sc_main(int ac, char *av[])
{

  // ************************ ICACHE ***********************************
  // ICACHE = ram_cs
  // ICACHE = ram_we
  // ICACHE = addr
  // ICACHE = ram_datain
  // ICACHE = ram_dataout
  // ICACHE = ld_valid = pid_valid
  // ICACHE = ld_data = pid_data
  sc_signal<bool> icache_valid("ICACHE_VALID") ;

  // ************************ BIOS ***********************************
  sc_signal<bool> ram_cs("RAM_CS") ;
  sc_signal<bool> ram_we("RAM_WE") ;
  sc_signal<unsigned > addr("Address") ;
  sc_signal<unsigned > ram_datain("RAM_DATAIN") ;
  sc_signal<unsigned > ram_dataout("RAM_DATAOUT") ;
  sc_signal<bool> bios_valid("BIOS_VALID") ;
  const int delay_cycles = 2;

  // ************************ Paging ***********************************
  // Paging paging_din = ram_datain
  // Paging paging_csin = ram_cs
  // Paging paging_wein = ram_we
  // Paging logical_address = addr 
  sc_signal<unsigned > icache_din("ICACHE_DIN") ;
  sc_signal<bool> icache_validin("ICACHE_VALIDIN") ;
  sc_signal<bool> icache_stall("ICACHE_STALL") ;
  sc_signal<unsigned > paging_dout("PAGING_DOUT") ;
  sc_signal<bool> paging_csout("PAGING_CSOUT") ;
  sc_signal<bool> paging_weout("PAGING_WEOUT") ;
  sc_signal<unsigned > physical_address("PHYSICAL_ADDRESS") ;
  // Paging dataout  = ram_dataout 
  // Paging data_valid = icache_valid
  // Paging stall_ifu = stall_fetch


  // ************************ Fetch ***********************************
  // IFU ramdata = ram_dataout
  sc_signal<unsigned > branch_target_address("BRANCH_TARGET_ADDRESS") ;
  sc_signal<bool> next_pc("NEXT_PC") ;
  sc_signal<bool> branch_valid("BRANCH_VALID") ;
  sc_signal<bool> stall_fetch("STALL_FETCH") ;
  sc_signal<bool> pred_fetch("PRED_FETCH") ;
  // IFU ram_valid = bios_valid
  // IFU ram_cs = ram_cs
  // IFU ram_we = ram_we
  // IFU address = addr
  // IFU smc_instrction = ram_datain
  // IFU pred_branch_address = pred_branch_address
  // IFU pred_branch_valid = pred_branch_valid
  sc_signal<unsigned> instruction("INSTRUCTION") ;
  sc_signal<bool> instruction_valid("INSTRUCTION_VALID") ;
  sc_signal<unsigned > program_counter("PROGRAM_COUNTER") ;
  sc_signal<bool> branch_clear("BRANCH_CLEAR") ;
  sc_signal<bool> pred_fetch_valid("PRED_FETCH_VALID") ;
  sc_signal<bool> reset("RESET") ;

  // ************************ Branch ***********************************
  // BPU: fetch_inst = instruction
  // BPU: fetch_pc = program_counter
  // BPU: fetch_valid = instruction_valid
  // BPU: branch_inst_addr = branch_instruction_address
  // BPU: branch_target_address = branch_target_address
  // BPU: branch_valid = branch_valid
  sc_signal<unsigned > pred_branch_address("PRED_BRANCH_ADDRESS");
  sc_signal<bool> pred_branch_valid("PRED_BRANCH_VALID") ;
  sc_signal<bool> pred_tellid("PRED_TELLID") ;
  sc_signal<unsigned> pred_instruction("PRED_INSTRUCTION") ;
  sc_signal<bool> pred_inst_valid("PRED_INST_VALID") ;
  sc_signal<unsigned > pred_inst_pc("PRED_INST_PC");


  // ************************ Decode ***********************************
  // ID instruction = instruction
  // ID instruction = instruction_valid
  // ID destreg_write = out_valid
  // ID destreg_write_src = destout
  // ID clear_branch     = branch_clear 
  // ID pc = program_counter
  sc_signal<bool> pred_on("PRED_ON") ;
  sc_signal<unsigned > branch_instruction_address("BR_INSTRUCTION_ADDRESS");
  // ID alu_dataout = dout from EXEC 
  sc_signal<signed> dram_dataout("DRAM_DATAOUT") ;
  sc_signal<bool> dram_rd_valid("DRAM_RD_VALID") ;
  sc_signal<unsigned> dram_write_src("DRAM_WRITE_SRC");
  // ID next_pc     = next_pc
  // ID branch_valid = branch_valid
  // ID branch_target_address = branch_target_address
  sc_signal<bool> mem_access("MEM_ACCESS") ;
  sc_signal<unsigned > mem_address("MEM_ADDRESS") ;
  sc_signal<int> alu_op("ALU_OP") ;
  sc_signal<bool> mem_write("MEM_WRITE") ;
  sc_signal<unsigned> alu_src("ALU_SRC") ;
  sc_signal<bool> reg_write("REG_WRITE") ;
  sc_signal<signed int> src_A("SRC_A") ;
  sc_signal<signed int> src_B("SRC_B") ;
  sc_signal<bool> forward_A("FORWARD_A") ;
  sc_signal<bool> forward_B("FORWARD_B") ;
  // ID stall_fetch = stall_fetch
  sc_signal<bool> decode_valid("DECODE_VALID") ;
  sc_signal<bool> float_valid("FLOAT_VALID") ;
  sc_signal<bool> mmx_valid("MMX_VALID") ;
  sc_signal<bool> pid_valid("PID_VALID") ;
  sc_signal<signed> pid_data("PID_DATA") ;

  // ************************ DCACHE  ***********************************
  sc_signal<signed> mmic_datain("MMIC_DATAIN") ; /* DCU: datain 	*/
  sc_signal<unsigned> mmic_statein("MMIC_STATEIN") ;/* DCU: statein */
  sc_signal<bool> mmic_cs("MMIC_CS") ; /* DCU: cs 	*/
  sc_signal<bool> mmic_we("MMIC_WE") ; /* DCU: we 	*/
  sc_signal<unsigned > mmic_addr("MMIC_ADDR") ; /* DCU: addr	*/
  sc_signal<unsigned> mmic_dest("MMIC_DEST") ; /* DCU: dest 	*/
  sc_signal<unsigned> mmic_destout("MMIC_DESTOUT") ;/* DCU: destout */
  sc_signal<signed> mmic_dataout("MMIC_DATAOUT") ;/* DCU: dataout */
  sc_signal<bool> mmic_out_valid("MMIC_OUT_VALID") ;/* DCU: out_valid*/
  sc_signal<unsigned> mmic_stateout("MMIC_STATEOUT") ;/* DCU: stateout */

  // ************************ Execute ***********************************
  // EXEC in_valid = decode_valid
  sc_signal<bool> in_valid("IN_VALID") ;
  // EXEC opcode = alu_op
  sc_signal<bool> negate("NEGATE") ;
  sc_signal<int> add1("ADD1") ;
  sc_signal<bool> shift_sel("SHIFT_SEL") ;
  // EXEC dina = src_A
  // EXEC dinb = src_B
  // EXEC dest = alu_src
  sc_signal<bool> c("C") ;
  sc_signal<bool> v("V") ;
  sc_signal<bool> z("Z") ;
  sc_signal<signed> dout("DOUT") ;
  sc_signal<bool> out_valid("OUTPUT_VALID") ;
  sc_signal<unsigned> destout("DESTOUT") ;

  // ************************ Floating point ******************************
  // FPU in_valid = float_valid
  // FPU opcode = alu_op
  // FPU floata = src_A
  // FPU floatb = src_B
  // FPU dest = alu_src
  sc_signal<signed> fdout("FDOUT") ;
  sc_signal<bool> fout_valid("FOUT_VALID") ;
  sc_signal<unsigned> fdestout("FDESTOUT") ;

  // ************************ PIC *****************************************
  sc_signal<bool> ireq0("IREQ0") ;
  sc_signal<bool> ireq1("IREQ1") ;
  sc_signal<bool> ireq2("IREQ2") ;
  sc_signal<bool> ireq3("IREQ3") ;
  // PIC cs = interrupt_ack
  // PIC intack_cpu = interrupt_ack
  sc_signal<bool> rd_wr("RD_WR") ;
  sc_signal<bool> intreq("INTREQ") ;
  sc_signal<unsigned> vectno("VECTNO") ;
  sc_signal<bool> intack("INTACK") ;
  sc_signal<bool> intack_cpu("INTACK_CPU") ;

  // ************************ MMX ***********************************
  // MMX mmx_valid = mmx_valid
  // MMX opcode = alu_op
  // MMX mmxa = src_A
  // MMX mmxb = src_B
  // MMX dest = dest
  // MMX mmxdout = fdout
  // MMX mmxout_valid = fpu_valid
  // MMX mmxdestout = fpu_destout 

  // ************************ DSP *****************************************
  sc_signal<int> dsp_in1("DPS_IN1");
  sc_signal<int> dsp_out1("DSP_OUT1");
  sc_signal<bool> dsp_data_valid("DSP_DATA_VALID");
  sc_signal<bool> dsp_input_valid("DSP_INPUT_VALID");
  sc_signal<bool> dsp_data_requested("DSP_DATA_REQUESTED");

  ////////////////////////////////////////////////////////////////////////////
  // 				MAIN PROGRAM 
  ////////////////////////////////////////////////////////////////////////////
  sc_clock clk("Clock", 1, 0.5, 0.0);

  printf("/////////////////////////////////////////////////////////////////////////\n");
  printf("//  This code is written at SYNOPSYS, Inc.\n");
  printf("/////////////////////////////////////////////////////////////////////////\n");
  printf("//  Module   : main of CPU Model\n");
  printf("//  Author   : Martin Wang\n");
  printf("//  Company  : SYNOPSYS, Inc.\n");
  printf("//  Purpose  : This is a simple CPU modeling using SystemC.\n");
  printf("//             Instruction Set Architecure defined by Martin Wang.\n");
  printf("//             \n");
  printf("//           SystemC (TM) Copyright (c) 1988-2001 by Synopsys, Inc.  \n");
  printf("//             \n");
  printf("/////////////////////////////////////////////////////////////////////////\n");
  cout << "// IN THIS MACHINE Integer is " << sizeof (int) << " bytes.\n";
  cout << "// IN THIS MACHINE Floating is " << sizeof (float) << " bytes.\n";
  cout << "// IN THIS MACHINE Double is " << sizeof (double) << " bytes.\n";
  printf("//     \n");
  printf("//     \n");
  printf("//                            .,,uod8B8bou,,.\n");
  printf("//                   ..,uod8BBBBBBBBBBBBBBBBRPFT?l!i:.\n");
  printf("//              ,=m8BBBBBBBBBBBBBBBRPFT?!||||||||||||||\n");
  printf("//              !...:!TVBBBRPFT||||||||||!!^^\"\"    ||||\n");
  printf("//              !.......:!?|||||!!^^\"\"'            ||||\n");
  printf("//              !.........||||        ###  #  #    ||||\n");
  printf("//              !.........||||  ###  #  #  #  #    ||||\n");
  printf("//              !.........|||| #     #  #  #  #    ||||\n");
  printf("//              !.........|||| #     # #   #  #    ||||\n");
  printf("//              !.........|||| #     ##    #  #    ||||\n");
  printf("//              !.........|||| #     #     ###     ||||\n");
  printf("//              `.........|||| #   # #            ,||||\n");
  printf("//               .;.......||||  ###          _.-!!|||||\n");
  printf("//        .,uodWBBBBb.....||||       _.-!!|||||||||!:'\n");
  printf("//     !YBBBBBBBBBBBBBBb..!|||:..-!!|||||||!iof68BBBBBb....\n");
  printf("//     !..YBBBBBBBBBBBBBBb!!||||||||!iof68BBBBBBRPFT?!::   `.\n");
  printf("//     !....YBBBBBBBBBBBBBBbaaitf68BBBBBBRPFT?!:::::::::     `.\n");
  printf("//     !......YBBBBBBBBBBBBBBBBBBBRPFT?!::::::;:!^\"`;:::       `.\n");
  printf("//     !........YBBBBBBBBBBRPFT?!::::::::::^''...::::::;         iBBbo.\n");
  printf("//     `..........YBRPFT?!::::::::::::::::::::::::;iof68bo.      WBBBBbo.\n");
  printf("//       `..........:::::::::::::::::::::::;iof688888888888b.     `YBBBP^'\n");
  printf("//         `........::88::::::::::::;iof688888888888888888888b.     `\n");
  printf("//           `......::81:::::;iof688888888888888888888888888888b.\n");
  printf("//             `....:::;iof688888888888888888888888888888888899fT!\n");
  printf("//               `..::!8888888888888888888888888888888899fT|!^\"'\n");
  printf("//                 `' !!988888888888888888888888899fT|!^\"'\n");
  printf("//                     `!!8888888888888888899fT|!^\"'\n");
  printf("//                       `!988888888899fT|!^\"'\n");
  printf("//                         `!9899fT|!^\"'\n");
  printf("//                           `!^\"'\n");
  printf("//     \n");
  printf("//     \n");
  printf("/////////////////////////////////////////////////////////////////////////\n\n\n");


  fetch IFU("FETCH_BLOCK");
                IFU.init_param(delay_cycles);
  IFU << ram_dataout << branch_target_address << next_pc << branch_valid
  << stall_fetch << intreq << vectno << bios_valid << icache_valid
  << pred_fetch << pred_branch_address << pred_branch_valid << ram_cs << ram_we
  << addr << ram_datain << instruction << instruction_valid << program_counter
  << intack_cpu << branch_clear << pred_fetch_valid << reset << clk;

  decode IDU("DECODE_BLOCK");
  IDU << reset << instruction << pred_instruction << instruction_valid
  << pred_inst_valid << out_valid << destout << dout << dram_dataout
  << dram_rd_valid << destout << fdout << fout_valid << fdestout
         << branch_clear << dsp_data_valid << program_counter << pred_on
  << branch_instruction_address << next_pc << branch_valid
  << branch_target_address << mem_access << mem_address << alu_op
  << mem_write << alu_src << reg_write << src_A << src_B << forward_A
  << forward_B << stall_fetch << decode_valid << float_valid << mmx_valid
  << pid_valid << pid_data << clk;

  exec IEU("EXEC_BLOCK");
  IEU << reset << decode_valid << alu_op << negate << add1 << shift_sel
  << src_A << src_B << forward_A << forward_B << alu_src << c << v << z
  << dout << out_valid << destout << clk;

  floating FPU("FLOAT_BLOCK"); // order dependent
  FPU << float_valid << alu_op << src_A << src_B << alu_src
  << fdout << fout_valid << fdestout << clk;

  mmxu MMXU("MMX_BLOCK");
  MMXU << mmx_valid << alu_op << src_A << src_B << alu_src
  << fdout << fout_valid << fdestout << clk;

  bios BIOS("BIOS_BLOCK");
                BIOS.init_param(delay_cycles);
  BIOS.datain(ram_datain); // order independent
                BIOS.cs(ram_cs);
  BIOS.we(ram_we);
  BIOS.addr(addr);
  BIOS.dataout(ram_dataout);
  BIOS.bios_valid(bios_valid);
  BIOS.stall_fetch(stall_fetch);
  BIOS.CLK(clk);

  paging PAGING("PAGING_BLOCK");
  PAGING << ram_datain << ram_cs << ram_we << addr << icache_din
  << icache_validin << icache_stall << paging_dout << paging_csout
  << paging_weout << physical_address << ram_dataout << icache_valid
  << stall_fetch << clk ;

  icache ICACHE("ICACHE_BLOCK");
  ICACHE.init_param(delay_cycles);
  ICACHE << paging_dout << paging_csout << paging_weout
  << physical_address << pid_valid << pid_data << icache_din << icache_validin
  << icache_stall << clk;

  dcache DCACHE("DCACHE_BLOCK");
  DCACHE.init_param(delay_cycles);
  DCACHE << mmic_datain << mmic_statein << mmic_cs << mmic_we << mmic_addr
  << mmic_dest << mmic_destout << mmic_dataout << mmic_out_valid << mmic_stateout << clk;

  pic APIC("PIC_BLOCK");
  APIC << ireq0 << ireq1 << ireq2 << ireq3 <<intack_cpu << rd_wr
  << intack_cpu << intreq << intack << vectno;

  time_t tbuffer = time(NULL);

  sc_start(clk, -1);

  cout << "Time for simulation = " << (time(NULL) - tbuffer) << endl;

  return 0; /* this is necessary */
}
