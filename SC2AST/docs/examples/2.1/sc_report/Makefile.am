CXX_FILES = \
	main.cpp 


H_FILES = 

bin_PROGRAMS = sc_report

check_PROGRAMS = sc_report
TESTS = $(check_PROGRAMS) 

INCLUDES = -I$(top_srcdir)/include

sc_report_SOURCES = $(CXX_FILES) $(H_FILES)

sc_report_LDADD = -L$(top_srcdir)/lib-$(TARGET_ARCH) -lsystemc -lm

sc_report_INSTALLS = $(sc_report_SOURCES) README.txt
