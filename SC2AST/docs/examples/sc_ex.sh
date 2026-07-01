#!/bin/sh


#global variables
export JDK_HOME=$1

JAVA="$JDK_HOME -jar"
WORK_DIR=`pwd`
SC2AST="$WORK_DIR/sc2ast.jar"

   echo  '****************************************'
   echo  '*'
   echo  '* TLM examples ...'
   echo  '*'
   echo  '****************************************'

   echo
   echo
   echo '**************************'
   echo '* Compile nb_transport ...'
   echo '**************************'
   echo
   cd $WORK_DIR/tlm/nb_transport

   $JAVA $SC2AST -f prodcons.cpp -o prodcons_res.cpp -i ../../user.h

   echo  '****************************************'
   echo  '*'
   echo  '* SC 2.0.1 examples ...'
   echo  '*'
   echo  '****************************************'

   echo
   echo
   echo '**************************'
   echo '* Compile fft_flpt ...'
   echo '**************************'
   echo
   cd $WORK_DIR/fft/fft_flpt

   $JAVA $SC2AST -f fft.cpp sink.cpp source.cpp main.cpp -o fft_flpt_res.cpp -i ../../user.h


   echo
   echo '**************************'
   echo '* Compile fft_fxpt ...'
   echo '**************************'
   echo

   cd $WORK_DIR/fft/fft_fxpt

   $JAVA $SC2AST -f fft.cpp sink.cpp source.cpp main.cpp -o fft_fxpt_res.cpp -i ../../user.h


   echo
   echo '**************************'
   echo '* Compile fir ...'
   echo '**************************'
   echo

   cd $WORK_DIR/fir

   $JAVA $SC2AST -f stimulus.cpp display.cpp fir_fsm.cpp fir_data.cpp main_rtl.cpp -o fir_res.cpp -i ../user.h


   echo
   echo '**************************'
   echo '* Compile pipe ...'
   echo '**************************'
   echo

   cd $WORK_DIR/pipe

   $JAVA $SC2AST -f display.cpp numgen.cpp stage1.cpp stage2.cpp stage3.cpp -o pipe_res.cpp -i ../user.h


   echo
   echo '**************************'
   echo '* Compile pkt_switch ...'
   echo '**************************'
   echo

   cd $WORK_DIR/pkt_switch

   echo $JAVA $SC2AST -f receiver.cpp fifo.cpp sender.cpp switch.cpp switch_clk.cpp main.cpp -o pkt_switch_res.cpp -i ../user.h
   
   $JAVA $SC2AST -f receiver.cpp fifo.cpp sender.cpp switch.cpp switch_clk.cpp main.cpp -o pkt_switch_res.cpp -i ../user.h


   echo
   echo '**************************'
   echo '* Compile risc_cpu ...'
   echo '**************************'
   echo

   cd $WORK_DIR/risc_cpu

   $JAVA $SC2AST -f bios.cpp dcache.cpp decode.cpp exec.cpp fetch.cpp floating.cpp icache.cpp mmxu.cpp paging.cpp pic.cpp main.cpp -o risc_cpu_res.cpp -i ../user.h


   echo
   echo '**************************'
   echo '* Compile rsa ...'
   echo '**************************'
   echo

   cd $WORK_DIR/rsa

   $JAVA $SC2AST -f rsa.cpp -o rsa_res.cpp -i ../user.h


   echo
   echo '**************************'
   echo '* Compile simple_bus ...'
   echo '**************************'
   echo

   cd $WORK_DIR/simple_bus

   $JAVA $SC2AST -f simple_bus.cpp simple_bus_arbiter.cpp simple_bus_master_blocking.cpp simple_bus_master_direct.cpp simple_bus_master_non_blocking.cpp simple_bus_types.cpp simple_bus_tools.cpp simple_bus_main.cpp -o simple_bus_res.cpp -i ../user.h

   echo
   echo '**************************'
   echo '* Compile simple_fifo ...'
   echo '**************************'
   echo

   cd $WORK_DIR/simple_fifo

   $JAVA $SC2AST -f simple_fifo.cpp -o simple_fifo_res.cpp -i ../user.h

   echo
   echo '**************************'
   echo '* Compile simple_perf ...'
   echo '**************************'
   echo
   cd $WORK_DIR/simple_perf

   $JAVA $SC2AST -f simple_perf.cpp -o simple_perf_res.cpp -i ../user.h


 echo  '****************************************'
 echo  '*'
 echo  '* SC 2.1 examples ...'
 echo  '*'
 echo  '****************************************'
 echo

 echo '****************************************'
 echo '* Compile dpipe .'
 echo '****************************************'
 echo
 cd $WORK_DIR/2.1/dpipe

 $JAVA $SC2AST -f main.cpp -o main_res.cpp -i ../../user.h


 echo
 echo '****************************************'
 echo '* Compile  forkjoin.'
 echo '****************************************'
 echo
 cd $WORK_DIR/2.1/forkjoin

 $JAVA $SC2AST -f forkjoin.cpp -o forkjoin_res.cpp -i ../../user.h

 echo
 echo '****************************************'
 echo '* Compile  sc_barrier.'
 echo '****************************************'
 echo
 cd $WORK_DIR/2.1/sc_barrier

 $JAVA $SC2AST -f main.cpp -o main_res.cpp -i ../../user.h

 echo
 echo '****************************************'
 echo '* Compile  reset_signal_is.'
 echo '****************************************'
 echo
 cd $WORK_DIR/2.1/reset_signal_is

 $JAVA $SC2AST -f reset_signal_is.cpp -o reset_signal_is_res.cpp -i ../../user.h

 echo
 echo '****************************************'
 echo '* Compile  sc_export.'
 echo '****************************************'
 echo
 cd $WORK_DIR/2.1/sc_export

 $JAVA $SC2AST -f main.cpp -o main_res.cpp -i ../../user.h


 echo
 echo '****************************************'
 echo '* Compile  sc_report.'
 echo '****************************************'
 echo
 cd $WORK_DIR/2.1/sc_report

 $JAVA $SC2AST -f main.cpp -o main_res.cpp -i ../../user.h


echo
echo '****************************************'
echo '* Compile  scx_barrier.'
echo '****************************************'
echo
cd $WORK_DIR/2.1/scx_barrier

$JAVA $SC2AST -f main.cpp -o main_res.cpp -i ../../user.h

echo
echo '****************************************'
echo '* Compile  scx_mutex_w_policy.'
echo '****************************************'
echo
cd $WORK_DIR/2.1/scx_mutex_w_policy

$JAVA $SC2AST -f scx_mutex_w_policy.cpp -o scx_mutex_w_policy_res.cpp -i ../../user.h

